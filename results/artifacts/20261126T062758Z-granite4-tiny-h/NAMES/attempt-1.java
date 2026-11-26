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
        line = line.concat("                                        ");
        String inFirst = line.substring(0, 12).trim();
        String inLast = line.substring(12, 27).trim();
        String inDept = line.substring(27, 37).trim();

        String wsFull = inLast + ", " + inFirst + " (" + inDept + ")";
        System.out.println("ENTRY    [" + wsFull + "]");
        System.out.println("LENGTH   " + wsFull.length());

        int wsCountA = 0;
        for (char c : wsFull.toCharArray()) {
            if (c == 'A') {
                wsCountA++;
            }
        }
        System.out.println("A COUNT  " + wsCountA);

        int wsCountLead = 0;
        for (int i = 0; i < inFirst.length(); i++) {
            if (inFirst.charAt(i) == ' ') {
                wsCountLead++;
            } else {
                break;
            }
        }
        System.out.println("LEADING  " + wsCountLead);

        String wsInitials = "";
        if (inFirst.length() > 0 && inLast.length() > 0) {
            wsInitials = inFirst.substring(0, 1).toUpperCase() + "." + inLast.substring(0, 1).toUpperCase() + ".";
        }
        System.out.println("INITIALS [" + wsInitials + "]");

        String wsUserId = inLast.substring(0, 6) + inFirst.substring(0, 1) + "0";
        wsUserId = wsUserId.replace(' ', 'X');
        wsUserId = wsUserId.substring(0, 8);

        System.out.println("USERID   " + wsUserId);

        String styledFull = "";
        for (char c : wsFull.toCharArray()) {
            if ("AEIOUaeiou".indexOf(c) >= 0) {
                styledFull += Character.toLowerCase(c);
            } else {
                styledFull += c;
            }
        }
        System.out.println("STYLED   [" + styledFull + "]");
    }
}
