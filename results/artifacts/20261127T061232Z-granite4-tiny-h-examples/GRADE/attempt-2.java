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

        int wsAdjusted = inScore;
        String wsGrade = " ";
        String wsRemark = " ";

        if (inScore < 0 || inScore > 100 || inAttend > 100) {
            System.out.println(inName + " INVALID INPUT " + inScore + "/" + inAttend);
            System.exit(0);
        }

        wsAdjusted = inScore;
        if ("Y".equalsIgnoreCase(inExtra)) {
            wsAdjusted += 5;
            if (wsAdjusted > 100) {
                wsAdjusted = 100;
            }
        }

        if (wsAdjusted >= 90 && inAttend >= 75) {
            wsGrade = "A";
            wsRemark = "EXCELLENT";
        } else if (wsAdjusted >= 80 && inAttend >= 75) {
            wsGrade = "B";
            wsRemark = "GOOD";
        } else if (wsAdjusted >= 70 && inAttend >= 75) {
            wsGrade = "C";
            wsRemark = "SATISFACTORY";
        } else if (wsAdjusted >= 60 && inAttend >= 75) {
            wsGrade = "D";
            wsRemark = "PASS";
        } else if ((wsAdjusted < 60 || inAttend < 75) && !"F".equalsIgnoreCase(wsGrade)) {
            wsGrade = "F";
            wsRemark = "LOW ATTENDANCE";
        } else {
            wsGrade = "F";
            wsRemark = "FAIL";
        }

        System.out.println(inName + " SCORE " + wsAdjusted + " GRADE " + wsGrade + " REMARK " + wsRemark);
    }
}
