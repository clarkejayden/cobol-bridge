import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            line = "";
        }
        line = line.concat("                                        ").substring(0, 58);

        char[] inAccount = new char[8];
        System.arraycopy(line.toCharArray(), 0, inAccount, 0, 8);
        String inAccountStr = new String(inAccount);

        char[] inOpening = new char[10];
        System.arraycopy(line.toCharArray(), 8, inOpening, 0, 10);
        BigDecimal inOpening = new BigDecimal(new String(inOpening));

        char[] inTxn = new char[40];
        System.arraycopy(line.toCharArray(), 18, inTxn, 0, 40);
        BigDecimal[] inTxnArray = new BigDecimal[5];
        for (int i = 0; i < 5; i++) {
            inTxnArray[i] = new BigDecimal(new String(inTxn, i * 8, 8));
        }

        BigDecimal wsBalance = inOpening;
        BigDecimal wsCredits = BigDecimal.ZERO;
        BigDecimal wsDebits = BigDecimal.ZERO;

        for (int i = 0; i < 5; i++) {
            if (inTxnArray[i].compareTo(BigDecimal.ZERO) < 0) {
                wsDebits = wsDebits.add(inTxnArray[i]);
            } else {
                wsCredits = wsCredits.add(inTxnArray[i]);
            }
            wsBalance = wsBalance.add(inTxnArray[i]);
            String outTxn = String.format("%+5,2d", i + 1);
            String outBal = String.format("%,2d", wsBalance.stripTrailingZeros().scaleByPowerOfTen(-2).intValue());
            System.out.println("TXN " + outTxn + " BAL " + outBal);
        }

        String outCrdb = String.format("%,2d", wsCredits.stripTrailingZeros().scaleByPowerOfTen(-2).intValue());
        System.out.println("CREDITS " + outCrdb);
        String outDebits = String.format("%,2d", wsDebits.stripTrailingZeros().scaleByPowerOfTen(-2).intValue());
        System.out.println("DEBITS  " + outDebits);
        String outBal = String.format("%,2d", wsBalance.stripTrailingZeros().scaleByPowerOfTen(-2).intValue());
        System.out.println("CLOSING " + outBal);

        if (wsBalance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("STATUS  OVERDRAWN");
        } else if (wsBalance.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("STATUS  ZERO");
        } else {
            System.out.println("STATUS  IN CREDIT");
        }
    }
}
