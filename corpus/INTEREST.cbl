       IDENTIFICATION DIVISION.
       PROGRAM-ID. INTEREST.
      *----------------------------------------------------------------
      * Simple and compound interest on a deposit.
      * Input record: principal, annual rate (percent), term in years.
      * Compound interest is credited yearly, rounded to the cent.
      *----------------------------------------------------------------
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  IN-REC.
           05  IN-PRINCIPAL         PIC 9(7)V99.
           05  IN-RATE              PIC 9(2)V9(4).
           05  IN-YEARS             PIC 9(2).
       01  WS-SIMPLE                PIC 9(11)V99 VALUE 0.
       01  WS-BALANCE               PIC 9(13)V99 VALUE 0.
       01  WS-INTEREST              PIC 9(13)V99 VALUE 0.
       01  WS-YEAR                  PIC 9(3)     VALUE 0.
       01  WS-OVERFLOW-FLAG         PIC X        VALUE "N".
           88  OVERFLOWED                        VALUE "Y".
       01  OUT-MONEY                PIC Z,ZZZ,ZZZ,ZZZ,ZZ9.99.
       01  OUT-RATE                 PIC Z9.9999.
       01  OUT-YEAR                 PIC Z9.
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT IN-REC
           MOVE IN-PRINCIPAL TO OUT-MONEY
           DISPLAY "PRINCIPAL " OUT-MONEY
           MOVE IN-RATE TO OUT-RATE
           DISPLAY "RATE      " OUT-RATE "%"
           DISPLAY "YEARS     " IN-YEARS
           COMPUTE WS-SIMPLE ROUNDED =
               IN-PRINCIPAL * IN-RATE / 100 * IN-YEARS
           MOVE WS-SIMPLE TO OUT-MONEY
           DISPLAY "SIMPLE    " OUT-MONEY
           MOVE IN-PRINCIPAL TO WS-BALANCE
           PERFORM VARYING WS-YEAR FROM 1 BY 1
                   UNTIL WS-YEAR > IN-YEARS OR OVERFLOWED
               COMPUTE WS-INTEREST ROUNDED =
                   WS-BALANCE * IN-RATE / 100
               ADD WS-INTEREST TO WS-BALANCE
                   ON SIZE ERROR
                       SET OVERFLOWED TO TRUE
               END-ADD
               MOVE WS-YEAR TO OUT-YEAR
               IF OVERFLOWED
                   DISPLAY "OVERFLOW IN YEAR " OUT-YEAR
               ELSE
                   MOVE WS-BALANCE TO OUT-MONEY
                   DISPLAY "YEAR " OUT-YEAR "   " OUT-MONEY
               END-IF
           END-PERFORM
           MOVE WS-BALANCE TO OUT-MONEY
           DISPLAY "COMPOUND  " OUT-MONEY
           STOP RUN.
