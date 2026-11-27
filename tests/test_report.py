from __future__ import annotations

from pathlib import Path
from typing import Any

from cobol_bridge.report.render import render_html, render_markdown, select_runs


def rec(run_id: str, model: str, program: str, **kw: Any) -> dict[str, Any]:
    base: dict[str, Any] = {
        "run_id": run_id,
        "model": model,
        "program": program,
        "repair_strategy": "fresh",
        "repeated_repairs": 0,
        "passed": False,
        "passed_first_try": False,
        "compile_ok": False,
        "final_stage": "compile",
        "tests_passed": 0,
        "tests_total": 0,
        "attempts": 3,
        "llm_latency_s": 30.0,
        "prompt_tokens": 100,
        "completion_tokens": 50,
        "attempt_log": [{"stage": "compile"}] * 3,
    }
    base.update(kw)
    return base


PASS = {
    "passed": True,
    "passed_first_try": True,
    "compile_ok": True,
    "final_stage": "passed",
    "tests_passed": 20,
    "tests_total": 20,
    "attempts": 1,
    "attempt_log": [{"stage": "passed"}],
}


def test_latest_run_per_model_and_strategy() -> None:
    records = [
        rec("20260101-a", "m1", "P1"),
        rec("20260102-a", "m1", "P1", **PASS),  # newer run of m1 replaces the older
        rec("20260101-b", "m2", "P1"),
        rec("20260101-c", "m2", "P1", repair_strategy="replay"),
    ]
    runs = select_runs(records)
    assert {(s.model, s.repair_strategy, s.run_id) for s in runs} == {
        ("m1", "fresh", "20260102-a"),
        ("m2", "fresh", "20260101-b"),
        ("m2", "replay", "20260101-c"),
    }
    assert runs[0].model == "m1"  # best pass rate first


def test_prompt_variants_reported_separately() -> None:
    records = [
        rec("20260101-a", "m1", "P1"),  # older records have no prompt_variant: base
        rec("20260102-a", "m1", "P1", prompt_variant="examples"),
    ]
    runs = select_runs(records)
    assert {(s.prompt_variant, s.run_id) for s in runs} == {
        ("base", "20260101-a"),
        ("examples", "20260102-a"),
    }
    assert {s.config for s in runs} == {"fresh repair", "fresh repair, examples prompt"}


def test_metrics() -> None:
    records = [
        rec("r", "m", "A", **PASS),
        rec(
            "r",
            "m",
            "B",
            final_stage="run",
            compile_ok=True,
            tests_passed=5,
            tests_total=20,
            repeated_repairs=1,
        ),
    ]
    (s,) = select_runs(records)
    assert (s.passed, s.passed_first_try, s.compiled) == (1, 1, 2)
    assert (s.cases_passed, s.cases_total) == (25, 40)
    assert s.avg_attempts == 2.0
    assert (s.repairs, s.repeated_repairs) == (2, 1)
    assert s.stage_counts == {"passed": 1, "compile": 3}


def test_markdown_and_html() -> None:
    records = [
        rec("r1", "granite4:micro", "LOAN", **PASS),
        rec("r1", "granite4:micro", "PAYROLL", final_stage="static_check"),
        rec("r2", "qwen<b>", "LOAN"),
    ]
    runs = select_runs(records)
    md = render_markdown(runs)
    assert "| Pass rate, after repair | 1/2 (50%) | 0/1 (0%) |" in md
    assert "| LOAN | PASS (1) | FAIL: compile error |" in md
    assert "| PAYROLL | FAIL: used floating point | - |" in md
    page = render_html(runs)
    assert page.startswith("<!DOCTYPE html>")
    assert "qwen&lt;b&gt;" in page and "qwen<b>" not in page
    assert 'class="pass">PASS (1)' in page


def test_repeated_repairs_recomputed_from_artifacts(tmp_path: Path) -> None:
    old = rec("r", "m", "P")
    del old["repeated_repairs"]
    folder = tmp_path / "r" / "P"
    folder.mkdir(parents=True)
    for k, code in enumerate(["class A {}", "class A  {}", "class B {}"], start=1):
        (folder / f"attempt-{k}.java").write_text(code)
    (s,) = select_runs([old], artifacts=tmp_path)
    assert s.repeated_repairs == 1
    (s,) = select_runs([old])
    assert s.repeated_repairs is None
