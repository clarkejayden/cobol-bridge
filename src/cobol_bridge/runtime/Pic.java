import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * COBOL data representation for translated programs, so that a translation only
 * has to get the business logic right.
 *
 * <p>Every method takes the field's PICTURE string exactly as written in the COBOL
 * source ("9(5)V99", "S9(3)", "ZZ,ZZ9.99", "X(10)", ...). Behaviour follows
 * GnuCOBOL and is checked against it by the project's tests.
 *
 * <ul>
 *   <li>{@code num(text, pic)}: the value of a numeric field read from the input record.
 *   <li>{@code fit(value, pic)} / {@code fit(value, pic, rounded)}: the value a field holds
 *       after a MOVE or arithmetic store (decimals truncated or ROUNDED, high-order
 *       digits dropped, sign dropped if the PIC has no sign).
 *   <li>{@code overflows(value, pic, rounded)}: true when ON SIZE ERROR would trigger.
 *   <li>{@code show(value, pic)}: the exact text DISPLAY prints for the field.
 * </ul>
 */
public final class Pic {
    private Pic() {}

    // ---- input ----------------------------------------------------------------

    /** The value of a numeric field's text from the record ("01234" with 9(3)V99 is 12.34). */
    public static BigDecimal num(String text, String pic) {
        Shape s = Shape.of(pic);
        String t = text;
        boolean negative = false;
        if (!t.isEmpty() && (t.charAt(0) == '+' || t.charAt(0) == '-')) {
            negative = t.charAt(0) == '-';
            t = t.substring(1);
        } else if (!t.isEmpty() && (t.endsWith("+") || t.endsWith("-"))) {
            negative = t.endsWith("-");
            t = t.substring(0, t.length() - 1);
        }
        StringBuilder digits = new StringBuilder();
        for (char c : t.toCharArray()) {
            digits.append(Character.isDigit(c) ? c : '0');
        }
        if (digits.length() == 0) {
            digits.append('0');
        }
        BigDecimal v = new BigDecimal(new BigInteger(digits.toString()), s.scale);
        return negative && s.signed ? v.negate() : v;
    }

    // ---- storing ----------------------------------------------------------------

    /** The value after storing into the field without ROUNDED (extra decimals truncated). */
    public static BigDecimal fit(BigDecimal value, String pic) {
        return fit(value, pic, false);
    }

    /** The value after storing into the field, with or without ROUNDED. */
    public static BigDecimal fit(BigDecimal value, String pic, boolean rounded) {
        Shape s = Shape.of(pic);
        BigDecimal v = value.setScale(s.scale, rounded ? RoundingMode.HALF_UP : RoundingMode.DOWN);
        v = v.remainder(BigDecimal.TEN.pow(s.intDigits)).setScale(s.scale);
        return s.signed ? v : v.abs();
    }

    /** True if the integer part does not fit the field, i.e. ON SIZE ERROR would run. */
    public static boolean overflows(BigDecimal value, String pic, boolean rounded) {
        Shape s = Shape.of(pic);
        BigDecimal v = value.setScale(s.scale, rounded ? RoundingMode.HALF_UP : RoundingMode.DOWN);
        return v.abs().compareTo(BigDecimal.TEN.pow(s.intDigits)) >= 0;
    }

    // ---- DISPLAY ----------------------------------------------------------------

    /** The text DISPLAY prints for a numeric field (unedited or numeric-edited). */
    public static String show(BigDecimal value, String pic) {
        Shape s = Shape.of(pic);
        if (s.alpha) {
            return show(value.toPlainString(), pic);
        }
        return s.edited ? edit(value, s) : plain(value, s);
    }

    public static String show(long value, String pic) {
        return show(BigDecimal.valueOf(value), pic);
    }

    /** The text DISPLAY prints for an alphanumeric field: padded or cut to its width. */
    public static String show(String value, String pic) {
        int width = Shape.of(pic).width;
        String v = value == null ? "" : value;
        if (v.length() >= width) {
            return v.substring(0, width);
        }
        return v + " ".repeat(width - v.length());
    }

