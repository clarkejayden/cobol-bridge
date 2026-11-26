public class Main {

    public static void main(String[] args) throws Exception {
        // Read input line
        String line = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in)).readLine();

        // Pad input line to the record length (16 characters)
        if (line.length() < 16) {
            line = line + "                ";
        }

        // Slice fields by offsets
        String inEmpId = line.substring(0, 6).trim();
        String inStatus = line.substring(6, 7).trim();
        String inHours = line.substring(7, 11).trim();
        String inRate = line.substring(11, 16).trim();

        // Convert input fields to BigDecimal with scale 2
        BigDecimal wsRegHours = new BigDecimal(inHours).setScale(2, RoundingMode.HALF_UP);
        BigDecimal wsOtHours = new BigDecimal(inHours).setScale(2, RoundingMode.HALF_UP);
        BigDecimal wsGross = new BigDecimal(inRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal wsTax = new BigDecimal(inRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal wsBenefits = new BigDecimal(inRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal wsNet = new BigDecimal(inRate).setScale(2, RoundingMode.HALF_UP);

        // Process input
        if (!inStatus.equals("F") && !inStatus.equals("P") && !inStatus.equals("C")) {
            System.out.println("EMPLOYEE " + inEmpId + " REJECTED: BAD STATUS [" + inStatus + "]");
            return;
        }

        // Split hours worked into regular and overtime
        BigDecimal hoursWorked = new BigDecimal(inHours).setScale(2, RoundingMode.HALF_UP);
        if (hoursWorked.compareTo(BigDecimal.valueOf(40)) > 0) {
            wsRegHours = BigDecimal.valueOf(40).setScale(2, RoundingMode.HALF_UP);
            wsOtHours = hoursWorked.subtract(BigDecimal.valueOf(40)).setScale(2, RoundingMode.HALF_UP);
        } else {
            wsRegHours = hoursWorked;
            wsOtHours = BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP);
        }

        // Calculate gross pay
        BigDecimal rate = new BigDecimal(inRate).setScale(2, RoundingMode.HALF_UP);
        if (inStatus.equals("C")) {
            wsGross = wsRegHours.multiply(rate);
        } else {
            wsGross = wsRegHours.multiply(rate)
                    .add(wsOtHours.multiply(rate).multiply(BigDecimal.valueOf(1.5))).setScale(2, RoundingMode.HALF_UP);
        }

        // Calculate tax
        BigDecimal gross = wsGross;
        if (gross.compareTo(BigDecimal.valueOf(500)) <= 0) {
            wsTax = wsGross.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
        } else if (gross.compareTo(BigDecimal.valueOf(1500)) <= 0) {
            wsTax = BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_UP)
                    .add(gross.subtract(BigDecimal.valueOf(500)).multiply(BigDecimal.valueOf(0.20))).setScale(2, RoundingMode.HALF_UP);
        } else {
            wsTax = BigDecimal.valueOf(250).setScale(2, RoundingMode.HALF_UP)
                    .add(gross.subtract(BigDecimal.valueOf(1500)).multiply(BigDecimal.valueOf(0.30))).setScale(2, RoundingMode.HALF_UP);
        }

        // Calculate benefits
        if (inStatus.equals("F")) {
            wsBenefits = BigDecimal.valueOf(45.50).setScale(2, RoundingMode.HALF_UP);
        } else if (inStatus.equals("P")) {
            wsBenefits = BigDecimal.valueOf(12.25).setScale(2, RoundingMode.HALF_UP);
        } else {
            wsBenefits = BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP);
        }

        // Compute net pay
        wsNet = wsGross.subtract(wsTax).subtract(wsBenefits).setScale(2, RoundingMode.HALF_UP);

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
