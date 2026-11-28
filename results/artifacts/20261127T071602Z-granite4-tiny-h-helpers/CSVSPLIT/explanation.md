This COBOL program, named CSVSPLIT, takes a comma-separated line of data (up to 40 characters) as input and splits it into up to four fields. It then reports the length of each field, the number of fields filled, and whether the line had more fields than fit.

The program starts by accepting the input record (IN-REC) and checking if it's a header row. If it is, the program skips it and stops. Otherwise, it initializes the working fields: WS-FIELDS (an array of 4 PIC X(10) variables), WS-LENGTHS (an array of 4 PIC 9(2) variables), WS-FILLED (a PIC 9(2) variable), WS-COMMAS (a PIC 9(2) variable), and WS-PTR (a PIC 9 variable).

The program then counts the number of commas in the input line and stores the count in WS-COMMAS. It uses the UNSTRING statement to split the input line into the four fields, storing the lengths of each field in WS-LENGTHS and the actual fields in WS-FIELDS. The UNSTRING statement also counts the number of fields filled and stores the count in WS-FILLED.

After splitting the line, the program displays the number of commas and fields filled. It then iterates through the fields (1 to 4) and displays the length and content of each field.

In summary, this program takes a comma-separated line of data, splits it into up to four fields, and reports the length and content of each field, along with the number of fields filled and whether there were extra fields beyond the fourth one.