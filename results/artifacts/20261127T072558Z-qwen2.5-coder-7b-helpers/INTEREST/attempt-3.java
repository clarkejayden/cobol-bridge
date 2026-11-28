import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        line = String.format("%-17s", line).replace(' ', '0');

        BigDecimal inPrincipal = Pic.num(line.substring(0, 9), "9(7)V99");
        BigDecimal inRate = Pic.num(line.substring(9, 15), "9(2)V99");
        int inYears = Pic.num(line.substring(15, 17), "9(2)").intValue();

        BigDecimal wsSimple = BigDecimal.ZERO;
        BigDecimal wsBalance = inPrincipal;
        BigDecimal wsInterest = BigDecimal.ZERO;
        int wsYear = 0;
        boolean overflowed = false;

        BigDecimal outMoney = Pic.fit(inPrincipal, "Z,ZZZ,ZZZ,ZZZ,ZZ9.99");
        BigDecimal outRate = Pic.fit(inRate, "Z9.9999");
        int outYear = 0;

        System.out.println("PRINCIPAL " + Pic.show(outMoney, "Z,ZZZ,ZZZ,ZZZ,ZZ9.99"));
        System.out.println("RATE      " + Pic.show(outRate, "Z9.9999") + "%");
        System.out.println("YEARS     " + inYears);

        wsSimple = inPrincipal.multiply(inRate).divide(new BigDecimal("100"), 20, RoundingMode.HALF_UP).multiply(new BigDecimal(inYears)).setScale(2, RoundingMode.HALF_UP);
        outMoney = Pic.fit(wsSimple, "Z,ZZZ,ZZZ,ZZZ,ZZ9.99");
        System.out.println("SIMPLE    " + Pic.show(outMoney, "Z,ZZZ,ZZZ,ZZZ,ZZ9.99"));

        for (wsYear = 1; wsYear <= inYears; wsYear++) {
            wsInterest = wsBalance.multiply(inRate).divide(new BigDecimal("100"), 20, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
            wsBalance = wsBalance.add(wsInterest);
            if (Pic.overflows(wsBalance, "9(13)V99", true)) {
                overflowed = true;
                break;
            }
            outYear = wsYear;
            if (overflowed) {
                System.out.println("OVERFLOW IN YEAR " + outYear);
            } else {
                outMoney = Pic.fit(wsBalance, "Z,ZZZ,ZZZ,ZZZ,ZZ9.99");
                System.out.println("YEAR " + outYear + "   " + Pic.show(outMoney, "Z,ZZZ,ZZZ,ZZZ,ZZ9.99"));
            }
        }

        outMoney = Pic.fit(wsBalance, "Z,ZZZ,ZZZ,ZZZ,ZZ9.99");
        System.out.println("COMPOUND  " + Pic.show(outMoney, "Z,ZZZ,ZZZ,ZZZ,ZZ9.99"));
    }
}
