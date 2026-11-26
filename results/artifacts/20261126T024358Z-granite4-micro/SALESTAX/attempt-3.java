public class Main {

    public static void main(String[] args) throws Exception {
        // Read one line from standard input, pad with spaces to the record length (11)
        String line = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in)).readLine();
        if (line == null) {
            line = "          ";
        }

        // Define fields by offsets
        String inState = line.substring(0, 2);
        BigDecimal inAmount = new BigDecimal(line.substring(2, 10));
        String inExempt = line.substring(10, 11);

        // --- RATE-DATA -------------------------------------------------
        String[] rateData = {
                "CA0725",
                "NY0400",
                "TX0625",
                "WA0650",
                "OR0000",
                "FL0600"
        };
        // --- RATE-TABLE -------------------------------------------------
        String[][] rateTable = {
                {"CA", "7.25"},
                {"NY", "4.00"},
                {"TX", "6.25"},
                {"WA", "6.50"},
                {"OR", "0.00"},
                {"FL", "6.00"}
        };
        // --- IN-REC ----------------------------------------------------
        boolean knownState = rateTable.stream()
                .anyMatch(row -> inState.equalsIgnoreCase(row[0]));

        if (!knownState) {
            System.out.println("INVALID STATE [" + inState + "]");
            return;
        }

        // --- SEARCH RATE ------------------------------------------------
        BigDecimal tax = BigDecimal.ZERO;
        for (String[] row : rateTable) {
            if (inState.equalsIgnoreCase(row[0])) {
                tax = new BigDecimal(row[1]).multiply(inAmount).divide(BigDecimal.valueOf(100), 20, RoundingMode.HALF_UP);
                break;
            }
        }
        if (tax == BigDecimal.ZERO) {
            System.out.println("NO RATE ON FILE FOR " + inState);
            return;
        }

        // --- PRICE-ORDER -------------------------------------------------
        BigDecimal total = inAmount.add(tax);
        BigDecimal ratePct = new BigDecimal(rateTable[rateTable.length - 1][1]);

        // Output
        System.out.println("STATE    " + inState + "  RATE " + ratePct + "%");
        System.out.println("SUBTOTAL " + inAmount);
        System.out.println("TAX      " + tax + (tax.equals(BigDecimal.ZERO) ? " (EXEMPT)" : ""));
        System.out.println("TOTAL  " + total);
    }
}
