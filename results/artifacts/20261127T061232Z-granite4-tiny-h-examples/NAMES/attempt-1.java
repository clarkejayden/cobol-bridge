import java.io.BufferedReader;
import java.io.IOException;
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
        
        char[] inFirst = line.substring(0, 12).toCharArray();
        char[] inLast = line.substring(12, 27).toCharArray();
        char[] inDept = line.substring(27, 37).toCharArray();
        
        StringBuilder wsFull = new StringBuilder();
        int wsPtr = 1;
        int wsLen = 0;
        int wsCountA = 0;
        int wsCountLead = 0;
        
        for (int i = 0; i < inLast.length; i++) {
            if (i < inLast.length - 1) {
                wsFull.append(inLast[i]);
                wsPtr++;
            } else {
                wsFull.append(inLast[i]);
                wsLen = wsPtr - 1;
            }
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
            }
        }
        
        System.out.println("LEADING  " + wsCountLead);
        
        StringBuilder wsInitials = new StringBuilder();
        if (inLast.length > 0) {
            wsInitials.append(inLast[0]);
        }
        if (inFirst.length > 0) {
            wsInitials.append(inFirst[0]);
        }
        wsInitials.append(". ");
        if (inLast.length > 0) {
            wsInitials.append(inLast.substring(0, 1));
        }
        if (inFirst.length > 0) {
            wsInitials.append(inFirst.substring(0, 1));
        }
        wsInitials.append(". ");
        wsInitials.setLength(Math.min(wsInitials.length(), 4));
        System.out.println("INITIALS [" + wsInitials + "]");
        
        StringBuilder wsUserId = new StringBuilder();
        if (inLast.length > 0) {
            wsUserId.append(inLast.substring(0, 6));
        }
        if (inFirst.length > 0) {
            wsUserId.append(inFirst.substring(0, 1));
        }
        wsUserId.append("0");
        wsUserId.setLength(8);
        
        wsUserId = new StringBuilder(wsUserId.toString().replaceAll(" ", "X"));
        wsUserId = new StringBuilder(wsUserId.toString().toUpperCase());
        wsUserId = new StringBuilder(wsUserId.toString().replaceAll("[A-Z]", "x"));
        
        System.out.println("USERID   " + wsUserId);
        
        StringBuilder wsStyled = new StringBuilder();
        for (char c : wsFull.toString().toCharArray()) {
            if ("AEIOUaeiou".indexOf(c) >= 0) {
                wsStyled.append(Character.toLowerCase(c));
            } else {
                wsStyled.append(c);
            }
        }
        System.out.println("STYLED   [" + wsStyled + "]");
    }
}
