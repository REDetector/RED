package com.xl.datatypes.feature;

import com.xl.datatypes.sequence.Location;
import com.xl.utils.Strand;

import java.io.Serializable;
import java.util.List;

public class Feature implements Comparable<Feature>, Serializable {

    private String name = null;
    private String chr = null;
    private Strand strand = null;


    private List<Location> allLocations;
    private String aliasName = null;

    public Feature(String name, String chr, Strand strand, List<Location> allLocations, String aliasName) {
        this.name = name;
        this.chr = chr;
        this.strand = strand;
        this.allLocations = allLocations;
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
        return allLocations.get(0);
    }

    public Location getCdsLocation() {
        return allLocations.get(1);
    }

    public List<Location> getExonLocations() {
        return allLocations.subList(2, allLocations.size());
    }

    public List<Location> getAllLocations() {
        return allLocations;
    }


    public String getAliasName() {
        return aliasName;
    }

    /**
     * Get human readable locus string. It is assumed that this.start and
     * this.end are 0-based and end-exclusive, and the returned string is
     * 1-based and end-inclusive. So basically we just add 1 to the start.
     *
     * @return
     */
//    public String getLocusString() {
//        return getChr() + ":" + (txLocation.getStart() + 1) + "-" + txLocation.getEnd();
//    }
    @Override
    public int compareTo(Feature o) {
        // TODO Auto-generated method stub
        Location txLocation = allLocations.get(0);
        if (txLocation.getStart() != o.getTxLocation().getStart()) {
            return (txLocation.getStart() - o.getTxLocation().getStart());
        } else if (txLocation.getEnd() != o.getTxLocation().getEnd()) {
            return (txLocation.getEnd() - o.getTxLocation().getEnd());
        } else {
            return 0;
        }
    }

//    @Override
//    public String toString() {
//        return name + "\t" + chr + "\t" + Strand.parseStrand(strand) + "\t" + txLocation.toString() + "\t" + cdsLocation.toString() + "\t"
//                + toExonString() + "\t" + aliasName;
//    }

    public int getTotalLength() {
        return getTxLocation().getEnd() - getTxLocation().getStart();
    }

//    public String toExonString() {
//        StringBuilder stringBuilder = new StringBuilder();
//        for (Location exon : exonLocations) {
//            stringBuilder.append(exon.toString());
//            stringBuilder.append(",");
//        }
//        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
//        return stringBuilder.toString();
//    }

}
