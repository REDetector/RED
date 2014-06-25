package com.xl.display.featureviewer;

import com.xl.datatypes.sequence.Location;
import com.xl.utils.Strand;

import java.awt.*;
import java.io.Serializable;

public class Feature implements Comparable<Feature>, Serializable {

    private String name = null;
    private String chr = null;
    private Strand strand = null;
    private Location txLocation = null;
    private Location cdsLocation = null;
    private Location exonLocations[] = null;
    private String aliasName = null;
    private Color color = null;

    public Feature(String name, String chr, Strand strand,
                   Location txLocation, Location cdsLocation,
                   Location[] exonLocations, String aliasName) {
        this.name = name;
        this.chr = chr;
        this.strand = strand;
        this.txLocation = txLocation;
        this.cdsLocation = cdsLocation;
        this.exonLocations = exonLocations;
        this.aliasName = aliasName;
    }

    public String getName() {
        return name;
    }

    public String getChr() {
        return chr;
    }

    public Strand getStrand() {
        return strand;
    }

    public Location getTxLocation() {
        return txLocation;
    }

    public Location getCdsLocation() {
        return cdsLocation;
    }

    public Location[] getExonLocations() {
        return exonLocations;
    }

    public String getAliasName() {
        return aliasName;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Get human readable locus string. It is assumed that this.start and
     * this.end are 0-based and end-exclusive, and the returned string is
     * 1-based and end-inclusive. So basically we just add 1 to the start.
     *
     * @return
     */
    public String getLocusString() {
        return getChr() + ":" + (txLocation.getStart() + 1) + "-"
                + txLocation.getEnd();
    }

    @Override
    public int compareTo(Feature o) {
        // TODO Auto-generated method stub
        if (txLocation.getStart() != o.getTxLocation().getStart()) {
            return (txLocation.getStart() - o.getTxLocation().getStart());
        } else if (txLocation.getEnd() != o.getTxLocation().getEnd()) {
            return (txLocation.getEnd() - o.getTxLocation().getEnd());
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return name + "\t" + chr + "\t" + Strand.parseStrand(strand) + "\t"
                + txLocation.toString() + "\t" + cdsLocation.toString() + "\t"
                + toExonString() + "\t" + aliasName;
    }

    public int getTotalLength() {
        return txLocation.getEnd() - txLocation.getStart();
    }

    public String toExonString() {
        StringBuffer string = new StringBuffer();
        for (Location exon : exonLocations) {
            string.append(exon.toString());
            string.append(",");
        }
        string.deleteCharAt(string.length() - 1);
        return string.toString();
    }

}
