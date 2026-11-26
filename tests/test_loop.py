"""The translate -> verify -> repair loop, driven by a scripted fake provider (no Ollama)."""

from __future__ import annotations

from pathlib import Path

import pytest

from cobol_bridge.llm.fake import FakeProvider
from cobol_bridge.pipeline.loop import PipelineConfig, RunRecord, translate_program
from cobol_bridge.sandbox.toolchain import Toolchain

ROOT = Path(__file__).resolve().parents[1]
PROGRAM = ROOT / "corpus" / "SALESTAX.cbl"
FIXTURES = ROOT / "tests" / "fixtures"

pytestmark = pytest.mark.toolchain


def java(name: str) -> str:
    return (
        "Here is the translation:\n```java\n"
        + (FIXTURES / f"salestax_{name}.java").read_text()
        + "```\n"
    )


def run(
    replies: list[str], toolchain: Toolchain, tmp_path: Path, attempts: int = 3
) -> tuple[RunRecord, FakeProvider]:
    provider = FakeProvider(["It computes sales tax.", *replies])
    cfg = PipelineConfig(max_attempts=attempts, artifacts_dir=tmp_path)
    rec = translate_program(PROGRAM, provider, toolchain, cfg, run_id="test")
    return rec, provider


def test_correct_translation_passes_first_try(toolchain: Toolchain, tmp_path: Path) -> None:
    rec, provider = run([java("correct")], toolchain, tmp_path)
    assert rec.passed and rec.passed_first_try
    assert rec.attempts == 1
    assert rec.tests_passed == rec.tests_total > 10
    assert len(provider.calls) == 2  # explain + translate
    assert (tmp_path / "SALESTAX" / "Main.java").exists()
    assert (tmp_path / "SALESTAX" / "explanation.md").read_text() == "It computes sales tax."


def test_repairs_compile_error_then_wrong_output(toolchain: Toolchain, tmp_path: Path) -> None:
    rec, provider = run(
        [java("compile_error"), java("wrong_output"), java("correct")], toolchain, tmp_path
    )
    assert rec.passed and not rec.passed_first_try
    assert [a.stage for a in rec.attempt_log] == ["compile", "run", "passed"]
    assert rec.attempt_log[1].tests_passed < rec.attempt_log[1].tests_total

    compile_feedback = provider.calls[2][-1].content
    assert "does not compile" in compile_feedback
    assert "';' expected" in compile_feedback

    mismatch_feedback = provider.calls[3][-1].content
    assert "differs from COBOL" in mismatch_feedback
    assert "-TAX" in mismatch_feedback and "+TAX" in mismatch_feedback
    # the repair conversation carries only the latest answer, not the whole history
    assert len(provider.calls[3]) == 4


def test_gives_up_after_max_attempts(toolchain: Toolchain, tmp_path: Path) -> None:
    rec, _ = run([java("wrong_output")] * 2, toolchain, tmp_path, attempts=2)
    assert not rec.passed
    assert rec.attempts == 2
    assert rec.final_stage == "run"
    assert rec.compile_ok
    assert 0 < rec.tests_passed < rec.tests_total


def test_floating_point_translation_rejected_before_compile(
    toolchain: Toolchain, tmp_path: Path
) -> None:
    floaty = java("correct").replace('new BigDecimal("7.25")', "BigDecimal.valueOf(7.25)")
    rec, provider = run([floaty, java("correct")], toolchain, tmp_path)
    assert [a.stage for a in rec.attempt_log] == ["static_check", "passed"]
    assert "BigDecimal" in provider.calls[2][-1].content


def test_answer_without_code_is_retried(toolchain: Toolchain, tmp_path: Path) -> None:
    rec, _ = run(["Sorry, I can't.", java("correct")], toolchain, tmp_path)
    assert [a.stage for a in rec.attempt_log] == ["no_code", "passed"]


def test_record_is_serialisable(toolchain: Toolchain, tmp_path: Path) -> None:
    import json

    rec, _ = run([java("correct")], toolchain, tmp_path)
    data = json.loads(json.dumps(rec.to_json()))
    for key in (
        "model",
        "attempts",
        "compile_ok",
        "tests_passed",
        "tests_total",
        "prompt_tokens",
        "completion_tokens",
        "llm_latency_s",
        "seed",
        "temperature",
    ):
        assert key in data
