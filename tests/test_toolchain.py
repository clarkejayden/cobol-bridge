from cobol_bridge.sandbox.toolchain import JAVA_MAJOR


def test_targets_java_21() -> None:
    assert JAVA_MAJOR == 21
