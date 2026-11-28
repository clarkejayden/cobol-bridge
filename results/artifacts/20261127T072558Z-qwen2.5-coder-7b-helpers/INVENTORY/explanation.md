### Explanation of the COBOL Program

#### Data Layout

- **Input Record (`IN-REC`)**:
  - `IN-WAREHOUSE`: A fixed-length string of 3 characters representing the warehouse code.
  - `IN-ITEM`: An array of 6 items, each with:
    - `IN-SKU`: A fixed-length string of 5 characters representing the SKU.
    - `IN-QTY`: A numeric field of 4 digits representing the quantity.
    - `IN-COST`: A numeric field of 4 digits followed by 2 decimal places representing the cost per unit.

- **Working Fields**:
  - `WS-VALUE`: Accumulates the value of each item.
  - `WS-TOTAL`: Accumulates the total value of all items.
  - `WS-TOTAL-QTY`: Accumulates the total quantity of all items.
  - `WS-MAX-VALUE`: Tracks the maximum value of any item.
  - `WS-MAX-IDX`: Tracks the index of the item with the maximum value.
  - `WS-REORDER`: Counts the number of items that need to be reordered (quantity less than 100).
  - `WS-AVG-COST`: Calculates the average unit cost.
  - `WS-I`: Loop counter.

- **Report Line (`RPT-LINE`)**:
  - `RPT-SKU`: SKU of the item.
  - `RPT-QTY`: Quantity of the item.
  - `RPT-COST`: Cost per unit of the item.
  - `RPT-VALUE`: Total value of the item.
  - `RPT-FLAG`: Flag indicating whether the item needs to be reordered.

- **Output Fields**:
  - `OUT-TOTAL`: Total value of all items.
  - `OUT-QTY`: Total quantity of all items.
  - `OUT-AVG`: Average unit cost.

#### Business Logic

1. **Accept Input Record**: The program reads the input record `IN-REC`.
2. **Display Header**: It displays the warehouse code and a header for the report.
3. **Process Each Item**:
   - For each item in the `IN-ITEM` array:
     - Calculate the value of the item (`IN-QTY * IN-COST`).
     - Add the item's value to `WS-TOTAL`.
     - Add the item's quantity to `WS-TOTAL-QTY`.
     - Update `WS-MAX-VALUE` and `WS-MAX-IDX` if the current item's value is greater.
     - Set the `RPT-FLAG` to "REORDER" if the item's quantity is less than 100 and increment `WS-REORDER`.
     - Populate the `RPT-LINE` with the item's details.
     - Display the `RPT-LINE`.
4. **Calculate Average Cost**:
   - If `WS-TOTAL-QTY` is greater than 0, calculate the average unit cost (`WS-TOTAL / WS-TOTAL-QTY`).
   - Otherwise, set `WS-AVG-COST` to 0.
5. **Display Summary**:
   - Display the total value, total quantity, average unit cost, SKU of the most valuable item, and the number of items that need to be reordered.

#### Output

The program prints a fixed-width report line for each item, followed by a summary line with the total value, total quantity, average unit cost, SKU of the most valuable item, and the number of items that need to be reordered.