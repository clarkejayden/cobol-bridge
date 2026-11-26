"""Every corpus program must compile under GnuCOBOL and run cleanly on its generated inputs."""

from pathlib import Path

import pytest

from cobol_bridge.inputs.generator import generate_cases
from cobol_bridge.sandbox.programs import compile_cobol
from cobol_bridge.sandbox.runner import workspace
from cobol_bridge.sandbox.toolchain import Toolchain

CORPUS = Path(__file__).resolve().parents[1] / "corpus"
PROGRAMS = sorted(CORPUS.glob("*.cbl"))


def test_corpus_has_ten_programs() -> None:
    assert len(PROGRAMS) >= 10


@pytest.mark.toolchain
@pytest.mark.parametrize("path", PROGRAMS, ids=lambda p: p.stem)
def test_program_compiles_and_runs(path: Path, toolchain: Toolchain) -> None:
    source = path.read_text()
    with workspace(toolchain.search_path()) as ws:
        outcome = compile_cobol(ws, toolchain, source)
        assert outcome.program is not None, outcome.diagnostics
        for case in generate_cases(source, seed=42, program=path.stem):
            res = outcome.program.run(case.stdin)
            assert res.ok, f"{case.name}: rc={res.returncode} {res.stderr!r}"
            assert res.stdout.strip(), f"{case.name}: no output"
