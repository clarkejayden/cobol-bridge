This COBOL program calculates a repayment schedule for a straight-line loan. It takes three inputs: the loan amount (IN-AMOUNT), the annual interest rate (IN-RATE), and the number of months to repay (IN-MONTHS).

The program first checks if the number of months is valid (between 1 and 360). If not, it displays an error message and stops.

It then initializes working fields for the balance, principal, interest, total interest, month, and output fields for interest, principal, balance, and total interest.

The program calculates the monthly principal payment by dividing the loan amount by the number of months. It displays a header row with column names.

For each month, it:
1. Calculates the interest for the month by multiplying the current balance by the annual interest rate divided by 1200 (to convert to monthly rate).
2. If it's the last month, it uses the remaining balance as the principal payment.
3. Subtracts the principal payment from the balance.
4. Adds the interest to the total interest.
5. Stores the month, interest, principal, and balance in output fields.
6. Displays the month, interest, principal, and balance for that month.

After the loop, it moves the total interest to the output total field and displays the total interest.

The output is formatted with the month, interest, principal, and balance, each right-justified with varying widths. The total interest is displayed on a separate line.