       IDENTIFICATION DIVISION.
       PROGRAM-ID. SALESTAX.
      *----------------------------------------------------------------
      * Looks up a state's sales tax rate in a table loaded from
      * literals (REDEFINES) with SEARCH, then prices an order.
      * NV is a known state with no rate on file.
      *----------------------------------------------------------------
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  RATE-DATA.
           05  FILLER               PIC X(6)     VALUE "CA0725".
           05  FILLER               PIC X(6)     VALUE "NY0400".
           05  FILLER               PIC X(6)     VALUE "TX0625".
           05  FILLER               PIC X(6)     VALUE "WA0650".
           05  FILLER               PIC X(6)     VALUE "OR0000".
           05  FILLER               PIC X(6)     VALUE "FL0600".
       01  RATE-TABLE REDEFINES RATE-DATA.
           05  RATE-ENTRY           OCCURS 6 TIMES INDEXED BY RX.
               10  RATE-STATE       PIC XX.
               10  RATE-PCT         PIC 99V99.
       01  IN-REC.
           05  IN-STATE             PIC XX.
               88  KNOWN-STATE      VALUE "CA" "NY" "TX" "WA" "OR"
                                          "FL" "NV".
           05  IN-AMOUNT            PIC 9(6)V99.
           05  IN-EXEMPT            PIC X.
               88  IS-EXEMPT                    VALUE "Y".
       01  WS-TAX                   PIC 9(6)V99  VALUE 0.
       01  WS-TOTAL                 PIC 9(7)V99  VALUE 0.
       01  OUT-AMT                  PIC ZZZ,ZZ9.99.
       01  OUT-TOTAL                PIC Z,ZZZ,ZZ9.99.
       01  OUT-PCT                  PIC Z9.99.
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT IN-REC
           IF NOT KNOWN-STATE
               DISPLAY "INVALID STATE [" IN-STATE "]"
               STOP RUN
           END-IF
           SET RX TO 1
           SEARCH RATE-ENTRY
               AT END
                   DISPLAY "NO RATE ON FILE FOR " IN-STATE
               WHEN RATE-STATE (RX) = IN-STATE
                   PERFORM PRICE-ORDER
           END-SEARCH
           STOP RUN.

       PRICE-ORDER.
           IF IS-EXEMPT
               MOVE 0 TO WS-TAX
           ELSE
               COMPUTE WS-TAX ROUNDED =
                   IN-AMOUNT * RATE-PCT (RX) / 100
           END-IF
           COMPUTE WS-TOTAL = IN-AMOUNT + WS-TAX
           MOVE RATE-PCT (RX) TO OUT-PCT
           DISPLAY "STATE    " IN-STATE "  RATE " OUT-PCT "%"
           MOVE IN-AMOUNT TO OUT-AMT
           DISPLAY "SUBTOTAL " OUT-AMT
           MOVE WS-TAX TO OUT-AMT
           IF IS-EXEMPT
               DISPLAY "TAX      " OUT-AMT " (EXEMPT)"
           ELSE
               DISPLAY "TAX      " OUT-AMT
           END-IF
           MOVE WS-TOTAL TO OUT-TOTAL
           DISPLAY "TOTAL  " OUT-TOTAL.
