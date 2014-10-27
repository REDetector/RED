package com.xl.datatypes.probes;

import com.xl.datatypes.sequence.Location;
import com.xl.utils.ChromosomeNameComparator;

/**
 * The Class Probe represents a location at which a measurement can be made
 */
public class Probe extends Location {

    /**
     * The chr.
     */
    private String chr;

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
        super(position, position);
        this.chr = chr;
        this.refBase = refBase;
        this.altBase = altBase;
    }

    public char getRefBase() {
        return refBase;
    }

    public char getAltBase() {
        return altBase;
    }

    public String getChr() {
        return chr;
    }

    @Override
    public int compareTo(Location o) {
        int result = 0;
        if (o instanceof Probe) {
            result = ChromosomeNameComparator.getInstance().compare(getChr(), ((Probe) o).getChr());
            if (result == 0) {
                if (getStart() > o.getStart()) {
                    result = 1;
                } else if (getStart() < o.getStart()) {
                    result = -1;
                } else {
                    result = 0;
                }
            }
        }
        return result;
    }
}
