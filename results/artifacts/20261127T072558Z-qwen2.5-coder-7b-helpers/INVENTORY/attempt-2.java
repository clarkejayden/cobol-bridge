import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        line = String.format("%-93s", line).substring(0, 93);

        String inWarehouse = line.substring(0, 3);
        BigDecimal[] inQty = new BigDecimal[6];
        BigDecimal[] inCost = new BigDecimal[6];
        String[] inSku = new String[6];

        for (int i = 0; i < 6; i++) {
            inSku[i] = line.substring(3 + i * 12, 8 + i * 12);
            inQty[i] = Pic.num(line.substring(8 + i * 12, 12 + i * 12), "9(4)");
            inCost[i] = Pic.num(line.substring(12 + i * 12, 18 + i * 12), "9(4)V99");
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
            valueItem(inSku, inQty, inCost, wsValue, wsTotal, wsTotalQty, wsMaxValue, wsMaxIdx, wsReorder, wsI);
        }

        if (wsTotalQty > 0) {
            wsAvgCost = wsTotal.divide(BigDecimal.valueOf(wsTotalQty), 2, RoundingMode.HALF_UP);
        }

        String outTotal = Pic.show(wsTotal, "ZZZ,ZZZ,ZZ9.99");
        System.out.println("TOTAL VALUE    " + outTotal);

        String outQty = Pic.show(BigDecimal.valueOf(wsTotalQty), "ZZZ,ZZ9");
        System.out.println("TOTAL UNITS    " + outQty);

        String outAvg = Pic.show(wsAvgCost, "Z,ZZ9.99");
        System.out.println("AVG UNIT COST  " + outAvg);

        String outMaxLine = Pic.show(inSku[wsMaxIdx - 1], "X(5)");
        System.out.println("TOP LINE       " + outMaxLine);

        String outReorder = Pic.show(wsReorder, "9");
        System.out.println("TO REORDER     " + outReorder);
    }

    private static void valueItem(String[] inSku, BigDecimal[] inQty, BigDecimal[] inCost, BigDecimal wsValue, BigDecimal wsTotal, int wsTotalQty, BigDecimal wsMaxValue, int wsMaxIdx, int wsReorder, int wsI) {
        wsValue = inQty[wsI].multiply(inCost[wsI]);
        wsTotal = wsTotal.add(wsValue);
        wsTotalQty += inQty[wsI].intValue();

        if (wsValue.compareTo(wsMaxValue) > 0) {
            wsMaxValue = wsValue;
            wsMaxIdx = wsI + 1;
        }

        String rptFlag = "      ";
        if (inQty[wsI].compareTo(BigDecimal.valueOf(100)) < 0) {
            rptFlag = "REORDER";
            wsReorder++;
        }

        String rptLine = String.format("%-5s %-2s %5s %-2s %7s %-2s %10s %-1s %7s",
                inSku[wsI], "  ", inQty[wsI], "  ", inCost[wsI], "  ", wsValue, " ", rptFlag);
        System.out.println(rptLine);
    }
}
