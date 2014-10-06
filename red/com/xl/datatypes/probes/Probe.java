package com.xl.datatypes.probes;

import com.xl.datatypes.sequence.Location;
import com.xl.utils.ChromosomeNameComparator;

/**
 * The Class Probe represents a location at which a measurement can be made
 */
public class Probe implements Location {

    /**
     * The chr.
     */
    private String chr;

    private int position = 0;


    private char refBase;

    private char altBase;

    /**
     * Instantiates a new probe.
     *
     * @param chr      the chr
     * @param position the position
     * @param altBase  the editing base
     */
    public Probe(String chr, int position, char refBase, char altBase) {
        this.chr = chr;
        this.position = position;
        this.refBase = refBase;
        this.altBase = altBase;
    }

    public char getRefBase() {
        return refBase;
    }

    public char getAltBase() {
        return altBase;
    }

    @Override
    public String getChr() {
        return chr;
    }

    @Override
    public int getStart() {
        return position;
    }

    @Override
    public int getEnd() {
        return position;
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public int compareTo(Location o) {
        int result = ChromosomeNameComparator.getInstance().compare(getChr(), o.getChr());
        if (result == 0) {
            if (getStart() > o.getStart()) {
                result = 1;
            } else if (getStart() < o.getStart()) {
                result = -1;
            } else {
                result = 0;
            }
        }
        return result;
    }
}
