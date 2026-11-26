import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (line.length() < 19) {
            line += " ";
        }
        scanner.close();

        String inName = line.substring(0, 12).trim();
        BigDecimal inScore = new BigDecimal(line.substring(12, 15).trim());
        BigDecimal inAttend = new BigDecimal(line.substring(15, 18).trim());
        char inExtra = line.charAt(18);

        BigDecimal wsAdjusted = inScore;
        String wsGrade = " ";
        String wsRemark = " ";

        if (inScore.compareTo(BigDecimal.ZERO) < 0 || inScore.compareTo(new BigDecimal("100")) > 0 || inAttend.compareTo(new BigDecimal("100")) > 0) {
            System.out.println(inName + " INVALID INPUT " + inScore + "/" + inAttend);
            return;
        }

        if (inExtra == 'Y') {
            wsAdjusted = wsAdjusted.add(new BigDecimal("5"));
            if (wsAdjusted.compareTo(new BigDecimal("100")) > 0) {
                wsAdjusted = new BigDecimal("100");
            }
        }

        if (wsAdjusted.compareTo(new BigDecimal("90")) >= 0 && inAttend.compareTo(new BigDecimal("75")) >= 0) {
            wsGrade = "A";
            wsRemark = "EXCELLENT";
        } else if (wsAdjusted.compareTo(new BigDecimal("80")) >= 0 && inAttend.compareTo(new BigDecimal("75")) >= 0) {
            wsGrade = "B";
            wsRemark = "GOOD";
        } else if (wsAdjusted.compareTo(new BigDecimal("70")) >= 0 && inAttend.compareTo(new BigDecimal("75")) >= 0) {
            wsGrade = "C";
            wsRemark = "SATISFACTORY";
        } else if (wsAdjusted.compareTo(new BigDecimal("60")) >= 0 && inAttend.compareTo(new BigDecimal("75")) >= 0) {
            wsGrade = "D";
            wsRemark = "PASS";
        } else if (inAttend.compareTo(new BigDecimal("75")) < 0) {
            wsGrade = "F";
            wsRemark = "LOW ATTENDANCE";
        } else {
            wsGrade = "F";
            wsRemark = "FAIL";
        }

        // Pad wsAdjusted with leading zeros if it is zero
        if (wsAdjusted.compareTo(BigDecimal.ZERO) == 0) {
            wsAdjusted = new BigDecimal("000");
        }

        System.out.println(inName + " SCORE " + wsAdjusted + " GRADE " + wsGrade + " REMARK " + wsRemark);
    }
}
