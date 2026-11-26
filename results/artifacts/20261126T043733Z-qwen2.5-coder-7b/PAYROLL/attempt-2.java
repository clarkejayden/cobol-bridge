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
        BigDecimal inHours = new BigDecimal(line.substring(7, 11).trim()).setScale(1, RoundingMode.DOWN);
        BigDecimal inRate = new BigDecimal(line.substring(11, 16).trim()).setScale(2, RoundingMode.DOWN);

        BigDecimal wsRegHours = BigDecimal.ZERO;
        BigDecimal wsOtHours = BigDecimal.ZERO;
        BigDecimal wsGross = BigDecimal.ZERO;
        BigDecimal wsTax = BigDecimal.ZERO;
        BigDecimal wsBenefits = BigDecimal.ZERO;
        BigDecimal wsNet = BigDecimal.ZERO;

        if (!"F".equals(inStatus) && !"P".equals(inStatus) && !"C".equals(inStatus)) {
            System.out.println("EMPLOYEE " + inEmpId + " REJECTED: BAD STATUS [" + inStatus + "]");
            return;
        }

        splitHours(inHours, wsRegHours, wsOtHours);
        calcGross(inHours, inRate, wsRegHours, wsOtHours, wsGross, inStatus);
        calcTax(wsGross, wsTax, inStatus);
        calcBenefits(inStatus, wsBenefits);
        wsNet = wsGross.subtract(wsTax).subtract(wsBenefits);

        printStub(inEmpId, inStatus, wsRegHours, wsOtHours, wsGross, wsTax, wsBenefits, wsNet);
    }

    private static void splitHours(BigDecimal inHours, BigDecimal wsRegHours, BigDecimal wsOtHours) {
        if (inHours.compareTo(new BigDecimal("40")) > 0) {
            wsRegHours = new BigDecimal("40");
            wsOtHours = inHours.subtract(new BigDecimal("40"));
        } else {
            wsRegHours = inHours;
            wsOtHours = BigDecimal.ZERO;
        }
    }

    private static void calcGross(BigDecimal inHours, BigDecimal inRate, BigDecimal wsRegHours, BigDecimal wsOtHours, BigDecimal wsGross, String inStatus) {
        if ("C".equals(inStatus)) {
            wsGross = inHours.multiply(inRate).setScale(2, RoundingMode.HALF_UP);
        } else {
            wsGross = wsRegHours.multiply(inRate)
                    .add(wsOtHours.multiply(inRate).multiply(new BigDecimal("1.5")))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    private static void calcTax(BigDecimal wsGross, BigDecimal wsTax, String inStatus) {
        if ("C".equals(inStatus)) {
            wsTax = BigDecimal.ZERO;
        } else {
            if (wsGross.compareTo(new BigDecimal("500")) <= 0) {
                wsTax = wsGross.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
            } else if (wsGross.compareTo(new BigDecimal("1500")) <= 0) {
                wsTax = new BigDecimal("50").add(wsGross.subtract(new BigDecimal("500")).multiply(new BigDecimal("0.20"))).setScale(2, RoundingMode.HALF_UP);
            } else {
                wsTax = new BigDecimal("250").add(wsGross.subtract(new BigDecimal("1500")).multiply(new BigDecimal("0.30"))).setScale(2, RoundingMode.HALF_UP);
            }
        }
    }

    private static void calcBenefits(String inStatus, BigDecimal wsBenefits) {
        if ("F".equals(inStatus)) {
            wsBenefits = new BigDecimal("45.50");
        } else if ("P".equals(inStatus)) {
            wsBenefits = new BigDecimal("12.25");
        } else {
            wsBenefits = BigDecimal.ZERO;
        }
    }

    private static void printStub(String inEmpId, String inStatus, BigDecimal wsRegHours, BigDecimal wsOtHours, BigDecimal wsGross, BigDecimal wsTax, BigDecimal wsBenefits, BigDecimal wsNet) {
        System.out.println("EMPLOYEE  " + inEmpId + "  STATUS " + inStatus);
        System.out.println("REG HOURS " + wsRegHours.setScale(1, RoundingMode.DOWN));
        System.out.println("OT HOURS  " + wsOtHours.setScale(1, RoundingMode.DOWN));
        System.out.println("GROSS     " + wsGross.setScale(2, RoundingMode.HALF_UP));
        System.out.println("TAX       " + wsTax.setScale(2, RoundingMode.HALF_UP));
        System.out.println("BENEFITS  " + wsBenefits.setScale(2, RoundingMode.HALF_UP));
        System.out.println("NET PAY  " + wsNet.setScale(2, RoundingMode.HALF_UP));
    }
}
