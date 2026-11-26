"""Command-line interface."""

from __future__ import annotations

import typer

app = typer.Typer(help="Translate COBOL to Java with an LLM and prove it with differential tests.")


@app.command()
def version() -> None:
    """Print the version."""
    from cobol_bridge import __version__

    typer.echo(__version__)
