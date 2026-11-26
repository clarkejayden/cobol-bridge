       IDENTIFICATION DIVISION.
       PROGRAM-ID. LOAN.
      *----------------------------------------------------------------
      * Repayment schedule for a straight-line loan: equal principal
      * each month (truncated to the cent, the last month clears the
      * balance) plus interest on the outstanding balance.
      *----------------------------------------------------------------
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  IN-REC.
           05  IN-AMOUNT            PIC 9(7)V99.
           05  IN-RATE              PIC 9(2)V999.
           05  IN-MONTHS            PIC 9(3).
       01  WS-BALANCE               PIC 9(7)V99  VALUE 0.
       01  WS-PRINCIPAL             PIC 9(7)V99  VALUE 0.
       01  WS-INTEREST              PIC 9(7)V99  VALUE 0.
       01  WS-TOTAL-INT             PIC 9(9)V99  VALUE 0.
       01  WS-MONTH                 PIC 9(3)     VALUE 0.
       01  OUT-MONTH                PIC ZZ9.
       01  OUT-INT                  PIC ZZ,ZZZ,ZZ9.99.
       01  OUT-PRIN                 PIC ZZ,ZZZ,ZZ9.99.
       01  OUT-BAL                  PIC ZZ,ZZZ,ZZ9.99.
       01  OUT-TOTAL                PIC ZZZ,ZZZ,ZZ9.99.
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT IN-REC
           IF IN-MONTHS = 0 OR IN-MONTHS > 360
               DISPLAY "INVALID TERM: " IN-MONTHS
               STOP RUN
           END-IF
           MOVE IN-AMOUNT TO WS-BALANCE
           COMPUTE WS-PRINCIPAL = IN-AMOUNT / IN-MONTHS
           DISPLAY "MONTH      INTEREST     PRINCIPAL       BALANCE"
           PERFORM VARYING WS-MONTH FROM 1 BY 1
                   UNTIL WS-MONTH > IN-MONTHS
               COMPUTE WS-INTEREST ROUNDED =
                   WS-BALANCE * IN-RATE / 1200
               IF WS-MONTH = IN-MONTHS
                   MOVE WS-BALANCE TO WS-PRINCIPAL
               END-IF
               SUBTRACT WS-PRINCIPAL FROM WS-BALANCE
               ADD WS-INTEREST TO WS-TOTAL-INT
               MOVE WS-MONTH TO OUT-MONTH
               MOVE WS-INTEREST TO OUT-INT
               MOVE WS-PRINCIPAL TO OUT-PRIN
               MOVE WS-BALANCE TO OUT-BAL
               DISPLAY "  " OUT-MONTH " " OUT-INT " " OUT-PRIN
                       " " OUT-BAL
           END-PERFORM
           MOVE WS-TOTAL-INT TO OUT-TOTAL
           DISPLAY "TOTAL INTEREST " OUT-TOTAL
           STOP RUN.
