from __future__ import annotations

import os
from collections.abc import Iterator

import pytest

from cobol_bridge.sandbox.runner import Workspace, workspace
from cobol_bridge.sandbox.toolchain import Toolchain, ToolchainError, find_toolchain


@pytest.fixture(scope="session")
def toolchain() -> Toolchain:
    """The real cobc + JDK 21; skipped locally if absent, a hard failure in CI."""
    try:
        return find_toolchain()
    except ToolchainError as exc:
        if os.environ.get("COBOL_BRIDGE_REQUIRE_TOOLCHAIN") == "1":
            raise
        pytest.skip(str(exc))


@pytest.fixture
def ws() -> Iterator[Workspace]:
    with workspace("/usr/bin:/bin") as w:
        yield w
