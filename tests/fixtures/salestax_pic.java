import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class Main {
    private static final Set<String> KNOWN_STATES = Set.of("CA", "NY", "TX", "WA", "OR", "FL", "NV");
    private static final Map<String, String> RATES = Map.of(
            "CA", "0725", "NY", "0400", "TX", "0625", "WA", "0650", "OR", "0000", "FL", "0600");

    public static void main(String[] args) throws Exception {
        String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
        String rec = Pic.show(line == null ? "" : line, "X(11)");
        String state = rec.substring(0, 2);
        BigDecimal amount = Pic.num(rec.substring(2, 10), "9(6)V99");
        boolean exempt = rec.charAt(10) == 'Y';

        if (!KNOWN_STATES.contains(state)) {
            System.out.print("INVALID STATE [" + Pic.show(state, "XX") + "]\n");
            return;
        }
        if (!RATES.containsKey(state)) {
            System.out.print("NO RATE ON FILE FOR " + state + "\n");
            return;
        }
        BigDecimal rate = Pic.num(RATES.get(state), "99V99");
        BigDecimal tax = exempt
                ? Pic.fit(BigDecimal.ZERO, "9(6)V99")
                : Pic.fit(amount.multiply(rate).divide(new BigDecimal(100)), "9(6)V99", true);
        BigDecimal total = Pic.fit(amount.add(tax), "9(7)V99");

        System.out.print("STATE    " + state + "  RATE " + Pic.show(rate, "Z9.99") + "%\n");
        System.out.print("SUBTOTAL " + Pic.show(amount, "ZZZ,ZZ9.99") + "\n");
        System.out.print("TAX      " + Pic.show(tax, "ZZZ,ZZ9.99") + (exempt ? " (EXEMPT)" : "") + "\n");
        System.out.print("TOTAL  " + Pic.show(total, "Z,ZZZ,ZZ9.99") + "\n");
    }
}
