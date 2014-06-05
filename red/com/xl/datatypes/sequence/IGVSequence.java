package com.xl.datatypes.sequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.samtools.seekablestream.SeekableStream;

import com.xl.datatypes.annotation.Cytoband;
import com.xl.datatypes.fasta.FastaIndexedSequence;
import com.xl.utils.ChromosomeNameComparator;
import com.xl.utils.ParsingUtils;

/**
 * Represents a sequence database composed of plain text files with no white
 * space, one per chromosome, in a directory. This is the original IGV
 * "sequence" format, replaced in favor of indexed fasta files.
 * 
 * @author jrobinso
 * @Date 8/8/11
 */

public class IGVSequence implements Sequence {

	private String dirPath;
	private Map<String, String> chrFileNameCache = new HashMap<String, String>();
	private HashMap<String, Integer> chromosomeLengths;
	private List<String> chromosomeNames;

	public IGVSequence(String dirPath) {
		if (!dirPath.endsWith("/")) {
			dirPath = dirPath + "/";
		}
		this.dirPath = dirPath;
	}

	public byte[] getSequence(String chr, int start, int end) {

		String fn = getChrFileName(chr);
		String seqFile = dirPath + fn;

		SeekableStream is = null;
		try {

			is = ParsingUtils.getStreamFor(seqFile);

			byte[] bytes = new byte[end - start];
			is.seek(start);
			is.read(bytes);
			return bytes;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ex) {
				}
			}
		}
	}

	@Override
	public byte getBase(String chr, int position) {
		throw new RuntimeException("getBase() is not implemented for class "
				+ FastaIndexedSequence.class.getName());
	}

	@Override
	public List<String> getChromosomeNames() {
		return chromosomeNames;
	}

	@Override
	public int getChromosomeLength(String chrname) {
		return chromosomeLengths.get(chrname);
	}

	/**
	 * Get a "legal" chromosome file name from the chr name. This method
	 * supports "old" style .genome files.
	 * 
	 * @param chr
	 * @return
	 */
	private String getChrFileName(String chr) {
		String chrFN = chrFileNameCache.get(chr);
		if (chrFN == null) {
			chrFN = chr;
			for (Map.Entry<String, String> entry : illegalChar.entrySet()) {
				chrFN = chrFN.replaceAll(entry.getValue(), entry.getKey());
			}
			chrFN += ".txt";
			chrFileNameCache.put(chr, chrFN);
		}
		return chrFN;
	}

	/**
	 * Generate chromosomes from the list of cytobands. This method is provided
	 * for backward compatibility for pre V2.1 genomes.
	 * 
	 * @param chrCytoMap
	 * @param chromosomesAreOrdered
	 */
	public void generateChromosomes(
			LinkedHashMap<String, List<Cytoband>> chrCytoMap,
			boolean chromosomesAreOrdered) {

		chromosomeLengths = new HashMap<String, Integer>();
		chromosomeNames = new ArrayList<String>(chrCytoMap.size());
		for (Map.Entry<String, List<Cytoband>> entry : chrCytoMap.entrySet()) {
			String chr = entry.getKey();
			chromosomeNames.add(chr);

			List<Cytoband> cytobands = entry.getValue();
			int length = cytobands.get(cytobands.size() - 1).getEnd();
			chromosomeLengths.put(chr, length);
		}

		if (!chromosomesAreOrdered) {
			Collections.sort(chromosomeNames,
					ChromosomeNameComparator.getInstance());
		}
	}

	static Map<String, String> illegalChar = new HashMap<String, String>();

	static {
		illegalChar.put("_qm_", "\\?");
		illegalChar.put("_fbr_", "\\[");
		illegalChar.put("_rbr_", "]");
		illegalChar.put("_fsl_", "/");
		illegalChar.put("_bsl_", "\\\\");
		illegalChar.put("_eq_", "=");
		illegalChar.put("_pl_", "\\+");
		illegalChar.put("_lt_", "<");
		illegalChar.put("_gt_", ">");
		illegalChar.put("_co_", ":");
		illegalChar.put("_sc_", ";");
		illegalChar.put("_dq_", "\"");
		illegalChar.put("_sq_", "'");
		illegalChar.put("_st_", "\\*");
		illegalChar.put("_pp_", "\\|");
	}

}
