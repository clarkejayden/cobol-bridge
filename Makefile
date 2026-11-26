MODEL ?= granite4:micro
ATTEMPTS ?= 3

.PHONY: help doctor install test lint typecheck check demo eval

help:
	@echo "make doctor     check toolchain, Ollama and model"
	@echo "make test       run unit tests (no Ollama needed)"
	@echo "make check      lint + typecheck + tests"
	@echo "make demo       translate and verify one corpus program"
	@echo "make eval       evaluate the whole corpus (MODEL=$(MODEL))"

doctor:
	@scripts/doctor.sh $(MODEL)

install:
	uv sync

test:
	uv run pytest -q

lint:
	uv run ruff check .
	uv run ruff format --check .

typecheck:
	uv run mypy

check: lint typecheck test

demo:
	uv run cobol-bridge translate corpus/INTEREST.cbl --model $(MODEL)

eval:
	uv run cobol-bridge eval corpus --model $(MODEL) --max-attempts $(ATTEMPTS)
