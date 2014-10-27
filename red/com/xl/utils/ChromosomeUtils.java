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
        chrNameSets.add("chrM");
    }

    public static boolean isStandardChromosomeName(String chr) {
        return chrNameSets.contains(chr);
    }

    public static String formatChromosomeName(String chr) {
        if (chr.length() == 1) {
            return "chr" + chr;
        } else if (chr.startsWith("ch") && !chr.startsWith("chr")) {
            return "chr" + chr.substring(2);
        } else {
            return chr;
        }
    }

    public static String getAliasChromosomeName(String chr) {
        if (chr.length() == 1) {
            return chr;
        } else {
            return chr.substring(3);
        }
    }

    public static void main(String[] args) {
        System.out.println(ChromosomeUtils.formatChromosomeName("8"));
        System.out.println(ChromosomeUtils.formatChromosomeName("ch8"));
        System.out.println(ChromosomeUtils.formatChromosomeName("chr8"));
        System.out.println(ChromosomeUtils.formatChromosomeName("Y"));
        System.out.println(ChromosomeUtils.formatChromosomeName("chY"));
        System.out.println(ChromosomeUtils.formatChromosomeName("chrY"));
        System.out.println(ChromosomeUtils.getAliasChromosomeName("chrY"));
    }
}
