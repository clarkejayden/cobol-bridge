"""Byte-level output comparison with one documented normalisation.

Normalisation: trailing spaces and tabs are removed from every line, and
trailing empty lines at the end of the output are dropped. Nothing else is
changed: leading spaces, inner spacing, case, digits, signs, line order and
line count all have to match exactly. COBOL pads DISPLAYed fields with
trailing spaces that carry no meaning; everything else is significant.
"""

from __future__ import annotations

import difflib
from dataclasses import dataclass


@dataclass(frozen=True)
class CompareResult:
    match: bool
    first_diff_line: int | None  # 1-based, after normalisation
    diff: str  # unified diff, empty when matching


def normalize(output: bytes) -> list[str]:
    """Split into lines and strip trailing whitespace; see the module docstring."""
    text = output.decode("utf-8", errors="replace").replace("\r\n", "\n")
    lines = [line.rstrip(" \t") for line in text.split("\n")]
    while lines and lines[-1] == "":
        lines.pop()
    return lines


def compare(
    expected: bytes, actual: bytes, *, context: int = 2, max_lines: int = 40
) -> CompareResult:
    """Compare COBOL (expected) and Java (actual) output after normalisation."""
    exp, act = normalize(expected), normalize(actual)
    if exp == act:
        return CompareResult(True, None, "")
    first = next(
        (i + 1 for i, (a, b) in enumerate(zip(exp, act, strict=False)) if a != b),
        min(len(exp), len(act)) + 1,
    )
    diff_lines = list(
        difflib.unified_diff(exp, act, "expected (COBOL)", "actual (Java)", n=context, lineterm="")
    )
    if len(diff_lines) > max_lines:
        diff_lines = [
            *diff_lines[:max_lines],
            f"... ({len(diff_lines) - max_lines} more diff lines)",
        ]
    return CompareResult(False, first, "\n".join(diff_lines))
