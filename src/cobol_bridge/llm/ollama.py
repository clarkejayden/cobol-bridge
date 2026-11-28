"""Ollama backend using its local HTTP API (``POST /api/chat``)."""

from __future__ import annotations

import os
import time
from typing import Any

import httpx

from cobol_bridge.llm.base import GenerationParams, LLMError, LLMResponse, Message

DEFAULT_HOST = "http://localhost:11434"
REQUEST_TIMEOUT_S = 900.0
# Waits before retrying a request that failed with a 5xx or a dropped connection.
# Requests are seeded, so a retry asks for the same completion again.
RETRY_DELAYS_S = (10.0, 30.0)


class OllamaProvider:
    """Chat with any model pulled into a local Ollama server."""

    def __init__(
        self,
        model: str,
        host: str | None = None,
        *,
        transport: httpx.BaseTransport | None = None,
        retry_delays_s: tuple[float, ...] = RETRY_DELAYS_S,
    ) -> None:
        self._model = model
        self._host = (host or os.environ.get("OLLAMA_HOST") or DEFAULT_HOST).rstrip("/")
        if not self._host.startswith("http"):
            self._host = f"http://{self._host}"
        self._client = httpx.Client(
            base_url=self._host, timeout=REQUEST_TIMEOUT_S, transport=transport
        )
        self._retry_delays_s = retry_delays_s
        self.retries = 0  # requests retried after a transient failure, for the logs

    @property
    def name(self) -> str:
        return "ollama"

    @property
    def model(self) -> str:
        return self._model

    def check_ready(self) -> None:
        """Raise LLMError if the server is down or the model has not been pulled."""
        try:
            tags = self._client.get("/api/tags", timeout=5).json()
        except httpx.HTTPError as exc:
            raise LLMError(
                f"Ollama not reachable at {self._host}: start it with `ollama serve`"
            ) from exc
        names = {m.get("name") for m in tags.get("models", [])}
        if self._model not in names and f"{self._model}:latest" not in names:
            raise LLMError(f"model {self._model} not pulled: run `ollama pull {self._model}`")

    def chat(self, messages: list[Message], params: GenerationParams) -> LLMResponse:
        payload: dict[str, Any] = {
            "model": self._model,
            "messages": [{"role": m.role, "content": m.content} for m in messages],
            "stream": False,
            "keep_alive": "15m",
            "options": {
                "temperature": params.temperature,
                "seed": params.seed,
                "num_ctx": params.num_ctx,
                "num_predict": params.max_tokens,
            },
        }
        resp, start = self._post_with_retries(payload)
        data = resp.json()
        return LLMResponse(
            text=data.get("message", {}).get("content", ""),
            prompt_tokens=int(data.get("prompt_eval_count", 0)),
            completion_tokens=int(data.get("eval_count", 0)),
            latency_s=time.monotonic() - start,
        )

    def _post_with_retries(self, payload: dict[str, Any]) -> tuple[httpx.Response, float]:
        """POST /api/chat; retry 5xx and dropped connections, not timeouts or 4xx.

        Returns the response and the start time of the attempt that succeeded, so
        latency measures one generation rather than the waits between retries.
        """
        delays = [*self._retry_delays_s, None]
        for delay in delays:
            start = time.monotonic()
            try:
                resp = self._client.post("/api/chat", json=payload)
                resp.raise_for_status()
                return resp, start
            except httpx.HTTPStatusError as exc:
                transient = exc.response.status_code >= 500
                error: httpx.HTTPError = exc
            except (httpx.ConnectError, httpx.ReadError, httpx.RemoteProtocolError) as exc:
                transient = True
                error = exc
            except httpx.HTTPError as exc:
                raise LLMError(f"Ollama request failed: {exc}") from exc
            if not transient or delay is None:
                raise LLMError(f"Ollama request failed: {error}") from error
            self.retries += 1
            time.sleep(delay)
        raise AssertionError("unreachable")
