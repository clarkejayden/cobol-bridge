public class Main {

    public static void main(String[] args) throws Exception {
        // Read one line from standard input, pad with spaces to 40 characters
        String line = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in)).readLine();
        if (line == null) {
            line = " ".repeat(40);
        } else if (line.length() < 40) {
            line = line + " ".repeat(40 - line.length());
        }

        // Define the COBOL-like fields
        String inLine = line; // IN-LINE
        boolean headerRow = inLine.equalsIgnoreCase("SKU,QTY,PRICE,LOC"); // HEADER-ROW

        if (headerRow) {
            System.out.println("HEADER ROW SKIPPED");
            return;
        }

        // WS-FIELDS: four 10-character fields
        String[] wsFields = new String[4];
        java.math.BigDecimal[] wsLengths = new java.math.BigDecimal[4];

        // WS-COMMAS: count commas
        int wsCommas = 0;
        // WS-FILLED: count fields filled
        int wsFilled = 0;

        // WS-PTR: pointer for UNSTRING
        int wsPtr = 1;

        // WS-I: loop index
        int wsI = 0;

        // Split the line using UNSTRING logic
        String[] parts = line.split(",");
        wsCommas = parts.length - 1; // number of commas

        // Fill WS-FIELDS and WS-LENGTHS
        for (wsI = 0; wsI < Math.min(4, parts.length); wsI++) {
            wsFields[wsI] = parts[wsI].trim();
            wsLengths[wsI] = new java.math.BigDecimal(parts[wsI].trim());
            wsFilled++;
        }

        // Display results
        System.out.println("COMMAS " + wsCommas + " FIELDS " + wsFilled);
        for (wsI = 0; wsI < 4; wsI++) {
            System.out.println("FIELD " + (wsI + 1) + " LEN " + wsLengths[wsI].intValue()
                    + " [" + wsFields[wsI] + "]");
        }
    }
}
