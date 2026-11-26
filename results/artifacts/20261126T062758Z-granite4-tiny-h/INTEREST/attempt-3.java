import java.io.BufferedReader;
import java.io.IOException;
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
        line = line.concat("                                        ");
        int IN_PRINCIPAL_OFFSET = 0;
        int IN_PRINCIPAL_LENGTH = 9;
        int IN_RATE_OFFSET = 9;
        int IN_RATE_LENGTH = 6;
        int IN_YEARS_OFFSET = 15;
        int IN_YEARS_LENGTH = 2;

        int IN_PRINCIPAL = Integer.parseInt(line.substring(IN_PRINCIPAL_OFFSET, IN_PRINCIPAL_OFFSET + IN_PRINCIPAL_LENGTH).trim());
        int IN_RATE = Integer.parseInt(line.substring(IN_RATE_OFFSET, IN_RATE_OFFSET + IN_RATE_LENGTH).trim());
        int IN_YEARS = Integer.parseInt(line.substring(IN_YEARS_OFFSET, IN_YEARS_OFFSET + IN_YEARS_LENGTH).trim());

        BigDecimal simpleInterest = BigDecimal.valueOf(IN_PRINCIPAL).multiply(BigDecimal.valueOf(IN_RATE)).divide(BigDecimal.valueOf(100), IN_YEARS, RoundingMode.HALF_UP);
        System.out.println("PRINCIPAL " + simpleInterest);
        BigDecimal balance = BigDecimal.valueOf(IN_PRINCIPAL);
        boolean overflowed = false;

        for (int year = 1; year <= IN_YEARS && !overflowed; year++) {
            BigDecimal interest = balance.multiply(BigDecimal.valueOf(IN_RATE)).divide(BigDecimal.valueOf(100), 20, RoundingMode.HALF_UP);
            balance = balance.add(interest);
            if (balance.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
                overflowed = true;
            }
            System.out.println("YEAR " + year + "   " + balance);
        }

        System.out.println("COMPOUND " + balance);

        reader.close();
    }
}
