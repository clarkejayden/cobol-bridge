import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        line = String.format("%-17s", line).substring(0, 17);

        if (line.trim().isEmpty()) {
            System.out.println("INVALID TERM: ");
            return;
        }

        BigDecimal inAmount = Pic.num(line.substring(0, 9), "9(7)V99");
        BigDecimal inRate = Pic.num(line.substring(9, 14), "9(2)V999");
        int inMonths = Integer.parseInt(line.substring(14, 17).trim());

        if (inMonths == 0 || inMonths > 360) {
            System.out.println("INVALID TERM: " + Pic.show(inMonths, "ZZZ"));
            return;
        }

        BigDecimal wsBalance = Pic.fit(inAmount, "9(7)V99");
        BigDecimal wsPrincipal = Pic.fit(inAmount.divide(new BigDecimal(inMonths), 20, RoundingMode.DOWN), "9(7)V99");
        BigDecimal wsInterest = BigDecimal.ZERO;
        BigDecimal wsTotalInt = BigDecimal.ZERO;
        int wsMonth = 0;

        System.out.println("MONTH      INTEREST     PRINCIPAL       BALANCE");

        for (wsMonth = 1; wsMonth <= inMonths; wsMonth++) {
            wsInterest = Pic.fit(wsBalance.multiply(inRate).divide(new BigDecimal(1200), 20, RoundingMode.DOWN), "9(7)V99");
            if (wsMonth == inMonths) {
                wsPrincipal = Pic.fit(wsBalance, "9(7)V99");
            }
            wsBalance = Pic.fit(wsBalance.subtract(wsPrincipal), "9(7)V99");
            wsTotalInt = Pic.fit(wsTotalInt.add(wsInterest), "9(9)V99");

            System.out.printf("%2d %8s %8s %8s%n",
                    wsMonth,
                    Pic.show(wsInterest, "ZZ,ZZZ,ZZ9.99"),
                    Pic.show(wsPrincipal, "ZZ,ZZZ,ZZ9.99"),
                    Pic.show(wsBalance, "ZZ,ZZZ,ZZ9.99")
            );
        }

        System.out.printf("TOTAL INTEREST %8s%n", Pic.show(wsTotalInt, "ZZZ,ZZZ,ZZ9.99"));
    }
}
