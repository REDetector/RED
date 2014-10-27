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

package com.xl.parsers.dataparsers;

import com.xl.datatypes.DataSet;
import com.xl.datatypes.sequence.Alignment;
import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.utils.ChromosomeUtils;
import net.sf.samtools.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses data in the program-independent BAM file format. Can cope with simple,
 * paired end and spliced reads. Has mainly been tested with TopHat output but
 * reports of success with other programs have been received.
 */
public class BAMFileParser extends DataParser {

    private SAMFileReader reader = null;

    public BAMFileParser() {
    }

    public BAMFileParser(File bamFile) {
        init(bamFile);
    }

    public static void main(String[] args) throws IOException {
        System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + "MB");
        String bamFile = "E:\\Master\\ChongQing\\Data\\BJ22N_DNA_RNA\\BJ22N.RNA.chr8.bam";
        String bamFile1 = "E:\\Master\\ChongQing\\Data\\BJ22N_DNA_RNA\\BJ22T.RNA.chr8.bam";
        String bamFile2 = "E:\\Master\\ChongQing\\Data\\HCC448N\\HCC448N.recal.chr8.bam";
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(bamFile+".bai")));

//        System.out.println(count);
        SAMFileReader reader = new SAMFileReader(new File(bamFile1));
//        List<SAMRecord> records = parser.query("8",81234,82234,false);
//        System.out.println(records);
//        System.out.println("--------------------------------------------------------");
//        records = parser.query("8",82234,83234,false);
//        System.out.println(records);
//        reader.enableFileSource(true);
//        SAMFileHeader header = reader.getFileHeader();
//        File output = new File(bamFile1 + ".bai");
//        BAMIndexer bamIndexer = new BAMIndexer(output, header);
//        for (SAMRecord record : reader) {
////            samRecords.add(record);
//            bamIndexer.processAlignment(record);
//        }
//        bamIndexer.finish();

