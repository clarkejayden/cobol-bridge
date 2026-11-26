### Explanation of the COBOL Program

#### Data Layout

- **Input Record (`IN-REC`)**:
  - `IN-AMOUNT`: A numeric field representing the amount to be converted, with up to 7 digits before the decimal and 2 digits after.
  - `IN-FROM`: A 3-character field indicating the currency to convert from, which must be one of "USD", "EUR", "GBP", or "JPY".
  - `IN-TO`: A 3-character field indicating the currency to convert to, which must also be one of "USD", "EUR", "GBP", or "JPY".

- **Working Fields**:
  - `WS-FROM-RATE` and `WS-TO-RATE`: Numeric fields to store the conversion rates from and to USD.
  - `WS-CODE`: A 3-character field used to temporarily store the currency code during rate lookup.
  - `WS-RATE`: A numeric field to store the current conversion rate.
  - `WS-USD`: A numeric field to store the amount in USD.
  - `WS-RESULT`: A numeric field to store the converted amount.
  - `WS-WHOLE`: A numeric field to store the whole-unit result of the conversion.
  - `WS-COUNT`: A numeric field to store the count of each note.
  - `WS-REM`: A numeric field to store the remainder after division.
  - `WS-I`: A numeric field used as a loop counter.
  - `NOTE-DATA`: A fixed-length string containing the values of the notes (1000, 500, 200, 100, 50, 1).
  - `NOTE-TABLE`: A redefined table from `NOTE-DATA` to access the note values.
  - `OUT-AMT`: A numeric field to store the output amount formatted with commas.
  - `OUT-RATE`: A numeric field to store the output rate formatted with commas.
  - `OUT-NOTE`: A numeric field to store the output note value.
  - `OUT-COUNT`: A numeric field to store the output count of notes.

#### Business Logic

1. **Input Validation**:
   - The program reads the input record (`IN-REC`).
   - It checks if the `IN-FROM` and `IN-TO` currencies are valid. If not, it displays "UNSUPPORTED PAIR" and stops.

2. **Rate Lookup**:
   - The program sets the `WS-CODE` to `IN-FROM` and calls the `LOOKUP-RATE` subroutine to get the conversion rate from USD to `IN-FROM`.
   - It sets the `WS-CODE` to `IN-TO` and calls the `LOOKUP-RATE` subroutine to get the conversion rate from USD to `IN-TO`.

3. **Conversion**:
   - The program converts the input amount to USD using the `WS-FROM-RATE`.
   - If the target currency is JPY, it multiplies the USD amount by the `WS-TO-RATE` and rounds the result. If the result is too large, it calls the `TOO-LARGE` subroutine.
   - If the target currency is not JPY, it multiplies the USD amount by the `WS-TO-RATE` and rounds the result. If the result is too large, it calls the `TOO-LARGE` subroutine.

4. **Output**:
   - The program displays the original amount and currency.
   - It displays the converted amount and currency.
   - It displays the conversion rate.
   - It breaks the whole-unit result into notes and displays each note value and count.

5. **Error Handling**:
   - If the input currency pair is unsupported, it displays "UNSUPPORTED PAIR" and stops.
   - If the converted amount is too large, it displays "AMOUNT TOO LARGE TO CONVERT" and stops.

#### Output

The program prints the following:

1. The original amount and currency.
2. The converted amount and currency.
3. The conversion rate.
4. The breakdown of the converted amount into notes (1000, 500, 200, 100, 50, 1).

For example, if the input is:
```
1234.56 USD EUR
```
The output might be:
```
FROM 1234.56 USD
TO   1367.14 EUR
RATE 0.9150 EUR PER USD
  1000 x 1
  500 x 1
  200 x 1
  100 x 1
  50 x 1
  1 x 1
```