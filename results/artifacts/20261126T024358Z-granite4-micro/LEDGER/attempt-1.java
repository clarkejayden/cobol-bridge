import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        // Read input line and pad to required length
        String line = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in)).readLine();
        if (line == null || line.isEmpty()) {
            line = "                                                                                ";
        }

        // Define offsets and lengths for fields
        int accountOffset = 0;
        int accountLength = 8;
        int openingOffset = 8;
        int openingLength = 10;
        int txnOffsets[] = {26, 34, 42, 50, 58}; // 5 transactions
        int txnLength = 8;

        // Extract fields
        String account = line.substring(accountOffset, accountOffset + accountLength);
        BigDecimal opening = new BigDecimal(line.substring(openingOffset, openingOffset + openingLength));
        BigDecimal[] transactions = new BigDecimal[5];
        for (int i = 0; i < 5; i++) {
            transactions[i] = new BigDecimal(line.substring(txnOffsets[i], txnOffsets[i] + txnLength));
        }

        // Initialize working storage
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal credits = BigDecimal.ZERO;
        BigDecimal debits = BigDecimal.ZERO;
        int i = 0;

        // Process each transaction
        for (BigDecimal txn : transactions) {
            if (txn.compareTo(BigDecimal.ZERO) < 0) {
                debits = debits.add(txn);
            } else {
                credits = credits.add(txn);
            }
            balance = balance.add(txn);

            // Prepare output for current transaction
            BigDecimal formattedTxn = txn.setScale(5, RoundingMode.HALF_UP);
            BigDecimal formattedBalance = balance.setScale(5, RoundingMode.HALF_UP);

            // Display transaction, balance, and formatted values
            System.out.println("TXN " + (i + 1) + " " + formattedTxn + " BAL " + formattedBalance);

            i++;
        }

        // Prepare output for credits, debits, and closing balance
        BigDecimal formattedCredits = credits.setScale(5, RoundingMode.HALF_UP);
        BigDecimal formattedDebits = debits.setScale(5, RoundingMode.HALF_UP);
        BigDecimal formattedClosing = balance.setScale(5, RoundingMode.HALF_UP);

        // Display credits
        System.out.println("CREDITS " + formattedCredits);

        // Display debits
        System.out.println("DEBITS  " + formattedDebits);

        // Display closing balance and status
        System.out.println("CLOSING " + formattedClosing);
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("STATUS  OVERDRAWN");
        } else if (balance.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("STATUS  ZERO");
        } else {
            System.out.println("STATUS  IN CREDIT");
        }
    }
}