//        SAMRecordIterator iterator = reader.query("8", 44587, 45587, true);
//        while (iterator.hasNext()) {
//            SAMRecord record = iterator.next();
//            System.out.println(record.getSAMString());
//
////            List<AlignmentBlock> blocks = record.getAlignmentBlocks();
////            builder.append(record.getAlignmentStart() + "\t" + record.getAlignmentEnd() + "\t" + record.getCigarString() + "\t" + record.getCigar().getReadLength()
////                    + "\t" + record.getCigar().getReferenceLength());
////            builder.append(record.getBaseQualityString() + "\t" + record.getReadString() + "\t");
////            builder.append(record.getMappingQuality() + "\t" + record.getReadLength() + "\t");
////            builder.append(record.getReadName() + "\t" + record.getReadNameLength() + "\t" + record.getReferenceName() + "\t");
////            builder.append(record.getMappingQuality());
////              builder.append(record.format());
////            builder.append(record.getSAMString());
////            builder.append(record.toString());
////            for (AlignmentBlock block : blocks) {
////                System.out.println(block.getReadStart() + "\t" + block.getReferenceStart() + "\t" + block.getLength());
////            }
//        }
//        iterator.close();
//        System.out.println("-----------------------------------------------");
//        iterator = reader.query("8", 44587, 45587, true);
//        while (iterator.hasNext()) {
//            SAMRecord record = iterator.next();
//            System.out.println(record.getSAMString());
//
//        }
        System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + "MB");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        System.out.println(BAMFileParser.class.getName() + ":run()");
        int lineCount = 0;
        File bamFile = getFile();
        System.out.println(this.getClass().getName() + ":samFiles:" + bamFile.getName());
        DataSet newData;
        SAMRecordIterator iter = null;
        try {

            init(bamFile);

            newData = new DataSet(bamFile.getName(), bamFile.getCanonicalPath());
            newData.setDataParser(this);

            // Now process the file

            iter = reader.iterator();

            while (iter.hasNext()) {
                SAMRecord samRecord = iter.next();
                if (cancel) {
                    reader.close();
                    progressCancelled();
                    return;
                }

                ++lineCount;

                if (lineCount % 100000 == 0) {
                    progressUpdated("Read " + lineCount + " lines from " + bamFile.getName(), 0, 0);
                }

                if (samRecord.getReadUnmappedFlag()) {
                    // There was no match
                    continue;
                }
                String sequenceName = samRecord.getReferenceName();

                if (lineCount == 1 && !ChromosomeUtils.isStandardChromosomeName(sequenceName)) {
                    newData.setStandardChromosomeName(false);
                }
                int alignmentStart = samRecord.getAlignmentStart();
                int alignmentEnd = samRecord.getAlignmentEnd();
                boolean negative = samRecord.getReadNegativeStrandFlag();
                Alignment alignment = new Alignment(sequenceName, alignmentStart, alignmentEnd, negative);
                newData.addData(alignment);
            }

            // Cache the data in the new dataset
            progressUpdated("Caching data from " + bamFile.getName(), 0, 0);
            newData.finalise();
        } catch (Exception ex) {
            progressExceptionReceived(ex);
            progressCancelled();
            return;
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
        processingComplete(new DataSet[]{newData});
    }

    private void init(File bamFile) {
        if (reader == null) {
            SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.SILENT);
            reader = new SAMFileReader(bamFile);
            if (!reader.hasIndex()) {
                progressUpdated("There is no bam index file. Creating the index file...", 0, 0);
                createBAMIndexFile(bamFile, new File(bamFile.getAbsolutePath() + ".bai"));
                reader = new SAMFileReader(bamFile);
            }
        }
    }

    public void createBAMIndexFile(File bamFileInput, File bamIndexoutput) {
        SAMFileReader reader = new SAMFileReader(bamFileInput);
        reader.enableFileSource(true);
        SAMFileHeader header = reader.getFileHeader();
        BAMIndexer indexer = new BAMIndexer(bamIndexoutput, header);
        SAMRecordIterator iterator = reader.iterator();
        while (iterator.hasNext()) {
            indexer.processAlignment(iterator.next());
        }
        indexer.finish();
        iterator.close();
    }

    public List<SequenceRead> query(String sequence, int start, int end, boolean contained) throws IOException {
        if (reader == null) {
            throw new IOException("BAM file has not been loaded.");
        }
        SAMRecordIterator iterator = reader.query(sequence, start, end, contained);
        List<SequenceRead> samRecords = new ArrayList<SequenceRead>();
        while (iterator.hasNext()) {
            samRecords.add(new SequenceRead(iterator.next()));
        }
        iterator.close();
        return samRecords;
    }

    /**
     * Gets a split single end read. The only reason for asking about whether
     * the import is single or paired end is that if we're doing a paired end
     * import then we reverse the strand for all second read data. For single
     * end import we leave the strand alone whichever read it originates from.
     *
     * @param samRecord The picard record entry for this read
     * @return The read which was read
     * @throws com.xl.exception.REDException
     */
