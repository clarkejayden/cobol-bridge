import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (line.length() < 16) {
            line += " ";
        }
        scanner.close();

        String inEmpId = line.substring(0, 6).trim();
        String inStatus = line.substring(6, 7);
        BigDecimal inHours = new BigDecimal(line.substring(7, 11).trim()).setScale(1, RoundingMode.HALF_UP);
        BigDecimal inRate = new BigDecimal(line.substring(11, 16).trim()).setScale(2, RoundingMode.HALF_UP);

        BigDecimal wsRegHours = BigDecimal.ZERO;
        BigDecimal wsOtHours = BigDecimal.ZERO;
        BigDecimal wsGross = BigDecimal.ZERO;
        BigDecimal wsTax = BigDecimal.ZERO;
        BigDecimal wsBenefits = BigDecimal.ZERO;
        BigDecimal wsNet = BigDecimal.ZERO;
        BigDecimal outHours = BigDecimal.ZERO;
        BigDecimal outAmt = BigDecimal.ZERO;
        BigDecimal outNet = BigDecimal.ZERO;

        if (!"F".equals(inStatus) && !"P".equals(inStatus) && !"C".equals(inStatus)) {
            System.out.println("EMPLOYEE " + inEmpId + " REJECTED: BAD STATUS [" + inStatus + "]");
            return;
        }

        if (inHours.compareTo(new BigDecimal("40")) > 0) {
            wsRegHours = new BigDecimal("40");
            wsOtHours = inHours.subtract(new BigDecimal("40"));
        } else {
            wsRegHours = inHours;
        }

        if ("C".equals(inStatus)) {
            wsGross = inHours.multiply(inRate).setScale(2, RoundingMode.HALF_UP);
        } else {
            wsGross = wsRegHours.multiply(inRate).setScale(2, RoundingMode.HALF_UP)
                               .add(wsOtHours.multiply(inRate).multiply(new BigDecimal("1.5")).setScale(2, RoundingMode.HALF_UP));
        }

        if (!"C".equals(inStatus)) {
            if (wsGross.compareTo(new BigDecimal("500")) <= 0) {
                wsTax = wsGross.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
            } else if (wsGross.compareTo(new BigDecimal("1500")) <= 0) {
                wsTax = new BigDecimal("50").setScale(2, RoundingMode.HALF_UP)
                                          .add(wsGross.subtract(new BigDecimal("500")).multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP));
            } else {
                wsTax = new BigDecimal("250").setScale(2, RoundingMode.HALF_UP)
                                           .add(wsGross.subtract(new BigDecimal("1500")).multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP));
            }
        }

        if ("F".equals(inStatus)) {
            wsBenefits = new BigDecimal("45.50").setScale(2, RoundingMode.HALF_UP);
        } else if ("P".equals(inStatus)) {
            wsBenefits = new BigDecimal("12.25").setScale(2, RoundingMode.HALF_UP);
        }

        wsNet = wsGross.subtract(wsTax).subtract(wsBenefits).setScale(2, RoundingMode.HALF_UP);

        System.out.println("EMPLOYEE  " + inEmpId + "  STATUS " + inStatus);
        outHours = wsRegHours.setScale(1, RoundingMode.HALF_UP);
        System.out.println("REG HOURS " + outHours);
        outHours = wsOtHours.setScale(1, RoundingMode.HALF_UP);
        System.out.println("OT HOURS  " + outHours);
        outAmt = wsGross.setScale(2, RoundingMode.HALF_UP);
        System.out.println("GROSS     " + outAmt);
        outAmt = wsTax.setScale(2, RoundingMode.HALF_UP);
        System.out.println("TAX       " + outAmt);
        outAmt = wsBenefits.setScale(2, RoundingMode.HALF_UP);
        System.out.println("BENEFITS  " + outAmt);
        outNet = wsNet.setScale(2, RoundingMode.HALF_UP);
        System.out.println("NET PAY  " + outNet);
    }
}
