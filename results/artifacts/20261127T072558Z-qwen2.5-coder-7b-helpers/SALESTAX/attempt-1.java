import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        line = String.format("%-11s", line).substring(0, 11);

        String inState = line.substring(0, 2);
        BigDecimal inAmount = Pic.num(line.substring(2, 10), "9(6)V99");
        String inExempt = line.substring(10, 11);

        BigDecimal wsTax = BigDecimal.ZERO;
        BigDecimal wsTotal = BigDecimal.ZERO;
        BigDecimal outAmt = BigDecimal.ZERO;
        BigDecimal outTotal = BigDecimal.ZERO;
        BigDecimal outPct = BigDecimal.ZERO;

        if (!"CA".equals(inState) && !"NY".equals(inState) && !"TX".equals(inState) && !"WA".equals(inState) && !"OR".equals(inState) && !"FL".equals(inState) && !"NV".equals(inState)) {
            System.out.println("INVALID STATE [" + inState + "]");
            return;
        }

        BigDecimal ratePct = BigDecimal.ZERO;
        for (int i = 0; i < 6; i++) {
            String state = line.substring(i * 6, i * 6 + 2);
            if (state.equals(inState)) {
                ratePct = Pic.num(line.substring(i * 6 + 2, i * 6 + 6), "99V99");
                break;
            }
        }

        if (ratePct.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("NO RATE ON FILE FOR " + inState);
            return;
        }

        if ("Y".equals(inExempt)) {
            wsTax = BigDecimal.ZERO;
        } else {
            wsTax = inAmount.multiply(ratePct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        wsTotal = inAmount.add(wsTax);
        outPct = ratePct;
        outAmt = inAmount;
        System.out.println("STATE    " + inState + "  RATE " + Pic.show(outPct, "Z9.99") + "%");
        System.out.println("SUBTOTAL " + Pic.show(outAmt, "ZZZ,ZZ9.99"));
        if ("Y".equals(inExempt)) {
            System.out.println("TAX      " + Pic.show(wsTax, "ZZZ,ZZ9.99") + " (EXEMPT)");
        } else {
            System.out.println("TAX      " + Pic.show(wsTax, "ZZZ,ZZ9.99"));
        }
        outTotal = wsTotal;
        System.out.println("TOTAL  " + Pic.show(outTotal, "Z,ZZZ,ZZ9.99"));
    }
}
