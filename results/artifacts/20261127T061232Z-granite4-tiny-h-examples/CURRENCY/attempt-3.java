import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            line = "";
        }
        line = line.concat("                                        ");
        String inAmount = line.substring(0, 9).trim();
        String inFrom = line.substring(9, 12).trim();
        String inTo = line.substring(12, 15).trim();

        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("USD", BigDecimal.valueOf(1.0));
        rates.put("EUR", BigDecimal.valueOf(0.9150));
        rates.put("GBP", BigDecimal.valueOf(0.7820));
        rates.put("JPY", BigDecimal.valueOf(151.2400));

        BigDecimal inAmountBD = new BigDecimal(inAmount);
        BigDecimal fromRate = rates.get(inFrom);
        BigDecimal toRate = rates.get(inTo);

        if (fromRate == null || toRate == null) {
            System.out.println("UNSUPPORTED PAIR " + inFrom + "/" + inTo);
            System.exit(0);
        }

        BigDecimal usd = inAmountBD.divide(fromRate, 20, RoundingMode.DOWN);
        BigDecimal result;
        if ("JPY".equals(inTo)) {
            result = usd.multiply(toRate).setScale(0, RoundingMode.HALF_UP);
        } else {
            result = usd.multiply(toRate).setScale(0, RoundingMode.HALF_UP);
        }

        System.out.println("FROM " + inAmount + " " + inFrom);
        System.out.println("TO   " + result + " " + inTo);
        System.out.println("RATE " + toRate + " " + inTo + " PER USD");

        String noteData = "100050020010005001";
        Map<Integer, Integer> noteValues = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            noteValues.put(i, Integer.parseInt(String.valueOf(noteData.charAt((i - 1) * 3))));
        }

        int whole = result.intValue();
        for (int i = 1; i <= 6; i++) {
            int noteValue = noteValues.get(i);
            int count = whole / noteValue;
            if (count > 0) {
                System.out.println("  " + noteValue + " x " + count);
            }
            whole = whole % noteValue;
        }

        System.exit(0);
    }
}
