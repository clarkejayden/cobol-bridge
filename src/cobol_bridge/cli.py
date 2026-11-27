"""Command-line interface: ``cobol-bridge translate | eval | report``."""

from __future__ import annotations

import re
import subprocess
from datetime import UTC, datetime
from pathlib import Path
from typing import Annotated, Any

import typer

from cobol_bridge.llm.base import GenerationParams, LLMError
from cobol_bridge.llm.ollama import OllamaProvider
from cobol_bridge.llm.prompts import PROMPT_VARIANTS
from cobol_bridge.pipeline.loop import PipelineConfig, RunRecord, translate_program
from cobol_bridge.report.render import render_html, render_markdown, select_runs
from cobol_bridge.report.store import append_record, read_records
from cobol_bridge.sandbox.toolchain import ToolchainError, find_toolchain, tool_versions

app = typer.Typer(
    help="Translate COBOL to Java with a local LLM and prove it with differential tests.",
    no_args_is_help=True,
    add_completion=False,
)

DEFAULT_MODEL = "granite4:micro"
DEFAULT_RESULTS = Path("results/results.jsonl")

ModelOpt = Annotated[str, typer.Option("--model", "-m", help="Any model pulled into Ollama.")]
AttemptsOpt = Annotated[int, typer.Option(help="Translate + repair attempts per program.")]
SeedOpt = Annotated[int, typer.Option(help="Sampling seed passed to the model.")]
TempOpt = Annotated[float, typer.Option(help="Sampling temperature.")]
CtxOpt = Annotated[int, typer.Option(help="Model context window (tokens).")]
InputSeedOpt = Annotated[int, typer.Option(help="Seed for random test inputs.")]
RandomOpt = Annotated[int, typer.Option(help="Random test cases per program.")]
ResultsOpt = Annotated[Path, typer.Option(help="JSONL file results are appended to.")]
RepairOpt = Annotated[
    str,
    typer.Option(
        help="fresh: new prompt quoting the failing code, sampled at --repair-temperature; "
        "replay: replay the answer as the model's own turn (Phase 1 behaviour)."
    ),
]
RepairTempOpt = Annotated[float, typer.Option(help="Temperature for repair attempts (fresh).")]
VariantOpt = Annotated[
    str,
    typer.Option(
        help="base: rules only; examples: + worked DISPLAY examples; helpers: + the Pic class."
    ),
]


def _echo(msg: str) -> None:
    typer.echo(msg, err=True)


def _run_id(model: str, variant: str = "base") -> str:
    stamp = datetime.now(UTC).strftime("%Y%m%dT%H%M%SZ")
    suffix = "" if variant == "base" else f"-{variant}"
    return f"{stamp}-{re.sub(r'[^A-Za-z0-9.]+', '-', model)}{suffix}"


def _git_commit() -> str:
    try:
        out = subprocess.run(
            ["git", "rev-parse", "--short", "HEAD"],
            capture_output=True,
            text=True,
            timeout=5,
            check=False,
        )
        return out.stdout.strip()
    except OSError:
        return ""


def _setup(model: str) -> tuple[OllamaProvider, Any, dict[str, str]]:
    try:
        tc = find_toolchain()
    except ToolchainError as exc:
        _echo(f"error: {exc}")
        raise typer.Exit(2) from exc
    provider = OllamaProvider(model)
    try:
        provider.check_ready()
    except LLMError as exc:
        _echo(f"error: {exc}")
        raise typer.Exit(2) from exc
    return provider, tc, tool_versions(tc)


def _config(
    attempts: int,
    seed: int,
    temperature: float,
    num_ctx: int,
    input_seed: int,
    n_random: int,
    artifacts: Path,
    repair_strategy: str,
    repair_temperature: float,
    prompt_variant: str,
) -> PipelineConfig:
    if repair_strategy not in ("fresh", "replay"):
        _echo("error: --repair-strategy must be 'fresh' or 'replay'")
        raise typer.Exit(2)
    if prompt_variant not in PROMPT_VARIANTS:
        _echo(f"error: --prompt-variant must be one of {', '.join(PROMPT_VARIANTS)}")
        raise typer.Exit(2)
    return PipelineConfig(
        prompt_variant=prompt_variant,
        repair_strategy=repair_strategy,
        repair_temperature=repair_temperature,
        max_attempts=attempts,
        input_seed=input_seed,
        n_random=n_random,
        params=GenerationParams(temperature=temperature, seed=seed, num_ctx=num_ctx),
        artifacts_dir=artifacts,
    )


def _save(rec: RunRecord, results: Path, versions: dict[str, str], commit: str) -> None:
    data = rec.to_json()
    data["toolchain"] = versions
    data["git_commit"] = commit
    append_record(results, data)


