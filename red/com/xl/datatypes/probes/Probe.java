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

    private char editingBase;

    /**
     * Instantiates a new probe.
     *
     * @param chr  the chr
     * @param position    the position
     * @param editingBase the editing base
     */
    public Probe(String chr, int position, char editingBase) {
        this.chr = chr;
        this.position = position;
        this.editingBase = editingBase;
    }

    public char getEditingBase() {
        return editingBase;
    }

    public void setEditingBase(char editingBase) {
        this.editingBase = editingBase;
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
