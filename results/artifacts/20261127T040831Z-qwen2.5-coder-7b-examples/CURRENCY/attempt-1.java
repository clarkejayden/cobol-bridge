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

        if (!"USD EUR GBP JPY".contains(inFrom + " " + inTo)) {
            System.out.println("UNSUPPORTED PAIR " + inFrom + "/" + inTo);
            return;
        }

        BigDecimal fromRate = getRate(inFrom);
        BigDecimal toRate = getRate(inTo);

        BigDecimal usdAmount = inAmount.divide(fromRate, 4, RoundingMode.DOWN);
        BigDecimal result;
        if ("JPY".equals(inTo)) {
            result = usdAmount.multiply(toRate).setScale(0, RoundingMode.HALF_UP);
        } else {
            result = usdAmount.multiply(toRate).setScale(2, RoundingMode.HALF_UP);
        }

        if (result.scale() > 2) {
            System.out.println("AMOUNT TOO LARGE TO CONVERT");
            return;
        }

        System.out.println("FROM " + inAmount + " " + inFrom);
        System.out.println("TO   " + result + " " + inTo);
        System.out.println("RATE " + toRate + " " + inTo + " PER USD");

        BigDecimal whole = result.setScale(0, RoundingMode.DOWN);
        BigDecimal[] notes = {new BigDecimal(1000), new BigDecimal(500), new BigDecimal(200), new BigDecimal(100), new BigDecimal(50), new BigDecimal(1)};
        for (int i = 0; i < notes.length; i++) {
            BigDecimal count = whole.divide(notes[i], 0, RoundingMode.DOWN);
            if (count.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("  " + notes[i] + " x " + count);
            }
            whole = whole.remainder(notes[i]);
        }
    }

    private static BigDecimal getRate(String currency) {
        switch (currency) {
            case "USD":
                return new BigDecimal("1.0000");
            case "EUR":
                return new BigDecimal("0.9150");
            case "GBP":
                return new BigDecimal("0.7820");
            case "JPY":
                return new BigDecimal("151.2400");
            default:
                throw new IllegalArgumentException("Invalid currency: " + currency);
        }
    }
}
