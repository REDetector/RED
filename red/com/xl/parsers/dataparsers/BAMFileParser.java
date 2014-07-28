package com.xl.parsers.dataparsers;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.exception.REDException;
import com.xl.panel.DataParserOptionsPanel;
import com.xl.utils.ChromosomeUtils;
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
    private DataParserOptionsPanel prefs = new DataParserOptionsPanel();
    private int minMappingQuality = 0;

    /**
     * Instantiates a new SAM file parser.
     *
     * @param dataCollection The dataCollection to which new data will be added.
     */
    public BAMFileParser(DataCollection dataCollection) {
        super(dataCollection);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        System.out.println(BAMFileParser.class.getName() + ":run()");
        minMappingQuality = prefs.minMappingQuality();

        File[] samFiles = getFiles();
        DataSet[] newData = new DataSet[samFiles.length];
        System.out.println(this.getClass().getName() + ":samFiles:" + samFiles[0].getName());
        try {
            for (int f = 0; f < samFiles.length; f++) {

                SAMFileReader
                        .setDefaultValidationStringency(SAMFileReader.ValidationStringency.SILENT);

                SAMFileReader inputSam = new SAMFileReader(samFiles[f]);


                newData[f] = new DataSet(samFiles[f].getName(),
                        samFiles[f].getCanonicalPath(),
                        prefs.removeDuplicates());

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


                    if (samRecord.getReadUnmappedFlag()) {
                        // There was no match
                        continue;
                    }

                    if (minMappingQuality > 0
                            && samRecord.getMappingQuality() < minMappingQuality) {
                        // The match isn't good enough
                        continue;
                    }

                    try {
//                            System.out.println(this.getClass().getName() + ":else\tgetSingleEndRead(samRecord)");
                        SequenceRead read = getSingleEndRead(samRecord);
                        if (read != null) {
                            newData[f].addData(read);
                        }
                    } catch (REDException ex) {
                        progressWarningReceived(ex);
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
            progressCancelled();
            return;
        }

        processingComplete(newData);
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

        Chromosome c = null;
        try {
            String chrName = samRecord.getReferenceName();
            if (chrName.length() <= 3) {
                chrName = "chr" + chrName;
            }
            if (ChromosomeUtils.isStandardChromosomeName(chrName)) {
                c = collection.genome().getChromosome(chrName);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
        start = samRecord.getAlignmentStart();
        end = samRecord.getAlignmentEnd();

        if (samRecord.getReadNegativeStrandFlag()) {
            strand = Strand.NEGATIVE;
        } else {
            strand = Strand.POSITIVE;
        }

        Chromosome c;
        try {
            String chrName = samRecord.getReferenceName();
            if (chrName.length() <= 3) {
                chrName = "chr" + chrName;
            }
            if (ChromosomeUtils.isStandardChromosomeName(chrName)) {
                c = collection.genome().getChromosome(chrName);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
        return new SequenceRead(c.getName(), start, strand, samRecord
                .getReadBases(), samRecord.getBaseQualities());
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

        Chromosome c = null;
        try {
            String chrName = samRecord.getReferenceName();
            if (chrName.length() <= 3) {
                chrName = "chr" + chrName;
            }
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

        Chromosome c = null;
        try {
            String chrName = samRecord.getReferenceName();
            if (chrName.length() <= 3) {
                chrName = "chr" + chrName;
            }
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

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataParsers.DataParser#description()
     */
    public String getDescription() {
        return "Imports Standard BAM/SAM Format Files";
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
