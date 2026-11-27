```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            line = "";
        }
        line = line.concat(" ".repeat(93 - line.length()));

        char[] inWarehouse = line.substring(0, 3).toCharArray();
        char[] inSKU = new char[5];
        System.arraycopy(line.toCharArray(), 3, inSKU, 0, 5);
        char[] inQty = new char[4];
        System.arraycopy(line.toCharArray(), 8, inQty, 0, 4);
        char[] inCost = new char[6];
        System.arraycopy(line.toCharArray(), 12, inCost, 0, 6);
        char[] inSKU2 = new char[5];
        System.arraycopy(line.toCharArray(), 18, inSKU2, 0, 5);
        char[] inQty2 = new char[4];
        System.arraycopy(line.toCharArray(), 23, inQty2, 0, 4);
        char[] inCost2 = new char[6];
        System.arraycopy(line.toCharArray(), 27, inCost2, 0, 6);
        char[] inSKU3 = new char[5];
        System.arraycopy(line.toCharArray(), 33, inSKU3, 0, 5);
        char[] inQty3 = new char[4];
        System.arraycopy(line.toCharArray(), 38, inQty3, 0, 4);
        char[] inCost3 = new char[6];
        System.arraycopy(line.toCharArray(), 42, inCost3, 0, 6);
        char[] inSKU4 = new char[5];
        System.arraycopy(line.toCharArray(), 48, inSKU4, 0, 5);
        char[] inQty4 = new char[4];
        System.arraycopy(line.toCharArray(), 53, inQty4, 0, 4);
        char[] inCost4 = new char[6];
        System.arraycopy(line.toCharArray(), 57, inCost4, 0, 6);
        char[] inSKU5 = new char[5];
        System.arraycopy(line.toCharArray(), 63, inSKU5, 0, 5);
        char[] inQty5 = new char[4];
        System.arraycopy(line.toCharArray(), 68, inQty5, 0, 4);
        char[] inCost5 = new char[6];
        System.arraycopy(line.toCharArray(), 72, inCost5, 0, 6);
        char[] inSKU6 = new char[5];
        System.arraycopy(line.toCharArray(), 78, inSKU6, 0, 5);
        char[] inQty6 = new char[4];
        System.arraycopy(line.toCharArray(), 83, inQty6, 0, 4);
        char[] inCost6 = new char[6];
        System.arraycopy(line.toCharArray(), 87, inCost6, 0, 6);

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal maxValue = BigDecimal.ZERO;
        int maxIdx = 1;
        int reorderCount = 0;
        BigDecimal avgCost = BigDecimal.ZERO;

        for (int i = 0; i < 6; i++) {
            BigDecimal value = i < inQty.length && !inQty[i].isEmpty() ? new BigDecimal(inQty[i]) : BigDecimal.ZERO;
            value = value.multiply(new BigDecimal(inCost[i]));
            totalValue = totalValue.add(value);
            totalQty = totalQty.add(i < inQty.length && !inQty[i].isEmpty() ? new BigDecimal(inQty[i]) : BigDecimal.ZERO);

            if (value.compareTo(maxValue) > 0) {
                maxValue = value;
                maxIdx = i + 1;
            }

            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                                                if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                                                    if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                                                        if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && inQty[i].charAt(0) != '-' && inQty[i].charAt(0) != '+' && inQty[i].charAt(0) != ' ') {
                                                                                                                                                                                                                            if (i < inQty.length && !inQty[i].isEmpty() && inQty[i].charAt(0) != '0' && in