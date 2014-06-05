package com.xl.datatypes.genome;

import java.io.File;

public class GenomeDescripter {

	private static GenomeDescripter genomeDescripter = null;
	private String name;
	private boolean chrNamesAltered;
	private String id;
	protected String cytoBandFileName;
	protected String geneFileName;
	protected String chrAliasFileName;
	private String geneTrackName;
	private String url;
	private String sequenceLocation;
	private boolean hasCustomSequenceLocation;
	private boolean chromosomesAreOrdered = false;
	private boolean fasta = false;
	private boolean fastaDirectory = false;
	private String[] fastaFileNames;

	private GenomeDescripter() {
	}

	public void setAttributes(String name, boolean chrNamesAltered, String id,
			String cytoBandFileName, String geneFileName,
			String chrAliasFileName, String geneTrackName, String url,
			String sequenceLocation, boolean hasCustomSequenceLocation,
			boolean chromosomesAreOrdered, boolean fasta,
			boolean fastaDirectory, String fastaFileNameString) {
		this.chrNamesAltered = chrNamesAltered;
		this.name = name;
		this.id = id;
		this.cytoBandFileName = cytoBandFileName;
		this.geneFileName = geneFileName;
		this.chrAliasFileName = chrAliasFileName;
		this.geneTrackName = geneTrackName;
		this.sequenceLocation = sequenceLocation;
		this.hasCustomSequenceLocation = hasCustomSequenceLocation;
		this.chromosomesAreOrdered = chromosomesAreOrdered;
		this.fasta = fasta;
		this.fastaDirectory = fastaDirectory;
		if (fastaFileNameString != null) {
			fastaFileNames = fastaFileNameString.split(",");
		}

		// Fix for legacy .genome files
		if (sequenceLocation != null && sequenceLocation.startsWith("/")) {
			if (!(new File(sequenceLocation)).exists()) {
				String tryThis = sequenceLocation.replaceFirst("/", "");
				if ((new File(tryThis)).exists()) {
					this.sequenceLocation = tryThis;
				}
			}
		}
	}

	public static GenomeDescripter getInstance() {
		if (genomeDescripter == null) {
			genomeDescripter = new GenomeDescripter();
		}
		return genomeDescripter;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isChrNamesAltered() {
		return chrNamesAltered;
	}

	public void setChrNamesAltered(boolean chrNamesAltered) {
		this.chrNamesAltered = chrNamesAltered;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCytoBandFileName() {
		return cytoBandFileName;
	}

	public void setCytoBandFileName(String cytoBandFileName) {
		this.cytoBandFileName = cytoBandFileName;
	}

	public boolean hasCytobands() {
		return cytoBandFileName != null && cytoBandFileName.length() > 0;
	}

	public String getGeneFileName() {
		return geneFileName;
	}

	public void setGeneFileName(String geneFileName) {
		this.geneFileName = geneFileName;
	}

	public String getChrAliasFileName() {
		return chrAliasFileName;
	}

	public void setChrAliasFileName(String chrAliasFileName) {
		this.chrAliasFileName = chrAliasFileName;
	}

	public String getGeneTrackName() {
		if (geneTrackName == null) {
			return name;
		} else {
			return geneTrackName;
		}
	}

	public void setGeneTrackName(String geneTrackName) {
		this.geneTrackName = geneTrackName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSequenceLocation() {
		return sequenceLocation;
	}

	public void setSequenceLocation(String sequenceLocation) {
		this.sequenceLocation = sequenceLocation;
	}

	public boolean isHasCustomSequenceLocation() {
		return hasCustomSequenceLocation;
	}

	public void setHasCustomSequenceLocation(boolean hasCustomSequenceLocation) {
		this.hasCustomSequenceLocation = hasCustomSequenceLocation;
	}

	public boolean isChromosomesAreOrdered() {
		return chromosomesAreOrdered;
	}

	public void setChromosomesAreOrdered(boolean chromosomesAreOrdered) {
		this.chromosomesAreOrdered = chromosomesAreOrdered;
	}

	public boolean isFasta() {
		return fasta;
	}

	public void setFasta(boolean fasta) {
		this.fasta = fasta;
	}

	public boolean isFastaDirectory() {
		return fastaDirectory;
	}

	public void setFastaDirectory(boolean fastaDirectory) {
		this.fastaDirectory = fastaDirectory;
	}

	public String[] getFastaFileNames() {
		return fastaFileNames;
	}

	public void setFastaFileNames(String[] fastaFileNames) {
		this.fastaFileNames = fastaFileNames;
	}

}