    /** Digits of |value| with the field's scale, high-order digits dropped, zero-filled. */
    private static String digits(BigDecimal value, int intDigits, int scale) {
        BigInteger unscaled = value.abs().setScale(scale, RoundingMode.DOWN).unscaledValue();
        String d = unscaled.toString();
        int n = intDigits + scale;
        if (d.length() > n) {
            return d.substring(d.length() - n);
        }
        return "0".repeat(n - d.length()) + d;
    }

    /** The digit of |value| in the 10^intDigits place, just above the field's digits. */
    private static int leadDigit(BigDecimal value, Shape s) {
        BigInteger whole = value.abs().setScale(0, RoundingMode.DOWN).toBigInteger();
        return whole.divide(BigInteger.TEN.pow(s.intDigits)).mod(BigInteger.TEN).intValue();
    }

    private static boolean negative(BigDecimal value, Shape s) {
        return value.signum() < 0 && !digits(value, s.intDigits, s.scale).matches("0*");
    }

    private static String plain(BigDecimal value, Shape s) {
        String d = digits(value, s.intDigits, s.scale);
        StringBuilder out = new StringBuilder();
        if (s.signed) {
            out.append(negative(value, s) ? '-' : '+');
        }
        out.append(d, 0, s.intDigits);
        if (s.scale > 0) {
            out.append('.').append(d, s.intDigits, d.length());
        }
        return out.toString();
    }

    private static String edit(BigDecimal value, Shape s) {
        String d = digits(value, s.intDigits, s.scale);
        boolean neg = negative(value, s);
        char[] p = s.body.toCharArray();
        boolean star = s.body.indexOf('*') >= 0;
        char fill = star ? '*' : ' ';

        if (d.matches("0*") && s.body.indexOf('9') < 0) {
            // All digit positions suppress and the value is zero: the whole field is blank,
            // or all asterisks except the decimal point.
            if (!star) {
                return " ".repeat(s.width);
            }
            StringBuilder out = new StringBuilder();
            for (char c : p) {
                if (c != 'V') {
                    out.append(c == '.' ? '.' : '*');
                }
            }
            return out.toString();
        }

        StringBuilder out = new StringBuilder();
        int di = 0;
        // GnuCOBOL lines the value up with a leading currency sign or floating symbol as if
        // that position held a digit: if the (otherwise dropped) digit landing there is
        // nonzero, no leading zero is suppressed. Plain Z and * fields are not affected.
        boolean currencyOrFloat = s.floatChar != 0 || s.body.startsWith("$");
        boolean significant = currencyOrFloat && leadDigit(value, s) != 0;
        int firstSig = significant ? 1 : -1;
        int floatStart = -1;
        int floatEnd = -1;
        for (int i = 0; i < p.length; i++) {
            char c = p[i];
            boolean inFloat = s.floatChar != 0 && i >= s.floatFirst && i <= s.floatLast;
            if (inFloat && i == s.floatFirst) {
                floatStart = out.length();
                floatEnd = out.length();
                out.append(' ');
                continue;
            }
            if (inFloat) {
                floatEnd = out.length();
            }
            boolean digitPos = c == '9' || c == 'Z' || c == '*' || (inFloat && c == s.floatChar);
            if (digitPos) {
                char digit = d.charAt(di++);
                if (c == '9' || significant || digit != '0') {
                    if (!significant) {
                        significant = true;
                        firstSig = out.length();
                    }
                    out.append(digit);
                } else {
                    out.append(inFloat ? ' ' : fill);
                }
                continue;
            }
            switch (c) {
                case 'V' -> {
                    // implied decimal point: no character position
                }
                case '.' -> {
                    if (!significant) {
                        significant = true;
                        firstSig = out.length();
                    }
                    out.append('.');
                }
                case ',', 'B', '0', '/' -> {
                    char shown = c == 'B' ? ' ' : c;
                    out.append(significant ? shown : (inFloat ? ' ' : fill));
                }
                case '+' -> out.append(neg ? '-' : '+');
                case '-' -> out.append(neg ? '-' : ' ');
                case '$' -> out.append('$');
                default -> throw new IllegalArgumentException("unsupported PIC character " + c);
            }
        }
        if (s.floatChar != 0) {
            int at = firstSig < 0 ? floatEnd : Math.min(firstSig - 1, floatEnd);
            at = Math.max(at, floatStart);
            char symbol = switch (s.floatChar) {
                case '+' -> neg ? '-' : '+';
                case '-' -> neg ? '-' : ' ';
                default -> s.floatChar;
            };
            out.setCharAt(at, symbol);
        }
        if (!s.trailer.isEmpty()) {
            out.append(neg ? s.trailer : "  ");
        }
        return out.toString();
    }

