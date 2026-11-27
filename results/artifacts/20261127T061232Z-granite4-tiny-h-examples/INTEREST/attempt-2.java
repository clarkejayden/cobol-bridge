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
        line = line.concat("                                        ").substring(0, 17);

        char[] inPrincipal = line.substring(0, 9).toCharArray();
        char[] inRate = line.substring(9, 15).toCharArray();
        char[] inYears = line.substring(15, 17).toCharArray();

        int principal = 0;
        for (char c : inPrincipal) {
            if (c != ' ') {
                principal = principal * 10 + (c - '0');
            }
        }
        principal = principal * 100 + (inPrincipal[8] - '0') * 10 + (inPrincipal[7] - '0');

        int rate = 0;
        for (char c : inRate) {
            if (c != ' ') {
                rate = rate * 10 + (c - '0');
            }
        }
        rate = rate * 10 + (inRate[4] - '0') * 10 + (inRate[3] - '0');

        int years = 0;
        for (char c : inYears) {
            if (c != ' ') {
                years = years * 10 + (c - '0');
            }
        }

        BigDecimal simpleInterest = new BigDecimal(principal).multiply(new BigDecimal(rate)).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(years));
        System.out.println("PRINCIPAL " + new BigDecimal(principal).divide(newBigDecimal(100).pow(2).multiply(new BigDecimal(years)), 2, RoundingMode.HALF_UP)));
        System.out.println("RATE      " + new BigDecimal(rate).divide(newBigDecimal(100).pow(2), 4, RoundingMode.HALF_UP)));
        System.out.println("YEARS     " + years);

        BigDecimal balance = new BigDecimal(principal);
        for (int year = 1; year <= years && balance.scale() <= 2; year++) {
            BigDecimal interest = balance.multiply(new BigDecimal(rate)).divide(new BigDecimal(100), 20, RoundingMode.HALF_UP);
            balance = balance.add(interest);
            if (balance.scale() > 2) {
                System.out.println("OVERFLOW IN YEAR " + year);
            } else {
                System.out.println("YEAR " + year + "   " + balance.setScale(2, RoundingMode.HALF_UP));
            }
        }

        BigDecimal compoundInterest = balance;
        System.out.println("COMPOUND  " + compoundInterest.setScale(2, RoundingMode.HALF_UP));

        reader.close();
    }
}
