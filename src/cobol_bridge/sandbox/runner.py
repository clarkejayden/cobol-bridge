"""Process-level isolation for compiling and running untrusted generated code.

This is deliberately *not* a security sandbox: it limits accidents (runaway
loops, huge output, stray files, inherited secrets) rather than defending
against hostile code. Every child process:

* runs in a fresh temporary directory outside the repository, deleted afterwards;
* is started without a shell, from an argument list;
* gets a minimal environment (PATH to the toolchain, HOME and TMPDIR set to the
  workspace, LANG=C) instead of the caller's environment;
* has a wall-clock timeout, after which its whole process group is killed;
* has RLIMIT_CPU and RLIMIT_FSIZE set, and its output read back truncated.
"""

from __future__ import annotations

import contextlib
import os
import resource
import shutil
import signal
import subprocess
import tempfile
import time
from collections.abc import Callable, Iterator, Mapping, Sequence
from dataclasses import dataclass
from pathlib import Path

DEFAULT_TIMEOUT_S = 10.0
DEFAULT_MAX_OUTPUT = 1_000_000
_REPO_ROOT = Path(__file__).resolve().parents[3]


@dataclass(frozen=True)
class ExecResult:
    """Outcome of one child process."""

    returncode: int
    stdout: bytes
    stderr: bytes
    timed_out: bool
    truncated: bool
    duration_s: float

    @property
    def ok(self) -> bool:
        return self.returncode == 0 and not self.timed_out


class Workspace:
    """A throwaway directory in which files are written and commands run."""

    def __init__(self, root: Path, path_env: str) -> None:
        self.root = root
        self._path_env = path_env

    def write(self, files: Mapping[str, bytes | str]) -> None:
        """Write files relative to the workspace root; names may not escape it."""
        for name, content in files.items():
            target = (self.root / name).resolve()
            if not target.is_relative_to(self.root):
                raise ValueError(f"file name escapes workspace: {name!r}")
            target.parent.mkdir(parents=True, exist_ok=True)
            data = content.encode() if isinstance(content, str) else content
            target.write_bytes(data)

    def env(self) -> dict[str, str]:
        return {
            "PATH": self._path_env,
            "HOME": str(self.root),
            "TMPDIR": str(self.root),
            "LANG": "C",
            "LC_ALL": "C",
        }

    def run(
        self,
        cmd: Sequence[str | os.PathLike[str]],
        *,
        stdin: bytes = b"",
        timeout_s: float = DEFAULT_TIMEOUT_S,
        max_output: int = DEFAULT_MAX_OUTPUT,
        cpu_limit_s: int | None = None,
    ) -> ExecResult:
        """Run ``cmd`` in the workspace with the limits described in the module docstring."""
        args = [os.fspath(c) for c in cmd]
        cpu = cpu_limit_s if cpu_limit_s is not None else int(timeout_s * 4) + 1
        out_path, err_path = self.root / ".stdout", self.root / ".stderr"
        start = time.monotonic()
        with out_path.open("wb") as out, err_path.open("wb") as err:
            proc = subprocess.Popen(  # argument list, never a shell
                args,
                cwd=self.root,
                env=self.env(),
                stdin=subprocess.PIPE,
                stdout=out,
                stderr=err,
                start_new_session=True,
                preexec_fn=_limits(cpu, max_output * 2),
            )
            timed_out = _communicate(proc, stdin, timeout_s)
        duration = time.monotonic() - start
        stdout, t1 = _read_capped(out_path, max_output)
        stderr, t2 = _read_capped(err_path, max_output)
        out_path.unlink(missing_ok=True)
        err_path.unlink(missing_ok=True)
        return ExecResult(
            returncode=proc.returncode,
            stdout=stdout,
            stderr=stderr,
            timed_out=timed_out,
            truncated=t1 or t2,
            duration_s=duration,
        )


@contextlib.contextmanager
def workspace(path_env: str, prefix: str = "cobol-bridge-") -> Iterator[Workspace]:
    """Create a temporary workspace outside the repository and delete it on exit."""
    root = Path(tempfile.mkdtemp(prefix=prefix)).resolve()
    if root.is_relative_to(_REPO_ROOT):
        shutil.rmtree(root, ignore_errors=True)
        raise RuntimeError(f"refusing to run generated code inside the repository: {root}")
    try:
        yield Workspace(root, path_env)
    finally:
        shutil.rmtree(root, ignore_errors=True)


def _limits(cpu_s: int, fsize: int) -> Callable[[], None]:
    def apply() -> None:
        resource.setrlimit(resource.RLIMIT_CPU, (cpu_s, cpu_s))
        resource.setrlimit(resource.RLIMIT_FSIZE, (fsize, fsize))
        # Writing past RLIMIT_FSIZE should fail the write, not kill the child,
        # so a chatty program still exits and we report truncation.
        signal.signal(signal.SIGXFSZ, signal.SIG_IGN)

    return apply


def _communicate(proc: subprocess.Popen[bytes], stdin: bytes, timeout_s: float) -> bool:
    """Feed stdin and wait; on timeout kill the whole process group. Returns timed_out."""
    try:
        proc.communicate(input=stdin, timeout=timeout_s)
        return False
    except subprocess.TimeoutExpired:
        with contextlib.suppress(ProcessLookupError):
            os.killpg(proc.pid, signal.SIGKILL)
        proc.wait()
        return True


def _read_capped(path: Path, limit: int) -> tuple[bytes, bool]:
    if not path.exists():
        return b"", False
    with path.open("rb") as f:
        data = f.read(limit + 1)
    return data[:limit], len(data) > limit
