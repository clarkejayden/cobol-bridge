"""A scripted provider for deterministic tests: replies are returned in order."""

from __future__ import annotations

from collections.abc import Iterable

from cobol_bridge.llm.base import GenerationParams, LLMError, LLMResponse, Message


class FakeProvider:
    def __init__(self, replies: Iterable[str], model: str = "fake-model") -> None:
        self._replies = list(replies)
        self._model = model
        self.calls: list[list[Message]] = []

    @property
    def name(self) -> str:
        return "fake"

    @property
    def model(self) -> str:
        return self._model

    def chat(self, messages: list[Message], params: GenerationParams) -> LLMResponse:
        self.calls.append(list(messages))
        if not self._replies:
            raise LLMError("FakeProvider ran out of scripted replies")
        text = self._replies.pop(0)
        prompt_tokens = sum(len(m.content.split()) for m in messages)
        return LLMResponse(text, prompt_tokens, len(text.split()), latency_s=0.0)
