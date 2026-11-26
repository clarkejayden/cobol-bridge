"""Derive deterministic test inputs from the record a program ACCEPTs.

Cases come in two groups:

* boundary cases: every field at zero/spaces, every field at its maximum
  width, signed fields at their most negative, the smallest non-zero unit,
  and, field by field, the edges of each 88-level condition (both ends of a
  THRU range and the values just outside it, every listed literal, and one
  value that matches none);
* random cases from a seeded RNG: fields with 88-levels mostly take values
  that satisfy one of them (so the interesting branches run), other values
  have magnitudes spread evenly across the field's digit range.

Each case is one fixed-width record followed by a newline. The same source,
seed and count always produce the same bytes.
"""

from __future__ import annotations

import random
import zlib
from dataclasses import dataclass
from decimal import Decimal

from cobol_bridge.inputs.picparse import Condition, Field, accepted_record

ALNUM_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.-"
Value = Decimal | str


@dataclass(frozen=True)
class TestCase:
    """One input to run through both programs."""

    name: str
    stdin: bytes

    __test__ = False  # not a pytest class


# --------------------------------------------------------------------------- encoding


def max_value(f: Field) -> Decimal:
    return Decimal(10**f.digits - 1).scaleb(-f.scale)


def unit(f: Field) -> Decimal:
    return Decimal(1).scaleb(-f.scale)


def encode(f: Field, value: Value) -> str:
    """Render one field value as the characters it occupies in the record."""
    if not f.numeric:
        text = str(value)
        return text[: f.length].ljust(f.length)
    assert isinstance(value, Decimal)
    if abs(value) > max_value(f) or (value < 0 and not f.signed):
        raise ValueError(f"{value} does not fit {f.name}")
    digits = str(int(abs(value).scaleb(f.scale))).zfill(f.digits)
    if not f.signed:
        return digits
    sign = "-" if value < 0 else "+"
    return sign + digits if f.sign_leading else digits + sign


def encode_record(fields: list[Field], values: list[Value]) -> bytes:
    return ("".join(encode(f, v) for f, v in zip(fields, values, strict=True)) + "\n").encode()


# --------------------------------------------------------------------------- values


def _fits(f: Field, v: Decimal) -> bool:
    return abs(v) <= max_value(f) and (v >= 0 or f.signed) and v == v.quantize(unit(f))


def _zero(f: Field) -> Value:
    return Decimal(0) if f.numeric else ""


def _maximum(f: Field) -> Value:
    return max_value(f) if f.numeric else "Z" * f.length


def _minimum(f: Field) -> Value:
    if not f.numeric:
        return "A" * f.length
    return -max_value(f) if f.signed else unit(f)


def condition_values(f: Field) -> list[Value]:
    """Edge values for a field's 88-level conditions, plus one that matches none."""
    out: list[Value] = []
    for cond in f.conditions:
        out.extend(_edges(f, cond))
    if f.conditions and not f.numeric:
        literals = {str(c.low) for c in f.conditions}
        miss = next(
            (c * f.length for c in "?#XQ" if c * f.length not in literals),
            "",
        )
        out.append(miss)
    return list(dict.fromkeys(out))


def _edges(f: Field, cond: Condition) -> list[Value]:
    if not f.numeric:
        return [str(cond.low)]
    lo = Decimal(cond.low)
    hi = Decimal(cond.high) if cond.high is not None else lo
    step = unit(f)
    return [v for v in (lo - step, lo, hi, hi + step) if _fits(f, v)]


def matching_value(f: Field, cond: Condition, rng: random.Random) -> Value:
    """A value that satisfies ``cond`` (uniform inside a THRU range)."""
    if not f.numeric or cond.high is None:
        return cond.low if not f.numeric else Decimal(cond.low)
    step = unit(f)
    lo, hi = Decimal(cond.low), Decimal(cond.high)
    steps = int((hi - lo) / step)
    return lo + step * rng.randint(0, max(steps, 0))


def _free_value(f: Field, rng: random.Random) -> Value:
    """A random value ignoring conditions; numeric magnitudes spread over digit counts."""
    if not f.numeric:
        length = rng.randint(0, f.length)
        return "".join(rng.choice(ALNUM_ALPHABET) for _ in range(length))
    width = rng.randint(1, f.digits)
    unscaled = rng.randrange(10 ** (width - 1), 10**width) if width > 1 else rng.randrange(10)
    value = Decimal(unscaled).scaleb(-f.scale)
    if f.signed and rng.random() < 0.5:
        value = -value
    return value


def random_value(f: Field, rng: random.Random, p_condition: float | None = None) -> Value:
    """Mostly values the program's 88-levels expect, sometimes anything that fits.

    By default a field with n conditions takes a matching value with probability
    n/(n+1), capped at 0.8: a lone 88 (often a sentinel such as a header row)
    is hit half the time, a list of valid codes most of the time.
    """
    if p_condition is None:
        n = len(f.conditions)
        p_condition = min(0.8, n / (n + 1))
    if f.conditions and rng.random() < p_condition:
        return matching_value(f, rng.choice(f.conditions), rng)
    return _free_value(f, rng)


# --------------------------------------------------------------------------- cases


def boundary_cases(fields: list[Field], rng: random.Random) -> list[tuple[str, list[Value]]]:
    """Whole-record edge cases, then one case per condition edge of each field."""
    cases: list[tuple[str, list[Value]]] = [
        ("zeros", [_zero(f) for f in fields]),
        ("max", [_maximum(f) for f in fields]),
        ("min", [_minimum(f) for f in fields]),
        ("unit", [unit(f) if f.numeric else "A" for f in fields]),
    ]
    baseline = [random_value(f, rng, p_condition=1.0) for f in fields]
    for i, f in enumerate(fields):
        for v in condition_values(f):
            values = list(baseline)
            values[i] = v
            cases.append((f"{f.name}={str(v).strip() or 'SPACES'}", values))
    return cases


def generate_cases(
    source: str, *, seed: int, n_random: int = 20, program: str = ""
) -> list[TestCase]:
    """Boundary plus seeded random cases for the record ``source`` ACCEPTs."""
    _, fields = accepted_record(source)
    rng = random.Random(seed ^ zlib.crc32(program.encode()))
    named = boundary_cases(fields, rng)
    named += [(f"random-{k + 1}", [random_value(f, rng) for f in fields]) for k in range(n_random)]
    seen: set[bytes] = set()
    cases: list[TestCase] = []
    for name, values in named:
        data = encode_record(fields, values)
        if data not in seen:
            seen.add(data)
            cases.append(TestCase(name, data))
    return cases