//    private SequenceRead[] getSplitSingleEndRead(SAMRecord samRecord) throws REDException {
//        Strand strand;
//        int start;
//        int lastEnd = -1;
//
//        start = samRecord.getAlignmentStart();
//
//        // For paired end data we want to flip the strand of the second read
//        // so that we get the correct strand for the fragment so we don't end
//        // up making mixed libraries from what should be strand specific data
//
//        if (samRecord.getReadNegativeStrandFlag()) {
//            if (samRecord.getReadPairedFlag()
//                    && samRecord.getSecondOfPairFlag()) {
//                strand = Strand.POSITIVE;
//            } else {
//                strand = Strand.NEGATIVE;
//            }
//        } else {
//            if (samRecord.getReadPairedFlag()
//                    && samRecord.getSecondOfPairFlag()) {
//                strand = Strand.NEGATIVE;
//            } else {
//                strand = Strand.POSITIVE;
//            }
//        }
//
//        Chromosome c = null;
//        try {
//            String chrName = samRecord.getReferenceName();
//            if (chrName.length() <= 3) {
//                chrName = "chr" + chrName;
//            }
//            if (ChromosomeUtils.isStandardChromosomeName(chrName)) {
//                c = REDApplication.getInstance().dataCollection().genome().getChromosome(chrName);
//            } else {
//                return null;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//
//        if (start < 1) {
//            throw new REDException("Reading position " + start
//                    + " was before the start of chr" + c.getName() + " ("
//                    + c.getLength() + ")");
//        }
//
//        // We now need to work our way through the cigar string breaking every
//        // time there is a skip section in the cigar string
//
//        // TODO: Can we avoid this if this is a continuous read? This seems to
//        // be the limiting code when importing RNA-Seq data and it would be
//        // worth trying to speed this up.
//
//        String[] cigarOperations = samRecord.getCigarString().split("\\d+");
//        String[] cigarNumbers = samRecord.getCigarString().split("[MIDNSHP]");
//
//        if (cigarOperations.length != cigarNumbers.length + 1) {
//            throw new REDException("Couldn't parse CIGAR string "
//                    + samRecord.getCigarString() + " counts were "
//                    + cigarOperations.length + " vs " + cigarNumbers.length);
//        }
//
//        Vector<SequenceRead> newReads = new Vector<SequenceRead>();
//
//        int currentPosition = start;
//        for (int pos = 0; pos < cigarNumbers.length; pos++) {
//
//            if (cigarOperations[pos + 1].equals("M")) {
//                currentPosition += Integer.parseInt(cigarNumbers[pos]) - 1;
//            } else if (cigarOperations[pos + 1].equals("I")) {
//                currentPosition += Integer.parseInt(cigarNumbers[pos]) - 1;
//            } else if (cigarOperations[pos + 1].equals("D")) {
//                currentPosition -= Integer.parseInt(cigarNumbers[pos]) - 1;
//            } else if (cigarOperations[pos + 1].equals("N")) {
//                // Make a new sequence as far as this point
//
//                // We also don't allow readings which are beyond the end of
//                // the chromosome
//                if (currentPosition > c.getLength()) {
//                    int overrun = currentPosition - c.getLength();
//                    throw new REDException("Reading position "
//                            + currentPosition + " was " + overrun
//                            + "bp beyond the end of chr" + c.getName()
//                            + " (" + c.getLength() + ")");
//                }
//
//                newReads.add(new SequenceRead(c.getName(), start, strand, samRecord
//                        .getReadBases(), samRecord.getBaseQualities()));
//            }
//
//            currentPosition += Integer.parseInt(cigarNumbers[pos]) + 1;
//            start = currentPosition;
//
//        }
//
//        // We have to process the last read in the string.
//
//        // We also don't allow readings which are beyond the end of the
//        // chromosome
//        if (currentPosition > c.getLength()) {
//            int overrun = currentPosition - c.getLength();
//            throw new REDException("Reading position " + currentPosition + " was " + overrun + "bp beyond the end of chr" + c.getName() + " (" + c
//                    .getLength() + ")");
//        }
//
//        newReads.add(new SequenceRead(c.getName(), start, strand, samRecord.getReadBases(), samRecord.getBaseQualities()));
//
//        return newReads.toArray(new SequenceRead[0]);
//
//    }

    /**
     * Gets a single end read.
     *
     * @param samRecord The tab split sections from the SAM file
     * @return The read which was read
     * @throws com.xl.exception.REDException
     */
//    private SequenceRead getSingleEndRead(SAMRecord samRecord) throws REDException {
//
//        Strand strand;
//        int start;
//        int end;
//        start = samRecord.getAlignmentStart();
//        end = samRecord.getAlignmentEnd();
//
//        if (samRecord.getReadNegativeStrandFlag()) {
//            strand = Strand.NEGATIVE;
//        } else {
//            strand = Strand.POSITIVE;
//        }
//
//        Chromosome c;
//        try {
//            String chrName = samRecord.getReferenceName();
//            if (chrName.length() <= 3) {
//                chrName = "chr" + chrName;
//            }
//            if (ChromosomeUtils.isStandardChromosomeName(chrName)) {
//                c = REDApplication.getInstance().dataCollection().genome().getChromosome(chrName);
//            } else {
//                return null;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//        // We also don't allow readings which are beyond the end of the
//        // chromosome
//        if (end > c.getLength()) {
//            int overrun = end - c.getLength();
//            throw new REDException("Reading position " + end + " was " + overrun + "bp beyond the end of chr" + c.getName() + " (" + c.getLength() + ")");
//        }
//        if (start < 1) {
//            throw new REDException("Reading position " + start + " was before the start of chr" + c.getName() + " (" + c.getLength() + ")");
//        }
//
//        // We can now make the new reading
//        return new SequenceRead(c.getName(), start, strand, samRecord.getReadBases(), samRecord.getBaseQualities());
//    }

    /**
     * Gets a paired end read. This method assumes that it will only be passed
     * the first read of a pair to avoid duplicating the reads which were seen.
     *
     * @return The read which was read
     * @throws com.xl.exception.REDException
     */
