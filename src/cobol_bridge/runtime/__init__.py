"""Java support code that a translation can be compiled with (see ``Pic.java``)."""

from __future__ import annotations

from importlib.resources import files


def pic_java() -> str:
    """Source of the ``Pic`` helper class: COBOL field parsing, storing and DISPLAY."""
    return files(__package__).joinpath("Pic.java").read_text(encoding="utf-8")
