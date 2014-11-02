/**
 * Copyright Copyright 2007-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.xl.datatypes;

import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sites.Site;
import com.xl.utils.Strand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class DataGroup is a virtual DataStore which can combine
 * one or more DataSets.  It does not store read information, but
 * does store its own quantitated data.
 */
public class DataGroup extends DataStore {

    /**
     * The data sets.
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

    public void setName(String name) {
        super.setName(name);
        if (collection() != null) {
            collection().dataGroupRenamed(this);
        }
    }


    /**
     * Contains data set.
     *
     * @param s the s
     * @return true, if successful
     */
    public boolean containsDataSet(DataSet s) {
        for (int i = 0; i < dataSets.length; i++) {
            if (dataSets[i] == s)
                return true;
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
        for (int i = 0; i < dataSets.length; i++) {
            if (dataSets[i] == s) continue;
            newSet[j] = dataSets[i];
            j++;
        }

        dataSets = newSet;

        if (collection() != null) {
            collection().dataGroupSamplesChanged(this);
        }
    }

    public int getReadCountForChromosome(String chr) {
        int count = 0;
        for (int i = 0; i < dataSets.length; i++) {
            count += dataSets[i].getReadCountForChromosome(chr);
        }
        return count;
    }

    public List<Location> getReadsForChromosome(String chr) {
        List<Location> allReads = new ArrayList<Location>();
        for (DataSet dataSet : dataSets) {
            allReads.addAll(dataSet.getReadsForChromosome(chr));
        }
        Collections.sort(allReads);
        return allReads;
    }

    public int getTotalReadCount() {
        int count = 0;
        for (int i = 0; i < dataSets.length; i++) {
            count += dataSets[i].getTotalReadCount();
        }
        return count;
    }

    public int getReadCountForStrand(Strand strand) {
        int count = 0;
        for (int i = 0; i < dataSets.length; i++) {
            count += dataSets[i].getReadCountForStrand(strand);
        }
        return count;
    }

    public long getTotalReadLength() {
        long count = 0;
        for (int i = 0; i < dataSets.length; i++) {
            count += dataSets[i].getTotalReadLength();
        }
        return count;
    }

    public List<Location> getReadsForSite(Site p) {
        List<Location> allReads = new ArrayList<Location>();
        for (DataSet dataSet : dataSets) {
            allReads.addAll(dataSet.getReadsForSite(p));
        }
        Collections.sort(allReads);
        return allReads;
    }

}
