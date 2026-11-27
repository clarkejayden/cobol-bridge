"""Prompts for the explain, translate and repair steps.

The translation rules spell out the COBOL semantics that most often break a
naive translation (fixed-point arithmetic, truncation, DISPLAY formatting),
including GnuCOBOL's own way of displaying unedited numeric fields.
"""

from __future__ import annotations

import re

from cobol_bridge.inputs.picparse import Field

SYSTEM = (
    "You are a senior mainframe modernisation engineer. You translate COBOL programs "
    "into idiomatic, behaviour-identical Java 21. Output from the Java program is "
    "compared byte for byte with the original COBOL program, so exact formatting matters."
)

TRANSLATION_RULES = """\
Rules for the Java program:
1. One file: `public class Main` with `public static void main(String[] args)`, no package,
   Java 21 standard library only. Return it in a single ```java code block.
2. Input: read ONE line from standard input, treating a missing line as empty:
       String line = new java.io.BufferedReader(
           new java.io.InputStreamReader(System.in)).readLine();
   then pad it on the right with spaces to the record length (declare `main` with
   `throws Exception`). Slice fields by the offsets given below. Import every class you
   use (java.math.BigDecimal, java.math.RoundingMode, ...).
   Numeric input digits carry an implied decimal point at V (e.g. PIC 9(3)V99 "01234"
   is 12.34). A SIGN LEADING SEPARATE field starts with '+' or '-'.
3. Money and every field with V, COMP-3 or decimals: use java.math.BigDecimal with an
   explicit scale equal to the COBOL field's decimal places. NEVER use double, float,
   floating-point literals such as 1.5 or Math.pow/round: write new BigDecimal("1.5").
   This is checked automatically and the translation is rejected if violated.
4. Storing a result into a field: keep the field's decimal places. Without ROUNDED, extra
   decimals are truncated (RoundingMode.DOWN). With ROUNDED use RoundingMode.HALF_UP.
   If the integer part is too large for the field, COBOL silently drops the high-order
   digits; with ON SIZE ERROR the field is left unchanged and the handler runs instead.
   Compute divisions with ample precision (e.g. scale 20) before setting the final scale.
5. Initial values: numeric fields start at zero, alphanumeric fields at spaces, unless VALUE.
   MOVE to an alphanumeric field left-justifies and pads or truncates on the right.
6. DISPLAY writes its operands with no separator and then a newline ("\\n").
   - Alphanumeric fields are shown at their full declared width, padded with spaces.
   - Numeric-edited fields follow their PIC: Z shows a space for a leading zero; a comma
     is shown only when a digit appears to its left, otherwise a space; '.' is the decimal
     point; a leading '-' shows '-' if negative else a space; a leading '+' shows '+' or
     '-'; a trailing CR shows "CR" if negative else two spaces. The width never changes.
   - Unedited numeric fields (USAGE DISPLAY or COMP-3) are shown with all digits including
     leading zeros, a '.' where V is, and a leading '+' or '-' if the PIC has S:
     PIC 9(3) value 7 -> "007"; PIC S9(3) value -7 -> "-007";
     PIC 9(3)V99 value 5.5 -> "005.50"; PIC S9(3)V99 COMP-3 value 5.5 -> "+005.50".
7. STOP RUN ends the program normally with exit code 0. Do not print anything extra.
"""

# (PIC, COBOL literal moved into the field, exact GnuCOBOL DISPLAY output).
# tests/test_prompts.py compiles these with cobc and checks every output, so the
# examples given to the model are the oracle's own behaviour, not hand-written.
FORMAT_EXAMPLES: tuple[tuple[str, str, str], ...] = (
    ("9(2)", "0", "00"),
    ("9(4)", "12", "0012"),
    ("9(5)V99", "36.5", "00036.50"),
    ("S9(4)V99", "-3.2", "-0003.20"),
    ("S9(3)", "7", "+007"),
    ("X(8)", '"AB"', "AB      "),
    ("X(5)", "SPACES", "     "),
    ("ZZ9", "0", "  0"),
    ("Z,ZZ9", "1234", "1,234"),
    ("Z,ZZ9", "5", "    5"),
    ("ZZ,ZZ9.99", "0", "     0.00"),
    ("ZZ,ZZ9.99", "36796.64", "36,796.64"),
    ("-Z,ZZ9", "-42", "-   42"),
    ("-Z,ZZ9", "42", "    42"),
    ("+ZZ,ZZ9", "42", "+    42"),
    ("+ZZ,ZZ9", "-1500", "- 1,500"),
    ("ZZ9.99CR", "-3.5", "  3.50CR"),
)


