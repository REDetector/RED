/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.datatypes.feature;

import com.xl.datatypes.sequence.Location;
import com.xl.utils.Strand;

import java.io.Serializable;
import java.util.List;

/**
 * A Feature represents a strip of information obtained from UCSC gene annotation file(i.e., gene.gtf).
 */
public class Feature implements Comparable<Feature>, Serializable {

    /**
     * The feature name.
     */
    private String name = null;
    /**
     * The chromosome name.
     */
    private String chr = null;
    /**
     * The strand of this feature.
     */
    private Strand strand = null;
    /**
     * The location, including transcribed region, coding region and exon region.
     */
    private List<Location> allLocations;
    /**
     * The alias name(e.g. gene_id from GTF)
     */
    private String aliasName = null;

    public Feature(String name, String chr, Strand strand, List<Location> allLocations, String aliasName) {
        this.name = name;
        this.chr = chr;
        this.strand = strand;
        this.allLocations = allLocations;
        this.aliasName = aliasName;
    }

    public boolean isInFeature(int position) {
        return getTxLocation().getStart() <= position && getTxLocation().getEnd() >= position;
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

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Feature o) {
        Location txLocation = allLocations.get(0);
        if (txLocation.getStart() != o.getTxLocation().getStart()) {
            return (txLocation.getStart() - o.getTxLocation().getStart());
        } else if (txLocation.getEnd() != o.getTxLocation().getEnd()) {
            return (txLocation.getEnd() - o.getTxLocation().getEnd());
        } else {
            return 0;
        }
    }

    public int getTotalLength() {
        return getTxLocation().getEnd() - getTxLocation().getStart();
    }

}
