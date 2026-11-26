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

        // Format output fields
        String outMoney = formatMoney(inPrincipal);
        String outRate = formatRate(inRate);
        int outYear = 0;

        System.out.println("PRINCIPAL " + outMoney);
        System.out.println("RATE      " + outRate + "%");
        System.out.println("YEARS     " + inYears);

        wsSimple = inPrincipal.multiply(inRate).divide(new BigDecimal("100")).multiply(new BigDecimal(inYears)).setScale(2, RoundingMode.HALF_UP);
        outMoney = formatMoney(wsSimple);
        System.out.println("SIMPLE    " + outMoney);

        for (wsYear = 1; wsYear <= inYears; wsYear++) {
            wsInterest = wsBalance.multiply(inRate).divide(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
            wsBalance = wsBalance.add(wsInterest).setScale(2, RoundingMode.HALF_UP);
            outYear = wsYear;
            if (wsBalance.compareTo(new BigDecimal("999999999999999.99")) > 0) {
                overflowed = true;
                break;
            }
            outMoney = formatMoney(wsBalance);
            System.out.println("YEAR " + outYear + "   " + outMoney);
        }

        if (overflowed) {
            System.out.println("OVERFLOW IN YEAR " + outYear);
        } else {
            outMoney = formatMoney(wsBalance);
            System.out.println("COMPOUND  " + outMoney);
        }
    }

    private static String formatMoney(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return "0.00";
        }
        String moneyStr = value.setScale(2, RoundingMode.HALF_UP).toString();
        int decimalIndex = moneyStr.indexOf('.');
        if (decimalIndex == -1) {
            decimalIndex = moneyStr.length();
        }
        int integerPartLength = decimalIndex;
        StringBuilder formattedMoney = new StringBuilder();
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            formattedMoney.append('-');
            moneyStr = moneyStr.substring(1);
        }
        for (int i = 0; i < integerPartLength; i++) {
            if (i > 0 && (integerPartLength - i) % 3 == 0) {
                formattedMoney.append(',');
            }
            formattedMoney.append(moneyStr.charAt(i));
        }
        formattedMoney.append('.');
        formattedMoney.append(moneyStr.substring(decimalIndex + 1));
        return formattedMoney.toString();
    }

    private static String formatRate(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return "0.0000";
        }
        String rateStr = value.setScale(4, RoundingMode.HALF_UP).toString();
        int decimalIndex = rateStr.indexOf('.');
        if (decimalIndex == -1) {
            decimalIndex = rateStr.length();
        }
        int integerPartLength = decimalIndex;
        StringBuilder formattedRate = new StringBuilder();
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            formattedRate.append('-');
            rateStr = rateStr.substring(1);
        }
        for (int i = 0; i < integerPartLength; i++) {
            if (i > 0 && (integerPartLength - i) % 3 == 0) {
                formattedRate.append(',');
            }
            formattedRate.append(rateStr.charAt(i));
        }
        formattedRate.append('.');
        formattedRate.append(rateStr.substring(decimalIndex + 1));
        return formattedRate.toString();
    }
}
