import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        while (line.length() < 93) {
            line += " ";
        }
        scanner.close();

        String inWarehouse = line.substring(0, 3);
        String[] inItems = new String[6];
        BigDecimal[] inQty = new BigDecimal[6];
        BigDecimal[] inCost = new BigDecimal[6];
        for (int i = 0; i < 6; i++) {
            inItems[i] = line.substring(3 + i * 15, 8 + i * 15);
            inQty[i] = new BigDecimal(line.substring(8 + i * 15, 12 + i * 15)).setScale(0, RoundingMode.DOWN);
            inCost[i] = new BigDecimal(line.substring(12 + i * 15, 18 + i * 15)).setScale(2, RoundingMode.DOWN);
        }

        BigDecimal wsValue = BigDecimal.ZERO;
        BigDecimal wsTotal = BigDecimal.ZERO;
        int wsTotalQty = 0;
        BigDecimal wsMaxValue = BigDecimal.ZERO;
        int wsMaxIdx = 1;
        int wsReorder = 0;
        BigDecimal wsAvgCost = BigDecimal.ZERO;
        int wsI = 0;

        System.out.println("WAREHOUSE " + inWarehouse);
        System.out.println("SKU        QTY      COST          VALUE");

        for (wsI = 0; wsI < 6; wsI++) {
            BigDecimal wsValueItem = inQty[wsI].multiply(inCost[wsI]);
            wsValue = wsValue.add(wsValueItem);
            wsTotal = wsTotal.add(wsValueItem);
            wsTotalQty += inQty[wsI].intValue();
            if (wsValueItem.compareTo(wsMaxValue) > 0) {
                wsMaxValue = wsValueItem;
                wsMaxIdx = wsI + 1;
            }
            String rptFlag = "     ";
            if (inQty[wsI].compareTo(new BigDecimal("100")) < 0) {
                rptFlag = "REORDER";
                wsReorder++;
            }
            System.out.printf("%-5s %6s %10s %10s %s\n", inItems[wsI], inQty[wsI], inCost[wsI], wsValueItem, rptFlag);
        }

        if (wsTotalQty > 0) {
            wsAvgCost = wsTotal.divide(new BigDecimal(wsTotalQty), 2, RoundingMode.HALF_UP);
        }

        System.out.printf("TOTAL VALUE    %10s\n", wsTotal);
        System.out.printf("TOTAL UNITS    %6d\n", wsTotalQty);
        System.out.printf("AVG UNIT COST  %10s\n", wsAvgCost);
        System.out.printf("TOP LINE       %5s\n", inItems[wsMaxIdx - 1]);
        System.out.printf("TO REORDER     %d\n", wsReorder);
    }
}
