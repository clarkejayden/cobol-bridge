import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        line = String.format("%-15s", line).substring(0, 15);

        if (line.trim().isEmpty()) {
            System.out.println("UNSUPPORTED PAIR    /");
            return;
        }

        BigDecimal inAmount = Pic.num(line.substring(0, 9), "9(7)V99");
        String inFrom = line.substring(9, 12).trim();
        String inTo = line.substring(12, 15).trim();

        if (!"USD".equals(inFrom) && !"EUR".equals(inFrom) && !"GBP".equals(inFrom) && !"JPY".equals(inFrom) ||
            !"USD".equals(inTo) && !"EUR".equals(inTo) && !"GBP".equals(inTo) && !"JPY".equals(inTo)) {
            System.out.println("UNSUPPORTED PAIR " + inFrom + "/" + inTo);
            return;
        }

        BigDecimal wsFromRate = BigDecimal.ZERO;
        BigDecimal wsToRate = BigDecimal.ZERO;
        String wsCode = inFrom;
        performLookupRate(wsCode, wsFromRate);
        wsFromRate = wsFromRate;

        wsCode = inTo;
        performLookupRate(wsCode, wsToRate);
        wsToRate = wsToRate;

        BigDecimal wsUsd = inAmount.divide(wsFromRate, 4, RoundingMode.DOWN);
        BigDecimal wsResult;
        if ("JPY".equals(inTo)) {
            try {
                wsResult = wsUsd.multiply(wsToRate).setScale(0, RoundingMode.HALF_UP);
            } catch (ArithmeticException e) {
                System.out.println("AMOUNT TOO LARGE TO CONVERT");
                return;
            }
        } else {
            try {
                wsResult = wsUsd.multiply(wsToRate).setScale(2, RoundingMode.HALF_UP);
            } catch (ArithmeticException e) {
                System.out.println("AMOUNT TOO LARGE TO CONVERT");
                return;
            }
        }

        System.out.println("FROM " + Pic.show(inAmount, "ZZZ,ZZZ,ZZ9.99") + " " + inFrom);
        System.out.println("TO   " + Pic.show(wsResult, "ZZZ,ZZZ,ZZ9.99") + " " + inTo);
        System.out.println("RATE " + Pic.show(wsToRate, "ZZ9.9999") + " " + inTo + " PER USD");

        BigDecimal wsWhole = wsResult.setScale(0, RoundingMode.DOWN);
        BigDecimal[] noteValues = {new BigDecimal("1000"), new BigDecimal("500"), new BigDecimal("200"), new BigDecimal("100"), new BigDecimal("50"), new BigDecimal("1")};
        for (int i = 0; i < noteValues.length; i++) {
            BigDecimal[] divResult = wsWhole.divideAndRemainder(noteValues[i]);
            BigDecimal wsCount = divResult[0];
            BigDecimal wsRem = divResult[1];
            if (wsCount.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("  " + Pic.show(noteValues[i], "ZZZ,ZZZ,ZZ9") + " x " + Pic.show(wsCount, "ZZZ,ZZZ,ZZ9"));
            }
            wsWhole = wsRem;
        }
    }

    private static void performLookupRate(String wsCode, BigDecimal wsRate) {
        switch (wsCode) {
            case "USD":
                wsRate = new BigDecimal("1.0000");
                break;
            case "EUR":
                wsRate = new BigDecimal("0.9150");
                break;
            case "GBP":
                wsRate = new BigDecimal("0.7820");
                break;
            case "JPY":
                wsRate = new BigDecimal("151.2400");
                break;
        }
    }
}
