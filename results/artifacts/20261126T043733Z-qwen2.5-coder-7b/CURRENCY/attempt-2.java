import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        String line = new java.io.BufferedReader(
            new java.io.InputStreamReader(System.in)).readLine();
        if (line == null) {
            line = "";
        }
        line = String.format("%-15s", line).substring(0, 15);

        BigDecimal inAmount = new BigDecimal(line.substring(0, 9)).setScale(2, RoundingMode.DOWN);
        String inFrom = line.substring(9, 12).trim();
        String inTo = line.substring(12, 15).trim();

        if (!"USD".equals(inFrom) && !"EUR".equals(inFrom) && !"GBP".equals(inFrom) && !"JPY".equals(inFrom) ||
            !"USD".equals(inTo) && !"EUR".equals(inTo) && !"GBP".equals(inTo) && !"JPY".equals(inTo)) {
            System.out.println("UNSUPPORTED PAIR " + inFrom + "/" + inTo);
            return;
        }

        BigDecimal wsFromRate = new BigDecimal(0);
        BigDecimal wsToRate = new BigDecimal(0);
        BigDecimal wsRate = new BigDecimal(0);
        BigDecimal wsUsd = new BigDecimal(0);
        BigDecimal wsResult = new BigDecimal(0);
        BigDecimal wsWhole = new BigDecimal(0);
        BigDecimal wsCount = new BigDecimal(0);
        BigDecimal wsRem = new BigDecimal(0);
        int wsI = 0;

        BigDecimal[] noteValues = {new BigDecimal(1000), new BigDecimal(500), new BigDecimal(200), new BigDecimal(100), new BigDecimal(50), new BigDecimal(1)};

        BigDecimal outAmt = new BigDecimal(0);
        BigDecimal outRate = new BigDecimal(0);
        BigDecimal outNote = new BigDecimal(0);
        BigDecimal outCount = new BigDecimal(0);

        wsCode = inFrom;
        lookupRate();
        wsFromRate = wsRate;

        wsCode = inTo;
        lookupRate();
        wsToRate = wsRate;

        wsUsd = inAmount.divide(wsFromRate, 20, RoundingMode.DOWN);

        if ("JPY".equals(inTo)) {
            try {
                wsWhole = wsUsd.multiply(wsToRate).setScale(0, RoundingMode.HALF_UP);
            } catch (ArithmeticException e) {
                System.out.println("AMOUNT TOO LARGE TO CONVERT");
                return;
            }
            wsResult = wsWhole;
        } else {
            try {
                wsResult = wsUsd.multiply(wsToRate).setScale(2, RoundingMode.HALF_UP);
            } catch (ArithmeticException e) {
                System.out.println("AMOUNT TOO LARGE TO CONVERT");
                return;
            }
        }

        outAmt = inAmount;
        System.out.println("FROM " + outAmt + " " + inFrom);

        outAmt = wsResult;
        System.out.println("TO   " + outAmt + " " + inTo);

        outRate = wsToRate;
        System.out.println("RATE " + outRate + " " + inTo + " PER USD");

        wsWhole = wsResult;
        for (wsI = 0; wsI < 6; wsI++) {
            wsWhole = wsWhole.divideAndRemainder(noteValues[wsI]);
            wsCount = wsWhole[0];
            wsRem = wsWhole[1];
            if (wsCount.compareTo(BigDecimal.ZERO) > 0) {
                outNote = noteValues[wsI];
                outCount = wsCount;
                System.out.println("  " + outNote + " x " + outCount);
            }
            wsWhole = wsRem;
        }
    }

    private static String wsCode;
    private static void lookupRate() {
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
