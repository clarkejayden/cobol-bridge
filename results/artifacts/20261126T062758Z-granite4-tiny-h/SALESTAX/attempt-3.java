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
        line = line.concat(" ".repeat(11));

        char inState[] = line.substring(0, 2).toCharArray();
        char inAmount[] = line.substring(2, 10).toCharArray();
        char inExempt[] = line.substring(10, 11).toCharArray();

        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < 6; i++) {
            String state = new String(new char[]{inState[0], inState[1]});
            BigDecimal rate = new BigDecimal(new String(new char[]{inAmount[0], inAmount[1], inAmount[2], inAmount[3], inAmount[4], inAmount[5]})).movePointRight(2);

            if (state.equals("CA") || state.equals("NY") || state.equals("TX") || state.equals("WA") || state.equals("OR") || state.equals("FL")) {
                if (state.equals("CA") || state.equals("NY") || state.equals("TX") || state.equals("WA") || state.equals("OR") || state.equals("FL")) {
                    if (inExempt[0] == 'Y') {
                        tax = BigDecimal.ZERO;
                    } else {
                        tax = inAmount.movePointLeft(2).multiply(rate).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
                    }
                    total = inAmount.movePointLeft(2).add(tax);
                }
            } else {
                System.out.println("NO RATE ON FILE FOR " + new String(inState));
                return;
            }
        }

        System.out.println("STATE    " + new String(inState) + "  RATE " + rate.movePointRight(2).stripTrailingZeros().toPlainString() + "%");
        System.out.println("SUBTOTAL " + total.stripTrailingZeros().toPlainString());
        System.out.println("TAX      " + tax.stripTrailingZeros().toPlainString() + (inExempt[0] == 'Y' ? " (EXEMPT)" : ""));
        System.out.println("TOTAL  " + total.stripTrailingZeros().toPlainString());
    }
}
