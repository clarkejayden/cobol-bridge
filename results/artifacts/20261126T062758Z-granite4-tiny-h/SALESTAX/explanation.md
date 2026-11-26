This COBOL program calculates the sales tax for an order based on the state and amount. Here's a concise explanation:

Data Layout:
- RATE-DATA: Holds state tax rate data as literals.
- RATE-TABLE: Redefines RATE-DATA, creating an indexed table of RATE-ENTRY with RATE-STATE (state code) and RATE-PCT (tax rate).
- IN-REC: Holds input record with IN-STATE (state code), IN-AMOUNT (order amount), and IN-EXEMPT (exempt flag).
- WS-TAX, WS-TOTAL, OUT-AMT, OUT-TOTAL, OUT-PCT: Working and output fields for tax calculations and display.

Business Logic:
1. Accepts IN-REC input.
2. Checks if IN-STATE is a known state. If not, displays "INVALID STATE" and stops.
3. Searches RATE-TABLE for matching RATE-STATE. If found, proceeds; otherwise, displays "NO RATE ON FILE FOR" state and stops.
4. If IN-EXEMPT is "Y", sets WS-TAX to 0. Otherwise, calculates WS-TAX as IN-AMOUNT * RATE-PCT / 100.
5. Calculates WS-TOTAL as IN-AMOUNT + WS-TAX.
6. Displays state, tax rate (%), subtotal, tax (if not exempt), and total.

The program outputs the state, tax rate, subtotal, tax (if not exempt), and total amount for the order.