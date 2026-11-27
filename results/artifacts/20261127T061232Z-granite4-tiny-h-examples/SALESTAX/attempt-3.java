import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        if (line == null || line.isEmpty()) {
            line = "";
        }
        line = line.concat("                                        ").substring(0, 25);

        char inState = line.charAt(0);
        char inExempt = line.charAt(10);
        int inAmount = Integer.parseInt(line.substring(2, 10));
        BigDecimal taxRate = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        switch (inState) {
            case 'C':
                if (line.substring(2, 6).equals("CA0725")) {
                    taxRate = BigDecimal.valueOf(7, 2);
                } else {
                    System.out.println("NO RATE ON FILE FOR " + inState);
                    return;
                }
                break;
            case 'N':
                if (line.substring(2, 6).equals("NY0400")) {
                    taxRate = BigDecimal.valueOf(4, 2);
                } else {
                    System.out.println("NO RATE ON FILE FOR " + inState);
                    return;
                }
                break;
            case 'T':
                if (line.substring(2, 6).equals("TX0625")) {
                    taxRate = BigDecimal.valueOf(6, 2);
                } else {
                    System.out.println("NO RATE ON FILE FOR " + inState);
                    return;
                }
                break;
            case 'W':
                if (line.substring(2, 6).equals("WA0650")) {
                    taxRate = BigDecimal.valueOf(6, 2);
                } else {
                    System.out.println("NO RATE ON FILE FOR " + inState);
                    return;
                }
                break;
            case 'O':
                if (line.substring(2, 6).equals("OR0000")) {
                    taxRate = BigDecimal.valueOf(0, 2);
                } else {
                    System.out.println("NO RATE ON FILE FOR " + inState);
                    return;
                }
                break;
            case 'F':
                if (line.substring(2, 6).equals("FL0600")) {
                    taxRate = BigDecimal.valueOf(6, 2);
                } else {
                    System.out.println("NO RATE ON FILE FOR " + inState);
                    return;
                }
                break;
            default:
                System.out.println("INVALID STATE [" + inState + "]");
                return;
        }

        if (inExempt == 'Y') {
            tax = BigDecimal.ZERO;
        } else {
            tax = taxRate.multiply(new BigDecimal(inAmount)).divide(new BigDecimal(100), 20, RoundingMode.HALF_UP);
        }

        total = new BigDecimal(inAmount).add(tax);

        System.out.printf("STATE    %-4s  RATE %.2f%%%n", inState, taxRate.doubleValue());
        System.out.printf("SUBTOTAL %7.2f%n", new BigDecimal(inAmount));
        System.out.printf("TAX      %7.2f%n", tax);
        System.out.printf("TOTAL  %7.2f%n", total);
    }
}
