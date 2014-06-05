package com.xl.datatypes.genome;

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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xl.datatypes.annotation.AnnotationCollection;
import com.xl.datatypes.annotation.Cytoband;
import com.xl.datatypes.sequence.Sequence;
import com.xl.utils.ChromosomeComparator;
import com.xl.utils.ChromosomeUtils;
import com.xl.utils.ChromosomeWithOffset;

/**
 * The Class Genome represents an entire annotated genome assembly
 */
public class Genome {

	public static final int MAX_WHOLE_GENOME = 10000;

	/** The species. */
	private String genomeId;

	/** The assembly. */
	private String displayName;

	private List<String> chromosomeNames = null;

	private List<String> longChromosomeNames;

	private LinkedHashMap<String, Chromosome> chromosomeMap = null;

	private long totalLength = -1;

	private long nominalLength = -1;

	private Map<String, Long> cumulativeOffsets = new HashMap<String, Long>();

	private Map<String, String> chrAliasTable = null;

	/** The annotation collection. */
	private AnnotationCollection annotationCollection = null;

	private Sequence sequence = null;

	private String species = null;

	public Genome(String genomeId, String displayName, Sequence sequence,
			boolean chromosOrdered) {
		this.genomeId = genomeId;
		this.displayName = displayName;
		this.sequence = sequence;
		this.chrAliasTable = new HashMap<String, String>();
		chromosomeNames = sequence.getChromosomeNames();
		int chromosomeLength = chromosomeNames.size();
		System.out.println(this.getClass().getName()+":"+chromosomeLength);

		List<Chromosome> tmpChromosomes = null;
		if (!chromosOrdered) {
			tmpChromosomes = new ArrayList<Chromosome>(chromosomeLength);
		}
		int maxLength = -1;
		chromosomeMap = new LinkedHashMap<String, Chromosome>(chromosomeLength);

		for (int i = 0; i < chromosomeLength; i++) {
			String chr = chromosomeNames.get(i);
			int length = sequence.getChromosomeLength(chr);
			maxLength = Math.max(maxLength, length);
			Chromosome chrom = new Chromosome(chr, length);
			if (chromosOrdered) {
				chromosomeMap.put(chr, chrom);
			} else {
				tmpChromosomes.add(chrom);
			}
		}

		if (!chromosOrdered) {
			ChromosomeComparator.sortChromosomeList(tmpChromosomes,
					maxLength / 10, chromosomeMap);
			chromosomeNames = new ArrayList<String>(chromosomeMap.keySet());
		}

		chrAliasTable.putAll(getAutoAliases());

		// Initial Annotation for Genome.
		annotationCollection = new AnnotationCollection(this);

	}

	public String getChromosomeAlias(String str) {
		if (str == null) {
			return str;
		} else {
			// We intern strings used as chromosomes
			// to prevent storing multiple times
			if (!chrAliasTable.containsKey(str)) {
				chrAliasTable.put(str, str);
			}
			return chrAliasTable.get(str);
		}
	}

	public Map<String, String> getChrAliasTable() {
		return chrAliasTable;
	}

	/**
	 * Populate the chr alias table. The input is a collection of chromosome
	 * synonym lists. The directionality is determined by the "true" chromosome
	 * names.
	 * 
	 * @param synonymsList
	 */
	public void addChrAliases(Collection<Collection<String>> synonymsList) {

		if (chrAliasTable == null)
			chrAliasTable = new HashMap<String, String>();

		// Convert names to a set for fast "contains" testing.
		Set<String> chrNameSet = new HashSet<String>(chromosomeNames);

		for (Collection<String> synonyms : synonymsList) {

			// Find the chromosome name as used in this genome
			String chr = null;
			for (String syn : synonyms) {
				if (chrNameSet.contains(syn)) {
					chr = syn;
					break;
				}
			}

			// If found register aliases
			if (chr != null) {
				for (String syn : synonyms) {
					chrAliasTable.put(syn, chr);
				}
			} else {
				// Nothing to do. SHould this be logged?
			}
		}
	}

