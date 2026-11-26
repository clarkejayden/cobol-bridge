### Explanation of the COBOL Program

#### Data Layout

- **RATE-DATA**: A literal table containing state codes and their corresponding sales tax rates.
- **RATE-TABLE**: A redefined version of `RATE-DATA` that allows for indexed access.
- **IN-REC**: The input record containing:
  - `IN-STATE`: The state code of the order.
  - `IN-AMOUNT`: The total amount of the order.
  - `IN-EXEMPT`: A flag indicating if the order is exempt from tax.
- **WS-TAX**: Working storage to hold the calculated tax amount.
- **WS-TOTAL**: Working storage to hold the total amount including tax.
- **OUT-AMT**: Output field for displaying amounts.
- **OUT-TOTAL**: Output field for displaying the total amount.
- **OUT-PCT**: Output field for displaying the tax rate.

#### Business Logic

1. **Accept Input**: The program accepts an input record (`IN-REC`).
2. **Validate State**: Checks if the state code is known. If not, it displays an error message and stops.
3. **Search for Tax Rate**: Searches the `RATE-TABLE` for the tax rate corresponding to the input state.
   - If the state is not found, it displays a message indicating no rate is on file and stops.
   - If the state is found, it proceeds to price the order.
4. **Price Order**:
   - If the order is exempt from tax (`IS-EXEMPT`), it sets the tax amount to zero.
   - Otherwise, it calculates the tax amount based on the state's tax rate.
   - It then computes the total amount by adding the tax to the order amount.
   - The program displays the state code, tax rate, subtotal, tax amount, and total amount.

#### Output

The program prints the following:
- If the state is invalid, it displays "INVALID STATE [state code]".
- If the state is valid but no rate is on file, it displays "NO RATE ON FILE FOR [state code]".
- If the state is valid and a rate is found, it displays:
  - The state code and tax rate.
  - The subtotal of the order.
  - The tax amount, with a note if the order is exempt from tax.
  - The total amount including tax.

### Example Output

- **Valid State with Tax**:
  ```
  STATE    CA   RATE 7.25%
  SUBTOTAL 100.00
  TAX      7.25
  TOTAL  107.25
  ```

- **Valid State with Exempt Order**:
  ```
  STATE    NV   RATE 0.00%
  SUBTOTAL 100.00
  TAX      0.00 (EXEMPT)
  TOTAL  100.00
  ```

- **Invalid State**:
  ```
  INVALID STATE [XX]
  ```

- **State with No Rate on File**:
  ```
  NO RATE ON FILE FOR XX
  ```