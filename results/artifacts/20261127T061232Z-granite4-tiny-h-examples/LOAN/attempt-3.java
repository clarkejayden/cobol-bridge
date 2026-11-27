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
        line = line.concat("                                        ").substring(0, 17);

        char[] inAmountChars = new char[9];
        char[] inRateChars = new char[5];
        char[] inMonthsChars = new char[3];
        System.arraycopy(line.toCharArray(), 0, inAmountChars, 0, 9);
        System.arraycopy(line.toCharArray(), 9, inRateChars, 0, 5);
        System.arraycopy(line.toCharArray(), 14, inMonthsChars, 0, 3);

        int inAmount = Integer.parseInt(new String(inAmountChars));
        int inRate = Integer.parseInt(new String(inRateChars));
        int inMonths = Integer.parseInt(new String(inMonthsChars));

        if (inMonths == 0 || inMonths > 360) {
            System.out.println("INVALID TERM: " + inMonths);
            return;
        }

        BigDecimal balance = new BigDecimal(inAmount);
        BigDecimal principal = new BigDecimal(inAmount).divide(new BigDecimal(inMonths), BigDecimal.ROUND_DOWN);
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal interest = BigDecimal.ZERO;

        System.out.println("MONTH      INTEREST     PRINCIPAL       BALANCE");
        for (int month = 1; month <= inMonths; month++) {
            interest = balance.multiply(new BigDecimal(inRate)).divide(new BigDecimal(1200), BigDecimal.ROUND_HALF_UP);
            if (month == inMonths) {
                principal = balance;
            } else {
                balance = balance.subtract(principal);
            }
            totalInterest = totalInterest.add(interest);
            System.out.printf("  %3d %9s %11s %11s%n", month, interest, principal, balance);
        }
        System.out.printf("TOTAL INTEREST %s%n", totalInterest);
    }
}
