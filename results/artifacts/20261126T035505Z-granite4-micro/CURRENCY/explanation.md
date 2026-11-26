This COBOL program converts an amount of money between supported currencies (USD, EUR, GBP, JPY) via USD as an intermediary. It truncates the USD intermediate to 4 decimal places and rounds the final result. Yen has no minor unit.

1. Data layout:
   - IN-REC: Contains IN-AMOUNT (9(7)V99), IN-FROM (X(3)), and IN-TO (X(3)) with valid values.
   - WS-FROM-RATE, WS-TO-RATE: Exchange rates for USD and target currency.
   - WS-CODE: Stores the currency code.
   - WS-RATE: Stores the exchange rate.
   - WS-USD: Stores the amount in USD.
   - WS-RESULT: Stores the converted amount.
   - WS-WHOLE: Stores the whole-unit part of the result.
   - WS-COUNT: Stores the count of notes.
   - WS-REM: Stores the remainder after division.
   - NOTE-DATA and NOTE-TABLE: Define an array of note values (100, 50, 20, 10, 5, 1).
   - OUT-AMT, OUT-RATE, OUT-NOTE, OUT-COUNT: Store the output values.

2. Business logic:
   - Accepts the input record containing the amount, from currency, and to currency.
   - Validates the from and to currencies. If invalid, displays an error and stops.
   - Looks up the exchange rate for the from currency and stores it in WS-FROM-RATE.
   - Looks up the exchange rate for the to currency and stores it in WS-TO-RATE.
   - Converts the amount to USD by dividing by WS-FROM-RATE.
   - If converting to JPY, multiplies the USD amount by WS-TO-RATE and rounds to the whole part. Otherwise, just rounds the result.
   - Displays the original and converted amounts with their respective currencies.
   - Divides the whole-unit result by the note values to determine the count and remainder.
   - Displays the count and value of each note.
   - If the amount is too large to convert, displays an error and stops.

3. Output:
   - Displays the original amount, currency, and the converted amount with the target currency.
   - Displays the exchange rate and the converted amount in the target currency.
   - Displays the count and value of each note required to represent the converted amount.