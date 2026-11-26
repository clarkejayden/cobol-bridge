from decimal import Decimal
from pathlib import Path

import pytest

from cobol_bridge.inputs.generator import encode, generate_cases, max_value
from cobol_bridge.inputs.picparse import Field, UnsupportedInput, accepted_record

CORPUS = Path(__file__).resolve().parents[1] / "corpus"

SAMPLE = """\
       IDENTIFICATION DIVISION.
       PROGRAM-ID. SAMPLE.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
      * a comment line with ACCEPT FAKE-REC in it.
       01  IN-REC.
           05  IN-ID                PIC X(4).
           05  IN-AMT               PIC S9(5)V99
                                    SIGN IS LEADING SEPARATE.
           05  IN-ROW               OCCURS 2 TIMES.
               10  IN-CODE          PIC X.
                   88  CODE-OK                  VALUE "A" "B".
               10  IN-QTY           PIC 9(3).
                   88  QTY-SMALL                VALUE 1 THRU 9.
       01  WS-OTHER                 PIC 9(3)V9 COMP-3.
       PROCEDURE DIVISION.
           ACCEPT IN-REC
           ACCEPT WS-DATE FROM DATE
           DISPLAY IN-ID
           STOP RUN.
"""


def fields() -> list[Field]:
    return accepted_record(SAMPLE)[1]


def test_record_layout() -> None:
    name, fs = accepted_record(SAMPLE)
    assert name == "IN-REC"
    assert [f.name for f in fs] == [
        "IN-ID",
        "IN-AMT",
        "IN-CODE(1)",
        "IN-QTY(1)",
        "IN-CODE(2)",
        "IN-QTY(2)",
    ]
    assert sum(f.length for f in fs) == 4 + 8 + 2 * (1 + 3)


def test_numeric_field_attributes() -> None:
    amt = fields()[1]
    assert amt.numeric and amt.signed and amt.sign_leading
    assert (amt.int_digits, amt.scale, amt.length) == (5, 2, 8)
    assert max_value(amt) == Decimal("99999.99")


def test_condition_values_parsed() -> None:
    code, qty = fields()[2], fields()[3]
    assert [str(c.low) for c in code.conditions] == ["A", "B"]
    assert (qty.conditions[0].low, qty.conditions[0].high) == (Decimal(1), Decimal(9))


def test_encode_numeric() -> None:
    amt = fields()[1]
    assert encode(amt, Decimal("-12.5")) == "-0001250"
    assert encode(amt, Decimal(0)) == "+0000000"
    with pytest.raises(ValueError):
        encode(amt, Decimal("100000"))


def test_encode_alnum_pads_and_truncates() -> None:
    assert encode(fields()[0], "AB") == "AB  "
    assert encode(fields()[0], "ABCDEFG") == "ABCD"


def test_cases_are_fixed_width_and_deterministic() -> None:
    a = generate_cases(SAMPLE, seed=7, program="S")
    b = generate_cases(SAMPLE, seed=7, program="S")
    assert a == b
    assert all(len(c.stdin) == 4 + 8 + 8 + 1 for c in a)
    assert len({c.stdin for c in a}) == len(a)


def test_different_seed_changes_random_cases() -> None:
    a = generate_cases(SAMPLE, seed=1, program="S")
    b = generate_cases(SAMPLE, seed=2, program="S")
    assert a != b


def test_boundary_cases_present() -> None:
    cases = {c.name: c.stdin.decode() for c in generate_cases(SAMPLE, seed=1, program="S")}
    assert cases["zeros"] == "    +0000000 000 000\n"
    assert cases["max"].startswith("ZZZZ+9999999")
    assert cases["min"][4:12] == "-9999999"
    # edges of the 1 THRU 9 range: 0, 1, 9 and 10
    for edge in ("0", "1", "9", "10"):
        assert f"IN-QTY(1)={edge}" in cases
    # every listed code plus one that matches none
    assert "IN-CODE(1)=A" in cases and "IN-CODE(1)=B" in cases
    assert "IN-CODE(1)=?" in cases


def test_packed_input_rejected() -> None:
    src = SAMPLE.replace("ACCEPT IN-REC", "ACCEPT WS-OTHER")
    with pytest.raises(UnsupportedInput):
        accepted_record(src)


def test_overpunched_sign_rejected() -> None:
    src = SAMPLE.replace("SIGN IS LEADING SEPARATE.", ".").replace(
        "PIC S9(5)V99\n", "PIC S9(5)V99.\n"
    )
    with pytest.raises(UnsupportedInput):
        accepted_record(src)


@pytest.mark.parametrize("path", sorted(CORPUS.glob("*.cbl")), ids=lambda p: p.stem)
def test_every_corpus_program_has_inputs(path: Path) -> None:
    cases = generate_cases(path.read_text(), seed=42, program=path.stem)
    assert len(cases) >= 10
