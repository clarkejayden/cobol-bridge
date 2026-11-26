import java.io.BufferedReader;
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
        line = line.concat("                                        ");
        String inAmount = line.substring(0, 9).trim();
        String inFrom = line.substring(9, 12).trim();
        String inTo = line.substring(12, 15).trim();

        if (!isValidCurrency(inFrom) || !isValidCurrency(inTo)) {
            System.out.println("UNSUPPORTED PAIR " + inFrom + "/" + inTo);
            System.exit(0);
        }

        BigDecimal inAmountBD = new BigDecimal(inAmount);
        BigDecimal fromRate = getRate(inFrom);
        BigDecimal toRate = getRate(inTo);

        BigDecimal usd = inAmountBD.divide(fromRate, 20, RoundingMode.DOWN);
        BigDecimal result;
        if (inTo.equals("JPY")) {
            result = usd.multiply(toRate).setScale(0, RoundingMode.HALF_UP);
        } else {
            result = usd.multiply(toRate).setScale(2, RoundingMode.HALF_UP);
        }

        System.out.println("FROM " + inAmount + " " + inFrom);
        System.out.println("TO   " + result + " " + inTo);
        System.out.println("RATE " + toRate + " " + inTo + " PER USD");

        String noteData = "100050020010005001";
        for (int i = 1; i <= 6; i++) {
            BigDecimal noteValue = new BigDecimal(noteData.substring((i - 1) * 3, i * 3));
            BigDecimal whole = result.divide(noteValue, 20, RoundingMode.DOWN);
            BigDecimal count, rem;
            if (whole.compareTo(BigDecimal.ZERO) > 0) {
                result = result.remainder(noteValue);
                count = result.divide(noteValue, 20, RoundingMode.DOWN);
                rem = result.remainder(noteValue);
                if (count.compareTo(BigDecimal.ZERO) > 0) {
                    System.out.println("  " + noteValue + " x " + count);
                }
                result = rem;
            }
        }
    }

    private static boolean isValidCurrency(String currency) {
        return currency.equals("USD") || currency.equals("EUR") || currency.equals("GBP") || currency.equals("JPY");
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
}
