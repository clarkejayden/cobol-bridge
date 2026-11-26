import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Main {
    private static final int RECORD_LENGTH = 11;
    private static final Set<String> KNOWN_STATES = Set.of("CA", "NY", "TX", "WA", "OR", "FL", "NV");
    private static final Map<String, BigDecimal> RATES = Map.of(
            "CA", new BigDecimal("7.25"),
            "NY", new BigDecimal("4.00"),
            "TX", new BigDecimal("6.25"),
            "WA", new BigDecimal("6.50"),
            "OR", new BigDecimal("0.00"),
            "FL", new BigDecimal("6.00"));

    public static void main(String[] args) throws Exception {
        String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
        String rec = String.format("%-" + RECORD_LENGTH + "s", line == null ? "" : line);
        String state = rec.substring(0, 2);
        BigDecimal amount = new BigDecimal(rec.substring(2, 10)).movePointLeft(2);
        boolean exempt = rec.charAt(10) == 'Y'

        if (!KNOWN_STATES.contains(state)) {
            System.out.print("INVALID STATE [" + state + "]\n");
            return;
        }
        BigDecimal rate = RATES.get(state);
        if (rate == null) {
            System.out.print("NO RATE ON FILE FOR " + state + "\n");
            return;
        }
        BigDecimal tax = exempt
                ? BigDecimal.ZERO.setScale(2)
                : amount.multiply(rate).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = amount.add(tax);

        System.out.print("STATE    " + state + "  RATE " + edit(rate, 5) + "%\n");
        System.out.print("SUBTOTAL " + edit(amount, 10) + "\n");
        System.out.print("TAX      " + edit(tax, 10) + (exempt ? " (EXEMPT)" : "") + "\n");
        System.out.print("TOTAL  " + edit(total, 12) + "\n");
    }

    /** Z,ZZZ,ZZ9.99-style editing: grouped digits, two decimals, right-aligned. */
    private static String edit(BigDecimal value, int width) {
        String digits = String.format(Locale.ROOT, "%,.2f", value.setScale(2, RoundingMode.DOWN));
        return String.format("%" + width + "s", digits);
    }
}
