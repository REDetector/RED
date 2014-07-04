package com.xl.utils;

public enum Strand {
    NONE, POSITIVE, NEGATIVE;

    public static String parseStrand(Strand strand) {
        switch (strand) {
            case POSITIVE:
                return "+";
            case NEGATIVE:
                return "-";
            case NONE:
                return ".";
            default:
                return ".";
        }
    }

    public static Strand parseStrand(String string) {
        if (string.equals("+")) {
            return Strand.POSITIVE;
        } else if (string.equals("-")) {
            return Strand.NEGATIVE;
        } else if (string.equals(".")) {
            return Strand.NONE;
        } else {
            throw new IllegalArgumentException(
                    "Wrong string received. The parse string muse be '+', '-' or '.'");
        }
    }
}
