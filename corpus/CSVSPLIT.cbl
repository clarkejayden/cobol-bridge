       IDENTIFICATION DIVISION.
       PROGRAM-ID. CSVSPLIT.
      *----------------------------------------------------------------
      * Splits a comma-separated line into up to four fields with
      * UNSTRING, reporting each field's length, the number of fields
      * filled and whether the line had more fields than fit.
      *----------------------------------------------------------------
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  IN-REC.
           05  IN-LINE              PIC X(40).
               88  HEADER-ROW            VALUE "SKU,QTY,PRICE,LOC".
       01  WS-FIELDS.
           05  WS-FIELD             PIC X(10)    OCCURS 4 TIMES.
       01  WS-LENGTHS.
           05  WS-LEN               PIC 9(2)     OCCURS 4 TIMES.
       01  WS-FILLED                PIC 9(2)     VALUE 0.
       01  WS-COMMAS                PIC 9(2)     VALUE 0.
       01  WS-PTR                   PIC 9(2)     VALUE 1.
       01  WS-I                     PIC 9        VALUE 0.
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT IN-REC
           IF HEADER-ROW
               DISPLAY "HEADER ROW SKIPPED"
               STOP RUN
           END-IF
           MOVE SPACES TO WS-FIELDS
           MOVE ZEROS TO WS-LENGTHS
           INSPECT IN-LINE TALLYING WS-COMMAS FOR ALL ","
           UNSTRING IN-LINE DELIMITED BY ","
               INTO WS-FIELD (1) COUNT IN WS-LEN (1)
                    WS-FIELD (2) COUNT IN WS-LEN (2)
                    WS-FIELD (3) COUNT IN WS-LEN (3)
                    WS-FIELD (4) COUNT IN WS-LEN (4)
               WITH POINTER WS-PTR
               TALLYING IN WS-FILLED
               ON OVERFLOW
                   DISPLAY "EXTRA DATA AFTER FIELD 4"
           END-UNSTRING
           DISPLAY "COMMAS " WS-COMMAS " FIELDS " WS-FILLED
           PERFORM VARYING WS-I FROM 1 BY 1 UNTIL WS-I > 4
               DISPLAY "FIELD " WS-I " LEN " WS-LEN (WS-I)
                       " [" WS-FIELD (WS-I) "]"
           END-PERFORM
           STOP RUN.
