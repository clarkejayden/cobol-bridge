"""The Java ``Pic`` helper, checked against GnuCOBOL on generated (PIC, value) cases.

One COBOL program and one Java program are generated from the same case list;
each prints one bracketed line per case, and the two outputs must be identical.
Pictures are every numeric PIC used in the corpus plus extra edited forms.
"""

from __future__ import annotations

import random
import re
from dataclasses import dataclass
from decimal import Decimal
from pathlib import Path

import pytest

from cobol_bridge.runtime import pic_java
from cobol_bridge.sandbox.programs import compile_cobol, compile_java
from cobol_bridge.sandbox.runner import Workspace
from cobol_bridge.sandbox.toolchain import Toolchain

pytestmark = pytest.mark.toolchain

CORPUS = Path(__file__).resolve().parents[1] / "corpus"

EXTRA_EDITED = [
    "ZZZ.ZZ",
    "Z(4)",
    "$$$,$$9.99",
    "$ZZ,ZZ9.99",
    "$ZZ9",
    "***9",
    "--,--9",
    "---9.99",
    "++++9",
    "***,**9.99",
    "ZZ9.99DB",
    "ZZ9.99CR",
    "ZZ9-",
    "ZZ9+",
    "99B99",
    "99/99/99",
    "-9(3)",
    "9(3).99",
    "ZZZV99",
]
EXTRA_UNEDITED = ["S9(5)V99", "9V9(4)", "S9", "9(15)"]


def corpus_pictures() -> list[str]:
    pics: set[str] = set()
    for path in CORPUS.glob("*.cbl"):
        pics.update(re.findall(r"\bPIC\s+([^\s.]+(?:\.[^\s.]+)*)", path.read_text()))
    return sorted(p for p in pics if not re.search(r"[XA]", p))


def is_edited(pic: str) -> bool:
    return bool(re.sub(r"\(\d+\)", "", pic).strip("S9V"))


def digit_shape(pic: str) -> tuple[int, int]:
    """(integer digits, decimals) of a picture, enough to choose interesting values."""
    e = re.sub(r"(.)\((\d+)\)", lambda m: m.group(1) * int(m.group(2)), pic)
    e = e.removesuffix("CR").removesuffix("DB")
    point = max(e.find("."), e.find("V"))

    def count(part: str) -> int:
        return sum(part.count(c) for c in "9Z*$+-")

    if point < 0:
        return count(e) - (1 if e[:2] in ("$$", "++", "--") else 0), 0
    return count(e[:point]) - (1 if e[:2] in ("$$", "++", "--") else 0), count(e[point + 1 :])


def values_for(pic: str, rng: random.Random) -> list[Decimal]:
    ints, dec = digit_shape(pic)
    top = Decimal(10) ** max(ints, 1)
    fixed = [Decimal(0), Decimal(1), Decimal(-1), Decimal("0.05"), Decimal("-0.5"), top - 1, top]
    fixed += [top * 7 + Decimal("3.456"), -(top - 1) / 3, Decimal("-0.001"), -top - 5, top * 10 + 3]
    for _ in range(8):
        magnitude = rng.randint(0, ints + 2)
        v = Decimal(rng.randint(0, 10**magnitude)) / (Decimal(10) ** rng.randint(0, dec + 2))
        fixed.append(v if rng.random() < 0.6 else -v)
    # Keep literals small enough for COBOL (31 digits) and quantised to 6 decimals.
    return [
        v.quantize(Decimal("0.000001")).normalize() for v in fixed if abs(v) < Decimal(10) ** 17
    ]


def literal(v: Decimal) -> str:
    text = format(v, "f")
    return text if "." not in text else text.rstrip("0").rstrip(".") or "0"


@dataclass(frozen=True)
class Case:
    kind: str  # show | rounded | size | num
    pic: str
    value: str  # COBOL/Java numeric literal, or the record text for "num"


def build_cases(seed: int = 7) -> list[Case]:
    rng = random.Random(seed)
    cases: list[Case] = []
    for pic in [*corpus_pictures(), *EXTRA_EDITED, *EXTRA_UNEDITED]:
        for v in values_for(pic, rng):
            cases.append(Case("show", pic, literal(v)))
            if not is_edited(pic):
                cases.append(Case("rounded", pic, literal(v)))
                cases.append(Case("size", pic, literal(v)))
    for pic in ["9(3)V99", "9(7)V99", "S9(5)V99", "S9(7)V99", "9(2)", "S9(3)"]:
        ints, dec = digit_shape(pic)
        for _ in range(6):
            digits = "".join(rng.choice("0123456789") for _ in range(ints + dec))
            sign = rng.choice("+-") if pic.startswith("S") else ""
            cases.append(Case("num", pic, sign + digits))
    return cases


