import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        line = String.format("%-58s", line).substring(0, 58);

        String inAccount = line.substring(0, 8);
        BigDecimal inOpening = Pic.num(line.substring(8, 18), "9(7)V99");
        BigDecimal[] inTxn = new BigDecimal[5];
        for (int i = 0; i < 5; i++) {
            inTxn[i] = Pic.num(line.substring(18 + i * 8, 26 + i * 8), "9(5)V99");
        }

        BigDecimal wsBalance = inOpening;
        BigDecimal wsCredits = BigDecimal.ZERO;
        BigDecimal wsDebits = BigDecimal.ZERO;
        int wsI = 0;

        for (BigDecimal txn : inTxn) {
            wsI++;
            if (txn.compareTo(BigDecimal.ZERO) < 0) {
                wsDebits = wsDebits.add(txn);
            } else {
                wsCredits = wsCredits.add(txn);
            }
            wsBalance = wsBalance.add(txn);
            String outTxn = Pic.show(txn, "+ZZ,ZZ9.99");
            String outBal = Pic.show(wsBalance, "-ZZZ,ZZZ,ZZ9.99");
            System.out.println("TXN " + wsI + " " + outTxn + " BAL " + outBal);
        }

        String outCrdb = Pic.show(wsCredits, "ZZZ,ZZZ,ZZ9.99CR");
        System.out.println("CREDITS " + outCrdb);
        outCrdb = Pic.show(wsDebits, "ZZZ,ZZZ,ZZ9.99CR");
        System.out.println("DEBITS  " + outCrdb);
        outCrdb = Pic.show(wsBalance, "ZZZ,ZZZ,ZZ9.99CR");
        System.out.println("CLOSING " + outCrdb);

        if (wsBalance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("STATUS  OVERDRAWN");
        } else if (wsBalance.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("STATUS  ZERO");
        } else {
            System.out.println("STATUS  IN CREDIT");
        }
    }
}
