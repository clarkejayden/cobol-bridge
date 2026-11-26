"""Static checks on generated Java before it is compiled.

The BigDecimal rule: COBOL fixed-point values must never pass through binary
floating point. We reject ``double``/``float`` types, floating-point literals
and the ``Math`` functions that return doubles. Comments and string literals
are blanked first so that, for example, ``"1.5"`` passed to ``new
BigDecimal(...)`` is allowed.
"""

from __future__ import annotations

import re

_COMMENT_OR_STRING = re.compile(
    r'//[^\n]*|/\*.*?\*/|"""(?:.|\n)*?"""|"(?:\\.|[^"\\\n])*"|\'(?:\\.|[^\'\\\n])*\'',
    re.DOTALL,
)
_RULES: list[tuple[re.Pattern[str], str]] = [
    (re.compile(r"\b(double|float|Double|Float)\b"), "floating-point type"),
    (
        re.compile(
            r"(?<![\w.])(\d+\.\d*|\.\d+)([eE][+-]?\d+)?[dDfF]?(?![\w.])"
            r"|\b\d+[eE][+-]?\d+[dDfF]?\b|\b\d+[dDfF]\b"
        ),
        "floating-point literal",
    ),
    (
        re.compile(r"\bMath\.(pow|sqrt|round|floor|ceil|rint|exp|log|log10|random)\b"),
        "Math function returning double",
    ),
    (re.compile(r"\.doubleValue\(|\.floatValue\("), "conversion to floating point"),
]


def _blank(match: re.Match[str]) -> str:
    """Replace a comment or literal with spaces, keeping newlines so line numbers hold."""
    return "".join("\n" if ch == "\n" else " " for ch in match.group(0))


def floating_point_violations(java: str) -> list[str]:
    """Lines of ``java`` that use floating point, as ``line N: <reason>: <code>``."""
    code = _COMMENT_OR_STRING.sub(_blank, java)
    original = java.splitlines()
    out: list[str] = []
    for n, line in enumerate(code.splitlines(), start=1):
        for pattern, reason in _RULES:
            if pattern.search(line):
                out.append(f"line {n}: {reason}: {original[n - 1].strip()}")
                break
    return out
