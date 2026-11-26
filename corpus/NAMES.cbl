       IDENTIFICATION DIVISION.
       PROGRAM-ID. NAMES.
      *----------------------------------------------------------------
      * Builds a directory entry from name parts: STRING with POINTER
      * and overflow, reference modification, INSPECT TALLYING,
      * REPLACING and CONVERTING.
      *----------------------------------------------------------------
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  IN-REC.
           05  IN-FIRST             PIC X(12).
           05  IN-LAST              PIC X(15).
           05  IN-DEPT              PIC X(10).
       01  WS-FULL                  PIC X(36)    VALUE SPACES.
       01  WS-PTR                   PIC 9(2)     VALUE 1.
       01  WS-LEN                   PIC 9(2)     VALUE 0.
       01  WS-COUNT-A               PIC 9(2)     VALUE 0.
       01  WS-COUNT-LEAD            PIC 9(2)     VALUE 0.
       01  WS-INITIALS              PIC X(4)     VALUE SPACES.
       01  WS-USERID                PIC X(8)     VALUE SPACES.
       PROCEDURE DIVISION.
       MAIN-PARA.
           ACCEPT IN-REC
           STRING IN-LAST  DELIMITED BY SPACE
                  ", "     DELIMITED BY SIZE
                  IN-FIRST DELIMITED BY SPACE
                  " ("     DELIMITED BY SIZE
                  IN-DEPT  DELIMITED BY SPACE
                  ")"      DELIMITED BY SIZE
               INTO WS-FULL
               WITH POINTER WS-PTR
               ON OVERFLOW
                   DISPLAY "WARNING: NAME TRUNCATED"
           END-STRING
           COMPUTE WS-LEN = WS-PTR - 1
           DISPLAY "ENTRY    [" WS-FULL "]"
           DISPLAY "LENGTH   " WS-LEN
           INSPECT WS-FULL TALLYING WS-COUNT-A FOR ALL "A"
           DISPLAY "A COUNT  " WS-COUNT-A
           INSPECT IN-FIRST TALLYING WS-COUNT-LEAD FOR LEADING SPACE
           DISPLAY "LEADING  " WS-COUNT-LEAD
           STRING IN-FIRST (1:1) "." IN-LAST (1:1) "."
               DELIMITED BY SIZE INTO WS-INITIALS
           END-STRING
           DISPLAY "INITIALS [" WS-INITIALS "]"
           MOVE IN-LAST (1:6) TO WS-USERID (1:6)
           MOVE IN-FIRST (1:1) TO WS-USERID (7:1)
           MOVE "0" TO WS-USERID (8:1)
           INSPECT WS-USERID REPLACING ALL SPACE BY "X"
           INSPECT WS-USERID CONVERTING
               "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
               TO "abcdefghijklmnopqrstuvwxyz"
           DISPLAY "USERID   " WS-USERID
           INSPECT WS-FULL CONVERTING "AEIOU" TO "aeiou"
           DISPLAY "STYLED   [" WS-FULL "]"
           STOP RUN.
