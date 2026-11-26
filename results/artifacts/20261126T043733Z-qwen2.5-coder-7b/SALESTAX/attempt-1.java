import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (line.length() < 11) {
            line += " ";
        }
        String inState = line.substring(0, 2);
        BigDecimal inAmount = new BigDecimal(line.substring(2, 10)).setScale(2, RoundingMode.HALF_UP);
        char inExempt = line.charAt(10);

        BigDecimal wsTax = BigDecimal.ZERO;
        BigDecimal wsTotal = BigDecimal.ZERO;
        BigDecimal outAmt = BigDecimal.ZERO;
        BigDecimal outTotal = BigDecimal.ZERO;
        BigDecimal outPct = BigDecimal.ZERO;

        String[] rateData = {"CA0725", "NY0400", "TX0625", "WA0650", "OR0000", "FL0600"};
        String[] rateStates = {"CA", "NY", "TX", "WA", "OR", "FL"};
        BigDecimal[] ratePcts = {
            new BigDecimal("0.0725"),
            new BigDecimal("0.0400"),
            new BigDecimal("0.0625"),
            new BigDecimal("0.0650"),
            new BigDecimal("0.0000"),
            new BigDecimal("0.0600")
        };

        boolean knownState = false;
        for (String state : rateStates) {
            if (state.equals(inState)) {
                knownState = true;
                break;
            }
        }

        if (!knownState) {
            System.out.println("INVALID STATE [" + inState + "]");
            return;
        }

        int rateIndex = -1;
        for (int i = 0; i < rateStates.length; i++) {
            if (rateStates[i].equals(inState)) {
                rateIndex = i;
                break;
            }
        }

        if (rateIndex == -1) {
            System.out.println("NO RATE ON FILE FOR " + inState);
            return;
        }

        if (inExempt == 'Y') {
            wsTax = BigDecimal.ZERO;
        } else {
            wsTax = inAmount.multiply(ratePcts[rateIndex]).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        wsTotal = inAmount.add(wsTax);
        outPct = ratePcts[rateIndex];
        outAmt = inAmount;
        System.out.println("STATE    " + inState + "  RATE " + outPct.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) + "%");
        System.out.println("SUBTOTAL " + outAmt.setScale(2, RoundingMode.HALF_UP));
        if (inExempt == 'Y') {
            System.out.println("TAX      " + wsTax.setScale(2, RoundingMode.HALF_UP) + " (EXEMPT)");
        } else {
            System.out.println("TAX      " + wsTax.setScale(2, RoundingMode.HALF_UP));
        }
        outTotal = wsTotal;
        System.out.println("TOTAL  " + outTotal.setScale(2, RoundingMode.HALF_UP));
    }
}
