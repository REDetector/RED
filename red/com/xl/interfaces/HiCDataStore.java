package com.xl.interfaces;

import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.sequence.HiCHitCollection;

public interface HiCDataStore {

	
	/**
	 * This method is used by collections (Groups, Replicate Sets) where
	 * they may or may not be composed of HiC data stores. They can implement
	 * this interface but then use this method to say if they can currently
	 * provide HiC information.
	 * 
	 * @return
	 */
	public boolean isValidHiC();
	
	public HiCHitCollection getHiCReadsForProbe (Probe p);
	
	public int getHiCReadCountForProbe(Probe p);
	
	public int getHiCReadCountForChromosome(String c);
	
	public HiCHitCollection getHiCReadsForChromosome (String c);
	
	/**
	 * This method is used by classes which want to export a non redundant
	 * set of read pairs, normally for saving or reimporting.  In normal
	 * HiC data structures each pair is duplicated to allow lookups in both
	 * the forward and reverse direction.  This call will return only one
	 * of these, but will not necessarily preserve the order (read 1 vs read 2)
	 * of the original import
	 * @param c The chromosome to query
	 * @return A hit collection of the reads to export
	 */
	public HiCHitCollection getExportableReadsForChromosome (String c);
	
	public String name();
	
	/*
	 * Returns the total number of hiC pairs in this data.  Should be half the
	 * number of reads
	 */
	public long getTotalPairCount ();
	
	public float getCorrectionForLength(String c, int minDist, int maxDist);
	
	/**
	 * Counts the number of reads (fragment ends) on this chromosome which participate
	 * in a cis interaction.
	 * 
	 * @param c the chromosome
	 * @return the number of reads in cis
	 */
	public int getCisCountForChromosome (String c);

	/**
	 * Counts the number of reads (fragment ends) on this chromosome which participate
	 * in a trans interaction.
	 * 
	 * @param c the chromosome
	 * @return the number of reads in trans
	 */
	public int getTransCountForChromosome (String c);

	/**
	 * Gets the total number of reads in cis in the whole genome
	 * 
	 * @return
	 */
	public int getCisCount();
	
	/**
	 * Gets the total number of reads in trans in the whole genome
	 * @return
	 */
	public int getTransCount();
}
