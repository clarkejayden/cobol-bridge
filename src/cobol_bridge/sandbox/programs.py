"""Compile and run COBOL and Java programs inside a workspace.

A compiled program is kept as a handle on its workspace so it can be run many
times (once per test case) without recompiling.
"""

from __future__ import annotations

from dataclasses import dataclass

from cobol_bridge.sandbox.runner import ExecResult, Workspace
from cobol_bridge.sandbox.toolchain import Toolchain

COMPILE_TIMEOUT_S = 120.0
RUN_TIMEOUT_S = 10.0
JAVA_CLASS = "Main"
_JVM_FLAGS = ["-Xmx256m", "-Xss4m", "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1"]


@dataclass(frozen=True)
class Compiled:
    """A successfully compiled program, runnable via ``run``."""

    ws: Workspace
    command: tuple[str, ...]

    def run(self, stdin: bytes, timeout_s: float = RUN_TIMEOUT_S) -> ExecResult:
        return self.ws.run(self.command, stdin=stdin, timeout_s=timeout_s)


@dataclass(frozen=True)
class CompileOutcome:
    """Compiler result; ``program`` is set only when compilation succeeded."""

    result: ExecResult
    program: Compiled | None

    @property
    def diagnostics(self) -> str:
        return (self.result.stderr + self.result.stdout).decode(errors="replace")


def compile_cobol(ws: Workspace, tc: Toolchain, source: str, name: str = "prog") -> CompileOutcome:
    """Compile fixed-format COBOL to a native executable with ``cobc -x``."""
    ws.write({f"{name}.cbl": source})
    res = ws.run(
        [str(tc.cobc), "-x", "-O0", "-o", name, f"{name}.cbl"],
        timeout_s=COMPILE_TIMEOUT_S,
    )
    program = Compiled(ws, (str(ws.root / name),)) if res.ok else None
    return CompileOutcome(res, program)


def compile_java(
    ws: Workspace, tc: Toolchain, source: str, support: dict[str, str] | None = None
) -> CompileOutcome:
    """Compile ``Main.java`` (plus any support files, e.g. ``Pic.java``) with javac 21."""
    extra = dict(support or {})
    ws.write({f"{JAVA_CLASS}.java": source, **extra})
    res = ws.run(
        [
            str(tc.javac),
            "--release",
            "21",
            "-encoding",
            "UTF-8",
            "-d",
            "classes",
            f"{JAVA_CLASS}.java",
            *sorted(extra),
        ],
        timeout_s=COMPILE_TIMEOUT_S,
        cpu_limit_s=600,
    )
    cmd = (str(tc.java), *_JVM_FLAGS, "-cp", "classes", JAVA_CLASS)
    program = Compiled(ws, cmd) if res.ok else None
    return CompileOutcome(res, program)
