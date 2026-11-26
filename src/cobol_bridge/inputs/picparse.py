"""A small parser for the parts of a COBOL DATA DIVISION that drive input generation.

It understands enough to describe the record a program ACCEPTs from stdin:
level numbers, PIC strings, OCCURS, SIGN ... SEPARATE, USAGE and 88-level
condition values (literals and THRU ranges). Anything it cannot represent as
stdin text (packed or binary input fields, overpunched signs) raises
UnsupportedInput rather than guessing.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from decimal import Decimal

_LEVEL = re.compile(r"^\d{1,2}$")
_ACCEPT = re.compile(r"\bACCEPT\s+([A-Z0-9][A-Z0-9-]*)(\s+FROM\b)?", re.IGNORECASE)


class UnsupportedInput(ValueError):
    """The ACCEPTed record uses a layout the generator cannot produce as text."""


@dataclass(frozen=True)
class Condition:
    """One 88-level value: a single literal, or an inclusive numeric range."""

    low: str | Decimal
    high: str | Decimal | None = None


@dataclass
class Item:
    """A data description entry (group or elementary)."""

    level: int
    name: str
    pic: str | None = None
    usage: str = "DISPLAY"
    occurs: int = 1
    sign_separate: bool = False
    sign_leading: bool = False
    conditions: list[Condition] = field(default_factory=list)
    children: list[Item] = field(default_factory=list)


@dataclass(frozen=True)
class Field:
    """An elementary field of the input record, after OCCURS expansion."""

    name: str
    numeric: bool
    length: int  # characters it occupies in the record
    int_digits: int = 0
    scale: int = 0
    signed: bool = False
    sign_leading: bool = True
    conditions: tuple[Condition, ...] = ()

    @property
    def digits(self) -> int:
        return self.int_digits + self.scale


# --------------------------------------------------------------------------- source


def source_text(source: str) -> str:
    """Code area (columns 8-72) of fixed-format source, without comment lines."""
    lines = []
    for raw in source.splitlines():
        line = raw.rstrip("\n")
        if len(line) >= 7 and line[6] in "*/":
            continue
        lines.append(line[7:72] if len(line) > 7 else "")
    return "\n".join(lines)


def _division(text: str, name: str) -> str:
    """Text of the DATA or PROCEDURE division (empty if absent)."""
    upper = text.upper()
    data = upper.find("DATA DIVISION")
    proc = upper.find("PROCEDURE DIVISION")
    if name == "DATA":
        return text[data:proc] if 0 <= data < proc else ""
    return text[proc:] if proc >= 0 else ""


def _entries(data_division: str) -> list[list[str]]:
    """Split into entries terminated by a period followed by whitespace."""
    tokens = re.findall(r'"[^"]*"|\'[^\']*\'|[^\s"\']+', data_division)
    entries: list[list[str]] = []
    current: list[str] = []
    for tok in tokens:
        if tok.endswith(".") and not tok.startswith(("'", '"')):
            stripped = tok[:-1]
            if stripped:
                current.append(stripped)
            if current:
                entries.append(current)
            current = []
        else:
            current.append(tok)
    if current:
        entries.append(current)
    return entries


def _literal(tok: str) -> str | Decimal:
    if tok[0] in "\"'":
        return tok[1:-1]
    upper = tok.upper()
    if upper in ("SPACE", "SPACES"):
        return " "
    if upper in ("ZERO", "ZEROS", "ZEROES"):
        return Decimal(0)
    return Decimal(tok)


def _parse_conditions(tokens: list[str]) -> list[Condition]:
    """Parse the literals after VALUE/VALUES in an 88-level entry."""
    i = 0
    while i < len(tokens) and tokens[i].upper() not in ("VALUE", "VALUES"):
        i += 1
    vals = [t for t in tokens[i + 1 :] if t.upper() not in ("IS", "ARE")]
    out: list[Condition] = []
    j = 0
    while j < len(vals):
        low = _literal(vals[j])
        if j + 2 < len(vals) and vals[j + 1].upper() in ("THRU", "THROUGH"):
            out.append(Condition(low, _literal(vals[j + 2])))
            j += 3
        else:
            out.append(Condition(low))
            j += 1
    return out


def _parse_item(tokens: list[str]) -> Item | None:
    if not tokens or not _LEVEL.match(tokens[0]):
        return None
    level = int(tokens[0])
    rest = tokens[1:]
    name = "FILLER"
    if rest and rest[0].upper() not in ("PIC", "PICTURE", "OCCURS", "VALUE", "REDEFINES"):
        name = rest[0].upper()
        rest = rest[1:]
    item = Item(level=level, name=name)
    upper = [t.upper() for t in rest]
    for k, tok in enumerate(upper):
        if tok in ("PIC", "PICTURE"):
            nxt = k + 2 if k + 1 < len(upper) and upper[k + 1] == "IS" else k + 1
            item.pic = upper[nxt]
        elif tok == "OCCURS":
            item.occurs = int(upper[k + 1])
        elif tok == "SEPARATE":
            item.sign_separate = True
        elif tok == "LEADING":
            item.sign_leading = True
        elif tok in ("COMP-3", "PACKED-DECIMAL", "COMP", "COMP-5", "BINARY", "COMPUTATIONAL"):
            item.usage = tok
    return item


def parse_data_division(source: str) -> dict[str, Item]:
    """Parse WORKING-STORAGE into level-01/77 items keyed by name (children nested)."""
    text = source_text(source)
    entries = _entries(_division(text, "DATA"))
    roots: dict[str, Item] = {}
    stack: list[Item] = []
    last: Item | None = None
    for tokens in entries:
        if tokens and tokens[0] == "88":
            if last is not None:
                last.conditions.extend(_parse_conditions(tokens))
            continue
        item = _parse_item(tokens)
        if item is None:
            continue
        while stack and stack[-1].level >= item.level:
            stack.pop()
        if item.level in (1, 77) or not stack:
            roots[item.name] = item
            stack = [item]
        else:
            stack[-1].children.append(item)
            stack.append(item)
        last = item
    return roots


# --------------------------------------------------------------------------- PIC


def _expand_pic(pic: str) -> str:
    return re.sub(r"(.)\((\d+)\)", lambda m: m.group(1) * int(m.group(2)), pic.upper())


def _elementary(item: Item, name: str) -> Field:
    assert item.pic is not None
    pic = _expand_pic(item.pic)
    if set(pic) <= set("XA"):
        return Field(name=name, numeric=False, length=len(pic), conditions=tuple(item.conditions))
    if not set(pic) <= set("S9V"):
        raise UnsupportedInput(f"{name}: edited or unusual PIC {item.pic} in input record")
    if item.usage != "DISPLAY":
        raise UnsupportedInput(f"{name}: {item.usage} fields cannot be supplied as text")
    signed = pic.startswith("S")
    if signed and not item.sign_separate:
        raise UnsupportedInput(f"{name}: signed input needs SIGN ... SEPARATE")
    body = pic.lstrip("S")
    int_part, _, frac_part = body.partition("V")
    return Field(
        name=name,
        numeric=True,
        length=len(int_part) + len(frac_part) + (1 if signed else 0),
        int_digits=len(int_part),
        scale=len(frac_part),
        signed=signed,
        sign_leading=item.sign_leading or not signed,
        conditions=tuple(item.conditions),
    )


def flatten(item: Item, suffix: str = "") -> list[Field]:
    """Elementary fields of ``item`` in record order, expanding OCCURS.

    Repeated fields are named with their subscripts, e.g. ``IN-SKU(3)``.
    """
    out: list[Field] = []
    for n in range(item.occurs):
        sub = suffix + (f"({n + 1})" if item.occurs > 1 else "")
        if item.children:
            for child in item.children:
                out.extend(flatten(child, sub))
        elif item.pic:
            out.append(_elementary(item, item.name + sub))
    return out


def accepted_record(source: str) -> tuple[str, list[Field]]:
    """Name and fields of the item the program ACCEPTs from stdin.

    Phase 1 programs read exactly one record with a single ``ACCEPT``.
    """
    proc = _division(source_text(source), "PROCEDURE")
    found = (m.group(1).upper() for m in _ACCEPT.finditer(proc) if not m.group(2))
    targets = list(dict.fromkeys(found))
    if len(targets) != 1:
        raise UnsupportedInput(f"expected one ACCEPT target, found {targets or 'none'}")
    items = parse_data_division(source)
    name = targets[0]
    if name not in items:
        raise UnsupportedInput(f"ACCEPT target {name} is not a level-01 item")
    return name, flatten(items[name])
