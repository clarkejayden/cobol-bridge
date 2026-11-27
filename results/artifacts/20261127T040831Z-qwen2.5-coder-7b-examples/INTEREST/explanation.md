### Explanation of the COBOL Program

#### Data Layout
- **Input Record (`IN-REC`)**:
  - `IN-PRINCIPAL`: Principal amount (7 digits, 2 decimal places)
  - `IN-RATE`: Annual interest rate (2 digits, 4 decimal places)
  - `IN-YEARS`: Term in years (2 digits)

- **Working Fields**:
  - `WS-SIMPLE`: Simple interest calculated (11 digits, 2 decimal places)
  - `WS-BALANCE`: Balance after compound interest (13 digits, 2 decimal places)
  - `WS-INTEREST`: Interest for the current year (13 digits, 2 decimal places)
  - `WS-YEAR`: Current year (3 digits)
  - `WS-OVERFLOW-FLAG`: Flag to indicate overflow (1 character)
  - `OVERFLOWED`: Condition name for overflow flag

- **Output Fields**:
  - `OUT-MONEY`: Formatted money amount (up to 15 digits, 2 decimal places)
  - `OUT-RATE`: Formatted interest rate (up to 6 digits, 4 decimal places)
  - `OUT-YEAR`: Formatted year (up to 3 digits)

#### Business Logic
1. **Accept Input**:
   - The program accepts an input record containing the principal amount, annual interest rate, and term in years.

2. **Display Input**:
   - The principal amount, interest rate, and term in years are displayed.

3. **Calculate Simple Interest**:
   - The simple interest is calculated as `IN-PRINCIPAL * IN-RATE / 100 * IN-YEARS`.
   - The result is displayed.

4. **Calculate Compound Interest**:
   - The balance is initialized to the principal amount.
   - A loop runs for each year from 1 to `IN-YEARS`.
   - For each year, the interest for that year is calculated as `WS-BALANCE * IN-RATE / 100`.
   - The interest is added to the balance.
   - If an overflow occurs during the addition, the overflow flag is set.
   - The balance is displayed for each year.
   - If an overflow occurs, a message is displayed indicating the year of overflow.

5. **Display Final Balance**:
   - The final balance after compound interest is displayed.

#### Output
- The program prints the principal amount, interest rate, term in years, simple interest, and compound interest for each year.
- If an overflow occurs during the calculation, a message indicating the year of overflow is displayed.

### Example Output
```
PRINCIPAL 1000.00
RATE      5.0000%
YEARS     10
SIMPLE    500.00
YEAR 1    1050.00
YEAR 2    1102.50
YEAR 3    1157.63
YEAR 4    1215.51
YEAR 5    1276.28
YEAR 6    1340.10
YEAR 7    1407.01
YEAR 8    1477.45
YEAR 9    1551.33
YEAR 10   1628.89
COMPOUND  1628.89
```

This output shows the principal amount, interest rate, term in years, simple interest, and compound interest for each year, with the final balance after compound interest.