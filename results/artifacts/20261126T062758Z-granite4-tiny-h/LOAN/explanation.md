This COBOL program calculates and displays a repayment schedule for a straight-line loan. It takes three inputs: the loan amount (IN-AMOUNT), the annual interest rate (IN-RATE), and the loan term in months (IN-MONTHS).

The program first checks if the loan term is valid (between 1 and 360 months). If not, it displays an error message and stops.

It then initializes the working variables: WS-BALANCE (loan amount), WS-PRINCIPAL (equal monthly principal payment), WS-INTEREST (interest for each month), WS-TOTAL-INT (total interest), and WS-MONTH (current month).

The program displays a header for the repayment schedule and enters a loop that iterates for each month of the loan term. In each iteration:

1. It calculates the monthly interest by multiplying the current balance by the annual interest rate and dividing by 1200 (12 months * 100 cents).
2. If it's the last month, it sets the principal payment to the remaining balance to clear the loan.
3. It subtracts the principal payment from the balance.
4. It adds the interest to the total interest.
5. It displays the current month, interest, principal payment, and remaining balance, formatted with leading zeros.

After the loop, it displays the total interest paid over the loan term.

The program ensures that the loan term is valid, calculates equal monthly principal payments (truncated to the cent), and displays the repayment schedule with interest and balance for each month, ending with the total interest paid.