def format_examples() -> str:
    """Worked DISPLAY examples, added to the translation rules by the "examples" variant."""
    width = max(len(pic) for pic, _, _ in FORMAT_EXAMPLES)
    lines = [
        "8. Worked DISPLAY examples from GnuCOBOL (brackets mark the exact width; every field",
        "   is printed at its full width, so leading zeros and padding spaces are never dropped):",
    ]
    for pic, value, shown in FORMAT_EXAMPLES:
        lines.append(f"     PIC {pic:<{width}}  value {value:<9} -> [{shown}]")
    lines += [
        "   java.text.DecimalFormat and String.valueOf do not produce these: build each field's",
        "   text yourself (digits with zero fill, then the PIC's editing), and keep its width.",
    ]
    return "\n".join(lines) + "\n"


PROMPT_VARIANTS = ("base", "examples")


def translation_rules(variant: str = "base") -> str:
    if variant not in PROMPT_VARIANTS:
        raise ValueError(f"unknown prompt variant {variant!r}")
    return TRANSLATION_RULES + (format_examples() if variant == "examples" else "")


def describe_record(record_name: str, fields: list[Field]) -> str:
    """A table of the input record's fields, offsets and types for the prompt."""
    rows = [f"Input record {record_name} (one line on stdin):", "offset  length  field  type"]
    offset = 0
    for f in fields:
        if f.numeric:
            kind = f"numeric {f.int_digits} integer digits, {f.scale} decimals"
            if f.signed:
                kind += ", leading separate sign"
        else:
            kind = "alphanumeric"
        rows.append(f"{offset:>6}  {f.length:>6}  {f.name}  {kind}")
        offset += f.length
    rows.append(f"record length: {offset}")
    return "\n".join(rows)


def explain(source: str) -> str:
    return (
        "Explain what this COBOL program does, in plain English, for an engineer who will "
        "port it. Cover: the data layout (input record, working fields and their types), "
        "the business logic step by step, and exactly what it prints. Be concise "
        "(under 300 words).\n\n```cobol\n" + source + "\n```"
    )


def translate(source: str, explanation: str, record: str, variant: str = "base") -> str:
    return (
        "Translate this COBOL program into Java 21.\n\n"
        f"```cobol\n{source}\n```\n\n"
        f"What the program does:\n{explanation.strip()}\n\n"
        f"{record}\n\n{translation_rules(variant)}"
    )


def repair(request: str, previous: str, feedback: str) -> str:
    """A self-contained repair request: the task, the failing program, and what is wrong.

    The failing code is quoted in the user turn rather than replayed as the
    model's own answer, which small models tend to repeat verbatim.
    """
    return (
        f"{request}\n\n"
        "A previous translation of this program failed:\n"
        f"```java\n{previous.rstrip()}\n```\n\n"
        f"{feedback}\n"
        "Change the code to fix the problem; returning the same program again will fail."
    )


def repair_compile(errors: str) -> str:
    return (
        "The Java program does not compile. javac reported:\n"
        f"```\n{errors}\n```\n"
        "Fix every error and return the complete corrected program in one ```java block."
    )


def repair_static(violations: list[str]) -> str:
    listed = "\n".join(f"- {v}" for v in violations)
    return (
        "The Java program uses floating point, which cannot represent COBOL decimals "
        f"exactly:\n{listed}\n"
        "Use java.math.BigDecimal with explicit scale and RoundingMode instead, and return "
        "the complete corrected program in one ```java block."
    )


def repair_mismatch(record: bytes, diff: str, passed: int, total: int) -> str:
    shown = record.decode(errors="replace").rstrip("\n")
    return (
        f"The Java program compiled but its output differs from COBOL on {total - passed} of "
        f"{total} test inputs. For this input record:\n```\n{shown}\n```\n"
        "the difference is (lines starting '-' are the COBOL output, '+' are yours):\n"
        f"```diff\n{diff}\n```\n"
        "Find the cause, fix it, and return the complete corrected program in one "
        "```java block."
    )


def repair_runtime(record: bytes, detail: str) -> str:
    shown = record.decode(errors="replace").rstrip("\n")
    return (
        f"The Java program failed at run time for this input record:\n```\n{shown}\n```\n"
        f"```\n{detail}\n```\n"
        "Fix it and return the complete corrected program in one ```java block."
    )


def repair_no_code() -> str:
    return (
        "I could not find a Java program in your answer. "
        "Return the complete program in one ```java block."
    )


_FENCE = re.compile(r"```[ \t]*([A-Za-z0-9_+-]*)[^\n]*\n(.*?)```", re.DOTALL)


def extract_java(text: str) -> str | None:
    """The Java code block from a model answer (the one declaring class Main, else the largest)."""
    blocks = [(str(m.group(1)).lower(), str(m.group(2))) for m in _FENCE.finditer(text)]
    if not blocks:
        return text if "class Main" in text else None
    with_main = [b for lang, b in blocks if "class Main" in b]
    if with_main:
        return max(with_main, key=len)
    java = [b for lang, b in blocks if lang == "java"]
    return max(java or [b for _, b in blocks], key=len)
