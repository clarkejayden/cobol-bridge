public class Main {

    public static void main(String[] args) throws Exception {
        // Read one line from standard input, pad with spaces to the record length (17)
        String line = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in)).readLine();
        if (line == null) line = "";

        // Define offsets and field widths
        int offsetAmount = 0, widthAmount = 9;
        int offsetRate = 9, widthRate = 5;
        int offsetMonths = 14, widthMonths = 3;

        // Parse input fields
        BigDecimal inAmount = new BigDecimal(line.substring(offsetAmount, offsetAmount + widthAmount));
        BigDecimal inRate = new BigDecimal(line.substring(offsetRate, offsetRate + widthRate));
        int inMonths = Integer.parseInt(line.substring(offsetMonths, offsetMonths + widthMonths));

        // Validate term
        if (inMonths == 0 || inMonths > 360) {
            System.out.println("INVALID TERM: " + inMonths);
            return;
        }

        // Initialize working fields
        BigDecimal wsBalance = BigDecimal.ZERO;
        BigDecimal wsPrincipal = inAmount.divide(BigDecimal.valueOf(inMonths), 20, RoundingMode.HALF_UP);
        BigDecimal wsInterest = BigDecimal.ZERO;
        BigDecimal wsTotalInt = BigDecimal.ZERO;
        int wsMonth = 0;

        // Output fields
        StringBuilder outMonth = new StringBuilder();
        StringBuilder outInt = new StringBuilder();
        StringBuilder outPrin = new StringBuilder();
        StringBuilder outBal = new StringBuilder();
        StringBuilder outTotal = new StringBuilder();

        // Display header
        System.out.println("MONTH      INTEREST     PRINCIPAL       BALANCE");
        System.out.println("  " + outMonth + " " + outInt + " " + outPrin + " " + outBal);

        // Calculate repayment schedule
        for (wsMonth = 1; wsMonth <= inMonths; wsMonth++) {
            wsInterest = wsBalance.multiply(inRate).divide(BigDecimal.valueOf(1200), 20, RoundingMode.HALF_UP);
            if (wsMonth == inMonths) {
                wsPrincipal = wsBalance;
            }
            wsBalance = wsBalance.subtract(wsPrincipal);
            wsInterest = wsInterest.add(wsTotalInt);
            wsTotalInt = wsTotalInt.add(wsInterest);

            // Format output fields
            outMonth.append(wsMonth).append('\n');
            outInt.append(wsInterest.setScale(2, RoundingMode.HALF_UP)).append('\n');
            outPrin.append(wsPrincipal.setScale(2, RoundingMode.HALF_UP)).append('\n');
            outBal.append(wsBalance.setScale(2, RoundingMode.HALF_UP)).append('\n');

            // Display monthly details
            System.out.println("  " + outMonth + " " + outInt + " " + outPrin + " " + outBal);
        }

        // Display total interest
        outTotal.append(wsTotalInt.setScale(2, RoundingMode.HALF_UP)).append('\n');
        System.out.println("TOTAL INTEREST " + outTotal);
    }
}
