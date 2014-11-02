package com.xl.datatypes.sites;

import com.xl.datatypes.sequence.Location;
import com.xl.utils.ChromosomeNameComparator;

/**
 * The Class Site represents a location at which a measurement can be made
 */
public class Site extends Location {

    /**
     * The chr.
     */
    private String chr;

    private char refBase;

    private char altBase;


    private int depth = 0;

    /**
     * Instantiates a new site.
     *
     * @param chr      the chr
     * @param position the position
     * @param altBase  the editing base
     */
    public Site(String chr, int position, char refBase, char altBase) {
        super(position, position);
        this.chr = chr;
        this.refBase = refBase;
        this.altBase = altBase;
    }

    public Site(String chr, int position, int depth) {
        super(position, position);
        this.chr = chr;
        this.depth = depth;
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

    public int getDepth() {
        return depth;
    }

    @Override
    public int compareTo(Location o) {
        int result = 0;
        if (o instanceof Site) {
            result = ChromosomeNameComparator.getInstance().compare(getChr(), ((Site) o).getChr());
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
