import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        line = String.format("%-58s", line).replace(' ', ' ');
        scanner.close();

        String inAccount = line.substring(0, 8);
        BigDecimal inOpening = new BigDecimal(line.substring(8, 18).substring(1)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal[] inTxn = new BigDecimal[5];
        for (int i = 0; i < 5; i++) {
            inTxn[i] = new BigDecimal(line.substring(18 + i * 8, 26 + i * 8).substring(1)).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal wsBalance = inOpening;
        BigDecimal wsCredits = BigDecimal.ZERO;
        BigDecimal wsDebits = BigDecimal.ZERO;
        int wsI = 0;

        System.out.println("ACCOUNT " + inAccount);
        System.out.println("OPENING " + inOpening);

        for (BigDecimal txn : inTxn) {
            wsI++;
            if (txn.compareTo(BigDecimal.ZERO) < 0) {
                wsDebits = wsDebits.add(txn);
            } else {
                wsCredits = wsCredits.add(txn);
            }
            wsBalance = wsBalance.add(txn);
            System.out.println("TXN " + wsI + " " + txn + " BAL " + wsBalance);
        }

        System.out.println("CREDITS " + wsCredits);
        System.out.println("DEBITS  " + wsDebits);

        BigDecimal outCrdb = wsBalance;
        System.out.println("CLOSING " + outCrdb + "CR");

        if (wsBalance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("STATUS  OVERDRAWN");
        } else if (wsBalance.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("STATUS  ZERO");
        } else {
            System.out.println("STATUS  IN CREDIT");
        }
    }
}
