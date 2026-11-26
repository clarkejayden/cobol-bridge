       IDENTIFICATION DIVISION.
       PROGRAM-ID. CURRENCY.
      *----------------------------------------------------------------
      * Converts an amount between currencies via USD (truncating the
      * USD intermediate to 4 places, rounding the result; yen has no
      * minor unit), guards overflow with ON SIZE ERROR, and breaks
      * the whole-unit result into notes with DIVIDE ... REMAINDER.
      *----------------------------------------------------------------
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  IN-REC.
           05  IN-AMOUNT            PIC 9(7)V99.
           05  IN-FROM              PIC X(3).
               88  FROM-VALID       VALUE "USD" "EUR" "GBP" "JPY".
           05  IN-TO                PIC X(3).
               88  TO-VALID         VALUE "USD" "EUR" "GBP" "JPY".
       01  WS-FROM-RATE             PIC 9(3)V9(4) VALUE 0.
       01  WS-TO-RATE               PIC 9(3)V9(4) VALUE 0.
       01  WS-CODE                  PIC X(3).
       01  WS-RATE                  PIC 9(3)V9(4) VALUE 0.
       01  WS-USD                   PIC 9(9)V9(4) VALUE 0.
       01  WS-RESULT                PIC 9(9)V99   VALUE 0.
       01  WS-WHOLE                 PIC 9(9)      VALUE 0.
       01  WS-COUNT                 PIC 9(9)      VALUE 0.
       01  WS-REM                   PIC 9(9)      VALUE 0.
       01  WS-I                     PIC 9         VALUE 0.
       01  NOTE-DATA                PIC X(18)
                                    VALUE "100050020010005001".
       01  NOTE-TABLE REDEFINES NOTE-DATA.
           05  NOTE-VALUE           PIC 9(3)      OCCURS 6 TIMES.
       01  OUT-AMT                  PIC ZZZ,ZZZ,ZZ9.99.
       01  OUT-RATE                 PIC ZZ9.9999.
       01  OUT-NOTE                 PIC ZZ9.
       01  OUT-COUNT                PIC ZZZ,ZZZ,ZZ9.
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT IN-REC
           IF NOT FROM-VALID OR NOT TO-VALID
               DISPLAY "UNSUPPORTED PAIR " IN-FROM "/" IN-TO
               STOP RUN
           END-IF
           MOVE IN-FROM TO WS-CODE
           PERFORM LOOKUP-RATE
           MOVE WS-RATE TO WS-FROM-RATE
           MOVE IN-TO TO WS-CODE
           PERFORM LOOKUP-RATE
           MOVE WS-RATE TO WS-TO-RATE
           COMPUTE WS-USD = IN-AMOUNT / WS-FROM-RATE
           IF IN-TO = "JPY"
               COMPUTE WS-WHOLE ROUNDED = WS-USD * WS-TO-RATE
                   ON SIZE ERROR
                       PERFORM TOO-LARGE
               END-COMPUTE
               MOVE WS-WHOLE TO WS-RESULT
           ELSE
               COMPUTE WS-RESULT ROUNDED = WS-USD * WS-TO-RATE
                   ON SIZE ERROR
                       PERFORM TOO-LARGE
               END-COMPUTE
           END-IF
           MOVE IN-AMOUNT TO OUT-AMT
           DISPLAY "FROM " OUT-AMT " " IN-FROM
           MOVE WS-RESULT TO OUT-AMT
           DISPLAY "TO   " OUT-AMT " " IN-TO
           MOVE WS-TO-RATE TO OUT-RATE
           DISPLAY "RATE " OUT-RATE " " IN-TO " PER USD"
           MOVE WS-RESULT TO WS-WHOLE
           PERFORM VARYING WS-I FROM 1 BY 1 UNTIL WS-I > 6
               DIVIDE WS-WHOLE BY NOTE-VALUE (WS-I)
                   GIVING WS-COUNT REMAINDER WS-REM
               IF WS-COUNT > 0
                   MOVE NOTE-VALUE (WS-I) TO OUT-NOTE
                   MOVE WS-COUNT TO OUT-COUNT
                   DISPLAY "  " OUT-NOTE " x " OUT-COUNT
               END-IF
               MOVE WS-REM TO WS-WHOLE
           END-PERFORM
           STOP RUN.

       LOOKUP-RATE.
           EVALUATE WS-CODE
               WHEN "USD"  MOVE 1.0000   TO WS-RATE
               WHEN "EUR"  MOVE 0.9150   TO WS-RATE
               WHEN "GBP"  MOVE 0.7820   TO WS-RATE
               WHEN "JPY"  MOVE 151.2400 TO WS-RATE
           END-EVALUATE.

       TOO-LARGE.
           DISPLAY "AMOUNT TOO LARGE TO CONVERT"
           STOP RUN.
