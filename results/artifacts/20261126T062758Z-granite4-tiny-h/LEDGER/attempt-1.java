import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            line = "";
        }
        line = line.concat("                                        ");
        char[] inAccount = line.substring(0, 8).toCharArray();
        char[] inOpening = line.substring(8, 18).toCharArray();
        char[] inTxn = new char[40];
        System.arraycopy(line.substring(18, 26).toCharArray(), 0, inTxn, 0, 8);
        System.arraycopy(line.substring(26, 34).toCharArray(), 0, inTxn, 8, 8);
        System.arraycopy(line.substring(34, 42).toCharArray(), 0, inTxn, 16, 8);
        System.arraycopy(line.substring(42, 50).toCharArray(), 0, inTxn, 24, 8);
        System.arraycopy(line.substring(50, 58).toCharArray(), 0, inTxn, 32, 8);

        BigDecimal wsBalance = new BigDecimal(inOpening);
        BigDecimal wsCredits = BigDecimal.ZERO;
        BigDecimal wsDebits = BigDecimal.ZERO;
        int wsI = 1;

        for (char[] txn : new char[][]{inTxn, inTxn, inTxn, inTxn, inTxn}) {
            BigDecimal txnAmount = new BigDecimal(new String(txn));
            if (txnAmount.signum() < 0) {
                wsDebits = wsDebits.add(txnAmount);
            } else {
                wsCredits = wsCredits.add(txnAmount);
            }
            wsBalance = wsBalance.add(txnAmount);
            String outTxn = String.format("+%8s", txnAmount.stripTrailingZeros());
            String outBal = String.format("-%9s", wsBalance.stripTrailingZeros());
            System.out.printf("TXN %d %s BAL %s%n", wsI++, outTxn, outBal);
        }

        String outCrdb = String.format("+%9s", wsCredits.stripTrailingZeros());
        System.out.printf("CREDITS %s%n", outCrdb);
        outCrdb = String.format("-%9s", wsDebits.stripTrailingZeros());
        System.out.printf("DEBITS  %s%n", outCrdb);
        String outBal = String.format("-%9s", wsBalance.stripTrailingZeros());
        System.out.printf("CLOSING %s%n", outBal);

        if (wsBalance.signum() < 0) {
            System.out.println("STATUS  OVERDRAWN");
        } else if (wsBalance.signum() == 0) {
            System.out.println("STATUS  ZERO");
        } else {
            System.out.println("STATUS  IN CREDIT");
        }
    }
}
