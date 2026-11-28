"""The Ollama client's request and retry behaviour, against a mock HTTP transport."""

from __future__ import annotations

import json

import httpx
import pytest

from cobol_bridge.llm.base import GenerationParams, LLMError, Message
from cobol_bridge.llm.ollama import OllamaProvider

OK = {"message": {"content": "hi"}, "prompt_eval_count": 7, "eval_count": 3}


def provider(statuses: list[int], seen: list[dict[str, object]]) -> OllamaProvider:
    replies = iter(statuses)

    def handle(request: httpx.Request) -> httpx.Response:
        seen.append(json.loads(request.content))
        status = next(replies)
        return httpx.Response(status, json=OK if status == 200 else {"error": "boom"})

    return OllamaProvider(
        "m", "http://ollama.test", transport=httpx.MockTransport(handle), retry_delays_s=(0, 0)
    )


def test_sends_explicit_sampling_options() -> None:
    seen: list[dict[str, object]] = []
    resp = provider([200], seen).chat(
        [Message("user", "x")], GenerationParams(temperature=0.4, seed=43)
    )
    assert (resp.text, resp.prompt_tokens, resp.completion_tokens) == ("hi", 7, 3)
    options = seen[0]["options"]
    assert isinstance(options, dict)
    assert options["temperature"] == 0.4 and options["seed"] == 43


def test_retries_server_errors_with_the_same_request() -> None:
    seen: list[dict[str, object]] = []
    p = provider([500, 503, 200], seen)
    assert p.chat([Message("user", "x")], GenerationParams()).text == "hi"
    assert p.retries == 2 and seen[0] == seen[1] == seen[2]


def test_gives_up_after_the_last_retry() -> None:
    p = provider([500, 500, 500], [])
    with pytest.raises(LLMError, match="500"):
        p.chat([Message("user", "x")], GenerationParams())


def test_does_not_retry_client_errors() -> None:
    seen: list[dict[str, object]] = []
    with pytest.raises(LLMError, match="404"):
        provider([404, 200], seen).chat([Message("user", "x")], GenerationParams())
    assert len(seen) == 1
