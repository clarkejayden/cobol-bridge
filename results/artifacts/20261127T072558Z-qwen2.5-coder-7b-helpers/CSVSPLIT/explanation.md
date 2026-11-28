### Explanation of the COBOL Program

#### Data Layout

- **Input Record (`IN-REC`)**:
  - `IN-LINE`: A fixed-length string of 40 characters.
  - `HEADER-ROW`: A condition name that checks if `IN-LINE` is "SKU,QTY,PRICE,LOC". If true, it skips the record.

- **Working Fields (`WS-FIELDS`)**:
  - `WS-FIELD`: An array of 4 strings, each up to 10 characters long.

- **Working Fields (`WS-LENGTHS`)**:
  - `WS-LEN`: An array of 4 integers to store the length of each field.

- **Working Fields (`WS-FILLED`)**:
  - `WS-FILLED`: An integer to count the number of fields filled.

- **Working Fields (`WS-COMMAS`)**:
  - `WS-COMMAS`: An integer to count the number of commas in the input line.

- **Working Fields (`WS-PTR`)**:
  - `WS-PTR`: An integer to keep track of the pointer during the UNSTRING operation.

- **Working Fields (`WS-I`)**:
  - `WS-I`: An integer used in the PERFORM loop to iterate through the fields.

#### Business Logic

1. **Accept Input Record**:
   - The program reads an input record into `IN-REC`.

2. **Check for Header Row**:
   - If `IN-LINE` matches "SKU,QTY,PRICE,LOC", it displays "HEADER ROW SKIPPED" and stops the program.

3. **Initialize Working Fields**:
   - `WS-FIELDS` and `WS-LENGTHS` are initialized to spaces and zeros, respectively.
   - `WS-COMMAS` and `WS-PTR` are initialized to zero.

4. **Count Commas**:
   - The `INSPECT` statement counts the number of commas in `IN-LINE` and stores it in `WS-COMMAS`.

5. **Split the Line**:
   - The `UNSTRING` statement splits `IN-LINE` into up to 4 fields, storing each field in `WS-FIELD` and its length in `WS-LEN`.
   - If there are more than 4 fields, it displays "EXTRA DATA AFTER FIELD 4".
   - `WS-FILLED` counts the number of fields filled.

6. **Display Results**:
   - The program displays the number of commas and the number of fields filled.
   - It then iterates through the fields, displaying each field's length and content.

7. **Stop the Program**:
   - The program stops after displaying the results.

#### Output

The program will print:
- "HEADER ROW SKIPPED" if the input line is "SKU,QTY,PRICE,LOC".
- The number of commas in the input line.
- The number of fields filled.
- Each field's length and content.

For example, if the input line is "SKU123,10,19.99,A1", the output will be:
```
HEADER ROW SKIPPED
COMMAS 3 FIELDS 3
FIELD 1 LEN 6 [SKU123]
FIELD 2 LEN 2 [10]
FIELD 3 LEN 6 [19.99]
FIELD 4 LEN 2 [A1]
```