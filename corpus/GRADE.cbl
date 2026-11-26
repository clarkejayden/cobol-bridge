       IDENTIFICATION DIVISION.
       PROGRAM-ID. GRADE.
      *----------------------------------------------------------------
      * Letter grade from an exam score, with optional extra credit
      * (capped at 100) and an attendance requirement.
      *----------------------------------------------------------------
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  IN-REC.
           05  IN-NAME              PIC X(12).
           05  IN-SCORE             PIC 9(3).
               88  SCORE-VALID                  VALUE 0 THRU 100.
               88  SCORE-A                      VALUE 90 THRU 100.
               88  SCORE-B                      VALUE 80 THRU 89.
               88  SCORE-C                      VALUE 70 THRU 79.
               88  SCORE-D                      VALUE 60 THRU 69.
           05  IN-ATTEND            PIC 9(3).
               88  ATTEND-OK                    VALUE 75 THRU 100.
           05  IN-EXTRA             PIC X.
               88  EXTRA-CREDIT                 VALUE "Y".
       01  WS-ADJUSTED              PIC 9(3)     VALUE 0.
       01  WS-GRADE                 PIC X(2)     VALUE SPACES.
       01  WS-REMARK                PIC X(16)    VALUE SPACES.
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT IN-REC
           IF NOT SCORE-VALID OR IN-ATTEND > 100
               DISPLAY IN-NAME " INVALID INPUT " IN-SCORE "/"
                       IN-ATTEND
               STOP RUN
           END-IF
           MOVE IN-SCORE TO WS-ADJUSTED
           IF EXTRA-CREDIT
               ADD 5 TO WS-ADJUSTED
               IF WS-ADJUSTED > 100
                   MOVE 100 TO WS-ADJUSTED
               END-IF
           END-IF
           EVALUATE TRUE ALSO TRUE
               WHEN WS-ADJUSTED >= 90 ALSO ATTEND-OK
                   MOVE "A" TO WS-GRADE
                   MOVE "EXCELLENT" TO WS-REMARK
               WHEN WS-ADJUSTED >= 80 ALSO ATTEND-OK
                   MOVE "B" TO WS-GRADE
                   MOVE "GOOD" TO WS-REMARK
               WHEN WS-ADJUSTED >= 70 ALSO ATTEND-OK
                   MOVE "C" TO WS-GRADE
                   MOVE "SATISFACTORY" TO WS-REMARK
               WHEN WS-ADJUSTED >= 60 ALSO ATTEND-OK
                   MOVE "D" TO WS-GRADE
                   MOVE "PASS" TO WS-REMARK
               WHEN ANY ALSO NOT ATTEND-OK
                   MOVE "F" TO WS-GRADE
                   MOVE "LOW ATTENDANCE" TO WS-REMARK
               WHEN OTHER
                   MOVE "F" TO WS-GRADE
                   MOVE "FAIL" TO WS-REMARK
           END-EVALUATE
           DISPLAY IN-NAME " SCORE " WS-ADJUSTED " GRADE " WS-GRADE
                   "REMARK " WS-REMARK
           STOP RUN.
