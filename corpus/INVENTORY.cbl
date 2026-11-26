       IDENTIFICATION DIVISION.
       PROGRAM-ID. INVENTORY.
      *----------------------------------------------------------------
      * Stock valuation for six items held in an OCCURS table: prints
      * a fixed-width report line per item, flags items to reorder,
      * and summarises total value, the most valuable line and the
      * weighted average unit cost.
      *----------------------------------------------------------------
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  IN-REC.
           05  IN-WAREHOUSE         PIC X(3).
           05  IN-ITEM              OCCURS 6 TIMES.
               10  IN-SKU           PIC X(5).
               10  IN-QTY           PIC 9(4).
               10  IN-COST          PIC 9(4)V99.
       01  WS-VALUE                 PIC 9(8)V99  VALUE 0.
       01  WS-TOTAL                 PIC 9(9)V99  VALUE 0.
       01  WS-TOTAL-QTY             PIC 9(6)     VALUE 0.
       01  WS-MAX-VALUE             PIC 9(8)V99  VALUE 0.
       01  WS-MAX-IDX               PIC 9        VALUE 1.
       01  WS-REORDER               PIC 9        VALUE 0.
       01  WS-AVG-COST              PIC 9(4)V99  VALUE 0.
       01  WS-I                     PIC 9        VALUE 0.
       01  RPT-LINE.
           05  RPT-SKU              PIC X(5).
           05  FILLER               PIC X(2)     VALUE SPACES.
           05  RPT-QTY              PIC Z,ZZ9.
           05  FILLER               PIC X(2)     VALUE SPACES.
           05  RPT-COST             PIC Z,ZZ9.99.
           05  FILLER               PIC X(2)     VALUE SPACES.
           05  RPT-VALUE            PIC ZZ,ZZZ,ZZ9.99.
           05  FILLER               PIC X(1)     VALUE SPACE.
           05  RPT-FLAG             PIC X(7).
       01  OUT-TOTAL                PIC ZZZ,ZZZ,ZZ9.99.
       01  OUT-QTY                  PIC ZZZ,ZZ9.
       01  OUT-AVG                  PIC Z,ZZ9.99.
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT IN-REC
           DISPLAY "WAREHOUSE " IN-WAREHOUSE
           DISPLAY "SKU        QTY      COST          VALUE"
           PERFORM VARYING WS-I FROM 1 BY 1 UNTIL WS-I > 6
               PERFORM VALUE-ITEM
           END-PERFORM
           IF WS-TOTAL-QTY > 0
               COMPUTE WS-AVG-COST ROUNDED = WS-TOTAL / WS-TOTAL-QTY
           ELSE
               MOVE 0 TO WS-AVG-COST
           END-IF
           MOVE WS-TOTAL TO OUT-TOTAL
           DISPLAY "TOTAL VALUE    " OUT-TOTAL
           MOVE WS-TOTAL-QTY TO OUT-QTY
           DISPLAY "TOTAL UNITS    " OUT-QTY
           MOVE WS-AVG-COST TO OUT-AVG
           DISPLAY "AVG UNIT COST  " OUT-AVG
           DISPLAY "TOP LINE       " IN-SKU (WS-MAX-IDX)
           DISPLAY "TO REORDER     " WS-REORDER
           STOP RUN.

       VALUE-ITEM.
           COMPUTE WS-VALUE = IN-QTY (WS-I) * IN-COST (WS-I)
           ADD WS-VALUE TO WS-TOTAL
           ADD IN-QTY (WS-I) TO WS-TOTAL-QTY
           IF WS-VALUE > WS-MAX-VALUE
               MOVE WS-VALUE TO WS-MAX-VALUE
               MOVE WS-I TO WS-MAX-IDX
           END-IF
           MOVE SPACES TO RPT-FLAG
           IF IN-QTY (WS-I) < 100
               MOVE "REORDER" TO RPT-FLAG
               ADD 1 TO WS-REORDER
           END-IF
           MOVE IN-SKU (WS-I) TO RPT-SKU
           MOVE IN-QTY (WS-I) TO RPT-QTY
           MOVE IN-COST (WS-I) TO RPT-COST
           MOVE WS-VALUE TO RPT-VALUE
           DISPLAY RPT-LINE.
