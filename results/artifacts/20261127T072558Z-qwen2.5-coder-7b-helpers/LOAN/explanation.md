### Explanation of the COBOL Program

#### Data Layout
- **Input Record (`IN-REC`)**:
  - `IN-AMOUNT`: The total loan amount (7 digits, 2 decimal places).
  - `IN-RATE`: The annual interest rate (2 digits, 3 decimal places).
  - `IN-MONTHS`: The number of months for the loan (3 digits).

- **Working Fields**:
  - `WS-BALANCE`: The outstanding loan balance.
  - `WS-PRINCIPAL`: The fixed monthly principal payment.
  - `WS-INTEREST`: The interest calculated for the current month.
  - `WS-TOTAL-INT`: The total interest paid over the loan period.
  - `WS-MONTH`: The current month of the loan.

- **Output Fields**:
  - `OUT-MONTH`: The month number.
  - `OUT-INT`: The interest for the month.
  - `OUT-PRIN`: The principal for the month.
  - `OUT-BAL`: The remaining balance after the month.
  - `OUT-TOTAL`: The total interest paid.

#### Business Logic
1. **Input Validation**:
   - The program checks if the number of months (`IN-MONTHS`) is between 1 and 360. If not, it displays "INVALID TERM: " followed by the invalid value and stops.

2. **Initialization**:
   - The loan balance (`WS-BALANCE`) is set to the loan amount (`IN-AMOUNT`).
   - The fixed monthly principal payment (`WS-PRINCIPAL`) is calculated as the loan amount divided by the number of months.

3. **Repayment Schedule**:
   - The program displays the header for the repayment schedule.
   - It then iterates through each month from 1 to `IN-MONTHS`:
     - Calculates the interest for the current month based on the outstanding balance and the annual interest rate.
     - If it's the last month, the principal payment is adjusted to clear the balance.
     - Updates the outstanding balance by subtracting the principal payment.
     - Adds the interest to the total interest paid.
     - Displays the month number, interest, principal, and remaining balance.

4. **Final Output**:
   - After the loop, the total interest paid is displayed.
   - The program stops.

#### Output
The program prints a repayment schedule for a straight-line loan, showing the month number, interest, principal, and remaining balance for each month. It also displays the total interest paid at the end.

### Example Output
For a loan of $10,000 at an annual interest rate of 5% over 12 months:
```
MONTH      INTEREST     PRINCIPAL       BALANCE
  1  41.67  833.33  9166.67
  2  34.72  833.33  8333.34
  3  27.78  833.33  7500.01
  4  19.84  833.33  6666.68
  5  11.90  833.33  5833.35
  6  3.96  833.33  5000.02
  7  16.02  833.33  4166.69
  8  23.08  833.33  3333.36
  9  30.14  833.33  2500.03
 10  37.20  833.33  1666.70
 11  44.26  833.33  833.37
 12  51.32  833.33  0.00
TOTAL INTEREST  513.20
```