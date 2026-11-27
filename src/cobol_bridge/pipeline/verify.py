"""Differential testing: run COBOL and Java on the same inputs and compare outputs.

The COBOL program is the oracle. Its outputs are computed once per program
(the *baseline*); any case on which COBOL itself fails is dropped and counted
rather than scored. A Java translation then passes only if it matches the
baseline on every remaining case.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from enum import StrEnum

from cobol_bridge.inputs.generator import TestCase
from cobol_bridge.pipeline.checks import floating_point_violations
from cobol_bridge.pipeline.compare import compare
from cobol_bridge.sandbox.programs import compile_cobol, compile_java
from cobol_bridge.sandbox.runner import workspace
from cobol_bridge.sandbox.toolchain import Toolchain

MAX_TIMEOUTS = 2  # stop running a translation after this many timed-out cases
MAX_COMPILE_ERROR_LINES = 40


class Stage(StrEnum):
    """How far a translation got; ``PASSED`` means every case matched."""

    NO_CODE = "no_code"
    STATIC = "static_check"
    COMPILE = "compile"
    RUN = "run"
    PASSED = "passed"


class CobolError(RuntimeError):
    """The original COBOL program could not be compiled; nothing to compare against."""


@dataclass(frozen=True)
class Baseline:
    cases: list[TestCase]
    outputs: dict[str, bytes]
    dropped: list[str]


@dataclass(frozen=True)
class CaseResult:
    name: str
    stdin: bytes
    passed: bool
    kind: str  # pass | mismatch | runtime_error | timeout | skipped
    detail: str = ""


@dataclass
class VerifyResult:
    stage: Stage
    static_violations: list[str] = field(default_factory=list)
    compile_errors: str = ""
    cases: list[CaseResult] = field(default_factory=list)

    @property
    def compile_ok(self) -> bool:
        return self.stage in (Stage.RUN, Stage.PASSED)

    @property
    def passed(self) -> int:
        return sum(c.passed for c in self.cases)

    @property
    def total(self) -> int:
        return len(self.cases)

    def first_failure(self) -> CaseResult | None:
        return next((c for c in self.cases if not c.passed and c.kind != "skipped"), None)


def cobol_baseline(source: str, cases: list[TestCase], tc: Toolchain) -> Baseline:
    """Compile the COBOL program and record its output for every case."""
    with workspace(tc.search_path()) as ws:
        outcome = compile_cobol(ws, tc, source)
        if outcome.program is None:
            raise CobolError(outcome.diagnostics)
        kept: list[TestCase] = []
        outputs: dict[str, bytes] = {}
        dropped: list[str] = []
        for case in cases:
            res = outcome.program.run(case.stdin)
            if res.ok and not res.truncated:
                kept.append(case)
                outputs[case.name] = res.stdout
            else:
                dropped.append(case.name)
    return Baseline(kept, outputs, dropped)


def _trim(text: str, max_lines: int) -> str:
    lines = text.strip().splitlines()
    if len(lines) > max_lines:
        lines = [*lines[:max_lines], f"... ({len(lines) - max_lines} more lines)"]
    return "\n".join(lines)


def verify_java(
    java: str | None,
    baseline: Baseline,
    tc: Toolchain,
    support: dict[str, str] | None = None,
) -> VerifyResult:
    """Static-check, compile and run a translation against the COBOL baseline.

    ``support`` files (e.g. the ``Pic`` helper) are compiled alongside but not
    static-checked: only the model's own code is.
    """
    if java is None:
        return VerifyResult(Stage.NO_CODE)
    violations = floating_point_violations(java)
    if violations:
        return VerifyResult(Stage.STATIC, static_violations=violations)
    with workspace(tc.search_path()) as ws:
        outcome = compile_java(ws, tc, java, support)
        if outcome.program is None:
            return VerifyResult(
                Stage.COMPILE, compile_errors=_trim(outcome.diagnostics, MAX_COMPILE_ERROR_LINES)
            )
        results: list[CaseResult] = []
        timeouts = 0
        for case in baseline.cases:
            if timeouts >= MAX_TIMEOUTS:
                results.append(CaseResult(case.name, case.stdin, False, "skipped"))
                continue
            res = outcome.program.run(case.stdin)
            if res.timed_out:
                timeouts += 1
                results.append(CaseResult(case.name, case.stdin, False, "timeout", "timed out"))
            elif res.returncode != 0:
                detail = f"exit code {res.returncode}\n" + _trim(
                    res.stderr.decode(errors="replace"), 15
                )
                results.append(CaseResult(case.name, case.stdin, False, "runtime_error", detail))
            else:
                cmp = compare(baseline.outputs[case.name], res.stdout)
                kind = "pass" if cmp.match else "mismatch"
                results.append(CaseResult(case.name, case.stdin, cmp.match, kind, cmp.diff))
    stage = Stage.PASSED if results and all(r.passed for r in results) else Stage.RUN
    return VerifyResult(stage, cases=results)
