"""Provider-neutral types for talking to a chat model.

The pipeline only depends on ``LLMProvider``; a hosted backend could be added
by implementing ``chat`` without touching the pipeline.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Literal, Protocol

Role = Literal["system", "user", "assistant"]


class LLMError(RuntimeError):
    """The provider could not produce a response."""


@dataclass(frozen=True)
class Message:
    role: Role
    content: str


@dataclass(frozen=True)
class GenerationParams:
    """Sampling settings, recorded with every result for reproducibility."""

    temperature: float = 0.0
    seed: int = 42
    num_ctx: int = 8192
    max_tokens: int = 4096


@dataclass(frozen=True)
class LLMResponse:
    text: str
    prompt_tokens: int
    completion_tokens: int
    latency_s: float


class LLMProvider(Protocol):
    """Anything that can answer a chat conversation."""

    @property
    def name(self) -> str: ...

    @property
    def model(self) -> str: ...

    def chat(self, messages: list[Message], params: GenerationParams) -> LLMResponse: ...
