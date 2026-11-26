import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (line.length() < 17) {
            line += " ";
        }
        scanner.close();

        BigDecimal inAmount = new BigDecimal(line.substring(0, 9)).setScale(2, RoundingMode.DOWN);
        BigDecimal inRate = new BigDecimal(line.substring(9, 14)).setScale(3, RoundingMode.DOWN);
        int inMonths = Integer.parseInt(line.substring(14, 17));

        if (inMonths == 0 || inMonths > 360) {
            System.out.println("INVALID TERM: " + inMonths);
            return;
        }

        BigDecimal wsBalance = inAmount;
        BigDecimal wsPrincipal = inAmount.divide(new BigDecimal(inMonths), 2, RoundingMode.DOWN);
        BigDecimal wsInterest = BigDecimal.ZERO;
        BigDecimal wsTotalInt = BigDecimal.ZERO;
        int wsMonth = 0;

        System.out.println("MONTH      INTEREST     PRINCIPAL       BALANCE");

        for (wsMonth = 1; wsMonth <= inMonths; wsMonth++) {
            wsInterest = wsBalance.multiply(inRate).divide(new BigDecimal(1200), 2, RoundingMode.DOWN);
            if (wsMonth == inMonths) {
                wsPrincipal = wsBalance;
            }
            wsBalance = wsBalance.subtract(wsPrincipal);
            wsTotalInt = wsTotalInt.add(wsInterest);

            System.out.printf("%2d %8.2f %8.2f %8.2f%n", wsMonth, wsInterest, wsPrincipal, wsBalance);
        }

        System.out.printf("TOTAL INTEREST %8.2f%n", wsTotalInt);
    }
}
