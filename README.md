# cobol-bridge

**An LLM translates COBOL to Java; differential testing against the real COBOL compiler proves whether each translation is correct.**

Runs entirely on a laptop: GnuCOBOL and a JDK compile both sides, and IBM's open
Granite models run locally through Ollama. No API keys and no cloud.

<!-- TODO: replace with a GIF of `make demo` -->
![demo placeholder](docs/demo-placeholder.svg)

## Results

Phase 1 baseline: `granite4:micro` (3B), 10 programs, up to 3 attempts each,
temperature 0, seed 42 (run `20261126T024358Z-granite4-micro`).

| Metric | granite4:micro |
|---|---|
| Programs passing on the first try | **0 / 10** |
| Programs passing after repair | **0 / 10** |
| Programs whose final attempt compiled | 3 / 10 |
| Test cases passed (where the Java compiled) | 0 / 66 |
| Average attempts | 3.00 |
| Average LLM time per program | 291 s |
| Tokens, whole corpus | 113,236 |

Where the 30 attempts stopped: 21 at `javac`, 6 at run time (an exception on every
input), 3 at the floating-point check. **15 of the 20 repair attempts returned
exactly the same code as the attempt before**, even though the feedback contained the
compiler error or stack trace. A 3B model at temperature 0 mostly does not use the
repair feedback. Larger models and a repair strategy that breaks that loop are
the next step (Phase 2).

**Why `granite4:micro` is the default:** it is a 2.1 GB download and leaves room for
the JDK on an 8 GB M1. `granite4:tiny-h` (7B mixture-of-experts, about 1B parameters
active) also fits: Ollama ran it 100% on the GPU at 4.4 GB. In a one-program spot check
(SALESTAX, not part of `results.jsonl`) it was faster than `micro` (160 s vs 186 s of
LLM time) but failed the same way, repeating code that did not compile. The full
multi-model comparison is Phase 2.

This is the point of the project: a translation that *looks* plausible is not
accepted until it reproduces the COBOL output byte for byte.

Every number above comes from [`results/results.jsonl`](results/results.jsonl) (one
record per program per run) and can be regenerated with `make eval`. The generated
Java, explanations and repair feedback for each attempt are in
[`results/artifacts/`](results/artifacts/).

## How it works

```mermaid
flowchart LR
    SRC[COBOL program] --> PARSE[Parse DATA DIVISION<br/>input record layout]
    PARSE --> GEN[Generate inputs<br/>boundary + seeded random]
    SRC --> COBC[cobc compile + run]
    GEN --> COBC
    COBC --> BASE[(Expected outputs)]

    SRC --> EXPLAIN[LLM: explain]
    EXPLAIN --> TRANS[LLM: translate to Java 21]
    TRANS --> CHECK{No double / float?}
    CHECK -- no --> REPAIR
    CHECK -- yes --> JAVAC[javac compile + run]
    GEN --> JAVAC
    JAVAC --> CMP{Output identical<br/>on every case?}
    BASE --> CMP
    JAVAC -- compile error --> REPAIR[LLM: repair with<br/>error or diff]
    CMP -- no --> REPAIR
    REPAIR -- up to N attempts --> CHECK
    CMP -- yes --> PASS[Verified Java]
    CMP --> JSONL[(results.jsonl)]
```

For each program in [`corpus/`](corpus/):

1. **Explain.** The model writes a plain-English summary (data layout, logic, output).
2. **Translate.** The model writes a single-file Java 21 program, given the source, its
   own explanation, the input record layout and a short list of COBOL semantics rules.
3. **Verify.** The COBOL is compiled with `cobc` and the Java with `javac`; both run on
   the same generated inputs, and their outputs must be identical.
4. **Repair.** On a compile error, a floating-point violation, a crash or an output
   mismatch, the model gets the compiler error or a unified diff for the first failing
   input and tries again, up to `--max-attempts` (default 3).
5. **Report.** One JSONL record per program: model, sampling settings, attempts,
   compile status, cases passed, tokens, latency, input and source hashes.

## Quickstart

```bash
brew install uv gnucobol openjdk@21 maven ollama
ollama serve &                 # if Ollama isn't already running
ollama pull granite4:micro
make doctor                    # checks every tool and prints versions
make demo                      # translate + verify one program
make eval                      # the whole corpus; MODEL=granite4:tiny-h to compare
uv run cobol-bridge report     # summary of the latest run per model
```

