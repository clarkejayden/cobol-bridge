"""The prompt variants, and the worked DISPLAY examples checked against GnuCOBOL."""

from __future__ import annotations

import pytest

from cobol_bridge.llm import prompts
from cobol_bridge.sandbox.programs import compile_cobol
from cobol_bridge.sandbox.runner import Workspace
from cobol_bridge.sandbox.toolchain import Toolchain


def _example_program() -> str:
    """A COBOL program that DISPLAYs every example between brackets."""
    fields = [f"       01 E{i} PIC {pic}." for i, (pic, _, _) in enumerate(prompts.FORMAT_EXAMPLES)]
    moves = [f"           MOVE {v} TO E{i}." for i, (_, v, _) in enumerate(prompts.FORMAT_EXAMPLES)]
    shows = [f'           DISPLAY "[" E{i} "]".' for i in range(len(prompts.FORMAT_EXAMPLES))]
    return "\n".join(
        [
            "       IDENTIFICATION DIVISION.",
            "       PROGRAM-ID. EXAMPLES.",
            "       DATA DIVISION.",
            "       WORKING-STORAGE SECTION.",
            *fields,
            "       PROCEDURE DIVISION.",
            *moves,
            *shows,
            "           STOP RUN.",
            "",
        ]
    )


@pytest.mark.toolchain
def test_format_examples_match_gnucobol(ws: Workspace, toolchain: Toolchain) -> None:
    out = compile_cobol(ws, toolchain, _example_program())
    assert out.program is not None, out.diagnostics
    result = out.program.run(b"")
    assert result.ok, result.stderr
    shown = result.stdout.decode().splitlines()
    assert shown == [f"[{expected}]" for _, _, expected in prompts.FORMAT_EXAMPLES]


def test_base_variant_is_the_phase_2_prompt() -> None:
    assert prompts.translation_rules("base") == prompts.TRANSLATION_RULES
    assert "Worked DISPLAY examples" not in prompts.translate("SRC", "EXPL", "REC")


def test_examples_variant_adds_every_example() -> None:
    text = prompts.translate("SRC", "EXPL", "REC", "examples")
    assert text.startswith(prompts.translate("SRC", "EXPL", "REC").rstrip("\n"))
    for pic, _, expected in prompts.FORMAT_EXAMPLES:
        assert f"PIC {pic}" in text
        assert f"[{expected}]" in text


def test_unknown_variant_is_rejected() -> None:
    with pytest.raises(ValueError):
        prompts.translation_rules("fancy")
