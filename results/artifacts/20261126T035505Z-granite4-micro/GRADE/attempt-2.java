import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        // Read input line
        String line = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in)).readLine();

        // Pad input line to the required record length (19 characters)
        if (line == null || line.length() < 19) {
            line = line + "                  ";
        }

        // Slice fields by offsets
        String inName = line.substring(0, 12).trim();
        BigDecimal inScore = new BigDecimal(line.substring(12, 15).trim());
        BigDecimal inAttend = new BigDecimal(line.substring(15, 18).trim());
        char inExtra = line.charAt(18);

        // Data layout
        BigDecimal wsAdjusted = new BigDecimal(0);
        String wsGrade = " ";
        String wsRemark = " ";

        // Validate score
        if (!inScore.compareTo(new BigDecimal(0)) >= 0 || !inScore.compareTo(new BigDecimal(100)) <= 0 || !inAttend.compareTo(new BigDecimal(75)) >= 0) {
            System.out.print(inName + " INVALID INPUT " + inScore + "/" + inAttend);
            return;
        }

        // Move IN-SCORE to WS-ADJUSTED
        wsAdjusted = inScore;

        // Extra credit
        if (inExtra == 'Y') {
            wsAdjusted = wsAdjusted.add(new BigDecimal(5));
            if (wsAdjusted.compareTo(new BigDecimal(100)) > 0) {
                wsAdjusted = wsAdjusted.setScale(0, RoundingMode.HALF_UP);
                wsAdjusted = wsAdjusted.max(new BigDecimal(100));
            }
        }

        // Attendance requirement
        boolean attendOk = inAttend.compareTo(new BigDecimal(75)) >= 0;

        // Evaluate WS-ADJUSTED and ATTEND-OK
        if (wsAdjusted.compareTo(new BigDecimal(90)) >= 0 && attendOk) {
            wsGrade = "A";
            wsRemark = "EXCELLENT";
        } else if (wsAdjusted.compareTo(new BigDecimal(80)) >= 0 && attendOk) {
            wsGrade = "B";
            wsRemark = "GOOD";
        } else if (wsAdjusted.compareTo(new BigDecimal(70)) >= 0 && attendOk) {
            wsGrade = "C";
            wsRemark = "SATISFACTORY";
        } else if (wsAdjusted.compareTo(new BigDecimal(60)) >= 0 && attendOk) {
            wsGrade = "D";
            wsRemark = "PASS";
        } else if (!attendOk) {
            wsGrade = "F";
            wsRemark = "LOW ATTENDANCE";
        } else {
            wsGrade = "F";
            wsRemark = "FAIL";
        }

        // Display results
        System.out.print(inName + " SCORE " + wsAdjusted + " GRADE " + wsGrade + " REMARK " + wsRemark);
    }
}