`make test` runs the unit tests without Ollama (a scripted fake model drives the loop).

## Design decisions

**Differential testing, not LLM self-assessment.** A translation is correct only if it
produces the same bytes as the original on every test input; the COBOL program
compiled by GnuCOBOL is the oracle. Inputs are derived from the record the program
`ACCEPT`s: every field at zero and at maximum width, signed fields at their most
negative, the smallest unit, both ends of every 88-level `THRU` range and the values
just outside it, every listed 88-level literal plus one that matches none, then 20
seeded random records. A case on which COBOL itself fails is dropped and counted.
The only normalisation is stripping trailing whitespace from each line (COBOL pads
`DISPLAY` fields with spaces); leading and inner spaces, signs, digits and line
counts must match exactly.

**The BigDecimal rule.** COBOL `PIC 9(5)V99` and `COMP-3` are exact decimal fixed point.
Binary `double` cannot represent 0.10 exactly, so any value that passes through it can
come out a cent off. The translation prompt requires `java.math.BigDecimal` with an
explicit scale, `RoundingMode.DOWN` for COBOL's default truncation and `HALF_UP` for
`ROUNDED`. A static check enforces it before compilation: `double`/`float` types,
floating-point literals, `Math.pow`/`round`/... and `doubleValue()` all fail the
attempt and are sent back for repair. The rule is covered by unit tests.

**Local models only.** Everything runs through Ollama's local HTTP API, defaulting to
IBM Granite 4. Temperature is 0 and the seed is fixed, and both are recorded with every
result. The provider interface (`LLMProvider.chat`) is small enough that a hosted
backend could be added without touching the pipeline.

**Process-level isolation, not a sandbox.** Generated code runs natively, through one
module ([`sandbox/runner.py`](src/cobol_bridge/sandbox/runner.py)): a fresh temporary
directory outside the repository that is deleted afterwards, no shell, a minimal
environment (no inherited secrets), a wall-clock timeout that kills the whole process
group, CPU-time and file-size limits, and capped output. This stops accidents (infinite
loops, runaway output, stray files) but it is **not** a security boundary: the code can
still read your files and use the network. That trade-off is acceptable here because
the inputs are a small corpus I wrote and reviewed, the generated programs are short
and use only the JDK, and the models run locally. Running untrusted COBOL or a hosted
model would need a container or VM.

**GnuCOBOL dialect.** Output is compared against GnuCOBOL, whose `DISPLAY` of an
unedited numeric field shows a sign and a decimal point (`PIC S9(3)V99` → `+005.50`),
unlike IBM Enterprise COBOL. The prompt states this, and the exact toolchain versions
are recorded with every result, because formatting can differ between versions.

## Toolchain used for the results

| Tool | Version |
|---|---|
| macOS (Apple M1, 8 GB) | 26.5.1 |
| GnuCOBOL (`cobc`) | 3.2.0 |
| OpenJDK | 21.0.13.1 |
| Maven | 3.10.2 (checked by `make doctor`; not needed by the Phase 1 pipeline) |
| Ollama | 0.40.15 |
| Python / uv | 3.12.15 / 0.13.5 |

CI (GitHub Actions, `ubuntu-latest`) installs GnuCOBOL from apt and Temurin 21, then
runs lint, type checks and the tests, including every corpus program under `cobc`.

## Layout

```
src/cobol_bridge/
  cli.py            translate | eval | report
  llm/              provider interface, Ollama backend, fake backend, prompts
  inputs/           DATA DIVISION parser and test-input generator
  sandbox/          process-isolated compile/run, toolchain discovery
  pipeline/         static checks, output comparison, verification, repair loop
  report/           JSONL storage
corpus/             10 COBOL programs (see corpus/README.md)
results/            results.jsonl and per-attempt artifacts
tests/              unit tests (no Ollama needed)
scripts/doctor.sh   toolchain check
```

## Roadmap

- **Phase 2:** compare several local models (two Granite sizes and one non-IBM model);
  `report` renders Markdown and static HTML tables.
- **Phase 3:** file-based I/O programs, copybooks, and a write-up of where models fail.

## License

Apache-2.0. See [LICENSE](LICENSE).
