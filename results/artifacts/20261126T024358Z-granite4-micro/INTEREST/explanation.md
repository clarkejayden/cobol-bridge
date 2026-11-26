This COBOL program calculates simple and compound interest on a deposit. It accepts three input values: principal (up to 9 digits and 2 decimal places), annual interest rate (up to 1 digit, 1 decimal place, and 4 decimal places), and term in years (up to 2 digits). 

The program initializes variables for simple interest, balance, interest, year, and overflow flag. It displays the principal amount. Then, it calculates and displays the simple interest by multiplying the principal, rate, and years, rounding the result to the cent.

For compound interest, it initializes the balance with the principal and iterates through each year, updating the balance by adding the interest for that year. Interest is calculated by multiplying the balance by the rate and rounding the result. If an overflow occurs during the addition, the overflow flag is set, and an error message is displayed for that year. Otherwise, the updated balance is displayed for each year.

Finally, the program displays the final compound balance.