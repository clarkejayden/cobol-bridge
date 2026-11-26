import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        if (line == null || line.isEmpty()) {
            line = "                    ";
        }
        line = line + "                    ".substring(0, 40 - line.length());

        String inLine = line.substring(0, 40);
        String[] wsFields = new String[4];
        int[] wsLengths = new int[4];
        int wsFilled = 0;
        int wsCommas = 0;
        int wsPtr = 1;
        int wsI = 0;

        for (int i = 0; i < inLine.length(); i++) {
            if (inLine.charAt(i) == ',') {
                wsCommas++;
            }
        }

        String[] fields = inLine.split(",", 4);
        for (int i = 0; i < fields.length; i++) {
            wsFields[i] = fields[i].trim();
            wsLengths[i] = wsFields[i].length();
            if (!wsFields[i].isEmpty()) {
                wsFilled++;
            }
        }

        if (wsCommas == 3) {
            System.out.println("HEADER ROW SKIPPED");
            return;
        }

        if (wsFilled > 4) {
            System.out.println("EXTRA DATA AFTER FIELD 4");
        }

        System.out.println("COMMAS " + wsCommas + " FIELDS " + wsFilled);
        for (int i = 0; i < 4; i++) {
            System.out.println("FIELD " + (i + 1) + " LEN " + wsLengths[i] + " [" + wsFields[i] + "]");
        }
    }
}
