       IDENTIFICATION DIVISION.
       PROGRAM-ID. PAYROLL.
      *----------------------------------------------------------------
      * Weekly pay for one employee: overtime at time-and-a-half over
      * 40 hours (not for contractors), marginal tax brackets, and a
      * benefits deduction by employment status. Packed-decimal work
      * fields, as in a typical batch payroll.
      *----------------------------------------------------------------
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  IN-REC.
           05  IN-EMP-ID            PIC X(6).
           05  IN-STATUS            PIC X.
               88  FULL-TIME                    VALUE "F".
               88  PART-TIME                    VALUE "P".
               88  CONTRACTOR                   VALUE "C".
           05  IN-HOURS             PIC 9(3)V9.
           05  IN-RATE              PIC 9(3)V99.
       01  WS-REG-HOURS             PIC 9(3)V9   COMP-3 VALUE 0.
       01  WS-OT-HOURS              PIC 9(3)V9   COMP-3 VALUE 0.
       01  WS-GROSS                 PIC 9(7)V99  COMP-3 VALUE 0.
       01  WS-TAX                   PIC 9(7)V99  COMP-3 VALUE 0.
       01  WS-BENEFITS              PIC 9(3)V99  COMP-3 VALUE 0.
       01  WS-NET                   PIC S9(7)V99 COMP-3 VALUE 0.
       01  OUT-HOURS                PIC ZZ9.9.
       01  OUT-AMT                  PIC Z,ZZZ,ZZ9.99.
       01  OUT-NET                  PIC -Z,ZZZ,ZZ9.99.
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT IN-REC
           IF NOT (FULL-TIME OR PART-TIME OR CONTRACTOR)
               DISPLAY "EMPLOYEE " IN-EMP-ID
                       " REJECTED: BAD STATUS [" IN-STATUS "]"
               STOP RUN
           END-IF
           PERFORM SPLIT-HOURS
           PERFORM CALC-GROSS
           PERFORM CALC-TAX
           PERFORM CALC-BENEFITS
           COMPUTE WS-NET = WS-GROSS - WS-TAX - WS-BENEFITS
           PERFORM PRINT-STUB
           STOP RUN.

       SPLIT-HOURS.
           IF IN-HOURS > 40
               MOVE 40 TO WS-REG-HOURS
               COMPUTE WS-OT-HOURS = IN-HOURS - 40
           ELSE
               MOVE IN-HOURS TO WS-REG-HOURS
               MOVE 0 TO WS-OT-HOURS
           END-IF.

       CALC-GROSS.
           IF CONTRACTOR
               COMPUTE WS-GROSS ROUNDED = IN-HOURS * IN-RATE
           ELSE
               COMPUTE WS-GROSS ROUNDED =
                   WS-REG-HOURS * IN-RATE
                   + WS-OT-HOURS * IN-RATE * 1.5
           END-IF.

       CALC-TAX.
           IF CONTRACTOR
               MOVE 0 TO WS-TAX
           ELSE
               EVALUATE TRUE
                   WHEN WS-GROSS <= 500
                       COMPUTE WS-TAX ROUNDED = WS-GROSS * 0.10
                   WHEN WS-GROSS <= 1500
                       COMPUTE WS-TAX ROUNDED =
                           50 + (WS-GROSS - 500) * 0.20
                   WHEN OTHER
                       COMPUTE WS-TAX ROUNDED =
                           250 + (WS-GROSS - 1500) * 0.30
               END-EVALUATE
           END-IF.

       CALC-BENEFITS.
           EVALUATE TRUE
               WHEN FULL-TIME
                   MOVE 45.50 TO WS-BENEFITS
               WHEN PART-TIME
                   MOVE 12.25 TO WS-BENEFITS
               WHEN OTHER
                   MOVE 0 TO WS-BENEFITS
           END-EVALUATE.

       PRINT-STUB.
           DISPLAY "EMPLOYEE  " IN-EMP-ID "  STATUS " IN-STATUS
           MOVE WS-REG-HOURS TO OUT-HOURS
           DISPLAY "REG HOURS " OUT-HOURS
           MOVE WS-OT-HOURS TO OUT-HOURS
           DISPLAY "OT HOURS  " OUT-HOURS
           MOVE WS-GROSS TO OUT-AMT
           DISPLAY "GROSS     " OUT-AMT
           MOVE WS-TAX TO OUT-AMT
           DISPLAY "TAX       " OUT-AMT
           MOVE WS-BENEFITS TO OUT-AMT
           DISPLAY "BENEFITS  " OUT-AMT
           MOVE WS-NET TO OUT-NET
           DISPLAY "NET PAY  " OUT-NET.
