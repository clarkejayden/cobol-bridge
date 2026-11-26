import pytest

from cobol_bridge.llm.prompts import TRANSLATION_RULES, extract_java
from cobol_bridge.pipeline.checks import floating_point_violations

CLEAN = """\
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    // a double in a comment is fine, so is 1.5 here
    /* and float in a block comment */
    public static void main(String[] args) {
        BigDecimal rate = new BigDecimal("1.5");
        BigDecimal x = rate.multiply(BigDecimal.TEN).setScale(2, RoundingMode.HALF_UP);
        String s = "double 2.5";
        int count = 3;
        long big = 10L;
        System.out.println(x + s + count + big + '.');
    }
}
"""


def test_bigdecimal_code_is_clean() -> None:
    assert floating_point_violations(CLEAN) == []


@pytest.mark.parametrize(
    "snippet",
    [
        "double rate = 0;",
        "float f = 0;",
        "Double boxed = null;",
        "BigDecimal x = BigDecimal.valueOf(1.5);",
        "BigDecimal x = new BigDecimal(0.1);",
        "var y = 2d;",
        "var z = 1e3;",
        "long p = Math.round(x);",
        "var q = Math.pow(1, 2);",
        "var r = amount.doubleValue();",
    ],
)
def test_floating_point_is_rejected(snippet: str) -> None:
    java = CLEAN.replace("int count = 3;", snippet)
    violations = floating_point_violations(java)
    assert len(violations) == 1
    assert violations[0].startswith("line 11:")


def test_rule_is_in_the_translation_prompt() -> None:
    assert "BigDecimal" in TRANSLATION_RULES
    assert "NEVER use double" in TRANSLATION_RULES


def test_extract_java_prefers_main_block() -> None:
    text = "Here:\n```text\nnotes\n```\n```java\npublic class Main {}\n```\nDone."
    assert extract_java(text) == "public class Main {}\n"


def test_extract_java_bare_code() -> None:
    assert extract_java("public class Main { }") == "public class Main { }"


def test_extract_java_none() -> None:
    assert extract_java("I cannot do that.") is None
