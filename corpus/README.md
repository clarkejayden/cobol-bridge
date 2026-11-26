# Corpus

Ten small, self-contained programs written for this project (Apache-2.0, like
the rest of the repository). Each is fixed-format COBOL that compiles with
`cobc -x` and reads exactly one fixed-width record from stdin with a single
`ACCEPT`, so test inputs can be derived from the record layout.

| Program | What it exercises |
|---|---|
| `INTEREST` | `PIC 9(7)V99` arithmetic, `ROUNDED`, a `PERFORM VARYING` loop, `ON SIZE ERROR` |
| `PAYROLL` | `COMP-3` work fields, overtime, marginal tax brackets, `EVALUATE`, 88-level status codes |
| `LOAN` | Repayment schedule: truncating vs rounded division, a loop of up to 360 report lines |
| `GRADE` | `EVALUATE TRUE ALSO TRUE`, 88-level `THRU` ranges, capped extra credit |
| `LEDGER` | Signed input (`SIGN LEADING SEPARATE`), `OCCURS`, `+`, `-` and `CR` editing |
| `NAMES` | `STRING ... WITH POINTER ON OVERFLOW`, reference modification, `INSPECT TALLYING / REPLACING / CONVERTING` |
| `CSVSPLIT` | `UNSTRING ... DELIMITED BY ... COUNT IN ... TALLYING IN ON OVERFLOW` |
| `INVENTORY` | An `OCCURS` group of items, a fixed-width report record with `FILLER`, weighted average |
| `SALESTAX` | A table built from literals with `REDEFINES`, `SEARCH ... AT END` with an index |
| `CURRENCY` | Truncated intermediate, `ROUNDED` result, `ON SIZE ERROR`, `DIVIDE ... GIVING ... REMAINDER` |

Input conventions the generator relies on:

* signed input fields use `SIGN IS LEADING SEPARATE` (overpunched signs are rejected);
* input fields are `USAGE DISPLAY` (packed or binary input is rejected);
* 88-level values on input fields describe the values the program expects;
  the generator tests both ends of every `THRU` range, each listed literal,
  and one value that matches none.

Third-party programs will be listed with their source URL and licence in
`SOURCES.md` when they are added.
