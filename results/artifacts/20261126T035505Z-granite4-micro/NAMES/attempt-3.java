public class Main {

    public static void main(String[] args) throws Exception {
        // Read one line from standard input, pad with spaces to the record length (37)
        String line = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in)).readLine();
        if (line == null) {
            line = "                                                                 ";
        }

        // Declare fields with the specified offsets and lengths
        String inFirst = line.substring(0, 12).trim();
        String inLast = line.substring(12, 27).trim();
        String inDept = line.substring(27, 37).trim();

        // Full name string with pointer
        String wsFull = inLast + ", " + inFirst + " (" + inDept + ")";

        // Pointer and length
        int wsPtr = 1;
        int wsLen = wsFull.length();

        // Count of 'A' characters
        int wsCountA = (int) wsFull.chars().filter(ch -> ch == 'A').count();

        // Count of leading spaces in inFirst
        int wsCountLead = (int) inFirst.chars().filter(ch -> ch == ' ').count();

        // Initials
        String wsInitials = inFirst.substring(0, 1) + "." + inLast.substring(0, 1) + ".";

        // User ID
        String wsUserId = inLast.substring(0, 6);
        wsUserId = wsUserId.substring(0, 1) + wsUserId.substring(1, 2) + "0" + wsUserId.substring(2, 3);
        wsUserId = wsUserId.replaceAll(" ", "X");

        // Convert vowels in wsFull to lowercase
        String styledWsFull = wsFull.replaceAll("(?i)[AEIOU]", match -> {
            char lowerCaseChar = Character.isUpperCase(match.charAt(0)) ? Character.toLowerCase(match.charAt(0)) : match.charAt(0);
            return String.valueOf(lowerCaseChar);
        });

        // Display results
        System.out.println("ENTRY    [" + wsFull + "]");
        System.out.println("LENGTH   " + wsLen);
        System.out.println("A COUNT  " + wsCountA);
        System.out.println("LEADING  " + wsCountLead);
        System.out.println("INITIALS [" + wsInitials + "]");
        System.out.println("USERID   " + wsUserId);
        System.out.println("STYLED   [" + styledWsFull + "]");
    }
}
