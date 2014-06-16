package com.xl.parsers.dataparsers;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.PairedDataSet;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.exception.REDException;
import com.xl.utils.ChromosomeUtils;
import com.xl.utils.MessageUtils;
import com.xl.utils.Strand;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Vector;

/**
 * Parses data in the program-independent BAM file format. Can cope with simple,
 * paired end and spliced reads. Has mainly been tested with TopHat output but
 * reports of success with other programs have been received.
 */
public class BAMFileParser extends DataParser {

    // Extra options which can be set
    private boolean pairedEndImport = false;
    private int pairedEndDistance = 1000;
    private boolean separateSplicedReads = false;
    private boolean importIntrons = false;
    private int extendBy = 0;
    private DataParserOptionsPanel prefs = new DataParserOptionsPanel(true,
            true, false, true);
    private int minMappingQuality = 0;

    /**
     * Instantiates a new SAM file parser.
     *
     * @param dataCollection The dataCollection to which new data will be added.
     */
    public BAMFileParser(DataCollection dataCollection) {
        super(dataCollection);
    }

    /**
     * Instantiates a new SAM file parser with all options set
     *
     * @param data                    The dataCollection to which new data will be added.
     * @param pairedEndImport         Whether to import this as paired end data
     * @param pairedEndDistanceCutoff The maximum distance (bp) between valid paired ends
     * @param splitSplicedReads       Whether to split up spliced reads into their component parts
     */
    public BAMFileParser(DataCollection data, boolean pairedEndImport,
                         int pairedEndDistanceCutoff, boolean splitSplicedReads) {
        super(data);
        this.pairedEndImport = pairedEndImport;
        this.separateSplicedReads = splitSplicedReads;
        pairedEndDistance = pairedEndDistanceCutoff;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        System.out.println(this.getClass().getName() + ":run()");
        pairedEndImport = prefs.pairedEnd();
        pairedEndDistance = prefs.pairDistanceCutoff();
        separateSplicedReads = prefs.splitSplicedReads();
        importIntrons = prefs.importIntrons();
        extendBy = prefs.extendReads();
        minMappingQuality = prefs.minMappingQuality();

        File[] samFiles = getFiles();
        DataSet[] newData = new DataSet[samFiles.length];
        System.out.println(this.getClass().getName() + ":samFiles:" + samFiles[0].getName());
        try {
            for (int f = 0; f < samFiles.length; f++) {

                SAMFileReader
                        .setDefaultValidationStringency(SAMFileReader.ValidationStringency.SILENT);

                SAMFileReader inputSam = new SAMFileReader(samFiles[f]);

                if (prefs.isHiC()) {
                    newData[f] = new PairedDataSet(samFiles[f].getName(),
                            samFiles[f].getCanonicalPath(),
                            prefs.removeDuplicates(), prefs.hiCDistance(),
                            prefs.hiCIgnoreTrans());
                } else {
                    newData[f] = new DataSet(samFiles[f].getName(),
                            samFiles[f].getCanonicalPath(),
                            prefs.removeDuplicates());
                }

                int lineCount = 0;
                // Now process the file

                // A flag we can set to skip the next record if we're getting
                // out of sync during single end HiC import.
                boolean skipNext = false;

                for (SAMRecord samRecord : inputSam) {
                    if (skipNext) {
                        skipNext = false;
                        continue;
                    }

                    if (cancel) {
                        inputSam.close();
                        progressCancelled();
                        return;
                    }

                    ++lineCount;

                    if (lineCount % 100000 == 0) {
                        progressUpdated("Read " + lineCount + " lines from "
                                + samFiles[f].getName(), f, samFiles.length);
                    }

                    if (pairedEndImport && !samRecord.getReadPairedFlag()) {
                        progressWarningReceived(new REDException(
                                "Data was single ended during paired end import"));
                        continue;
                    }

                    if (pairedEndImport && !separateSplicedReads
                            && !samRecord.getFirstOfPairFlag()) {
                        // This isn't the first read in a pair so we don't
                        // process
                        // it. We do process both ends if we're working on
                        // spliced data.
                        continue;
                    }

                    if (samRecord.getReadUnmappedFlag()) {
                        // There was no match
                        continue;
                    }
                    if (pairedEndImport && !separateSplicedReads
                            && samRecord.getMateUnmappedFlag()) {
                        // No match on the reverse strand. Doesn't matter if
                        // we're doing spliced reads.
                        continue;
                    }

                    if (minMappingQuality > 0
                            && samRecord.getMappingQuality() < minMappingQuality) {
                        // The match isn't good enough
                        continue;
                    }

                    // TODO: Check what this actually stores - might be a real
                    // name rather than 0/=
                    if (pairedEndImport && !separateSplicedReads
                            && !prefs.isHiC()
                            && samRecord.getMateReferenceName() == "0") {
                        if (samRecord.getMateReferenceName() != "=") {
                            try {
                                throw new REDException(
                                        "Unexpected mate referenece name "
                                                + samRecord
                                                .getMateReferenceName());
                            } finally {
                                if (inputSam != null) {
                                    inputSam.close();
                                }
                            }
                        }
                        // Matches were on different chromosomes
                        continue;
                    }
                    try {
                        if (pairedEndImport && prefs.isHiC()) {
//                            System.out.println(this.getClass().getName() + ":getPairedEndHiCRead(samRecord);");
                            SequenceRead[] reads = getPairedEndHiCRead(samRecord);
                            newData[f].addData(reads[0]);
                            newData[f].addData(reads[1]);
                        } else if (pairedEndImport && !separateSplicedReads) {
//                            System.out.println(this.getClass().getName() + ":getPairedEndRead(samRecord)");
                            SequenceRead read = getPairedEndRead(samRecord);
                            newData[f].addData(read);
                        } else if (separateSplicedReads) {
//                            System.out.println(this.getClass().getName() + ":getSplitSingleEndRead(samRecord)");
                            SequenceRead[] reads = getSplitSingleEndRead(samRecord);
                            for (int r = 0; r < reads.length; r++) {
                                newData[f].addData(reads[r]);
                            }
                        } else {
//                            System.out.println(this.getClass().getName() + ":else\tgetSingleEndRead(samRecord)");
                            SequenceRead read = getSingleEndRead(samRecord);
                            if (read != null) {
                                newData[f].addData(read);
                            }
                        }
                    } catch (REDException ex) {
                        progressWarningReceived(ex);

                        if (prefs.isHiC() && !pairedEndImport) {
                            if (((PairedDataSet) newData[f])
                                    .importSequenceSkipped()) {
                                // Skip the next line
                                skipNext = true;
                            }
                        }
                    }

                }
                // We're finished with the file.
                inputSam.close();

                // Cache the data in the new dataset
                progressUpdated("Caching data from " + samFiles[f].getName(),
                        f, samFiles.length);
                newData[f].finalise();
            }
        } catch (Exception ex) {
            progressExceptionReceived(ex);
            return;
        }

        processingFinished(newData);
    }

