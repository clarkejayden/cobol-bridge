from __future__ import annotations

import os
import sys
from pathlib import Path

import pytest

from cobol_bridge.sandbox.programs import compile_cobol, compile_java
from cobol_bridge.sandbox.runner import Workspace, workspace
from cobol_bridge.sandbox.toolchain import Toolchain

REPO = Path(__file__).resolve().parents[1]
PY = sys.executable


def test_workspace_is_outside_repo_and_removed() -> None:
    with workspace("/usr/bin:/bin") as w:
        root = w.root
        assert root.exists()
        assert not root.is_relative_to(REPO)
    assert not root.exists()


def test_runs_without_shell_and_passes_stdin(ws: Workspace) -> None:
    res = ws.run([PY, "-c", "import sys; print(sys.stdin.read().upper())"], stdin=b"a; rm -rf x")
    assert res.ok
    assert res.stdout == b"A; RM -RF X\n"


def test_minimal_environment(ws: Workspace, monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setenv("SECRET_TOKEN", "do-not-leak")
    res = ws.run([PY, "-c", "import os, json; print(json.dumps(dict(os.environ)))"])
    env = res.stdout.decode()
    assert "SECRET_TOKEN" not in env
    assert str(ws.root) in env  # HOME and TMPDIR point at the workspace


def test_cwd_is_workspace(ws: Workspace) -> None:
    res = ws.run([PY, "-c", "import os; print(os.getcwd())"])
    assert os.path.realpath(res.stdout.decode().strip()) == os.path.realpath(ws.root)


def test_timeout_kills_process(ws: Workspace) -> None:
    res = ws.run([PY, "-c", "while True: pass"], timeout_s=0.5)
    assert res.timed_out
    assert not res.ok
    assert res.duration_s < 5


def test_timeout_kills_child_processes(ws: Workspace) -> None:
    script = (
        "import subprocess, sys, time\n"
        "subprocess.Popen([sys.executable, '-c', 'import time; time.sleep(60)'])\n"
        "time.sleep(60)\n"
    )
    res = ws.run([PY, "-c", script], timeout_s=0.5)
    assert res.timed_out
    assert res.duration_s < 5


def test_output_is_capped(ws: Workspace) -> None:
    res = ws.run([PY, "-c", "print('x' * 5000)"], max_output=100)
    assert res.truncated
    assert len(res.stdout) == 100


def test_nonzero_exit_reported(ws: Workspace) -> None:
    res = ws.run([PY, "-c", "import sys; sys.stderr.write('boom'); sys.exit(3)"])
    assert res.returncode == 3
    assert res.stderr == b"boom"
    assert not res.ok


def test_rejects_paths_escaping_workspace(ws: Workspace) -> None:
    with pytest.raises(ValueError):
        ws.write({"../escape.txt": "x"})


HELLO_COBOL = """\
       IDENTIFICATION DIVISION.
       PROGRAM-ID. HELLO.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 WS-NAME PIC X(10).
       PROCEDURE DIVISION.
           ACCEPT WS-NAME
           DISPLAY "HELLO " WS-NAME "!"
           STOP RUN.
"""

HELLO_JAVA = """\
public class Main {
    public static void main(String[] args) throws Exception {
        var in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        String line = in.readLine();
        String name = String.format("%-10s", line == null ? "" : line).substring(0, 10);
        System.out.println("HELLO " + name + "!");
    }
}
"""


@pytest.mark.toolchain
def test_compile_and_run_cobol(toolchain: Toolchain) -> None:
    with workspace(toolchain.search_path()) as w:
        outcome = compile_cobol(w, toolchain, HELLO_COBOL)
        assert outcome.program is not None, outcome.diagnostics
        res = outcome.program.run(b"ADA\n")
        assert res.stdout == b"HELLO ADA       !\n"


@pytest.mark.toolchain
def test_cobol_compile_error_reported(toolchain: Toolchain) -> None:
    with workspace(toolchain.search_path()) as w:
        outcome = compile_cobol(w, toolchain, HELLO_COBOL.replace("DISPLAY", "DISPLAX"))
        assert outcome.program is None
        assert "DISPLAX" in outcome.diagnostics or "error" in outcome.diagnostics.lower()


@pytest.mark.toolchain
def test_compile_and_run_java(toolchain: Toolchain) -> None:
    with workspace(toolchain.search_path()) as w:
        outcome = compile_java(w, toolchain, HELLO_JAVA)
        assert outcome.program is not None, outcome.diagnostics
        assert outcome.program.run(b"ADA\n").stdout == b"HELLO ADA       !\n"


@pytest.mark.toolchain
def test_java_compile_error_reported(toolchain: Toolchain) -> None:
    with workspace(toolchain.search_path()) as w:
        outcome = compile_java(w, toolchain, HELLO_JAVA.replace("String name", "Strin name"))
        assert outcome.program is None
        assert "Strin" in outcome.diagnostics
