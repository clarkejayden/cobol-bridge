"""Turn JSONL run records into comparison tables (Markdown and static HTML).

A *run* is every record sharing a ``run_id``: one model, one configuration,
the whole corpus. By default the latest run for each (model, repair strategy,
prompt variant) is reported, so a re-run replaces older numbers but different
configurations (the Phase 1 ``replay`` baseline, the ``examples`` prompt) are
shown side by side.
"""

from __future__ import annotations

import html
import itertools
from collections import Counter, defaultdict
from collections.abc import Iterable
from dataclasses import dataclass
from pathlib import Path
from typing import Any

Record = dict[str, Any]


@dataclass(frozen=True)
class RunSummary:
    run_id: str
    model: str
    repair_strategy: str
    prompt_variant: str
    programs: int
    passed_first_try: int
    passed: int
    compiled: int
    cases_passed: int
    cases_total: int
    avg_attempts: float
    repairs: int
    repeated_repairs: int | None  # None when neither the record nor artifacts can tell
    avg_llm_s: float
    tokens: int
    stage_counts: dict[str, int]
    records: list[Record]

    @property
    def label(self) -> str:
        return f"{self.model} ({self.config})"

    @property
    def config(self) -> str:
        extra = "" if self.prompt_variant == "base" else f", {self.prompt_variant} prompt"
        return f"{self.repair_strategy} repair{extra}"


def _strategy(r: Record) -> str:
    return str(r.get("repair_strategy", "replay"))


def _variant(r: Record) -> str:
    return str(r.get("prompt_variant", "base"))


def _squash(text: str) -> str:
    return "".join(text.split())


def _repeated(r: Record, artifacts: Path | None) -> int | None:
    """Repairs whose code equalled the previous attempt's.

    Records from before this was tracked lack the field; for those it is
    recomputed from the saved ``attempt-N.java`` artifacts when available.
    """
    if "repeated_repairs" in r:
        return int(r["repeated_repairs"])
    if artifacts is None:
        return None
    folder = artifacts / r["run_id"] / r["program"]
    files = [folder / f"attempt-{k}.java" for k in range(1, r["attempts"] + 1)]
    if not all(f.exists() for f in files):
        return None
    code = [_squash(f.read_text(encoding="utf-8")) for f in files]
    return sum(a == b for a, b in itertools.pairwise(code))


def summarise(run_id: str, records: list[Record], artifacts: Path | None = None) -> RunSummary:
    n = len(records)
    repeated = [_repeated(r, artifacts) for r in records]
    attempts = [a for r in records for a in r["attempt_log"]]
    return RunSummary(
        run_id=run_id,
        model=records[0]["model"],
        repair_strategy=_strategy(records[0]),
        prompt_variant=_variant(records[0]),
        programs=n,
        passed_first_try=sum(bool(r["passed_first_try"]) for r in records),
        passed=sum(bool(r["passed"]) for r in records),
        compiled=sum(bool(r["compile_ok"]) for r in records),
        cases_passed=sum(r["tests_passed"] for r in records),
        cases_total=sum(r["tests_total"] for r in records),
        avg_attempts=sum(r["attempts"] for r in records) / n,
        repairs=sum(max(r["attempts"] - 1, 0) for r in records),
        repeated_repairs=None if None in repeated else sum(x or 0 for x in repeated),
        avg_llm_s=sum(r["llm_latency_s"] for r in records) / n,
        tokens=sum(r["prompt_tokens"] + r["completion_tokens"] for r in records),
        stage_counts=dict(Counter(a["stage"] for a in attempts)),
        records=sorted(records, key=lambda r: r["program"]),
    )


def select_runs(
    records: Iterable[Record],
    run_ids: list[str] | None = None,
    artifacts: Path | None = None,
) -> list[RunSummary]:
    """Group by run; keep the given runs, or the latest run per model and configuration."""
    by_run: dict[str, list[Record]] = defaultdict(list)
    for r in records:
        by_run[r["run_id"]].append(r)
    if run_ids:
        chosen = [rid for rid in run_ids if rid in by_run]
    else:
        latest: dict[tuple[str, str, str], str] = {}
        for rid, recs in by_run.items():
            key = (recs[0]["model"], _strategy(recs[0]), _variant(recs[0]))
            if rid > latest.get(key, ""):
                latest[key] = rid
        chosen = list(latest.values())
    runs = [summarise(rid, by_run[rid], artifacts) for rid in chosen]
    return sorted(runs, key=lambda s: (-s.passed, -s.cases_passed, s.model, s.config))


def _pct(k: int | None, n: int) -> str:
    if k is None:
        return "n/a"
    return f"{k}/{n} ({100 * k / n:.0f}%)" if n else "-"


def metric_rows(runs: list[RunSummary]) -> list[tuple[str, list[str]]]:
    """(metric name, one value per run) rows shared by both renderers."""
    return [
        ("Pass rate, first try", [_pct(s.passed_first_try, s.programs) for s in runs]),
        ("Pass rate, after repair", [_pct(s.passed, s.programs) for s in runs]),
        ("Final attempt compiled", [_pct(s.compiled, s.programs) for s in runs]),
        (
            "Test cases passed (compiled programs)",
            [f"{s.cases_passed}/{s.cases_total}" for s in runs],
        ),
        ("Avg attempts", [f"{s.avg_attempts:.2f}" for s in runs]),
        ("Repairs repeating previous code", [_pct(s.repeated_repairs, s.repairs) for s in runs]),
        ("Avg LLM time / program", [f"{s.avg_llm_s:.0f} s" for s in runs]),
        ("Tokens (whole corpus)", [f"{s.tokens:,}" for s in runs]),
    ]