    /**
     * Gets a split single end read. The only reason for asking about whether
     * the import is single or paired end is that if we're doing a paired end
     * import then we reverse the strand for all second read data. For single
     * end import we leave the strand alone whichever read it originates from.
     *
     * @param samRecord The picard record entry for this read
     * @return The read which was read
     * @throws REDException
     */
    private SequenceRead[] getSplitSingleEndRead(SAMRecord samRecord)
            throws REDException {
        Strand strand;
        int start;
        int lastEnd = -1;

        start = samRecord.getAlignmentStart();

        // For paired end data we want to flip the strand of the second read
        // so that we get the correct strand for the fragment so we don't end
        // up making mixed libraries from what should be strand specific data

        if (samRecord.getReadNegativeStrandFlag()) {
            if (samRecord.getReadPairedFlag()
                    && samRecord.getSecondOfPairFlag()) {
                strand = Strand.POSITIVE;
            } else {
                strand = Strand.NEGATIVE;
            }
        } else {
            if (samRecord.getReadPairedFlag()
                    && samRecord.getSecondOfPairFlag()) {
                strand = Strand.NEGATIVE;
            } else {
                strand = Strand.POSITIVE;
            }
        }

        Chromosome c;

        try {
            c = dataCollection().genome().getChromosome(
                    samRecord.getReferenceName());
        } catch (Exception e) {
            throw new REDException(e.getLocalizedMessage());
        }

        if (start < 1) {
            throw new REDException("Reading position " + start
                    + " was before the start of chr" + c.getName() + " ("
                    + c.getLength() + ")");
        }

        // We now need to work our way through the cigar string breaking every
        // time there is a skip section in the cigar string

        // TODO: Can we avoid this if this is a continuous read? This seems to
        // be the limiting code when importing RNA-Seq data and it would be
        // worth trying to speed this up.

        String[] cigarOperations = samRecord.getCigarString().split("\\d+");
        String[] cigarNumbers = samRecord.getCigarString().split("[MIDNSHP]");

        if (cigarOperations.length != cigarNumbers.length + 1) {
            throw new REDException("Couldn't parse CIGAR string "
                    + samRecord.getCigarString() + " counts were "
                    + cigarOperations.length + " vs " + cigarNumbers.length);
        }

        Vector<SequenceRead> newReads = new Vector<SequenceRead>();

        int currentPosition = start;
        for (int pos = 0; pos < cigarNumbers.length; pos++) {

            if (cigarOperations[pos + 1].equals("M")) {
                currentPosition += Integer.parseInt(cigarNumbers[pos]) - 1;
            } else if (cigarOperations[pos + 1].equals("I")) {
                currentPosition += Integer.parseInt(cigarNumbers[pos]) - 1;
            } else if (cigarOperations[pos + 1].equals("D")) {
                currentPosition -= Integer.parseInt(cigarNumbers[pos]) - 1;
            } else if (cigarOperations[pos + 1].equals("N")) {
                // Make a new sequence as far as this point

                if (importIntrons) {
                    if (lastEnd > 0) {
                        if (start > c.getLength()) {
                            int overrun = (start - 1) - c.getLength();
                            throw new REDException("Reading position "
                                    + (start - 1) + " was " + overrun
                                    + "bp beyond the end of chr" + c.getName()
                                    + " (" + c.getLength() + ")");
                        }
                        newReads.add(new SequenceRead(c.getName(), start, strand, samRecord
                                .getReadBases(), samRecord.getBaseQualities()));
                    }

                    // Update the lastEnd whether we added a read or not since
                    // this will be the start of the next intron
                    lastEnd = currentPosition;
                } else {
                    // We also don't allow readings which are beyond the end of
                    // the chromosome
                    if (currentPosition > c.getLength()) {
                        int overrun = currentPosition - c.getLength();
                        throw new REDException("Reading position "
                                + currentPosition + " was " + overrun
                                + "bp beyond the end of chr" + c.getName()
                                + " (" + c.getLength() + ")");
                    }

                    newReads.add(new SequenceRead(c.getName(), start, strand, samRecord
                            .getReadBases(), samRecord.getBaseQualities()));
                }

                currentPosition += Integer.parseInt(cigarNumbers[pos]) + 1;
                start = currentPosition;
            }

        }

        if (importIntrons) {
            if (lastEnd > 0) {
                if (start > c.getLength()) {
                    int overrun = (start - 1) - c.getLength();
                    throw new REDException("Reading position " + (start - 1)
                            + " was " + overrun + "bp beyond the end of chr"
                            + c.getName() + " (" + c.getLength() + ")");
                }
                newReads.add(new SequenceRead(c.getName(), start, strand, samRecord
                        .getReadBases(), samRecord.getBaseQualities()));
            }
        } else {
            // We have to process the last read in the string.

            // We also don't allow readings which are beyond the end of the
            // chromosome
            if (currentPosition > c.getLength()) {
                int overrun = currentPosition - c.getLength();
                throw new REDException("Reading position " + currentPosition
                        + " was " + overrun + "bp beyond the end of chr"
                        + c.getName() + " (" + c.getLength() + ")");
            }

            newReads.add(new SequenceRead(c.getName(), start, strand, samRecord
                    .getReadBases(), samRecord.getBaseQualities()));
        }

        return newReads.toArray(new SequenceRead[0]);

    }

