public class Main {

    public static void main(String[] args) throws Exception {
        // Read one line from standard input, pad with spaces to the record length (93)
        String line = new java.io.BufferedReader(
                new java.io.InputStreamReader(System.in)).readLine();
        if (line == null) line = "";

        // Define offsets and field lengths
        int inWarehouseOffset = 0;
        int inWarehouseLength = 3;
        int sku1Offset = 3;
        int sku1Length = 5;
        int qty1Offset = 8;
        int qty1Length = 4;
        int cost1Offset = 12;
        int cost1Length = 6;
        int sku2Offset = 18;
        int sku2Length = 5;
        int qty2Offset = 23;
        int qty2Length = 4;
        int cost2Offset = 27;
        int cost2Length = 6;
        int sku3Offset = 33;
        int sku3Length = 5;
        int qty3Offset = 38;
        int qty3Length = 4;
        int cost3Offset = 42;
        int cost3Length = 6;
        int sku4Offset = 48;
        int sku4Length = 5;
        int qty4Offset = 53;
        int qty4Length = 4;
        int cost4Offset = 57;
        int cost4Length = 6;
        int sku5Offset = 63;
        int sku5Length = 5;
        int qty5Offset = 68;
        int qty5Length = 4;
        int cost5Offset = 72;
        int cost5Length = 6;
        int sku6Offset = 78;
        int sku6Length = 5;
        int qty6Offset = 83;
        int qty6Length = 4;
        int cost6Offset = 87;
        int cost6Length = 6;

        // Parse fields
        String inWarehouse = line.substring(inWarehouseOffset, inWarehouseOffset + inWarehouseLength);
        String sku1 = line.substring(sku1Offset, sku1Offset + sku1Length);
        int qty1 = Integer.parseInt(line.substring(qty1Offset, qty1Offset + qty1Length));
        BigDecimal cost1 = new BigDecimal(line.substring(cost1Offset, cost1Offset + cost1Length));
        String sku2 = line.substring(sku2Offset, sku2Offset + sku2Length);
        int qty2 = Integer.parseInt(line.substring(qty2Offset, qty2Offset + qty2Length));
        BigDecimal cost2 = new BigDecimal(line.substring(cost2Offset, cost2Offset + cost2Length));
        String sku3 = line.substring(sku3Offset, sku3Offset + sku3Length);
        int qty3 = Integer.parseInt(line.substring(qty3Offset, qty3Offset + qty3Length));
        BigDecimal cost3 = new BigDecimal(line.substring(cost3Offset, cost3Offset + cost3Length));
        String sku4 = line.substring(sku4Offset, sku4Offset + sku4Length);
        int qty4 = Integer.parseInt(line.substring(qty4Offset, qty4Offset + qty4Length));
        BigDecimal cost4 = new BigDecimal(line.substring(cost4Offset, cost4Offset + cost4Length));
        String sku5 = line.substring(sku5Offset, sku5Offset + sku5Length);
        int qty5 = Integer.parseInt(line.substring(qty5Offset, qty5Offset + qty5Length));
        BigDecimal cost5 = new BigDecimal(line.substring(cost5Offset, cost5Offset + cost5Length));
        String sku6 = line.substring(sku6Offset, sku6Offset + sku6Length);
        int qty6 = Integer.parseInt(line.substring(qty6Offset, qty6Offset + qty6Length));
        BigDecimal cost6 = new BigDecimal(line.substring(cost6Offset, cost6Offset + cost6Length));

        // Initialize working storage
        BigDecimal wsValue = BigDecimal.ZERO;
        BigDecimal wsTotal = BigDecimal.ZERO;
        BigDecimal wsTotalQty = BigDecimal.ZERO;
        BigDecimal wsMaxValue = BigDecimal.ZERO;
        int wsMaxIdx = 1;
        int wsReorder = 0;
        BigDecimal wsAvgCost = BigDecimal.ZERO;

        // Process each item
        for (int i = 1; i <= 6; i++) {
            String sku = "";
            int qty = 0;
            BigDecimal cost = BigDecimal.ZERO;

            switch (i) {
                case 1:
                    sku = sku1;
                    qty = qty1;
                    cost = cost1;
                    break;
                case 2:
                    sku = sku2;
                    qty = qty2;
                    cost = cost2;
                    break;
                case 3:
                    sku = sku3;
                    qty = qty3;
                    cost = cost3;
                    break;
                case 4:
                    sku = sku4;
                    qty = qty4;
                    cost = cost4;
                    break;
                case 5:
                    sku = sku5;
                    qty = qty5;
                    cost = cost5;
                    break;
                case 6:
                    sku = sku6;
                    qty = qty6;
                    cost = cost6;
                    break;
            }

            BigDecimal itemValue = qty.multiply(cost);
            wsValue = wsValue.add(itemValue);
            wsTotal = wsTotal.add(qty);
            wsTotalQty = wsTotalQty.add(qty);

            if (itemValue.compareTo(wsMaxValue) > 0) {
                wsMaxValue = itemValue;
                wsMaxIdx = i;
            }

            if (qty < 100) {
                wsReorder++;
            }
        }

        // Calculate average cost
        if (wsTotalQty.compareTo(BigDecimal.ZERO) > 0) {
            wsAvgCost = wsValue.divide(wsTotalQty, 20, RoundingMode.HALF_UP);
        }

        // Display results
        String rptLine = String.format("%-5s%-2s%-2s%-2s%-2s%-2s",
                sku1, qty1, cost1, wsValue, wsTotalQty, wsAvgCost);
        System.out.println("WAREHOUSE " + inWarehouse);
        System.out.println("SKU        QTY      COST          VALUE");
        System.out.println("WAREHOUSE " + inWarehouse);
        System.out.println("SKU        QTY      COST          VALUE");
        System.out.println("WAREHOUSE " + inWarehouse);
        System.out.println("SKU        QTY      COST          VALUE");
        System.out.println("WAREHOUSE " + inWarehouse);
        System.out.println("SKU        QTY      COST          VALUE");
        System.out.println("WAREHOUSE " + inWarehouse);
        System.out.println("SKU        QTY      COST          VALUE");

        System.out.println("TOTAL VALUE    " + wsValue);
        System.out.println("TOTAL UNITS    " + wsTotalQty);
        System.out.println("AVG UNIT COST  " + wsAvgCost);
        System.out.println("TOP LINE       " + sku1);
        System.out.println("TO REORDER     " + wsReorder);
    }
}