STAGES = ["passed", "run", "compile", "static_check", "no_code"]
STAGE_NAMES = {
    "passed": "passed",
    "run": "wrong output / crash",
    "compile": "compile error",
    "static_check": "used floating point",
    "no_code": "no code",
}


def stage_rows(runs: list[RunSummary]) -> list[tuple[str, list[str]]]:
    return [
        (STAGE_NAMES[st], [str(s.stage_counts.get(st, 0)) for s in runs])
        for st in STAGES
        if any(s.stage_counts.get(st) for s in runs)
    ]


def program_cell(r: Record) -> str:
    if r["passed"]:
        return f"PASS ({r['attempts']})"
    stage = STAGE_NAMES.get(r["final_stage"], r["final_stage"])
    if r["final_stage"] == "run":
        return f"FAIL {r['tests_passed']}/{r['tests_total']}"
    return f"FAIL: {stage}"


def program_rows(runs: list[RunSummary]) -> list[tuple[str, list[str]]]:
    programs = sorted({r["program"] for s in runs for r in s.records})
    index = {(s.run_id, r["program"]): r for s in runs for r in s.records}
    return [
        (p, [program_cell(index[(s.run_id, p)]) if (s.run_id, p) in index else "-" for s in runs])
        for p in programs
    ]


# --------------------------------------------------------------------------- Markdown


def _md_table(header: list[str], rows: list[tuple[str, list[str]]]) -> str:
    lines = ["| " + " | ".join(header) + " |", "|" + "---|" * len(header)]
    lines += ["| " + " | ".join([name, *vals]) + " |" for name, vals in rows]
    return "\n".join(lines)


def render_markdown(runs: list[RunSummary]) -> str:
    header = ["", *[f"`{s.model}`<br>{s.config}" for s in runs]]
    parts = [
        "# cobol-bridge results",
        "",
        "Latest run per model and configuration (repair strategy, prompt variant). "
        "A program passes only if the Java output matches GnuCOBOL byte for byte on every "
        "generated input.",
        "",
        "## Summary",
        "",
        _md_table(header, metric_rows(runs)),
        "",
        "## Where attempts stopped",
        "",
        "Every attempt (first try and repairs) by the stage it reached.",
        "",
        _md_table(header, stage_rows(runs)),
        "",
        "## Per program",
        "",
        "`PASS (n)`: passed on attempt n. `FAIL k/m`: compiled and ran, k of m cases matched.",
        "",
        _md_table(header, program_rows(runs)),
        "",
        "## Runs",
        "",
        *[f"- `{s.run_id}`: {s.label}" for s in runs],
        "",
    ]
    return "\n".join(parts)


# --------------------------------------------------------------------------- HTML

_CSS = """
body{font:15px/1.5 -apple-system,system-ui,sans-serif;max-width:1100px;margin:2rem auto;
padding:0 1rem;color:#1f2328}
h1{font-size:1.6rem}h2{font-size:1.2rem;margin-top:2rem}
table{border-collapse:collapse;margin:.5rem 0 1rem}
th,td{border:1px solid #d0d7de;padding:.35rem .7rem;text-align:right;white-space:nowrap}
th:first-child,td:first-child{text-align:left}
thead th{background:#f6f8fa}
td.pass{background:#dafbe1}td.fail{background:#ffebe9}
code{font-size:.9em}
@media (prefers-color-scheme:dark){body{background:#0d1117;color:#e6edf3}
th,td{border-color:#30363d}thead th{background:#161b22}
td.pass{background:#033a16}td.fail{background:#4b1113}}
"""


def _html_table(header: list[str], rows: list[tuple[str, list[str]]], color: bool = False) -> str:
    head = "".join(f"<th>{h}</th>" for h in header)
    body = []
    for name, vals in rows:
        cells = []
        for v in vals:
            cls = ""
            if color:
                cls = ' class="pass"' if v.startswith("PASS") else ' class="fail"'
            cells.append(f"<td{cls}>{html.escape(v)}</td>")
        body.append(f"<tr><td>{html.escape(name)}</td>{''.join(cells)}</tr>")
    return f"<table><thead><tr>{head}</tr></thead><tbody>{''.join(body)}</tbody></table>"


def render_html(runs: list[RunSummary]) -> str:
    header = [
        "",
        *[f"<code>{html.escape(s.model)}</code><br>{html.escape(s.config)}" for s in runs],
    ]
    runs_list = "".join(
        f"<li><code>{html.escape(s.run_id)}</code>: {html.escape(s.label)}</li>" for s in runs
    )
    return f"""<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>cobol-bridge results</title><style>{_CSS}</style></head>
<body>
<h1>cobol-bridge results</h1>
<p>Latest run per model and configuration (repair strategy, prompt variant).
A program passes only if the Java output matches
GnuCOBOL byte for byte on every generated input.</p>
<h2>Summary</h2>
{_html_table(header, metric_rows(runs))}
<h2>Where attempts stopped</h2>
<p>Every attempt (first try and repairs) by the stage it reached.</p>
{_html_table(header, stage_rows(runs))}
<h2>Per program</h2>
<p><code>PASS (n)</code>: passed on attempt n. <code>FAIL k/m</code>: compiled and ran,
k of m cases matched.</p>
{_html_table(header, program_rows(runs), color=True)}
<h2>Runs</h2>
<ul>{runs_list}</ul>
</body></html>
"""
