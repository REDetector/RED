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
package com.xl.datatypes;

import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sites.Site;
import com.xl.utils.Strand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class DataGroup is a virtual DataStore which can combine one or more DataSets. It just contains the references of the combined DataSets and don't occupy
 * extra memory or local disk.
 */
public class DataGroup extends DataStore {

    /**
     * The data sets which this data group contains.
     */
    private DataSet[] dataSets;

    /**
     * Instantiates a new data group.
     *
     * @param name     the name
     * @param dataSets the data sets
     */
    public DataGroup(String name, DataSet[] dataSets) {
        super(name);
        this.dataSets = dataSets;
    }

    /**
     * Data sets.
     *
     * @return the data set[]
     */
    public DataSet[] dataSets() {
        return dataSets;
    }

    /**
     * Sets the data sets.
     *
     * @param sets the new data sets
     */
    public void setDataSets(DataSet[] sets) {
        dataSets = sets;
        if (collection() != null) {
            collection().dataGroupSamplesChanged(this);
        }
    }

    /**
     * Contains data set.
     *
     * @param s the s
     * @return true, if successful
     */
    public boolean containsDataSet(DataSet s) {
        for (DataSet dataSet : dataSets) {
            if (dataSet == s) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the data set.
     *
     * @param s the s
     */
    public void removeDataSet(DataSet s) {
        if (!containsDataSet(s)) return;

        DataSet[] newSet = new DataSet[dataSets.length - 1];
        int j = 0;
        for (DataSet dataSet : dataSets) {
            if (dataSet == s) {
                continue;
            }
            newSet[j++] = dataSet;
        }

        dataSets = newSet;

        if (collection() != null) {
            collection().dataGroupSamplesChanged(this);
        }
    }

    public List<Location> getReadsForSite(Site p) {
        List<Location> allReads = new ArrayList<Location>();
        for (DataSet dataSet : dataSets) {
            allReads.addAll(dataSet.getReadsForSite(p));
        }
        Collections.sort(allReads);
        return allReads;
    }

    public List<Location> getReadsForChromosome(String chr) {
        List<Location> allReads = new ArrayList<Location>();
        for (DataSet dataSet : dataSets) {
            allReads.addAll(dataSet.getReadsForChromosome(chr));
        }
        Collections.sort(allReads);
        return allReads;
    }

    public int getReadCountForChromosome(String chr) {
        int count = 0;
        for (DataSet dataSet : dataSets) {
            count += dataSet.getReadCountForChromosome(chr);
        }
        return count;
    }

    public int getTotalReadCount() {
        int count = 0;
        for (DataSet dataSet : dataSets) {
            count += dataSet.getTotalReadCount();
        }
        return count;
    }

    public int getReadCountForStrand(Strand strand) {
        int count = 0;
        for (DataSet dataSet : dataSets) {
            count += dataSet.getReadCountForStrand(strand);
        }
        return count;
    }

    public long getTotalReadLength() {
        long count = 0;
        for (DataSet dataSet : dataSets) {
            count += dataSet.getTotalReadLength();
        }
        return count;
    }

    public void setName(String name) {
        super.setName(name);
        if (collection() != null) {
            collection().dataStoreRenamed(this);
        }
    }

}
