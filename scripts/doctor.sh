#!/usr/bin/env bash
# Check that every tool cobol-bridge needs is installed, print versions, and
# check that Ollama is running with the chosen model pulled.
# Usage: scripts/doctor.sh [model]   (default: $COBOL_BRIDGE_MODEL or granite4:micro)
set -uo pipefail

MODEL="${1:-${COBOL_BRIDGE_MODEL:-granite4:micro}}"
OLLAMA_HOST="${OLLAMA_HOST:-http://localhost:11434}"
failures=0

ok()   { printf '  \033[32mok\033[0m    %-8s %s\n' "$1" "$2"; }
fail() { printf '  \033[31mFAIL\033[0m  %-8s %s\n' "$1" "$2"; failures=$((failures + 1)); }

first_line() { "$@" 2>&1 | head -n 1; }

find_java21() {
  if [[ -n "${COBOL_BRIDGE_JAVA_HOME:-}" ]]; then echo "$COBOL_BRIDGE_JAVA_HOME"; return; fi
  local brew="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
  if [[ -x "$brew/bin/java" ]]; then echo "$brew"; return; fi
  if [[ -x /usr/libexec/java_home ]] && /usr/libexec/java_home -v 21 >/dev/null 2>&1; then
    /usr/libexec/java_home -v 21; return
  fi
  echo "${JAVA_HOME:-}"
}

echo "cobol-bridge doctor"

if command -v uv >/dev/null; then ok uv "$(uv --version)"; else fail uv "not found (brew install uv)"; fi

if command -v cobc >/dev/null; then ok cobc "$(first_line cobc --version)"
else fail cobc "not found (brew install gnucobol)"; fi

java_home="$(find_java21)"
if [[ -n "$java_home" && -x "$java_home/bin/javac" ]] \
   && "$java_home/bin/java" -version 2>&1 | grep -q 'version "21'; then
  ok javac "$(first_line "$java_home/bin/javac" -version) ($java_home)"
else
  fail javac "JDK 21 not found (brew install openjdk@21, or set COBOL_BRIDGE_JAVA_HOME)"
fi

if command -v mvn >/dev/null; then ok mvn "$(first_line mvn -version)"
else fail mvn "not found (brew install maven)"; fi

if command -v ollama >/dev/null; then ok ollama "$(first_line ollama --version)"
else fail ollama "not found (brew install ollama)"; fi

if curl -fsS -m 3 "$OLLAMA_HOST/api/version" >/dev/null 2>&1; then
  ok server "Ollama is running at $OLLAMA_HOST"
  if curl -fsS -m 5 "$OLLAMA_HOST/api/tags" | grep -q "\"name\":\"$MODEL\""; then
    ok model "$MODEL is pulled"
  else
    fail model "$MODEL not pulled (ollama pull $MODEL)"
  fi
else
  fail server "Ollama not reachable at $OLLAMA_HOST (start it: ollama serve)"
fi

if (( failures > 0 )); then
  echo "$failures check(s) failed"; exit 1
fi
echo "all checks passed"
