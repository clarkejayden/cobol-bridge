import java.io.BufferedReader;
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
        char[] inEmpId = line.substring(0, 6).toCharArray();
        char inStatus = line.charAt(6);
        char[] inHours = line.substring(7, 11).toCharArray();
        char[] inRate = line.substring(11, 16).toCharArray();

        int empId = Integer.parseInt(new String(inEmpId));
        char status = inStatus;
        int hours = Integer.parseInt(new String(inHours));
        int rate = Integer.parseInt(new String(inRate));

        int regHours = 0;
        int otHours = 0;
        if (hours > 40) {
            regHours = 40;
            otHours = hours - 40;
        } else {
            regHours = hours;
            otHours = 0;
        }

        BigDecimal gross;
        if (status == 'C') {
            gross = BigDecimal.valueOf(hours * rate);
        } else {
            BigDecimal regAmount = BigDecimal.valueOf(regHours * rate);
            BigDecimal otAmount = BigDecimal.valueOf(otHours * rate * 1.5);
            gross = regAmount.add(otAmount);
        }

        BigDecimal tax = BigDecimal.ZERO;
        if (status != 'C') {
            if (gross.compareTo(BigDecimal.valueOf(500)) <= 0) {
                tax = gross.multiply(BigDecimal.valueOf(0.10));
            } else if (gross.compareTo(BigDecimal.valueOf(1500)) <= 0) {
                tax = BigDecimal.valueOf(50).add(gross.subtract(BigDecimal.valueOf(500)).multiply(BigDecimal.valueOf(0.20)));
            } else {
                tax = BigDecimal.valueOf(250).add(gross.subtract(BigDecimal.valueOf(1500)).multiply(BigDecimal.valueOf(0.30)));
            }
        }

        BigDecimal benefits = BigDecimal.ZERO;
        if (status == 'F') {
            benefits = BigDecimal.valueOf(45.50);
        } else if (status == 'P') {
            benefits = BigDecimal.valueOf(12.25);
        }

        BigDecimal net = gross.subtract(tax).subtract(benefits);

        System.out.println("EMPLOYEE  " + new String(inEmpId).trim() + "  STATUS " + status);
        System.out.println("REG HOURS " + String.format("%3d", regHours));
        System.out.println("OT HOURS  " + String.format("%3d", otHours));
        System.out.println("GROSS     " + new BigDecimal(gross.stripTrailingZeros().toPlainString()));
        System.out.println("TAX       " + new BigDecimal(tax.stripTrailingZeros().toPlainString()));
        System.out.println("BENEFITS  " + new BigDecimal(benefits.stripTrailingZeros().toPlainString()));
        System.out.println("NET PAY  " + new BigDecimal(net.stripTrailingZeros().toPlainString()));
    }
}
