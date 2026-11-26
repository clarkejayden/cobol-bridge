import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (line.length() < 58) {
            line += " ";
        }
        scanner.close();

        String inAccount = line.substring(0, 8).trim();
        BigDecimal inOpening = new BigDecimal(line.substring(8, 18).trim()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal[] inTxn = new BigDecimal[5];
        for (int i = 0; i < 5; i++) {
            inTxn[i] = new BigDecimal(line.substring(18 + i * 8, 26 + i * 8).trim()).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal wsBalance = inOpening;
        BigDecimal wsCredits = BigDecimal.ZERO;
        BigDecimal wsDebits = BigDecimal.ZERO;
        int wsI = 0;
        BigDecimal outTxn = BigDecimal.ZERO;
        BigDecimal outBal = BigDecimal.ZERO;
        BigDecimal outCrdb = BigDecimal.ZERO;

        System.out.println("ACCOUNT " + inAccount);
        System.out.println("OPENING " + inOpening);

        for (wsI = 0; wsI < 5; wsI++) {
            outTxn = inTxn[wsI];
            if (inTxn[wsI].compareTo(BigDecimal.ZERO) < 0) {
                wsDebits = wsDebits.add(inTxn[wsI]);
            } else {
                wsCredits = wsCredits.add(inTxn[wsI]);
            }
            wsBalance = wsBalance.add(inTxn[wsI]);
            outBal = wsBalance;
            System.out.println("TXN " + (wsI + 1) + " " + outTxn + " BAL " + outBal);
        }

        outCrdb = wsCredits;
        System.out.println("CREDITS " + outCrdb);
        outCrdb = wsDebits;
        System.out.println("DEBITS  " + outCrdb);
        outCrdb = wsBalance;
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
