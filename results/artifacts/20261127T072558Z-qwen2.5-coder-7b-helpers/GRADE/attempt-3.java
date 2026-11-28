import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        line = String.format("%-19s", line).substring(0, 19);

        if (line.trim().isEmpty()) {
            System.out.println("INVALID INPUT");
            return;
        }

        String inName = line.substring(0, 12).trim();
        BigDecimal inScore = Pic.num(line.substring(12, 15), "9(3)");
        BigDecimal inAttend = Pic.num(line.substring(15, 18), "9(3)");
        char inExtra = line.charAt(18);

        if (!Pic.overflows(inScore, "9(3)", false) && !Pic.overflows(inAttend, "9(3)", false)) {
            BigDecimal wsAdjusted = inScore;
            if (inExtra == 'Y') {
                wsAdjusted = wsAdjusted.add(new BigDecimal("5"));
                if (wsAdjusted.compareTo(new BigDecimal("100")) > 0) {
                    wsAdjusted = new BigDecimal("100");
                }
            }

            String wsGrade = "F";
            String wsRemark = "FAIL";
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
            }

            System.out.println(inName + " SCORE " + Pic.show(wsAdjusted, "9(3)") + " GRADE " + wsGrade + " REMARK " + wsRemark);
        } else {
            System.out.println(inName + " INVALID INPUT " + Pic.show(inScore, "9(3)") + "/" + Pic.show(inAttend, "9(3)"));
        }
    }
}