	private Map<String, String> getAutoAliases() {

		Map<String, String> autoAliases = new HashMap<String, String>();

		for (String name : chromosomeNames) {
			if (name.startsWith("gi|")) {
				// NCBI
				String alias = getNCBIName(name);
				autoAliases.put(alias, name);
			}
		}

		if (chromosomeNames.size() < 10000) {
			for (String name : chromosomeNames) {

				// UCSC Conventions
				if (name.toLowerCase().startsWith("chr")) {
					autoAliases.put(name.substring(3), name);
				} else {
					autoAliases.put("chr" + name, name);
				}
			}

			// These are legacy mappings, these are now defined in the genomes
			// alias file
			if (genomeId.startsWith("hg")
					|| genomeId.equalsIgnoreCase("1kg_ref"))

			{
				autoAliases.put("23", "chrX");
				autoAliases.put("24", "chrY");
				autoAliases.put("MT", "chrM");
			} else if (genomeId.startsWith("mm"))

			{
				autoAliases.put("21", "chrX");
				autoAliases.put("22", "chrY");
				autoAliases.put("MT", "chrM");
			} else if (genomeId.equals("b37"))

			{
				autoAliases.put("chrM", "MT");
				autoAliases.put("chrX", "23");
				autoAliases.put("chrY", "24");

			}

			Collection<Map.Entry<String, String>> aliasEntries = new ArrayList<Map.Entry<String, String>>(
					autoAliases.entrySet());
			for (Map.Entry<String, String> aliasEntry : aliasEntries) {
				// Illumina conventions
				String alias = aliasEntry.getKey();
				String chr = aliasEntry.getValue();
				if (!alias.endsWith(".fa")) {
					String illuminaName = alias + ".fa";
					autoAliases.put(illuminaName, chr);
				}
				if (!chr.endsWith(".fa")) {
					String illuminaName = chr + ".fa";
					autoAliases.put(illuminaName, chr);
				}
			}
		}
		return autoAliases;
	}

	/**
	 * Extract the user friendly name from an NCBI accession example:
	 * gi|125745044|ref|NC_002229.3| => NC_002229.3
	 */
	public static String getNCBIName(String name) {

		String[] tokens = name.split("\\|");
		return tokens[tokens.length - 1];
	}

	public Chromosome getChromosome(String chrName) {
		return chromosomeMap.get(chrName);
	}

	public List<String> getAllChromosomeNamesLists() {
		return chromosomeNames;
	}

	public String[] getAllChromosomeNamesString() {
		return chromosomeNames.toArray(new String[0]);
	}

	public Chromosome[] getAllChromosomes() {
		return chromosomeMap.values().toArray(new Chromosome[0]);
	}

	public Collection<Chromosome> getChromosomes() {
		return chromosomeMap.values();
	}

	public long getTotalLength() {
		if (totalLength < 0) {
			totalLength = 0;
			for (Chromosome chr : chromosomeMap.values()) {
				totalLength += chr.getLength();
			}
		}
		return totalLength;
	}

	public long getCumulativeOffset(String chr) {

		Long cumOffset = cumulativeOffsets.get(chr);
		if (cumOffset == null) {
			long offset = 0;
			for (String c : getLongChromosomeNames()) {
				if (chr.equals(c)) {
					break;
				}
				offset += getChromosome(c).getLength();
			}
			cumOffset = new Long(offset);
			cumulativeOffsets.put(chr, cumOffset);
		}
		return cumOffset.longValue();
	}

	/**
	 * Covert the chromosome coordinate in basepairs to genome coordinates in
	 * kilo-basepairs
	 * 
	 * @param chr
	 * @param locationBP
	 * @return The overall genome coordinate, in kilo-bp
	 */
	public int getGenomeCoordinate(String chr, int locationBP) {
		return (int) ((getCumulativeOffset(chr) + locationBP) / 1000);
	}

