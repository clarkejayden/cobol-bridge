import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            line = "";
        }
        line = line.concat("                                        ");
        String inName = line.substring(0, 12).trim();
        int inScore = Integer.parseInt(line.substring(12, 15));
        int inAttend = Integer.parseInt(line.substring(15, 18));
        String inExtra = line.substring(18, 19);

        int adjustedScore = inScore;
        String grade = "F";
        String remark = "FAIL";

        if (inScore < 0 || inScore > 100 || inAttend > 100) {
            System.out.println(inName + " INVALID INPUT " + inScore + "/" + inAttend);
            System.exit(0);
        }

        adjustedScore = inScore;
        if ("Y".equalsIgnoreCase(inExtra)) {
            adjustedScore += 5;
            if (adjustedScore > 100) {
                adjustedScore = 100;
            }
        }

        if (adjustedScore >= 90 && inAttend >= 75) {
            grade = "A";
            remark = "EXCELLENT";
        } else if (adjustedScore >= 80 && inAttend >= 75) {
            grade = "B";
            remark = "GOOD";
        } else if (adjustedScore >= 70 && inAttend >= 75) {
            grade = "C";
            remark = "SATISFACTORY";
        } else if (adjustedScore >= 60 && inAttend >= 75) {
            grade = "D";
            remark = "PASS";
        }

        System.out.println(inName + " SCORE " + adjustedScore + " GRADE " + grade + " REMARK " + remark);
    }
}
