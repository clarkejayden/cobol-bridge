# cobol-bridge results

Latest run per model and repair strategy. A program passes only if the Java output matches GnuCOBOL byte for byte on every generated input.

## Summary

|  | `qwen2.5-coder:7b`<br>fresh repair | `granite4:micro`<br>fresh repair | `granite4:tiny-h`<br>fresh repair | `granite4:micro`<br>replay repair |
|---|---|---|---|---|
| Pass rate, first try | 0/10 (0%) | 0/10 (0%) | 0/10 (0%) | 0/10 (0%) |
| Pass rate, after repair | 0/10 (0%) | 0/10 (0%) | 0/10 (0%) | 0/10 (0%) |
| Final attempt compiled | 9/10 (90%) | 4/10 (40%) | 6/10 (60%) | 3/10 (30%) |
| Test cases passed (compiled programs) | 21/238 | 13/103 | 6/153 | 0/66 |
| Avg attempts | 3.00 | 3.00 | 3.00 | 3.00 |
| Repairs repeating previous code | 7/20 (35%) | 11/20 (55%) | 10/20 (50%) | 15/20 (75%) |
| Avg LLM time / program | 461 s | 249 s | 141 s | 291 s |
| Tokens (whole corpus) | 132,951 | 112,491 | 105,407 | 113,236 |

## Where attempts stopped

Every attempt (first try and repairs) by the stage it reached.

|  | `qwen2.5-coder:7b`<br>fresh repair | `granite4:micro`<br>fresh repair | `granite4:tiny-h`<br>fresh repair | `granite4:micro`<br>replay repair |
|---|---|---|---|---|
| wrong output / crash | 26 | 9 | 14 | 6 |
| compile error | 4 | 20 | 10 | 21 |
| used floating point | 0 | 1 | 6 | 3 |

## Per program

`PASS (n)`: passed on attempt n. `FAIL k/m`: compiled and ran, k of m cases matched.

|  | `qwen2.5-coder:7b`<br>fresh repair | `granite4:micro`<br>fresh repair | `granite4:tiny-h`<br>fresh repair | `granite4:micro`<br>replay repair |
|---|---|---|---|---|
| CSVSPLIT | FAIL 1/18 | FAIL 0/18 | FAIL 0/18 | FAIL 0/18 |
| CURRENCY | FAIL: compile error | FAIL: compile error | FAIL: used floating point | FAIL: compile error |
| GRADE | FAIL 1/41 | FAIL: compile error | FAIL 1/41 | FAIL: compile error |
| INTEREST | FAIL 0/23 | FAIL: compile error | FAIL 0/23 | FAIL: compile error |
| INVENTORY | FAIL 0/24 | FAIL: compile error | FAIL: compile error | FAIL: compile error |
| LEDGER | FAIL 0/24 | FAIL 0/24 | FAIL 0/24 | FAIL 0/24 |
| LOAN | FAIL 5/23 | FAIL: compile error | FAIL 5/23 | FAIL: compile error |
| NAMES | FAIL 0/24 | FAIL: compile error | FAIL 0/24 | FAIL 0/24 |
| PAYROLL | FAIL 3/28 | FAIL 2/28 | FAIL: used floating point | FAIL: used floating point |
| SALESTAX | FAIL 11/33 | FAIL 11/33 | FAIL: compile error | FAIL: compile error |

## Runs

- `20261126T043733Z-qwen2.5-coder-7b`: qwen2.5-coder:7b (fresh repair)
- `20261126T035505Z-granite4-micro`: granite4:micro (fresh repair)
- `20261126T062758Z-granite4-tiny-h`: granite4:tiny-h (fresh repair)
- `20261126T024358Z-granite4-micro`: granite4:micro (replay repair)