	/**
	 * Translate a genome coordinate, in kilo-basepairs, to a chromosome &
	 * position in basepairs.
	 * 
	 * @param genomeKBP
	 *            The "genome coordinate" in kilo-basepairs. This is the
	 *            distance in kbp from the start of the first chromosome.
	 * @return the position on the corresponding chromosome
	 */
	public ChromosomeWithOffset getChromosomeCoordinate(int genomeKBP) {

		int cumOffset = 0;
		List<String> wgChrNames = getLongChromosomeNames();
		for (String c : wgChrNames) {
			int chrLen = getChromosome(c).getLength();
			if ((cumOffset + chrLen) / 1000 > genomeKBP) {
				int bp = genomeKBP * 1000 - cumOffset;
				return new ChromosomeWithOffset(getChromosome(c), bp);
			}
			cumOffset += chrLen;
		}
		String c = wgChrNames.get(wgChrNames.size() - 1);
		int bp = (genomeKBP - cumOffset) * 1000;
		return new ChromosomeWithOffset(getChromosome(c), bp);
	}

	/**
	 * Method description
	 * 
	 * @return
	 */
	public String getGenomeId() {
		return genomeId;
	}

	public String getSpecies() {
		if (species == null) {
			species = Genome.getSpeciesForID(genomeId);
		}
		return species;
	}

	public String getNextChrName(String chr) {
		List<String> chrList = getLongChromosomeNames();
		for (int i = 0; i < chrList.size() - 1; i++) {
			if (chrList.get(i).equals(chr)) {
				return chrList.get(i + 1);
			}
		}
		return null;
	}

	public String getPrevChrName(String chr) {
		List<String> chrList = getLongChromosomeNames();
		for (int i = chrList.size() - 1; i > 0; i--) {
			if (chrList.get(i).equals(chr)) {
				return chrList.get(i - 1);
			}
		}
		return null;
	}

	/**
	 * Return the nucleotide sequence on the + strand for the genomic interval.
	 * This method can return null if sequence is not available.
	 * 
	 * @param chr
	 * @param start
	 *            start position in "zero-based" coordinates
	 * @param end
	 *            end position
	 * @return sequence, or null if not available
	 */
	public byte[] getSequence(String chr, int start, int end) {

		if (sequence == null) {
			return null;
		}

		Chromosome c = getChromosome(chr);
		if (c == null) {
			return null;
		}
		end = Math.min(end, c.getLength());
		if (end <= start) {
			int temp = end;
			end = start;
			start = temp;
		}
		return sequence.getSequence(chr, start, end);
	}

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Return the reference base at the given position. Can return null if
	 * reference sequence is unknown
	 * 
	 * @param chr
	 * @param pos
	 * @return the reference base, or null if unknown
	 */
	public byte getReference(String chr, int pos) {
		return sequence == null ? null : sequence.getBase(chr, pos);
	}

	public void setCytobands(LinkedHashMap<String, List<Cytoband>> chrCytoMap) {

		for (Map.Entry<String, List<Cytoband>> entry : chrCytoMap.entrySet()) {
			String chr = entry.getKey();
			List<Cytoband> cytobands = entry.getValue();

			Chromosome chromosome = chromosomeMap.get(chr);
			if (chromosome != null) {
				chromosome.setCytobands(cytobands);
			}
		}
	}

	// public void setGeneTrack(FeatureTrack geneFeatureTrack) {
	// this.geneTrack = geneFeatureTrack;
	// }
	//
	// /**
	// * Return the annotation track associated with this genome. Can return
	// null
	// *
	// * @return a FeatureTrack, or null
	// */
	// public FeatureTrack getGeneTrack() {
	// return geneTrack;
	// }
	public Chromosome[] getLongChromosomes() {
		List<String> longChromosomeNames = getLongChromosomeNames();
		Chromosome[] chrs = new Chromosome[longChromosomeNames.size()];
		for (int i = 0; i < longChromosomeNames.size(); i++) {
			chrs[i] = getChromosome(longChromosomeNames.get(i));
		}
		return chrs;
	}

	public Chromosome[] getStandardChromosomes() {
		List<String> chrNames = getAllChromosomeNamesLists();
		List<String> chrNameSet = new ArrayList<String>();
		List<Chromosome> lists = new ArrayList<Chromosome>();
		for (String chrName : chrNames) {
			String standardName = ChromosomeUtils
					.getStandardChromosomeName(chrName);
			if (standardName!=null&&!chrNameSet.contains(standardName)) {
				chrNameSet.add(standardName);
			}
		}
		for(String chrName:chrNameSet){
			lists.add(getChromosome(chrName));
		}
//		Collections.sort(chrNameSet, ChromosomeNameComparator.getInstance());
		
		return lists.toArray(new Chromosome[0]);
	}

