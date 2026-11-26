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
        line = line.concat("                                        ");
        char[] inWarehouse = line.substring(0, 3).toCharArray();
        char[] inSKU = new char[5];
        char[] inQty = new char[4];
        char[] inCost = new char[6];
        System.arraycopy(line.toCharArray(), 3, inSKU, 0, 5);
        System.arraycopy(line.toCharArray(), 8, inQty, 0, 4);
        System.arraycopy(line.toCharArray(), 12, inCost, 0, 6);
        System.arraycopy(line.toCharArray(), 18, inSKU, 5, 5);
        System.arraycopy(line.toCharArray(), 23, inQty, 4, 4);
        System.arraycopy(line.toCharArray(), 27, inCost, 5, 6);
        System.arraycopy(line.toCharArray(), 33, inSKU, 10, 5);
        System.arraycopy(line.toCharArray(), 38, inQty, 9, 4);
        System.arraycopy(line.toCharArray(), 42, inCost, 10, 6);
        System.arraycopy(line.toCharArray(), 48, inSKU, 15, 5);
        System.arraycopy(line.toCharArray(), 53, inQty, 14, 4);
        System.arraycopy(line.toCharArray(), 57, inCost, 15, 6);
        System.arraycopy(line.toCharArray(), 63, inSKU, 20, 5);
        System.arraycopy(line.toCharArray(), 68, inQty, 19, 4);
        System.arraycopy(line.toCharArray(), 72, inCost, 20, 6);
        System.arraycopy(line.toCharArray(), 78, inSKU, 25, 5);
        System.arraycopy(line.toCharArray(), 83, inQty, 24, 4);
        System.arraycopy(line.toCharArray(), 87, inCost, 25, 6);

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal maxValue = BigDecimal.ZERO;
        int maxIdx = 1;
        int reorderCount = 0;
        BigDecimal avgCost = BigDecimal.ZERO;

        for (int i = 1; i <= 6; i++) {
            BigDecimal value = inQty[i - 1].equals("    ") ? BigDecimal.ZERO :
                    new BigDecimal(inQty[i - 1]).movePointRight(2);
            BigDecimal cost = inCost[i - 1].equals("      ") ? BigDecimal.ZERO :
                    new BigDecimal(inCost[i - 1]).movePointRight(2);
            BigDecimal itemValue = value.multiply(cost);
            totalValue = totalValue.add(itemValue);
            totalQty = totalQty.add(inQty[i - 1].equals("    ") ? BigDecimal.ZERO :
                    new BigDecimal(inQty[i - 1]));
            if (itemValue.compareTo(maxValue) > 0) {
                maxValue = itemValue;
                maxIdx = i;
            }
            if (inQty[i - 1].equals("    ") || inQty[i - 1].equals("    ")) {
                reorderCount++;
            }
            if (!inQty[i - 1].equals("    ") && inQty[i - 1].equals("    ")) {
                avgCost = avgCost.add(cost);
            }
        }

        if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
            avgCost = totalValue.divide(totalQty, RoundingMode.HALF_UP);
        } else {
            avgCost = BigDecimal.ZERO;
        }

        System.out.println("WAREHOUSE " + new String(inWarehouse));
        System.out.println("SKU        QTY      COST          VALUE");
        for (int i = 1; i <= 6; i++) {
            System.out.printf("%-5s%4s%4s.%02s%8s%12s.%02s%n",
                    new String(inSKU[i - 1]),
                    inQty[i - 1].equals("    ") ? "    " : inQty[i - 1].substring(0, 4),
                    new String(inCost[i - 1]).substring(0, 4),
                    new String(inCost[i - 1]).substring(4),
                    new String(itemValue.stripTrailingZeros().toPlainString()).replace(' ', '0').replace(',', ',').replace('.', '.').replace(' ', '0'),
                    new String(itemValue.stripTrailingZeros().toPlainString()).replace(' ', '0').replace(',', ',').replace('.', '.').replace(' ', '0')
            );
        }
        if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("TOTAL VALUE    " + totalValue.stripTrailingZeros().toPlainString().replace(' ', '0').replace(',', ',').replace('.', '.').replace(' ', '0'));
            System.out.println("TOTAL UNITS    " + totalQty.stripTrailingZeros().toPlainString().replace(' ', '0').replace(',', ',').replace('.', '.').replace(' ', '0'));
        }
        System.out.println("AVG UNIT COST  " + avgCost.stripTrailingZeros().toPlainString().replace(' ', '0').replace(',', ',').replace('.', '.').replace(' ', '0'));
        System.out.println("TOP LINE       " + new String(inSKU[maxIdx - 1]));
        System.out.println("TO REORDER     " + reorderCount);
    }
}
