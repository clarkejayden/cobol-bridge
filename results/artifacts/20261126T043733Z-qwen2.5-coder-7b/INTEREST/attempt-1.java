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

        BigDecimal inPrincipal = new BigDecimal(line.substring(0, 9)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal inRate = new BigDecimal(line.substring(9, 15)).setScale(4, RoundingMode.HALF_UP);
        int inYears = Integer.parseInt(line.substring(15, 17));

        BigDecimal wsSimple = BigDecimal.ZERO;
        BigDecimal wsBalance = inPrincipal;
        BigDecimal wsInterest = BigDecimal.ZERO;
        int wsYear = 0;
        boolean overflowed = false;

        BigDecimal outMoney = inPrincipal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal outRate = inRate.setScale(4, RoundingMode.HALF_UP);
        int outYear = 0;

        System.out.println("PRINCIPAL " + outMoney);
        System.out.println("RATE      " + outRate + "%");
        System.out.println("YEARS     " + inYears);

        wsSimple = inPrincipal.multiply(inRate).divide(new BigDecimal("100")).multiply(new BigDecimal(inYears)).setScale(2, RoundingMode.HALF_UP);
        outMoney = wsSimple.setScale(2, RoundingMode.HALF_UP);
        System.out.println("SIMPLE    " + outMoney);

        for (wsYear = 1; wsYear <= inYears; wsYear++) {
            wsInterest = wsBalance.multiply(inRate).divide(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
            wsBalance = wsBalance.add(wsInterest).setScale(2, RoundingMode.HALF_UP);
            outYear = wsYear;
            if (wsBalance.compareTo(new BigDecimal("999999999999999.99")) > 0) {
                overflowed = true;
                break;
            }
            outMoney = wsBalance.setScale(2, RoundingMode.HALF_UP);
            System.out.println("YEAR " + outYear + "   " + outMoney);
        }

        if (overflowed) {
            System.out.println("OVERFLOW IN YEAR " + outYear);
        } else {
            outMoney = wsBalance.setScale(2, RoundingMode.HALF_UP);
            System.out.println("COMPOUND  " + outMoney);
        }
    }
}
