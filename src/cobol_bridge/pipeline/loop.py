"""The explain -> translate -> verify -> repair loop for one program."""

from __future__ import annotations

import hashlib
import time
from collections.abc import Callable
from dataclasses import asdict, dataclass, field
from datetime import UTC, datetime
from pathlib import Path
from typing import Any

from cobol_bridge.inputs.generator import generate_cases
from cobol_bridge.inputs.picparse import accepted_record
from cobol_bridge.llm import prompts
from cobol_bridge.llm.base import GenerationParams, LLMProvider, LLMResponse, Message
from cobol_bridge.pipeline.verify import (
    Baseline,
    Stage,
    VerifyResult,
    cobol_baseline,
    verify_java,
)
from cobol_bridge.sandbox.toolchain import Toolchain

Log = Callable[[str], None]


@dataclass(frozen=True)
class PipelineConfig:
    max_attempts: int = 3
    input_seed: int = 42
    n_random: int = 20
    params: GenerationParams = field(default_factory=GenerationParams)
    artifacts_dir: Path | None = None


@dataclass
class AttemptRecord:
    attempt: int
    stage: str
    compile_ok: bool
    tests_passed: int
    tests_total: int
    failure: str  # short description of what went wrong, "" when passed
    prompt_tokens: int
    completion_tokens: int
    llm_latency_s: float


@dataclass
class RunRecord:
    """One JSONL line: the outcome of translating one program in one run."""

    run_id: str
    timestamp: str
    program: str
    provider: str
    model: str
    temperature: float
    seed: int
    num_ctx: int
    max_attempts: int
    input_seed: int
    attempts: int = 0
    passed: bool = False
    passed_first_try: bool = False
    final_stage: str = ""
    compile_ok: bool = False
    tests_passed: int = 0
    tests_total: int = 0
    cases_dropped: int = 0
    prompt_tokens: int = 0
    completion_tokens: int = 0
    llm_latency_s: float = 0.0
    wall_s: float = 0.0
    source_sha256: str = ""
    inputs_sha256: str = ""
    error: str = ""
    attempt_log: list[AttemptRecord] = field(default_factory=list)

    def to_json(self) -> dict[str, Any]:
        return asdict(self)


def _sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()[:16]


def _failure_summary(vr: VerifyResult) -> str:
    if vr.stage is Stage.NO_CODE:
        return "no Java code in answer"
    if vr.stage is Stage.STATIC:
        return f"floating point used ({len(vr.static_violations)} lines)"
    if vr.stage is Stage.COMPILE:
        first = vr.compile_errors.splitlines()[0] if vr.compile_errors else ""
        return f"compile error: {first[:160]}"
    bad = vr.first_failure()
    if bad is None:
        return ""
    return f"{bad.kind} on {vr.total - vr.passed}/{vr.total} cases (first: {bad.name})"


def feedback_for(vr: VerifyResult) -> str:
    """The repair prompt for a failed verification."""
    if vr.stage is Stage.NO_CODE:
        return prompts.repair_no_code()
    if vr.stage is Stage.STATIC:
        return prompts.repair_static(vr.static_violations)
    if vr.stage is Stage.COMPILE:
        return prompts.repair_compile(vr.compile_errors)
    bad = vr.first_failure()
    assert bad is not None, "feedback requested for a passing translation"
    if bad.kind == "mismatch":
        return prompts.repair_mismatch(bad.stdin, bad.detail, vr.passed, vr.total)
    return prompts.repair_runtime(bad.stdin, bad.detail)


class _Artifacts:
    """Writes explanation, each attempt's Java and feedback to disk (optional)."""

    def __init__(self, root: Path | None) -> None:
        self.root = root
        if root:
            root.mkdir(parents=True, exist_ok=True)

    def write(self, name: str, text: str) -> None:
        if self.root:
            (self.root / name).write_text(text, encoding="utf-8")


