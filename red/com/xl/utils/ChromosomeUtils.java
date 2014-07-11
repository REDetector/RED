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
        if (chrNameSets.contains(chr)) {
            return true;
        } else {
            return false;
        }
    }

}
