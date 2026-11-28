import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) throws IOException {
        String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (line == null || line.isEmpty()) {
            line = "";
        }
        line = line.concat("                                        ");
        String inAmount = line.substring(0, 9).trim();
        String inFrom = line.substring(9, 12).trim();
        String inTo = line.substring(12, 15).trim();

        if (!isValidCurrency(inFrom) || !isValidCurrency(inTo)) {
            System.out.println("UNSUPPORTED PAIR " + inFrom + "/" + inTo);
            return;
        }

        BigDecimal inAmountBD = new BigDecimal(inAmount);
        BigDecimal fromRate = getRate(inFrom);
        BigDecimal toRate = getRate(inTo);

        BigDecimal usd = inAmountBD.divide(fromRate, 20, RoundingMode.DOWN);
        BigDecimal result;
        if ("JPY".equals(inTo)) {
            result = usd.multiply(toRate).setScale(0, RoundingMode.HALF_UP);
        } else {
            result = usd.multiply(toRate).setScale(0, RoundingMode.HALF_UP);
        }

        System.out.println("FROM " + formatAmount(inAmountBD) + " " + inFrom);
        System.out.println("TO   " + formatAmount(result) + " " + inTo);
        System.out.println("RATE " + formatRate(fromRate) + " " + inTo + " PER USD");

        String whole = formatAmount(result);
        for (int i = 1; i <= 6; i++) {
            BigDecimal noteValue = new BigDecimal(String.valueOf(NOTE_VALUE[i]));
            BigDecimal count = result.divide(noteValue, 0, RoundingMode.DOWN);
            if (count.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("  " + formatAmount(noteValue) + " x " + count);
            }
            result = result.remainder(noteValue);
        }
    }

    private static boolean isValidCurrency(String currency) {
        return "USD".equals(currency) || "EUR".equals(currency) || "GBP".equals(currency) || "JPY".equals(currency);
    }

    private static BigDecimal getRate(String currency) {
        switch (currency) {
            case "USD":
                return BigDecimal.valueOf(1.0000);
            case "EUR":
                return BigDecimal.valueOf(0.9150);
            case "GBP":
                return BigDecimal.valueOf(0.7820);
            case "JPY":
                return BigDecimal.valueOf(151.2400);
            default:
                return BigDecimal.ZERO;
        }
    }

    private static String formatAmount(BigDecimal amount) {
        return Pic.show(amount, "9(7)V99");
    }

    private static String formatRate(BigDecimal rate) {
        return Pic.show(rate, "9(3)V9(4)");
    }

    private static String formatNote(BigDecimal noteValue) {
        return Pic.show(noteValue, "9(3)");
    }
}
