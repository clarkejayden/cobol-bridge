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
        char[] inEmpId = line.substring(0, 6).toCharArray();
        char inStatus = line.charAt(6);
        char[] inHours = line.substring(7, 11).toCharArray();
        char[] inRate = line.substring(11, 16).toCharArray();

        int empId = (int) inEmpId[0] - 48;
        char status = inStatus;
        int hours = (int) inHours[0] - 48;
        hours = hours * 10 + ((int) inHours[1] - 48);
        hours = hours * 10 + ((int) inHours[2] - 48);
        hours = hours + ((int) inHours[3] - 48) / 10.0;
        double rate = (inRate[0] - 48) * 10.0 + (inRate[1] - 48);

        BigDecimal regHours = BigDecimal.ZERO;
        BigDecimal otHours = BigDecimal.ZERO;
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal benefits = BigDecimal.ZERO;
        BigDecimal net = BigDecimal.ZERO;

        if (status != 'F' && status != 'P' && status != 'C') {
            System.out.println("EMPLOYEE " + empId + " REJECTED: BAD STATUS [" + status + "]");
            System.exit(0);
        }

        if (hours > 40) {
            regHours = BigDecimal.valueOf(40);
            otHours = BigDecimal.valueOf(hours - 40);
        } else {
            regHours = BigDecimal.valueOf(hours);
            otHours = BigDecimal.ZERO;
        }

        if (status == 'C') {
            gross = regHours.multiply(BigDecimal.valueOf(rate));
        } else {
            gross = regHours.multiply(BigDecimal.valueOf(rate))
                    .add(otHours.multiply(BigDecimal.valueOf(rate * 1.5)));
        }

        if (status == 'C') {
            tax = BigDecimal.ZERO;
        } else {
            if (gross.compareTo(BigDecimal.valueOf(500)) <= 0) {
                tax = gross.multiply(BigDecimal.valueOf(0.10));
            } else if (gross.compareTo(BigDecimal.valueOf(1500)) <= 0) {
                tax = BigDecimal.valueOf(50)
                        .add(gross.subtract(BigDecimal.valueOf(500))
                                .multiply(BigDecimal.valueOf(0.20)));
            } else {
                tax = BigDecimal.valueOf(250)
                        .add(gross.subtract(BigDecimal.valueOf(1500))
                                .multiply(BigDecimal.valueOf(0.30)));
            }
        }

        if (status == 'F') {
            benefits = BigDecimal.valueOf(45.50);
        } else if (status == 'P') {
            benefits = BigDecimal.valueOf(12.25);
        }

        net = gross.subtract(tax).subtract(benefits);

        System.out.println("EMPLOYEE  " + empId);
        System.out.println("REG HOURS " + regHours.setScale(0, RoundingMode.HALF_UP));
        System.out.println("OT HOURS  " + otHours.setScale(0, RoundingMode.HALF_UP));
        System.out.println("GROSS     " + gross.setScale(2, RoundingMode.HALF_UP));
        System.out.println("TAX       " + tax.setScale(2, RoundingMode.HALF_UP));
        System.out.println("BENEFITS  " + benefits.setScale(2, RoundingMode.HALF_UP));
        System.out.println("NET PAY  " + net.setScale(2, RoundingMode.HALF_UP));
    }
}
