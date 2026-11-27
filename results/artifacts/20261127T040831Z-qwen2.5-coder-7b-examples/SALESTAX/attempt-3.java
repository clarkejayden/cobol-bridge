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
        for (int i = 0; i < rateStates.length; i++) {
            if (rateStates[i].equals(inState)) {
                knownState = true;
                wsTax = inAmount.multiply(ratePcts[i]).setScale(2, RoundingMode.HALF_UP);
                wsTotal = inAmount.add(wsTax).setScale(2, RoundingMode.HALF_UP);
                outPct = ratePcts[i].setScale(2, RoundingMode.HALF_UP);
                outAmt = inAmount.setScale(2, RoundingMode.HALF_UP);
                outTotal = wsTotal.setScale(2, RoundingMode.HALF_UP);
                break;
            }
        }

        if (!knownState) {
            System.out.println("INVALID STATE [" + inState + "]");
        } else if (wsTax.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("NO RATE ON FILE FOR " + inState);
        } else {
            System.out.printf("STATE    %-2s  RATE %5s%%\n", inState, outPct.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            System.out.printf("SUBTOTAL %10s\n", outAmt);
            if (inExempt == 'Y') {
                System.out.printf("TAX      %10s (EXEMPT)\n", wsTax);
            } else {
                System.out.printf("TAX      %10s\n", wsTax);
            }
            System.out.printf("TOTAL  %10s\n", outTotal);
        }
    }
}
