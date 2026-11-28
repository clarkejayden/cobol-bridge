import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        line = String.format("%-16s", line).substring(0, 16);

        String inEmpId = line.substring(0, 6);
        String inStatus = line.substring(6, 7);
        BigDecimal inHours = Pic.num(line.substring(7, 11), "9(3)V9");
        BigDecimal inRate = Pic.num(line.substring(11, 16), "9(3)V99");

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

        splitHours(inHours, inRate, wsRegHours, wsOtHours);
        calcGross(wsRegHours, wsOtHours, inRate, wsGross);
        calcTax(wsGross, wsTax);
        calcBenefits(inStatus, wsBenefits);
        wsNet = wsGross.subtract(wsTax).subtract(wsBenefits);

        printStub(inEmpId, inStatus, wsRegHours, wsOtHours, wsGross, wsTax, wsBenefits, wsNet);
    }

    private static void splitHours(BigDecimal inHours, BigDecimal inRate, BigDecimal wsRegHours, BigDecimal wsOtHours) {
        if (inHours.compareTo(new BigDecimal("40")) > 0) {
            wsRegHours = new BigDecimal("40");
            wsOtHours = inHours.subtract(new BigDecimal("40"));
        } else {
            wsRegHours = inHours;
            wsOtHours = BigDecimal.ZERO;
        }
    }

    private static void calcGross(BigDecimal wsRegHours, BigDecimal wsOtHours, BigDecimal inRate, BigDecimal wsGross) {
        if ("C".equals(inStatus)) {
            wsGross = wsRegHours.multiply(inRate).setScale(2, RoundingMode.HALF_UP);
        } else {
            wsGross = wsRegHours.multiply(inRate).add(wsOtHours.multiply(inRate).multiply(new BigDecimal("1.5"))).setScale(2, RoundingMode.HALF_UP);
        }
    }

    private static void calcTax(BigDecimal wsGross, BigDecimal wsTax) {
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
        System.out.println("REG HOURS " + Pic.show(wsRegHours, "ZZ9.9"));
        System.out.println("OT HOURS  " + Pic.show(wsOtHours, "ZZ9.9"));
        System.out.println("GROSS     " + Pic.show(wsGross, "Z,ZZZ,ZZ9.99"));
        System.out.println("TAX       " + Pic.show(wsTax, "Z,ZZZ,ZZ9.99"));
        System.out.println("BENEFITS  " + Pic.show(wsBenefits, "Z,ZZZ,ZZ9.99"));
        System.out.println("NET PAY  " + Pic.show(wsNet, "-Z,ZZZ,ZZ9.99"));
    }
}
