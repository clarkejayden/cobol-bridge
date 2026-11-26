This COBOL program splits a comma-separated line of data into up to four fields. It reads the line into a 40-character working field (IN-LINE). If the line starts with "SKU,QTY,PRICE,LOC", it's considered a header row and the program skips processing it, displaying "HEADER ROW SKIPPED" and stopping.

For non-header lines, it initializes four 10-character fields (WS-FIELD) and their lengths (WS-LENGTHS). It also counts commas in the line (WS-COMMAS) and fields filled (WS-FILLED).

The program uses UNSTRING to split the line, delimited by commas, into the four fields. It records the count of each field in WS-LENGTHS and increments WS-FILLED for each field successfully filled. If there's data after the fourth field, it displays "EXTRA DATA AFTER FIELD 4".

Finally, it displays the number of commas, fields filled, and details of each field's length and content. The output is formatted to show each field's index, length, and value.