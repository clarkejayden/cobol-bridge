from cobol_bridge.pipeline.compare import compare, normalize


def test_identical_outputs_match() -> None:
    assert compare(b"A\nB\n", b"A\nB\n").match


def test_trailing_whitespace_is_ignored() -> None:
    assert compare(b"TOTAL   12.00   \nOK\n", b"TOTAL   12.00\nOK  \t\n").match


def test_trailing_blank_lines_are_ignored() -> None:
    assert compare(b"A\n", b"A\n\n\n").match
    assert compare(b"A", b"A\n").match


def test_crlf_treated_as_lf() -> None:
    assert compare(b"A\nB\n", b"A\r\nB\r\n").match


def test_leading_whitespace_is_significant() -> None:
    res = compare(b"   12.00\n", b"12.00\n")
    assert not res.match
    assert res.first_diff_line == 1


def test_inner_whitespace_is_significant() -> None:
    assert not compare(b"A  B\n", b"A B\n").match


def test_blank_line_in_middle_is_significant() -> None:
    assert not compare(b"A\n\nB\n", b"A\nB\n").match


def test_decimal_difference_detected() -> None:
    res = compare(b"NET 100.01\n", b"NET 100.00\n")
    assert not res.match
    assert "-NET 100.01" in res.diff
    assert "+NET 100.00" in res.diff


def test_missing_line_reports_position() -> None:
    res = compare(b"A\nB\nC\n", b"A\nB\n")
    assert not res.match
    assert res.first_diff_line == 3


def test_diff_is_capped() -> None:
    exp = b"".join(b"line %d\n" % i for i in range(200))
    res = compare(exp, b"")
    assert len(res.diff.splitlines()) <= 41


def test_normalize() -> None:
    assert normalize(b"x  \ny\t\n\n") == ["x", "y"]
