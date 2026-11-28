import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) throws IOException {
        String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (line == null || line.isEmpty()) {
            line = "";
        }
        line = line.concat("                                        ");
        String inName = line.substring(0, 12).trim();
        int inScore = Integer.parseInt(line.substring(12, 15));
        int inAttend = Integer.parseInt(line.substring(15, 18));
        String inExtra = line.substring(18, 19);

        BigDecimal wsAdjusted = new BigDecimal(inScore);
        boolean extraCredit = "Y".equalsIgnoreCase(inExtra);

        if (!isValidScore(inScore) || inAttend > 100) {
            System.out.println(inName + " INVALID INPUT " + inScore + "/" + inAttend);
            System.exit(0);
        }

        wsAdjusted = adjustScore(wsAdjusted, extraCredit);
        String wsGrade = determineGrade(wsAdjusted, inAttend);
        String wsRemark = determineRemark(wsAdjusted, inAttend);

        System.out.println(inName + " SCORE " + wsAdjusted.setScale(2, RoundingMode.HALF_UP) + " GRADE " + wsGrade + " REMARK " + wsRemark);
    }

    private static boolean isValidScore(int score) {
        return score >= 0 && score <= 100;
    }

    private static BigDecimal adjustScore(BigDecimal adjusted, boolean extraCredit) {
        if (extraCredit) {
            adjusted = adjusted.add(new BigDecimal(5));
            if (adjusted.compareTo(new BigDecimal(100)) > 0) {
                adjusted = new BigDecimal(100);
            }
        }
        return adjusted;
    }

    private static String determineGrade(BigDecimal adjusted, int attend) {
        if (adjusted.compareTo(new BigDecimal(90)) >= 0 && attend >= 75) {
            return "A";
        } else if (adjusted.compareTo(new BigDecimal(80)) >= 0 && attend >= 75) {
            return "B";
        } else if (adjusted.compareTo(new BigDecimal(70)) >= 0 && attend >= 75) {
            return "C";
        } else if (adjusted.compareTo(new BigDecimal(60)) >= 0 && attend >= 75) {
            return "D";
        } else {
            return "F";
        }
    }

    private static String determineRemark(BigDecimal adjusted, int attend) {
        if (adjusted.compareTo(new BigDecimal(90)) >= 0 && attend >= 75) {
            return "EXCELLENT";
        } else if (adjusted.compareTo(new BigDecimal(80)) >= 0 && attend >= 75) {
            return "GOOD";
        } else if (adjusted.compareTo(new BigDecimal(70)) >= 0 && attend >= 75) {
            return "SATISFACTORY";
        } else if (adjusted.compareTo(new BigDecimal(60)) >= 0 && attend >= 75) {
            return "PASS";
        } else {
            return "LOW ATTENDANCE";
        }
    }
}
