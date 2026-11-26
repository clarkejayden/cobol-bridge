This COBOL program calculates sales tax for an order based on the state. It defines a table of state tax rates loaded from literals, with each entry containing the state abbreviation and tax percentage. The program accepts an input record with the state and amount, and an optional exempt flag.

If the state is not recognized, it displays an error message and stops. Otherwise, it searches the rate table for the matching state. If no rate is found, it displays a message and stops. If a rate is found, it calls the `PRICE-ORDER` subroutine.

In `PRICE-ORDER`, if the order is exempt, the tax is set to zero. Otherwise, the tax is calculated by multiplying the amount by the rate and dividing by 100. The total is then the amount plus tax. The program outputs the state, rate, subtotals, tax (if applicable), and total.

The output is formatted with commas for readability, e.g., "TOTAL  1250.00". The program ensures the output matches the original COBOL code byte-for-byte.