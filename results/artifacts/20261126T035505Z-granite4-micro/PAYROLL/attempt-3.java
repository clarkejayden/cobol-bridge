import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        // Read input line
        String line = new Scanner(System.in).nextLine();

        // Slice fields by offsets
        String inEmpId = line.substring(0, 6).trim();
        String inStatus = line.substring(6, 7).trim();
        String inHours = line.substring(7, 11).trim();
        String inRate = line.substring(11, 16).trim();

        // Convert input fields to BigDecimal
        BigDecimal wsRegHours = new BigDecimal("0");
        BigDecimal wsOtHours = new BigDecimal("0");
        BigDecimal wsGross = new BigDecimal("0");
        BigDecimal wsTax = new BigDecimal("0");
        BigDecimal wsBenefits = new BigDecimal("0");
        BigDecimal wsNet = new BigDecimal("0");

        BigDecimal outHours = new BigDecimal("0");
        BigDecimal outAmt = new BigDecimal("0");
        BigDecimal outNet = new BigDecimal("0");

        // Process input
        if (!inStatus.equals("F") && !inStatus.equals("P") && !inStatus.equals("C")) {
            System.out.println("EMPLOYEE " + inEmpId + " REJECTED: BAD STATUS [" + inStatus + "]");
            return;
        }

        // Split hours worked into regular and overtime
        BigDecimal hoursWorked = new BigDecimal(inHours);
        if (hoursWorked.compareTo(new BigDecimal("40")) > 0) {
            wsRegHours = new BigDecimal("40");
            wsOtHours = hoursWorked.subtract(new BigDecimal("40"));
        } else {
            wsRegHours = hoursWorked;
            wsOtHours = new BigDecimal("0");
        }

        // Calculate gross pay
        BigDecimal rate = new BigDecimal(inRate);
        if (inStatus.equals("C")) {
            wsGross = wsRegHours.multiply(rate);
        } else {
            wsGross = wsRegHours.multiply(rate)
                    .add(wsOtHours.multiply(rate).multiply(new BigDecimal("1.5")));
        }

        // Calculate tax
        if (inStatus.equals("C")) {
            wsTax = new BigDecimal("0");
        } else {
            BigDecimal gross = wsGross;
            if (gross.compareTo(new BigDecimal("500")) <= 0) {
                wsTax = gross.multiply(new BigDecimal("0.10"));
            } else if (gross.compareTo(new BigDecimal("1500")) <= 0) {
                wsTax = new BigDecimal("50")
                        .add(gross.subtract(new BigDecimal("500")).multiply(new BigDecimal("0.20")));
            } else {
                wsTax = new BigDecimal("250")
                        .add(gross.subtract(new BigDecimal("1500")).multiply(new BigDecimal("0.30")));
            }
        }

        // Calculate benefits
        if (inStatus.equals("F")) {
            wsBenefits = new BigDecimal("45.50");
        } else if (inStatus.equals("P")) {
            wsBenefits = new BigDecimal("12.25");
        } else {
            wsBenefits = new BigDecimal("0");
        }

        // Compute net pay
        wsNet = wsGross.subtract(wsTax).subtract(wsBenefits);

        // Print output
        System.out.println("EMPLOYEE " + inEmpId + "  STATUS " + inStatus);
        System.out.println("REG HOURS " + wsRegHours);
        System.out.println("OT HOURS  " + wsOtHours);
        System.out.println("GROSS     " + wsGross);
        System.out.println("TAX       " + wsTax);
        System.out.println("BENEFITS  " + wsBenefits);
        System.out.println("NET PAY  " + wsNet);
    }
}
