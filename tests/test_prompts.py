"""The prompt variants, and the worked DISPLAY examples checked against GnuCOBOL."""

from __future__ import annotations

import pytest

from cobol_bridge.llm import prompts
from cobol_bridge.sandbox.programs import compile_cobol, compile_java
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


def test_helpers_variant_ships_pic() -> None:
    assert "Pic.show(" in prompts.translation_rules("helpers")
    assert set(prompts.support_files("helpers")) == {"Pic.java"}
    assert prompts.support_files("base") == {} == prompts.support_files("examples")


# (text as it appears in HELPER_RULES, Java expression, expected String.valueOf result)
HELPER_EXAMPLES = [
    ('"9(6)V99")        "00123456" -> 1234.56', 'Pic.num("00123456", "9(6)V99")', "1234.56"),
    ('"S9(3)V99")         "-01234"   -> -12.34', 'Pic.num("-01234", "S9(3)V99")', "-12.34"),
    (
        'Pic.show(new BigDecimal("5"), "9(3)") -> "005"',
        'Pic.show(new BigDecimal("5"), "9(3)")',
        "005",
    ),
    (
        'Pic.show(new BigDecimal("0"), "ZZ,ZZ9.99") -> "     0.00"',
        'Pic.show(new BigDecimal("0"), "ZZ,ZZ9.99")',
        "     0.00",
    ),
    ('Pic.show("AB", "X(5)")\n         -> "AB   "', 'Pic.show("AB", "X(5)")', "AB   "),
]


@pytest.mark.toolchain
def test_helper_rule_examples_are_true(ws: Workspace, toolchain: Toolchain) -> None:
    """The examples shown to the model in the helper rules hold when run."""
    for shown, _, _ in HELPER_EXAMPLES:
        assert shown in prompts.HELPER_RULES, shown
    body = "\n".join(
        f'    System.out.println("[" + {expr} + "]");' for _, expr, _ in HELPER_EXAMPLES
    )
    java = (
        "import java.math.BigDecimal;\n"
        "public class Main { public static void main(String[] a) {\n"
        f"{body}\n}} }}\n"
    )
    out = compile_java(ws, toolchain, java, prompts.support_files("helpers"))
    assert out.program is not None, out.diagnostics
    lines = out.program.run(b"").stdout.decode().splitlines()
    assert lines == [f"[{expected}]" for _, _, expected in HELPER_EXAMPLES]
