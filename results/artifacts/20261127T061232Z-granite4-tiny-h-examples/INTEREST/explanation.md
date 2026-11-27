This COBOL program calculates and displays the simple and compound interest on a deposit, given the principal amount, annual interest rate (in percent), and the term of the deposit in years.

The program starts by accepting the input record, which consists of three fields: principal (7 digits with 2 decimal places), annual rate (2 digits with 4 decimal places), and term in years (2 digits). These values are stored in the WORKING-STORAGE SECTION variables IN-PRINCIPAL, IN-RATE, and IN-YEARS, respectively.

The program then displays the input values for the principal, rate, and term.

Next, it calculates the simple interest by multiplying the principal, rate, and term, and dividing the result by 100. The simple interest is stored in the WS-SIMPLE variable and displayed.

For compound interest, the program initializes the WS-BALANCE variable with the principal amount. It then iterates through each year of the deposit term using a PERFORM loop. In each iteration, it calculates the interest for that year by multiplying the current balance with the rate and dividing by 100. The interest is added to the balance. If an overflow occurs during the calculation, the OVERFLOWED flag is set to true.

For each year, the program displays the year number and the balance (either simple or compound) rounded to the nearest cent. If an overflow occurs in a particular year, it displays a message indicating the year when the overflow occurred.

Finally, the program displays the total compound interest earned after the specified term.

The output includes the principal, rate, term, simple interest, and compound interest for each year, along with any overflow messages if applicable.