@app.command()
def translate(
    program: Annotated[Path, typer.Argument(exists=True, dir_okay=False, help="A .cbl file.")],
    model: ModelOpt = DEFAULT_MODEL,
    max_attempts: AttemptsOpt = 3,
    seed: SeedOpt = 42,
    temperature: TempOpt = 0.0,
    num_ctx: CtxOpt = 8192,
    input_seed: InputSeedOpt = 42,
    n_random: RandomOpt = 20,
    repair_strategy: RepairOpt = "fresh",
    repair_temperature: RepairTempOpt = 0.4,
    prompt_variant: VariantOpt = "base",
    results: ResultsOpt = DEFAULT_RESULTS,
) -> None:
    """Explain, translate, verify and repair one COBOL program."""
    provider, tc, versions = _setup(model)
    run_id = _run_id(model, prompt_variant)
    artifacts = results.parent / "artifacts" / run_id
    cfg = _config(
        max_attempts,
        seed,
        temperature,
        num_ctx,
        input_seed,
        n_random,
        artifacts,
        repair_strategy,
        repair_temperature,
        prompt_variant,
    )
    rec = translate_program(program, provider, tc, cfg, run_id=run_id, log=_echo)
    _save(rec, results, versions, _git_commit())
    status = "PASS" if rec.passed else "FAIL"
    typer.echo(
        f"{status} {rec.program}: {rec.tests_passed}/{rec.tests_total} cases, "
        f"{rec.attempts} attempt(s), {rec.wall_s:.0f}s"
    )
    typer.echo(f"artifacts: {artifacts / rec.program}")
    raise typer.Exit(0 if rec.passed else 1)


@app.command("eval")
def evaluate(
    corpus_dir: Annotated[Path, typer.Argument(exists=True, file_okay=False)],
    model: ModelOpt = DEFAULT_MODEL,
    max_attempts: AttemptsOpt = 3,
    seed: SeedOpt = 42,
    temperature: TempOpt = 0.0,
    num_ctx: CtxOpt = 8192,
    input_seed: InputSeedOpt = 42,
    n_random: RandomOpt = 20,
    repair_strategy: RepairOpt = "fresh",
    repair_temperature: RepairTempOpt = 0.4,
    prompt_variant: VariantOpt = "base",
    results: ResultsOpt = DEFAULT_RESULTS,
) -> None:
    """Run the full loop on every .cbl program in a directory."""
    programs = sorted(corpus_dir.glob("*.cbl"))
    if not programs:
        _echo(f"error: no .cbl files in {corpus_dir}")
        raise typer.Exit(2)
    provider, tc, versions = _setup(model)
    run_id = _run_id(model, prompt_variant)
    commit = _git_commit()
    cfg = _config(
        max_attempts,
        seed,
        temperature,
        num_ctx,
        input_seed,
        n_random,
        results.parent / "artifacts" / run_id,
        repair_strategy,
        repair_temperature,
        prompt_variant,
    )
    _echo(f"run {run_id}: {len(programs)} programs, model {model}")
    records: list[dict[str, Any]] = []
    for path in programs:
        try:
            rec = translate_program(path, provider, tc, cfg, run_id=run_id, log=_echo)
        except LLMError as exc:
            _echo(f"{path.stem}: LLM error: {exc}")
            raise typer.Exit(2) from exc
        _save(rec, results, versions, commit)
        records.append(rec.to_json())
    typer.echo(summary_table(records))
    typer.echo(f"\nrun id: {run_id}  results: {results}")


@app.command()
def report(
    results: ResultsOpt = DEFAULT_RESULTS,
    run_id: Annotated[
        list[str] | None, typer.Option(help="Report these runs (repeatable) instead of latest.")
    ] = None,
    out_dir: Annotated[
        Path | None, typer.Option(help="Where report.md and report.html go (default: results/).")
    ] = None,
) -> None:
    """Write Markdown and HTML comparison tables (latest run per model and configuration)."""
    records = list(read_records(results))
    if not records:
        _echo(f"no results in {results}")
        raise typer.Exit(1)
    runs = select_runs(records, run_id, artifacts=results.parent / "artifacts")
    if not runs:
        _echo("no matching runs")
        raise typer.Exit(1)
    target = out_dir or results.parent
    target.mkdir(parents=True, exist_ok=True)
    markdown = render_markdown(runs)
    (target / "report.md").write_text(markdown, encoding="utf-8")
    (target / "report.html").write_text(render_html(runs), encoding="utf-8")
    typer.echo(markdown)
    _echo(f"wrote {target / 'report.md'} and {target / 'report.html'}")


def summary_table(records: list[dict[str, Any]]) -> str:
    """Per-program rows plus aggregate metrics, as plain text."""
    lines = [
        f"{'program':<10} {'result':<6} {'attempts':>8} {'cases':>9} {'stage':<12} "
        f"{'tokens':>7} {'llm s':>7}"
    ]
    for r in records:
        lines.append(
            f"{r['program']:<10} {'PASS' if r['passed'] else 'FAIL':<6} {r['attempts']:>8} "
            f"{r['tests_passed']:>4}/{r['tests_total']:<4} {r['final_stage']:<12} "
            f"{r['prompt_tokens'] + r['completion_tokens']:>7} {r['llm_latency_s']:>7.0f}"
        )
    n = len(records)
    if n:
        first = sum(r["passed_first_try"] for r in records)
        passed = sum(r["passed"] for r in records)
        compiled = sum(r["compile_ok"] for r in records)
        avg_attempts = sum(r["attempts"] for r in records) / n
        cases = sum(r["tests_passed"] for r in records), sum(r["tests_total"] for r in records)
        lines += [
            "",
            f"model                     {records[0]['model']}",
            f"pass rate, first try      {first}/{n} ({100 * first / n:.0f}%)",
            f"pass rate, after repair   {passed}/{n} ({100 * passed / n:.0f}%)",
            f"compiled (final attempt)  {compiled}/{n}",
            f"test cases passed         {cases[0]}/{cases[1]}",
            f"avg attempts              {avg_attempts:.2f}",
            f"avg LLM time / program    {sum(r['llm_latency_s'] for r in records) / n:.0f}s",
        ]
    return "\n".join(lines)