def cobol_program(cases: list[Case]) -> str:
    data: list[str] = []
    proc: list[str] = []
    for i, c in enumerate(cases):
        f = f"F{i:04d}"
        if c.kind == "num":
            sign = " SIGN LEADING SEPARATE" if c.pic.startswith("S") else ""
            data += [
                f"       01 R{i:04d} PIC X({len(c.value)}).",
                f"       01 N{i:04d} REDEFINES R{i:04d} PIC {c.pic}{sign}.",
                f"       01 {f} PIC {c.pic}.",
            ]
            proc += [
                f'           MOVE "{c.value}" TO R{i:04d}.',
                f"           MOVE N{i:04d} TO {f}.",
                f'           DISPLAY "[" {f} "]".',
            ]
            continue
        data.append(f"       01 {f} PIC {c.pic}.")
        if c.kind == "show":
            proc.append(f"           MOVE {c.value} TO {f}.")
            proc.append(f'           DISPLAY "[" {f} "]".')
        elif c.kind == "rounded":
            proc.append(f"           COMPUTE {f} ROUNDED = {c.value}.")
            proc.append(f'           DISPLAY "[" {f} "]".')
        else:
            proc += [
                f"           COMPUTE {f} = {c.value}",
                '               ON SIZE ERROR DISPLAY "[SIZE]"',
                '               NOT ON SIZE ERROR DISPLAY "[OK]"',
                "           END-COMPUTE.",
            ]
    return "\n".join(
        [
            "       IDENTIFICATION DIVISION.",
            "       PROGRAM-ID. PICTEST.",
            "       DATA DIVISION.",
            "       WORKING-STORAGE SECTION.",
            *data,
            "       PROCEDURE DIVISION.",
            *proc,
            "           STOP RUN.",
            "",
        ]
    )


def java_program(cases: list[Case]) -> str:
    lines: list[str] = []
    for c in cases:
        pic = f'"{c.pic}"'
        v = f'new BigDecimal("{c.value}")'
        if c.kind == "show":
            expr = f"Pic.show({v}, {pic})"
        elif c.kind == "rounded":
            expr = f"Pic.show(Pic.fit({v}, {pic}, true), {pic})"
        elif c.kind == "size":
            expr = f'(Pic.overflows({v}, {pic}, false) ? "SIZE" : "OK")'
        else:
            expr = f'Pic.show(Pic.num("{c.value}", {pic}), {pic})'
        lines.append(f'        out.append("[").append({expr}).append("]\\n");')
    # Split into chunks: a single method this long would exceed the JVM's 64 KB limit.
    chunks = [lines[i : i + 200] for i in range(0, len(lines), 200)]
    methods = "\n".join(
        f"    static void part{k}(StringBuilder out) {{\n" + "\n".join(chunk) + "\n    }"
        for k, chunk in enumerate(chunks)
    )
    calls = "\n".join(f"        part{k}(out);" for k in range(len(chunks)))
    return (
        "import java.math.BigDecimal;\n\n"
        "public class Main {\n"
        f"{methods}\n"
        "    public static void main(String[] args) {\n"
        "        StringBuilder out = new StringBuilder();\n"
        f"{calls}\n"
        "        System.out.print(out);\n"
        "    }\n"
        "}\n"
    )


def run_both(ws: Workspace, tc: Toolchain, cases: list[Case]) -> tuple[list[str], list[str]]:
    cob = compile_cobol(ws, tc, cobol_program(cases))
    assert cob.program is not None, cob.diagnostics
    expected = cob.program.run(b"", timeout_s=30)
    assert expected.ok, expected.stderr
    java = compile_java(ws, tc, java_program(cases), {"Pic.java": pic_java()})
    assert java.program is not None, java.diagnostics
    actual = java.program.run(b"", timeout_s=30)
    assert actual.ok, actual.stderr.decode()
    return expected.stdout.decode().splitlines(), actual.stdout.decode().splitlines()


def known_difference(c: Case) -> bool:
    """Documented divergence: a trailing + or - sign on a negative value that truncates to
    zero. GnuCOBOL prints '-' for some such values (-0.5, -0.1) and not others (-0.001);
    Pic.java prints no sign. No corpus program uses a trailing sign."""
    return (
        c.kind == "show"
        and c.pic.endswith(("+", "-"))
        and Decimal(c.value) < 0
        and abs(Decimal(c.value)) < 1
    )


def test_pic_matches_gnucobol(ws: Workspace, toolchain: Toolchain) -> None:
    cases = build_cases()
    assert len(cases) > 500
    expected, actual = run_both(ws, toolchain, cases)
    assert len(expected) == len(actual) == len(cases)
    wrong = [
        f"{c.kind} PIC {c.pic} value {c.value}: COBOL {e} Java {a}"
        for c, e, a in zip(cases, expected, actual, strict=True)
        if e != a and not known_difference(c)
    ]
    assert not wrong, f"{len(wrong)} of {len(cases)} differ:\n" + "\n".join(wrong[:40])
    excused = sum(e != a for c, e, a in zip(cases, expected, actual, strict=True))
    assert excused <= 5, f"{excused} known-difference cases: the exclusion is hiding too much"


def test_alphanumeric_padding(ws: Workspace, toolchain: Toolchain) -> None:
    java = (
        "public class Main { public static void main(String[] a) {\n"
        '  System.out.println("[" + Pic.show("AB", "X(5)") + "]");\n'
        '  System.out.println("[" + Pic.show("ABCDEFG", "X(3)") + "]");\n'
        '  System.out.println("[" + Pic.show("", "XX") + "]");\n'
        "} }\n"
    )
    out = compile_java(ws, toolchain, java, {"Pic.java": pic_java()})
    assert out.program is not None, out.diagnostics
    assert out.program.run(b"").stdout == b"[AB   ]\n[ABC]\n[  ]\n"