//    private SequenceRead getPairedEndRead(SAMRecord samRecord) throws REDException {
//        Strand strand = null;
//        int start;
//        int end;
//        if (samRecord.getInferredInsertSize() == 0) {
//            if (samRecord.getReadNegativeStrandFlag()) {
//                end = samRecord.getAlignmentEnd();
//                start = samRecord.getMateAlignmentStart();
//            } else {
//                start = samRecord.getAlignmentStart();
//                end = samRecord.getMateAlignmentStart() + samRecord.getCigar().getPaddedReferenceLength();
//            }
//        } else if (samRecord.getReadNegativeStrandFlag()) {
//            end = samRecord.getAlignmentEnd();
//            start = end + (samRecord.getInferredInsertSize() + 1);
//        } else {
//            start = samRecord.getAlignmentStart();
//            end = start + samRecord.getInferredInsertSize() - 1;
//        }
//
//        if (end < start) {
//            int temp = start;
//            start = end;
//            end = temp;
//        }
//
//        // We assign the strand for the pair as the strand for the
//        // first read in the pair
//
//        if (samRecord.getReadNegativeStrandFlag() && !samRecord.getMateNegativeStrandFlag()) {
//            strand = Strand.NEGATIVE;
//        } else if (!samRecord.getReadNegativeStrandFlag() && samRecord.getMateNegativeStrandFlag()) {
//            strand = Strand.POSITIVE;
//        } else {
//            strand = Strand.NONE;
//        }
//
//        Chromosome c = null;
//        try {
//            String chrName = samRecord.getReferenceName();
//            if (chrName.length() <= 3) {
//                chrName = "chr" + chrName;
//            }
//            if (ChromosomeUtils.isStandardChromosomeName(chrName)) {
//                c = REDApplication.getInstance().dataCollection().genome().getChromosome(chrName);
//            } else {
//                return null;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//        // We also don't allow readings which are beyond the end of the
//        // chromosome
//        if (end > c.getLength()) {
//            int overrun = end - c.getLength();
//            throw new REDException("Reading position " + end + " was " + overrun + "bp beyond the end of chr" + c.getName() + " (" + c.getLength() + ")");
//        }
//        if (start < 1) {
//            throw new REDException("Reading position " + start + " was before the start of chr" + c.getName() + " (" + c.getLength() + ")");
//        }
//
//        // We can now make the new reading
//        SequenceRead read = new SequenceRead(c.getName(), start, strand, samRecord.getReadBases(), samRecord.getBaseQualities());
//
//        return read;
//    }
    public String getDescription() {
        return "Imports Standard BAM/SAM Format Files";
    }

    @Override
    public List<? extends Location> query(String chr, int start, int end) {
        if (reader == null) {
            return new ArrayList<Location>();
        }
        SAMRecordIterator iterator = reader.query(chr, start, end, false);
        List<SequenceRead> samRecords = new ArrayList<SequenceRead>();
        while (iterator.hasNext()) {
            samRecords.add(new SequenceRead(iterator.next()));
        }
        iterator.close();
        return samRecords;
    }

    public JPanel getOptionsPanel() {
        return null;
    }

    public boolean hasOptionsPanel() {
        return false;
    }

    public String parserName() {
        return "BAM File Parser";
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
