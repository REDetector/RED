package com.xl.utils;

import java.util.HashSet;
import java.util.Set;

public class ChromosomeUtils {
    private static final Set<String> chrNameSets = new HashSet<String>();

    static {
        for (int i = 1; i <= 22; i++) {
            chrNameSets.add("chr" + i);
        }
        chrNameSets.add("chrX");
        chrNameSets.add("chrY");
    }

    public static boolean isStandardChromosomeName(String chr) {
//        int chrNameLength = chr.length();
//        if (chrNameLength < 6 && chr.substring(0, 3).equalsIgnoreCase("chr")) {
//            if (Character.isDigit(chr.charAt(3))) {
//                return true;
//            } else if (chr.toLowerCase().charAt(3) == 'x' || chr.toLowerCase().charAt(3) == 'y') {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return false;
//        }
        if (chrNameSets.contains(chr)) {
            return true;
        } else {
            return false;
        }
    }

}