    /** A parsed PICTURE string. */
    private static final class Shape {
        final String body; // expanded, without S and without a trailing CR/DB
        final String trailer; // "CR", "DB" or ""
        final boolean alpha;
        final boolean edited;
        final boolean signed;
        final int intDigits;
        final int scale;
        final int width; // characters DISPLAY prints
        final char floatChar; // '$', '+', '-' for a floating insertion string, else 0
        final int floatFirst;
        final int floatLast;

        private Shape(String pic) {
            String e = expand(pic);
            String trail = "";
            if (e.endsWith("CR") || e.endsWith("DB")) {
                trail = e.substring(e.length() - 2);
                e = e.substring(0, e.length() - 2);
            }
            boolean s = e.startsWith("S");
            if (s) {
                e = e.substring(1);
            }
            body = e;
            trailer = trail;
            alpha = !e.isEmpty() && e.chars().allMatch(c -> c == 'X' || c == 'A');
            boolean hasAlpha = e.chars().anyMatch(c -> c == 'X' || c == 'A');
            if (hasAlpha && !alpha) {
                throw new IllegalArgumentException("alphanumeric-edited PIC not supported: " + pic);
            }
            if (e.indexOf('P') >= 0) {
                throw new IllegalArgumentException("PIC with P scaling not supported: " + pic);
            }
            edited = !alpha && e.chars().anyMatch(c -> c != '9' && c != 'V');

            char fc = 0;
            int first = -1;
            int last = -1;
            if (edited && !e.isEmpty() && "$+-".indexOf(e.charAt(0)) >= 0) {
                char c0 = e.charAt(0);
                int count = 0;
                int i = 0;
                while (i < e.length() && (e.charAt(i) == c0 || ",B0/".indexOf(e.charAt(i)) >= 0)) {
                    if (e.charAt(i) == c0) {
                        count++;
                        last = i;
                    }
                    i++;
                }
                if (count >= 2) {
                    fc = c0;
                    first = 0;
                }
            }
            floatChar = fc;
            floatFirst = first;
            floatLast = fc != 0 ? last : -1;

            int before = 0;
            int after = 0;
            boolean afterPoint = false;
            for (int i = 0; i < e.length(); i++) {
                char c = e.charAt(i);
                boolean digit = c == '9' || c == 'Z' || c == '*'
                        || (fc != 0 && c == fc && i > first && i <= floatLast);
                if (c == 'V' || (edited && c == '.')) {
                    afterPoint = true;
                } else if (digit) {
                    if (afterPoint) {
                        after++;
                    } else {
                        before++;
                    }
                }
            }
            intDigits = before;
            scale = after;
            boolean editedSign = edited && (fc == '+' || fc == '-'
                    || e.startsWith("+") || e.startsWith("-") || e.endsWith("+") || e.endsWith("-")
                    || !trail.isEmpty());
            signed = alpha ? false : (s || editedSign);
            if (alpha) {
                width = e.length();
            } else if (edited) {
                width = e.replace("V", "").length() + trail.length();
            } else {
                width = before + after + (after > 0 ? 1 : 0) + (s ? 1 : 0);
            }
        }

        static Shape of(String pic) {
            return new Shape(pic);
        }

        /** "Z(3)9V9(2)" -> "ZZZ9V99", upper-cased, spaces removed. */
        static String expand(String pic) {
            String p = pic.replace(" ", "").toUpperCase();
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < p.length(); i++) {
                char c = p.charAt(i);
                if (c == '(') {
                    int close = p.indexOf(')', i);
                    if (close < 0 || out.length() == 0) {
                        throw new IllegalArgumentException("bad PIC: " + pic);
                    }
                    int n = Integer.parseInt(p.substring(i + 1, close));
                    char prev = out.charAt(out.length() - 1);
                    out.append(String.valueOf(prev).repeat(n - 1));
                    i = close;
                } else {
                    out.append(c);
                }
            }
            return out.toString();
        }
    }
}
