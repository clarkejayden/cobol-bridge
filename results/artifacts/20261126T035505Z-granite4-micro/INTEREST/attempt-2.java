public class Main {

    public static void main(String[] args) throws Exception {
        // Read one line from standard input, pad with spaces to the record length (17)
        String line = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in)).readLine();
        if (line == null) line = "";

        // Define offsets and field widths
        int offsetPrincipal = 0, widthPrincipal = 9;
        int offsetRate = 9, widthRate = 6;
        int offsetYears = 15, widthYears = 2;

        // Pad input line to the required record length
        if (line.length() < 17) {
            line = line + "                "; // 17 - line.length() spaces
        }

        // Slice fields
        String principalStr = line.substring(offsetPrincipal, offsetPrincipal + widthPrincipal);
        String rateStr = line.substring(offsetRate, offsetRate + widthRate);
        String yearsStr = line.substring(offsetYears, offsetYears + widthYears);

        // Convert fields to BigDecimal with specified scales
        java.math.BigDecimal inPrincipal = new java.math.BigDecimal(principalStr);
        java.math.BigDecimal inRate = new java.math.BigDecimal(rateStr);
        int inYears = Integer.parseInt(yearsStr);

        // Initialize variables for simple and compound interest
        java.math.BigDecimal wsSimple = java.math.BigDecimal.ZERO;
        java.math.BigDecimal wsBalance = inPrincipal;
        java.math.BigDecimal wsInterest = java.math.BigDecimal.ZERO;
        int wsYear = 0;
        boolean overflowFlag = false;

        // Calculate simple interest
        wsSimple = inPrincipal.multiply(inRate).divide(java.math.BigDecimal.valueOf(100), 11, java.math.BigDecimal.RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal.valueOf(inYears));

        // Display simple interest
        System.out.println("PRINCIPAL " + wsSimple.setScale(2, java.math.BigDecimal.RoundingMode.HALF_UP));

        // Move simple interest to balance
        wsBalance = wsSimple;

        // Calculate compound interest for each year
        for (wsYear = 1; wsYear <= inYears || overflowFlag; wsYear++) {
            wsInterest = wsBalance.multiply(inRate).divide(java.math.BigDecimal.valueOf(100), 11, java.math.BigDecimal.RoundingMode.HALF_UP);

            // Check for overflow when adding interest to balance
            try {
                wsBalance = wsBalance.add(wsInterest);
            } catch (java.lang.ArithmeticException e) {
                overflowFlag = true;
                break;
            }

            // Display balance for each year
            System.out.println("YEAR " + wsYear + "   " + wsBalance.setScale(2, java.math.BigDecimal.RoundingMode.HALF_UP));
        }

        // Display final compound balance
        System.out.println("COMPOUND  " + wsBalance.setScale(2, java.math.BigDecimal.RoundingMode.HALF_UP));
    }
}
