/**
 * Copyright 2010-13 Simon Andrews
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
import com.xl.exception.REDException;
import com.xl.utils.LocationSorter;
import com.xl.utils.Strand;


/**
 * A replicate set is a way to group together data stores
 * which are biological replicates of each other.  Unlike
 * DataStores replicate sets do not store their own
 * quantitations but simply aggregate the distribution of
 * quantitated values from their component members.
 * 
 */
public class ReplicateSet extends DataStore{

	/** The data stores. */
	private DataStore [] dataStores;

	
	/**
	 * Instantiates a new replicate set.
	 * 
	 * @param name the name
	 * @param dataStores the data stores
	 */
	public ReplicateSet (String name, DataStore [] dataStores) {
		super(name);
		this.dataStores = dataStores;
	}
		
	/**
	 * Data stores.
	 * 
	 * @return the data store[]
	 */
	public DataStore [] dataStores () {
		return dataStores;
	}
	
	
	/**
	 * Sets the data sets.
	 * 
	 * @param sets the new data sets
	 */
	public void setDataStores (DataStore [] stores) {
		dataStores = stores;
		if (collection() != null) {
			collection().replicateSetStoresChanged(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#setName(java.lang.String)
	 */
	public void setName (String name) {
		super.setName(name);
		if (collection() != null) {
			collection().replicateSetRenamed(this);
		}
	}

	
	/**
	 * Contains data store.
	 * 
	 * @param s the s
	 * @return true, if successful
	 */
	public boolean containsDataStore (DataStore s) {
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i]==s)
				return true;
		}
		return false;
	}
	
	/**
	 * Removes a data store.
	 * 
	 * @param s the s
	 */
	public void removeDataStore (DataStore s) {
		if (! containsDataStore(s)) return;
		
		DataStore [] newSet = new DataStore[dataStores.length-1];
		int j=0;
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i] == s) continue;
			newSet[j] = dataStores[i];
			j++;
		}
		
		dataStores = newSet;
		
		if (collection() != null) {
			collection().replicateSetStoresChanged(this);
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadCountForChromosome(uk.ac.babraham.SeqMonk.DataTypes.Genome.Chromosome)
	 */
	public int getReadCountForChromosome(String c) {
		int count = 0;
		for (int i=0;i<dataStores.length;i++) {
			count += dataStores[i].getReadCountForChromosome(c);
		}
		return count;
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadsForChromsome(uk.ac.babraham.SeqMonk.DataTypes.Genome.Chromosome)
	 */
	public SequenceRead[] getReadsForChromosome(String c) {
		SequenceRead [][] readsFromAllChrs = new SequenceRead[dataStores.length][];
		
		for (int i=0;i<dataStores.length;i++) {
			readsFromAllChrs[i] = dataStores[i].getReadsForChromosome(c);
		}
		
		return LocationSorter.sortLocationSets(readsFromAllChrs);
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getTotalReadCount()
	 */
	public int getTotalReadCount() {
		int count = 0;
		for (int i=0;i<dataStores.length;i++) {
			count += dataStores[i].getTotalReadCount();
		}
		return count;
	}
	
	public long getTotalPairCount () {
		return getTotalReadCount()/2;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadCountForStrand()
	 */
	public int getReadCountForStrand(Strand strand) {
		int count = 0;
		for (int i=0;i<dataStores.length;i++) {
			count += dataStores[i].getReadCountForStrand(strand);
		}
		return count;
	}


	/* (non-Javadoc)
	 * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getTotalReadLength()
	 */
	public long getTotalReadLength() {
		long count = 0;
		for (int i=0;i<dataStores.length;i++) {
			count += dataStores[i].getTotalReadLength();
		}
		return count;
	}
	
	public int getMaxReadLength() {

		int max = 0;
		for (int i=0;i<dataStores.length;i++) {
			if (i==0 || dataStores[i].getMaxReadLength() > max) max = dataStores[i].getMaxReadLength();
		}

		return max;
	}

	public int getMinReadLength() {
		int min = 0;
		for (int i=0;i<dataStores.length;i++) {
			if (i==0 || dataStores[i].getMinReadLength() < min) min = dataStores[i].getMinReadLength();
		}

		return min;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.SeqMonk.DataTypes.DataStore#getReadsForProbe(uk.ac.babraham.SeqMonk.DataTypes.Probes.Probe)
	 */
	public SequenceRead[] getReadsForProbe(Probe p) {
		SequenceRead [][] returnReads = new SequenceRead [dataStores.length][];
		for (int i=0;i<dataStores.length;i++) {
			returnReads[i] = dataStores[i].getReadsForProbe(p);
		}
		return LocationSorter.sortLocationSets(returnReads);
	}
	
	
	/**
	 * Checks if is quantitated.  Only true if all of the stores
	 * in this set are quantitated.
	 * 
	 * @return true, if is quantitated
	 */
	public boolean isQuantitated () {
		
		if (dataStores.length == 0) return false;
		
		for (int i=0;i<dataStores.length;i++) {
			if (!dataStores[i].isQuantitated()) {
				return false;
			}
		}
		
		return true;	
	}
	
	/**
	 * Sets the value for probe.  You can't do this to a replicate set
	 * since it doesn't store probe values, so this will always throw
	 * an error.
	 * 
	 * @param p the p
	 * @param f the f
	 */
	public void setValueForProbe (Probe p, float f) {
		throw new IllegalArgumentException("You can't set probe values for a replicate set");
	}
	
	/**
	 * Checks whether we have a value for this probe.  This is only
	 * true if all of the data stores in this replicate set have a
	 * value for this probe
	 * 
	 * @param p the p
	 * @return true, if successful
	 */
	public boolean hasValueForProbe (Probe p) {

		for (int i=0;i<dataStores.length;i++) {
			if (!dataStores[i].hasValueForProbe(p)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Gets the mean value for this probe across all of the
	 * data stores underlying this set.
	 * 
	 * @param p the p
	 * @return the mean value for probe
	 * @throws REDException the seq monk exception
	 */
	public float getValueForProbe(Probe p) throws REDException {
		
		if (! hasValueForProbe(p)) {
			throw new REDException("No quantitation for probe "+p+" in "+name());			
		}
		
		if (dataStores.length == 0) {
			return 0;
		}

		float total = 0;
		for (int i=0;i<dataStores.length;i++) {
			total += dataStores[i].getValueForProbe(p);
		}
		
		return total/dataStores.length;
		
	}

}
