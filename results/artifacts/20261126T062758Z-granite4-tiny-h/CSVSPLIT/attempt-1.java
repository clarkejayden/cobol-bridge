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
        line = line.concat("                                            ");
        String inLine = line.substring(0, 40);

        boolean headerRow = "SKU,QTY,PRICE,LOC".equals(inLine.substring(0, 16));
        if (headerRow) {
            System.out.println("HEADER ROW SKIPPED");
            return;
        }

        String[] wsField = new String[4];
        int[] wsLengths = new int[4];
        int wsFilled = 0;
        int wsCommas = 0;
        int wsPtr = 1;

        int commaCount = 0;
        for (int i = 0; i < inLine.length(); i++) {
            if (inLine.charAt(i) == ',') {
                commaCount++;
            }
        }
        wsCommas = commaCount;

        String[] parts = inLine.split(",");
        for (int i = 0; i < Math.min(parts.length, wsField.length); i++) {
            wsField[i] = parts[i];
            wsLengths[i] = parts[i].length();
            wsFilled++;
        }

        for (int i = 0; i < wsField.length; i++) {
            System.out.println("FIELD " + (i + 1) + " LEN " + wsLengths[i] + " [" + wsField[i] + "]");
        }

        System.out.println("COMMAS " + wsCommas + " FIELDS " + wsFilled);

        System.exit(0);
    }
}