    /**
     * Gets a single end read.
     *
     * @param samRecord The tab split sections from the SAM file
     * @return The read which was read
     * @throws REDException
     */
    private SequenceRead getSingleEndRead(SAMRecord samRecord)
            throws REDException {

        Strand strand;
        int start;
        int end;
//        MessageUtils.showInfo(BAMFileParser.class,(samRecord==null)+"");
        start = samRecord.getAlignmentStart();
        end = samRecord.getAlignmentEnd();

        if (samRecord.getReadNegativeStrandFlag()) {
            strand = Strand.NEGATIVE;
        } else {
            strand = Strand.POSITIVE;
        }

        if (extendBy > 0) {
            if (strand == Strand.POSITIVE) {
                end += extendBy;
            } else if (strand == Strand.NEGATIVE) {
                start -= extendBy;
            }
        }

        Chromosome c = null;
        try {
            String chrName = samRecord.getReferenceName();
            MessageUtils.showInfo(BAMFileParser.class, chrName);
            if (ChromosomeUtils.isStandardChromosomeName(chrName)) {
                c = collection.genome().getChromosome(chrName);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // We also don't allow readings which are beyond the end of the
        // chromosome
        if (end > c.getLength()) {
            int overrun = end - c.getLength();
            throw new REDException("Reading position " + end + " was "
                    + overrun + "bp beyond the end of chr" + c.getName() + " ("
                    + c.getLength() + ")");
        }
        if (start < 1) {
            throw new REDException("Reading position " + start
                    + " was before the start of chr" + c.getName() + " ("
                    + c.getLength() + ")");
        }

        // We can now make the new reading
        SequenceRead read = new SequenceRead(c.getName(), start, strand, samRecord
                .getReadBases(), samRecord.getBaseQualities());
        return read;
    }

    /**
     * Gets a pair of reads for HiC from a single record.
     *
     * @param samRecord The tab split sections from the SAM file
     * @return The read which was read
     * @throws REDException
     */
    private SequenceRead[] getPairedEndHiCRead(SAMRecord samRecord)
            throws REDException {

        // We're going to return a pair of reads which can then be assembled
        // into a HiC pair by the calling code
        SequenceRead[] pairOfReads = new SequenceRead[2];

        // The first end is easy - we just read in the record we're looking at
        pairOfReads[0] = getSingleEndRead(samRecord);

        // For the second end we have more of a problem, we have to try to
        // figure this
        // out from the limited data we have available.
        Strand strand = null;
        int start;
        int end;

        if (samRecord.getMateNegativeStrandFlag()) {
            strand = Strand.NEGATIVE;
        } else {
            strand = Strand.POSITIVE;
        }

        start = samRecord.getMateAlignmentStart();
        end = start
                + (samRecord.getAlignmentEnd() - samRecord.getAlignmentStart())
                + 1;

        String mateReferenceName = samRecord.getMateReferenceName();
        if (mateReferenceName.equals("-")) {
            mateReferenceName = samRecord.getReferenceName();
        }

        Chromosome c;

        try {
            c = dataCollection().genome().getChromosome(mateReferenceName);
        } catch (Exception e) {
            throw new REDException(e.getLocalizedMessage());
        }

        // We also don't allow readings which are beyond the end of the
        // chromosome
        if (end > c.getLength()) {
            int overrun = end - c.getLength();
            throw new REDException("Reading position " + end + " was "
                    + overrun + "bp beyond the end of chr" + c.getName() + " ("
                    + c.getLength() + ")");
        }
        if (start < 1) {
            throw new REDException("Reading position " + start
                    + " was before the start of chr" + c.getName() + " ("
                    + c.getLength() + ")");
        }

        // We can now make the new reading
        pairOfReads[1] = new SequenceRead(c.getName(), start, strand, samRecord
                .getReadBases(), samRecord.getBaseQualities());

        return pairOfReads;
    }

    /**
     * Gets a paired end read. This method assumes that it will only be passed
     * the first read of a pair to avoid duplicating the reads which were seen.
     *
     * @param samRecord The tab split sections from the SAM file
     * @return The read which was read
     * @throws REDException
     */
    private SequenceRead getPairedEndRead(SAMRecord samRecord)
            throws REDException {
        Strand strand = null;
        int start;
        int end;
        if (samRecord.getInferredInsertSize() == 0) {
            if (samRecord.getReadNegativeStrandFlag()) {
                end = samRecord.getAlignmentEnd();
                start = samRecord.getMateAlignmentStart();
            } else {
                start = samRecord.getAlignmentStart();
                end = samRecord.getMateAlignmentStart()
                        + samRecord.getCigar().getPaddedReferenceLength();
            }
        } else if (samRecord.getReadNegativeStrandFlag()) {
            end = samRecord.getAlignmentEnd();
            start = end + (samRecord.getInferredInsertSize() + 1);
        } else {
            start = samRecord.getAlignmentStart();
            end = start + samRecord.getInferredInsertSize() - 1;
        }

        if (end < start) {
            int temp = start;
            start = end;
            end = temp;
        }

        // We assign the strand for the pair as the strand for the
        // first read in the pair

        if (samRecord.getReadNegativeStrandFlag()
                && !samRecord.getMateNegativeStrandFlag()) {
            strand = Strand.NEGATIVE;
        } else if (!samRecord.getReadNegativeStrandFlag()
                && samRecord.getMateNegativeStrandFlag()) {
            strand = Strand.POSITIVE;
        } else {
            strand = Strand.NONE;
        }

        if ((end - start) + 1 > pairedEndDistance) {
            throw new REDException("Distance between ends "
                    + ((end - start) + 1) + " was larger than cutoff ("
                    + pairedEndDistance + ")");
        }

        Chromosome c;

        try {
            c = dataCollection().genome().getChromosome(
                    samRecord.getReferenceName());
        } catch (Exception e) {
            throw new REDException(e.getLocalizedMessage());
        }

        // We also don't allow readings which are beyond the end of the
        // chromosome
        if (end > c.getLength()) {
            int overrun = end - c.getLength();
            throw new REDException("Reading position " + end + " was "
                    + overrun + "bp beyond the end of chr" + c.getName() + " ("
                    + c.getLength() + ")");
        }
        if (start < 1) {
            throw new REDException("Reading position " + start
                    + " was before the start of chr" + c.getName() + " ("
                    + c.getLength() + ")");
        }

        // We can now make the new reading
        SequenceRead read = new SequenceRead(c.getName(), start, strand, samRecord
                .getReadBases(), samRecord.getBaseQualities());

        return read;
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataParsers.DataParser#description()
     */
    public String getDescription() {
        return "Imports Data standard BAM Format files";
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataParsers.DataParser#getOptionsPanel()
     */
    public JPanel getOptionsPanel() {
        return prefs;
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataParsers.DataParser#hasOptionsPanel()
     */
    public boolean hasOptionsPanel() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataParsers.DataParser#name()
     */
    public String parserName() {
        return "BAM File Importer";
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataParsers.DataParser#readyToParse()
     */
    public boolean readyToParse() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataParsers.DataParser#getFileFilter()
     */
    public FileFilter getFileFilter() {
        return new FileFilter() {

            public String getDescription() {
                return "BAM/SAM Files";
            }

            public boolean accept(File f) {
                if (f.isDirectory()
                        || f.getName().toLowerCase().endsWith(".bam")
                        || f.getName().toLowerCase().endsWith(".sam")) {
                    return true;
                } else {
                    return false;
                }
            }

        };
    }

}
