"""Locate the native toolchain (GnuCOBOL and a JDK 21) and report its versions."""

from __future__ import annotations

import os
import platform
import shutil
import subprocess
from dataclasses import dataclass
from functools import cache
from pathlib import Path

JAVA_MAJOR = 21
_HOMEBREW_JDK21 = Path("/opt/homebrew/opt/openjdk@21")


class ToolchainError(RuntimeError):
    """Raised when a required tool cannot be found."""


@dataclass(frozen=True)
class Toolchain:
    """Absolute paths to the executables the pipeline needs."""

    cobc: Path
    java_home: Path

    @property
    def javac(self) -> Path:
        return self.java_home / "bin" / "javac"

    @property
    def java(self) -> Path:
        return self.java_home / "bin" / "java"

    def search_path(self) -> str:
        """A minimal PATH for child processes: cobc needs the C compiler and linker."""
        dirs = [str(self.cobc.parent), str(self.java_home / "bin")]
        cc = shutil.which("cc")
        if cc:
            dirs.append(str(Path(cc).parent))
        dirs += ["/usr/bin", "/bin"]
        return os.pathsep.join(dict.fromkeys(dirs))


def _java_home_candidates() -> list[Path]:
    candidates: list[Path] = []
    if env := os.environ.get("COBOL_BRIDGE_JAVA_HOME"):
        candidates.append(Path(env))
    if platform.system() == "Darwin":
        brew = _HOMEBREW_JDK21 / "libexec" / "openjdk.jdk" / "Contents" / "Home"
        candidates.append(brew)
        try:
            out = subprocess.run(
                ["/usr/libexec/java_home", "-v", str(JAVA_MAJOR)],
                capture_output=True,
                text=True,
                timeout=10,
                check=False,
            )
            if out.returncode == 0:
                candidates.append(Path(out.stdout.strip()))
        except (OSError, subprocess.TimeoutExpired):
            pass
    if env := os.environ.get("JAVA_HOME"):
        candidates.append(Path(env))
    return candidates


def java_major_version(java_home: Path) -> int | None:
    """Return the major version reported by ``java -version``, or None."""
    java = java_home / "bin" / "java"
    if not java.exists():
        return None
    out = subprocess.run(
        [str(java), "-XshowSettings:properties", "-version"],
        capture_output=True,
        text=True,
        timeout=30,
        check=False,
    )
    for line in out.stderr.splitlines():
        line = line.strip()
        if line.startswith("java.specification.version"):
            value = line.split("=", 1)[1].strip()
            return int(value.split(".")[0])
    return None


@cache
def find_toolchain() -> Toolchain:
    """Resolve cobc and a JDK 21; raise ToolchainError with a fix if either is missing."""
    cobc = os.environ.get("COBOL_BRIDGE_COBC") or shutil.which("cobc")
    if not cobc:
        raise ToolchainError("cobc not found: install GnuCOBOL (brew install gnucobol)")
    for home in _java_home_candidates():
        if java_major_version(home) == JAVA_MAJOR:
            return Toolchain(cobc=Path(cobc), java_home=home)
    raise ToolchainError(
        f"No JDK {JAVA_MAJOR} found: brew install openjdk@{JAVA_MAJOR}, "
        "or set COBOL_BRIDGE_JAVA_HOME"
    )


def tool_versions(tc: Toolchain) -> dict[str, str]:
    """First line of each tool's version banner, recorded with every result."""

    def first_line(cmd: list[str]) -> str:
        out = subprocess.run(cmd, capture_output=True, text=True, timeout=30, check=False)
        text = (out.stdout or out.stderr).strip()
        return text.splitlines()[0] if text else "unknown"

    return {
        "cobc": first_line([str(tc.cobc), "--version"]),
        "javac": first_line([str(tc.javac), "-version"]),
    }
