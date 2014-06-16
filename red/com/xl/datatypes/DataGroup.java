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

import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.utils.LocationSorter;
import com.xl.utils.Strand;

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

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#setDisplayName(java.lang.String)
     */
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

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadCountForChromosome(uk.ac.babraham.SeqMonk.DataTypes.Genome.Chromosome)
     */
    public int getReadCountForChromosome(String chr) {
        int count = 0;
        for (int i = 0; i < dataSets.length; i++) {
            count += dataSets[i].getReadCountForChromosome(chr);
        }
        return count;
    }

    public boolean isQuantitated() {
        if (dataSets.length == 0) return false;

        return super.isQuantitated();
    }


    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadsForChromsome(uk.ac.babraham.SeqMonk.DataTypes.Genome.Chromosome)
     */
    public SequenceRead[] getReadsForChromosome(String chr) {

        SequenceRead[][] readsFromAllChrs = new SequenceRead[dataSets.length][];

//		int totalCount = 0;

        for (int i = 0; i < dataSets.length; i++) {
            readsFromAllChrs[i] = dataSets[i].getReadsForChromosome(chr);
        }

        return LocationSorter.sortLocationSets(readsFromAllChrs);

//		int [] currentIndices = new int[dataSets.length];
//		
//		long [] returnedReads = new long[totalCount];
//		
//		for (int i=0;i<returnedReads.length;i++) {	
//			// Add the lowest read to the full set
//			int lowestIndex = -1;
//			long lowestValue = 0;
//			for (int j=0;j<currentIndices.length;j++) {
//				if (currentIndices[j] == readsFromAllChrs[j].length) continue; // Skip datasets we've already emptied
//				if (lowestValue == 0 || SequenceRead.compare(readsFromAllChrs[j][currentIndices[j]],lowestValue) < 0) {
//					lowestIndex = j;
//					lowestValue = readsFromAllChrs[j][currentIndices[j]];
//				}
//			}
//			
//			returnedReads[i] = lowestValue;
//			currentIndices[lowestIndex]++;
//			
//		}
//		
//		return returnedReads;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getTotalReadCount()
     */
    public int getTotalReadCount() {
        int count = 0;
        for (int i = 0; i < dataSets.length; i++) {
            count += dataSets[i].getTotalReadCount();
        }
        return count;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadCountForStrand()
     */
    public int getReadCountForStrand(Strand strand) {
        int count = 0;
        for (int i = 0; i < dataSets.length; i++) {
            count += dataSets[i].getReadCountForStrand(strand);
        }
        return count;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getTotalReadLength()
     */
    public long getTotalReadLength() {
        long count = 0;
        for (int i = 0; i < dataSets.length; i++) {
            count += dataSets[i].getTotalReadLength();
        }
        return count;
    }

    public int getMaxReadLength() {

        int max = 0;
        for (int i = 0; i < dataSets.length; i++) {
            if (i == 0 || dataSets[i].getMaxReadLength() > max) max = dataSets[i].getMaxReadLength();
        }

        return max;
    }

    public int getMinReadLength() {
        int min = 0;
        for (int i = 0; i < dataSets.length; i++) {
            if (i == 0 || dataSets[i].getMinReadLength() < min) min = dataSets[i].getMinReadLength();
        }

        return min;
    }

    public long getTotalPairCount() {
        return getTotalReadCount() / 2;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadsForProbe(uk.ac.babraham.SeqMonk.DataTypes.Probes.Probe)
     */
    public SequenceRead[] getReadsForProbe(Probe p) {
        SequenceRead[][] returnReads = new SequenceRead[dataSets.length][];
        for (int i = 0; i < dataSets.length; i++) {
            returnReads[i] = dataSets[i].getReadsForProbe(p);
        }
        return LocationSorter.sortLocationSets(returnReads);
    }

}
