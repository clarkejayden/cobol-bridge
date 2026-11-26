import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        // Read input line and pad with spaces to the record length (15)
        String line = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in)).readLine();
        if (line == null) line = "                     ";
        assert line.length() == 15 : "Input length must be 15";

        // Define offsets for fields
        int inAmountOffset = 0, inAmountLength = 7;
        int inFromOffset = 9, inFromLength = 3;
        int inToOffset = 12, inToLength = 3;

        // Parse fields
        String inFrom = line.substring(inFromOffset, inFromOffset + inFromLength);
        String inTo = line.substring(inToOffset, inToOffset + inToLength);
        BigDecimal inAmount = new BigDecimal(line.substring(inAmountOffset, inAmountOffset + inAmountLength));

        // Supported currencies
        String[] validCurrencies = {"USD", "EUR", "GBP", "JPY"};

        // Validate currencies
        if (!validCurrencies.contains(inFrom) || !validCurrencies.contains(inTo)) {
            System.out.println("UNSUPPORTED PAIR " + inFrom + "/" + inTo);
            return;
        }

        // Exchange rates
        BigDecimal usdToFrom = new BigDecimal("1.0000");
        BigDecimal usdToTo = new BigDecimal("1.0000");

        switch (inFrom) {
            case "USD":
                break;
            case "EUR":
                usdToFrom = usdToFrom.setScale(4, RoundingMode.HALF_UP).multiply(new BigDecimal("0.9150"));
                break;
            case "GBP":
                usdToFrom = usdToFrom.setScale(4, RoundingMode.HALF_UP).multiply(new BigDecimal("0.7820"));
                break;
            case "JPY":
                usdToFrom = usdToFrom.setScale(4, RoundingMode.HALF_UP).multiply(new BigDecimal("151.2400"));
                break;
        }

        switch (inTo) {
            case "USD":
                break;
            case "EUR":
                usdToTo = usdToTo.setScale(4, RoundingMode.HALF_UP).multiply(new BigDecimal("1.0850"));
                break;
            case "GBP":
                usdToTo = usdToTo.setScale(4, RoundingMode.HALF_UP).multiply(new BigDecimal("1.2820"));
                break;
            case "JPY":
                usdToTo = usdToTo.setScale(4, RoundingMode.HALF_UP).multiply(new BigDecimal("6.5660"));
                break;
        }

        // Convert to USD
        BigDecimal usdAmount = inAmount.divide(usdToFrom, 4, RoundingMode.HALF_UP);

        // Convert from USD to target currency
        BigDecimal result;
        if (inTo.equals("JPY")) {
            result = usdAmount.multiply(usdToTo).setScale(0, RoundingMode.HALF_UP);
        } else {
            result = usdAmount.multiply(usdToTo).setScale(4, RoundingMode.HALF_UP);
        }

        // Display original and converted amounts
        System.out.println("FROM " + inAmount + " " + inFrom);
        System.out.println("TO   " + result + " " + inTo);
        System.out.println("RATE " + usdToTo.setScale(4, RoundingMode.HALF_UP) + " " + inTo + " PER USD");

        // Break result into notes
        String[] noteValues = {"100", "50", "20", "10", "5", "1"};
        BigDecimal[] noteValuesBd = new BigDecimal[noteValues.length];
        for (int i = 0; i < noteValues.length; i++) {
            noteValuesBd[i] = new BigDecimal(noteValues[i]);
        }

        for (int i = 0; i < noteValues.length; i++) {
            BigDecimal count = result.divide(noteValuesBd[i], 0, RoundingMode.DOWN);
            if (count.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("  " + noteValuesBd[i] + " x " + count);
                result = result.subtract(count.multiply(noteValuesBd[i]));
            }
        }

        // If result is still non-zero, it's an error (amount too large)
        if (result.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("AMOUNT TOO LARGE TO CONVERT");
        }
    }
}