	/**
	 * Return "getChromosomeNames()" with small chromosomes removed.
	 * 
	 * @return
	 */
	public List<String> getLongChromosomeNames() {
		if (longChromosomeNames == null) {
			List<String> allChromosomeNames = getAllChromosomeNamesLists();
			longChromosomeNames = new ArrayList<String>(
					allChromosomeNames.size());
			long genomeLength = getTotalLength();
			int maxChromoLength = -1;
			for (String chrName : allChromosomeNames) {
				Chromosome chr = getChromosome(chrName);
				int length = chr.getLength();
				maxChromoLength = Math.max(maxChromoLength, length);
				if (length > (genomeLength / 3000)) {
					longChromosomeNames.add(chrName);
				}
			}

			/**
			 * At this point, we should have some long chromosome names.
			 * However, some genomes (draft ones perhaps) maybe have many small
			 * ones which aren't big enough. We arbitrarily take those which are
			 * above half the size of the max, only if the first method didn't
			 * work.
			 */
			if (longChromosomeNames.size() == 0) {
				for (String chrName : allChromosomeNames) {
					Chromosome chr = getChromosome(chrName);
					int length = chr.getLength();
					if (length > maxChromoLength / 2) {
						longChromosomeNames.add(chrName);
					}
				}
			}
		}
		return longChromosomeNames;
	}

	public long getNominalLength() {
		if (nominalLength < 0) {
			nominalLength = 0;
			for (String chrName : getLongChromosomeNames()) {
				Chromosome chr = getChromosome(chrName);
				nominalLength += chr.getLength();
			}
		}
		return nominalLength;
	}

	/**
	 * Annotation collection.
	 * 
	 * @return the annotation collection
	 */
	public AnnotationCollection getAnnotationCollection() {
		return annotationCollection;
	}

	/**
	 * Gets the chromosome count.
	 * 
	 * @return the chromosome count
	 */
	public int getAllChromosomeCount() {
		return chromosomeMap.size();
	}

	public int getLongChromosomeCount() {
		return longChromosomeNames.size();
	}

	/**
	 * Checks for chromosome.
	 * 
	 * @param c
	 *            the c
	 * @return true, if successful
	 */
	public boolean hasChromosome(Chromosome c) {
		if (chromosomeMap.containsValue(c)) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return displayName;
	}

	/**
	 * Gets the longest chromosome length.
	 * 
	 * @return the longest chromosome length
	 */
	public int getLongestChromosomeLength() {
		int longest = 0;
		Chromosome[] chromosomes = chromosomeMap.values().toArray(
				new Chromosome[0]);
		for (int i = 0; i < chromosomes.length; i++) {
			if (chromosomes[i].getLength() > longest) {
				longest = chromosomes[i].getLength();
			}
		}
		return longest;
	}

	// TODO A hack (obviously), we need to record a species in the genome
	// definitions
	private static Map<String, String> ucscSpeciesMap;

	private static synchronized String getSpeciesForID(String id) {
		if (ucscSpeciesMap == null) {
			ucscSpeciesMap = new HashMap<String, String>();

			InputStream is = null;

			try {
				is = Genome.class.getResourceAsStream("speciesMapping.txt");
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));

				String nextLine;
				while ((nextLine = br.readLine()) != null) {
					if (nextLine.startsWith("#"))
						continue;
					String[] tokens = nextLine.split("\t");
					if (tokens.length == 2) {
						ucscSpeciesMap.put(tokens[0], tokens[1]);
					} else {
						// log.error("Unexpected number of tokens in species mapping file for line: "
						// + nextLine);
					}
				}
			} catch (IOException e) {
				// log.error("Error reading species mapping table", e);
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException e) {
						// log.error("", e);
					}
			}

		}

		for (Map.Entry<String, String> entry : ucscSpeciesMap.entrySet()) {
			if (id.startsWith(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

}
