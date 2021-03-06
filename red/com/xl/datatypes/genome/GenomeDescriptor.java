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

package com.xl.datatypes.genome;

import com.xl.utils.namemanager.GenomeUtils;

import java.io.File;

/**
 * The GenomeDescriptor is a class to describe a genome and its relative resources.
 */
public class GenomeDescriptor {

    private static GenomeDescriptor genomeDescriptor = null;
    protected String cytoBandFileName;
    protected String geneFileName;
    protected String chrAliasFileName;
    private String displayName;
    private boolean chrNamesAltered;
    private String genomeId;
    private String geneTrackName;
    private String url;
    private String sequenceLocation;
    private boolean hasCustomSequenceLocation;
    private boolean chromosomesAreOrdered = false;
    private boolean fasta = false;
    private boolean fastaDirectory = false;
    private String[] fastaFileNames;

    private GenomeDescriptor() {
    }

    public static GenomeDescriptor getInstance() {
        if (genomeDescriptor == null) {
            genomeDescriptor = new GenomeDescriptor();
        }
        return genomeDescriptor;
    }

    public void setAttributes(String displayName, boolean chrNamesAltered, String genomeId,
                              String cytoBandFileName, String geneFileName,
                              String chrAliasFileName, String geneTrackName, String url,
                              String sequenceLocation, boolean hasCustomSequenceLocation,
                              boolean chromosomesAreOrdered, boolean fasta,
                              boolean fastaDirectory, String fastaFileNameString) {
        this.chrNamesAltered = chrNamesAltered;
        this.displayName = displayName;
        this.genomeId = genomeId;
        this.cytoBandFileName = cytoBandFileName;
        this.geneFileName = geneFileName;
        this.chrAliasFileName = chrAliasFileName;
        this.geneTrackName = geneTrackName;
        this.url = url;
        this.sequenceLocation = sequenceLocation;
        this.hasCustomSequenceLocation = hasCustomSequenceLocation;
        this.chromosomesAreOrdered = chromosomesAreOrdered;
        this.fasta = fasta;
        this.fastaDirectory = fastaDirectory;
        if (fastaFileNameString != null && fastaFileNameString.trim().length() != 0) {
            fastaFileNames = fastaFileNameString.split(",");
        } else {
            fastaFileNames = null;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isChrNamesAltered() {
        return chrNamesAltered;
    }

    public void setChrNamesAltered(boolean chrNamesAltered) {
        this.chrNamesAltered = chrNamesAltered;
    }

    public String getGenomeId() {
        return genomeId;
    }

    public void setGenomeId(String string) {
        this.genomeId = string;
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
        if (geneTrackName != null) {
            return geneTrackName;
        } else if (displayName != null) {
            return displayName;
        } else {
            return "Feature Track";
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(GenomeUtils.KEY_DISPLAY_NAME + "=" + displayName + "\r\n" +
                GenomeUtils.KEY_CHR_NAMES_ALTERED + "=" + chrNamesAltered + "\r\n" +
                GenomeUtils.KEY_GENOME_ID + "=" + genomeId + "\r\n" +
                GenomeUtils.KEY_CYTOBAND_FILE_NAME + "=" + cytoBandFileName + "\r\n" +
                GenomeUtils.KEY_GENE_FILE_NAME + "=" + geneFileName + "\r\n" +
                GenomeUtils.KEY_CHR_ALIAS_FILE_NAME + "=" + chrAliasFileName + "\r\n" +
                GenomeUtils.KEY_GENE_TRACK_NAME + "=" + geneTrackName + "\r\n" +
                GenomeUtils.KEY_URL + "=" + url + "\r\n" +
                GenomeUtils.KEY_SEQUENCE_LOCATION + "=" + sequenceLocation + "\r\n" +
                GenomeUtils.KEY_HAS_CUSTOM_SEQUENCE_LOCATION + "=" + hasCustomSequenceLocation + "\r\n" +
                GenomeUtils.KEY_CHROMOSOMES_ARE_ORDERED + "=" + chromosomesAreOrdered + "\r\n" +
                GenomeUtils.KEY_FASTA + "=" + fasta + "\r\n" +
                GenomeUtils.KEY_FASTA_DIRECTORY + "=" + fastaDirectory + "\r\n" +
                GenomeUtils.KEY_FASTA_FILE_NAME_STRING + "=");
        if (fastaFileNames != null) {
            for (String fastaFileName : fastaFileNames) {
                stringBuilder.append(fastaFileName + ",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }
}
