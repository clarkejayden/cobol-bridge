       IDENTIFICATION DIVISION.
       PROGRAM-ID. LEDGER.
      *----------------------------------------------------------------
      * Applies five signed transactions to an opening balance and
      * prints a running balance, credit and debit totals, and the
      * closing position with sign, CR and floating-sign editing.
      *----------------------------------------------------------------
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  IN-REC.
           05  IN-ACCOUNT           PIC X(8).
           05  IN-OPENING           PIC S9(7)V99
                                    SIGN IS LEADING SEPARATE.
           05  IN-TXN               PIC S9(5)V99
                                    SIGN IS LEADING SEPARATE
                                    OCCURS 5 TIMES.
       01  WS-BALANCE               PIC S9(9)V99 VALUE 0.
       01  WS-CREDITS               PIC S9(9)V99 VALUE 0.
       01  WS-DEBITS                PIC S9(9)V99 VALUE 0.
       01  WS-I                     PIC 9        VALUE 0.
       01  OUT-TXN                  PIC +ZZ,ZZ9.99.
       01  OUT-BAL                  PIC -ZZZ,ZZZ,ZZ9.99.
       01  OUT-CRDB                 PIC ZZZ,ZZZ,ZZ9.99CR.
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT IN-REC
           DISPLAY "ACCOUNT " IN-ACCOUNT
           DISPLAY "OPENING " IN-OPENING
           MOVE IN-OPENING TO WS-BALANCE
           PERFORM VARYING WS-I FROM 1 BY 1 UNTIL WS-I > 5
               IF IN-TXN (WS-I) < 0
                   ADD IN-TXN (WS-I) TO WS-DEBITS
               ELSE
                   ADD IN-TXN (WS-I) TO WS-CREDITS
               END-IF
               ADD IN-TXN (WS-I) TO WS-BALANCE
               MOVE IN-TXN (WS-I) TO OUT-TXN
               MOVE WS-BALANCE TO OUT-BAL
               DISPLAY "TXN " WS-I " " OUT-TXN " BAL " OUT-BAL
           END-PERFORM
           MOVE WS-CREDITS TO OUT-CRDB
           DISPLAY "CREDITS " OUT-CRDB
           MOVE WS-DEBITS TO OUT-CRDB
           DISPLAY "DEBITS  " OUT-CRDB
           MOVE WS-BALANCE TO OUT-CRDB
           DISPLAY "CLOSING " OUT-CRDB
           EVALUATE TRUE
               WHEN WS-BALANCE < 0
                   DISPLAY "STATUS  OVERDRAWN"
               WHEN WS-BALANCE = 0
                   DISPLAY "STATUS  ZERO"
               WHEN OTHER
                   DISPLAY "STATUS  IN CREDIT"
           END-EVALUATE
           STOP RUN.
