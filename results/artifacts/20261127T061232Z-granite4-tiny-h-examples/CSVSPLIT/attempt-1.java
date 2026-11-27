import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            line = "";
        }
        line = line.concat(" ".repeat(40 - line.length()));

        String inLine = line;
        String[] wsField = new String[4];
        int[] wsLengths = new int[4];
        int wsFilled = 0;
        int wsCommas = 0;
        int wsPtr = 1;
        int wsI;

        // Count commas
        for (wsI = 0; wsI < inLine.length(); wsI++) {
            if (inLine.charAt(wsI) == ',') {
                wsCommas++;
            }
        }

        // Split line into fields
        int start = 0;
        for (wsI = 0; wsI <= 4; wsI++) {
            int end = start;
            if (wsI < 4) {
                while (end < inLine.length() && inLine.charAt(end) != ',') {
                    end++;
                }
            }
            wsField[wsI] = inLine.substring(start, end);
            wsLengths[wsI] = end - start;
            start = end + 1;
        }

        // Unstring fields
        wsFilled = 0;
        wsPtr = 1;
        for (wsI = 0; wsI < 4; wsI++) {
            if (!wsField[wsI].isEmpty()) {
                wsFilled++;
                wsPtr = 1;
            }
            if (wsPtr <= wsLengths[wsI]) {
                System.out.printf("FIELD %d LEN %d [%s]%n", wsI + 1, wsLengths[wsI], wsField[wsI]);
            }
        }

        System.out.printf("COMMAS %d FIELDS %d%n", wsCommas, wsFilled);

        reader.close();
    }
}
