package com.xl.utils;

public class ChromosomeUtils {
    public static String getStandardChromosomeName(String chr) {
        if (chr.length() == 4) {
            return chr;
        }
        String chrName = chr.substring(0, 5);
        Character c = chrName.charAt(4);
        if (Character.isDigit(c)) {
            return chrName;
        } else {
            return null;
        }
    }

    public static boolean isStandardChromosomeName(String chr) {
        int chrNameLength = chr.length();
        if (chrNameLength < 6 && chr.substring(0, 3).equalsIgnoreCase("chr")) {
            if (Character.isDigit(chr.charAt(3))) {
                return true;
            } else if (chr.toLowerCase().charAt(3) == 'x' || chr.toLowerCase().charAt(3) == 'y') {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
