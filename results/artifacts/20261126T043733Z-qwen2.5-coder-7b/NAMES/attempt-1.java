import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) throws Exception {
        String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
        if (line == null) {
            line = "";
        }
        line = String.format("%-37s", line).substring(0, 37);

        String inFirst = line.substring(0, 12).trim();
        String inLast = line.substring(12, 27).trim();
        String inDept = line.substring(27, 37).trim();

        StringBuilder wsFull = new StringBuilder(36).append(' ');
        int wsPtr = 1;
        int wsLen = 0;
        int wsCountA = 0;
        int wsCountLead = 0;
        StringBuilder wsInitials = new StringBuilder(4).append(' ');
        StringBuilder wsUserid = new StringBuilder(8).append(' ');

        wsFull.append(inLast).append(", ").append(inFirst).append(" (").append(inDept).append(")");
        wsPtr = wsFull.length();
        wsLen = wsPtr - 1;

        if (wsLen > 36) {
            System.out.println("WARNING: NAME TRUNCATED");
            wsFull.setLength(36);
        }

        System.out.println("ENTRY    [" + wsFull + "]");
        System.out.println("LENGTH   " + wsLen);

        for (char c : wsFull.toString().toCharArray()) {
            if (c == 'A') {
                wsCountA++;
            }
        }
        System.out.println("A COUNT  " + wsCountA);

        for (char c : inFirst.toCharArray()) {
            if (c == ' ') {
                wsCountLead++;
            } else {
                break;
            }
        }
        System.out.println("LEADING  " + wsCountLead);

        wsInitials.setCharAt(0, inFirst.charAt(0));
        wsInitials.setCharAt(1, '.');
        wsInitials.setCharAt(2, inLast.charAt(0));
        wsInitials.setCharAt(3, '.');
        System.out.println("INITIALS [" + wsInitials + "]");

        wsUserid.setLength(6);
        wsUserid.append(inLast, 0, 6);
        wsUserid.append(inFirst.charAt(0));
        wsUserid.append('0');
        for (int i = 0; i < wsUserid.length(); i++) {
            if (wsUserid.charAt(i) == ' ') {
                wsUserid.setCharAt(i, 'X');
            }
        }
        wsUserid.setLength(8);
        wsUserid.setCharAt(7, Character.toLowerCase(wsUserid.charAt(7)));
        System.out.println("USERID   " + wsUserid);

        for (int i = 0; i < wsFull.length(); i++) {
            char c = wsFull.charAt(i);
            if (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U') {
                wsFull.setCharAt(i, Character.toLowerCase(c));
            }
        }
        System.out.println("STYLED   [" + wsFull + "]");
    }
}
