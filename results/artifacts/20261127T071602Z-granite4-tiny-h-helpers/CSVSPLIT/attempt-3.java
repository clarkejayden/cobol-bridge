import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            line = "";
        }
        line = line.concat(" ".repeat(40));

        String inLine = line.substring(0, 40);
        boolean headerRow = "SKU,QTY,PRICE,LOC".equals(inLine.substring(0, 16));
        if (headerRow) {
            System.out.println("HEADER ROW SKIPPED");
            return;
        }

        String[] wsFields = new String[4];
        int[] wsLengths = new int[4];
        int wsFilled = 0;
        int wsCommas = 0;
        int wsPtr = 1;
        int wsI;

        for (wsI = 0; wsI < inLine.length(); wsI++) {
            if (inLine.charAt(wsI) == ',') {
                wsCommas++;
            }
        }

        for (wsI = 0; wsI < 4; wsI++) {
            int startIndex = 10 * wsI;
            int endIndex = startIndex + 10;
            wsFields[wsI] = inLine.substring(startIndex, endIndex);
            wsLengths[wsI] = wsFields[wsI].length();
            if (wsFields[wsI].length() > 0) {
                wsFilled++;
            }
        }

        System.out.println("COMMAS " + wsCommas + " FIELDS " + wsFilled);

        for (wsI = 0; wsI < 4; wsI++) {
            System.out.println("FIELD " + (wsI + 1) + " LEN " + wsLengths[wsI] + " [" + wsFields[wsI] + "]");
        }
    }
}