def translate_program(
    path: Path,
    provider: LLMProvider,
    tc: Toolchain,
    cfg: PipelineConfig,
    *,
    run_id: str,
    log: Log = lambda _msg: None,
) -> RunRecord:
    """Translate one COBOL program, verifying and repairing up to ``cfg.max_attempts`` times."""
    start = time.monotonic()
    source = path.read_text(encoding="utf-8")
    program = path.stem.upper()
    p = cfg.params
    rec = RunRecord(
        run_id=run_id,
        timestamp=datetime.now(UTC).isoformat(timespec="seconds"),
        program=program,
        provider=provider.name,
        model=provider.model,
        temperature=p.temperature,
        seed=p.seed,
        num_ctx=p.num_ctx,
        max_attempts=cfg.max_attempts,
        input_seed=cfg.input_seed,
        source_sha256=_sha(source.encode()),
    )
    art = _Artifacts(cfg.artifacts_dir / program if cfg.artifacts_dir else None)

    record_name, fields = accepted_record(source)
    cases = generate_cases(source, seed=cfg.input_seed, n_random=cfg.n_random, program=program)
    rec.inputs_sha256 = _sha(b"".join(c.stdin for c in cases))
    baseline = cobol_baseline(source, cases, tc)
    rec.cases_dropped = len(baseline.dropped)
    log(f"{program}: {len(baseline.cases)} test cases ({len(baseline.dropped)} dropped)")

    def ask(messages: list[Message]) -> LLMResponse:
        resp = provider.chat(messages, p)
        rec.prompt_tokens += resp.prompt_tokens
        rec.completion_tokens += resp.completion_tokens
        rec.llm_latency_s += resp.latency_s
        return resp

    system = Message("system", prompts.SYSTEM)
    explanation = ask([system, Message("user", prompts.explain(source))]).text
    art.write("explanation.md", explanation)
    log(f"{program}: explained ({len(explanation.split())} words)")

    request = Message(
        "user",
        prompts.translate(source, explanation, prompts.describe_record(record_name, fields)),
    )
    messages = [system, request]
    for attempt in range(1, cfg.max_attempts + 1):
        resp = ask(messages)
        java = prompts.extract_java(resp.text)
        art.write(f"attempt-{attempt}.java", java or resp.text)
        vr = verify_java(java, baseline, tc)
        _record_attempt(rec, attempt, vr, resp)
        log(
            f"{program}: attempt {attempt}: {vr.stage} {vr.passed}/{vr.total} "
            f"{_failure_summary(vr)}".rstrip()
        )
        if vr.stage is Stage.PASSED:
            rec.passed = True
            rec.passed_first_try = attempt == 1
            art.write("Main.java", java or "")
            break
        feedback = feedback_for(vr)
        art.write(f"attempt-{attempt}.feedback.txt", feedback)
        messages = [system, request, Message("assistant", resp.text), Message("user", feedback)]

    rec.wall_s = round(time.monotonic() - start, 2)
    rec.llm_latency_s = round(rec.llm_latency_s, 2)
    return rec


def _record_attempt(rec: RunRecord, attempt: int, vr: VerifyResult, resp: LLMResponse) -> None:
    rec.attempts = attempt
    rec.final_stage = str(vr.stage)
    rec.compile_ok = vr.compile_ok
    rec.tests_passed = vr.passed
    rec.tests_total = vr.total
    rec.attempt_log.append(
        AttemptRecord(
            attempt=attempt,
            stage=str(vr.stage),
            compile_ok=vr.compile_ok,
            tests_passed=vr.passed,
            tests_total=vr.total,
            failure=_failure_summary(vr),
            prompt_tokens=resp.prompt_tokens,
            completion_tokens=resp.completion_tokens,
            llm_latency_s=round(resp.latency_s, 2),
        )
    )


__all__ = ["Baseline", "PipelineConfig", "RunRecord", "translate_program"]
