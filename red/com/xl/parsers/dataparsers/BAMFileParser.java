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
import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.datatypes.sites.Site;
import com.xl.utils.ChromosomeUtils;
import net.sf.picard.util.Interval;
import net.sf.picard.util.IntervalList;
import net.sf.picard.util.SamLocusIterator;
import net.sf.samtools.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses data in the program-independent BAM file format. Can cope with simple, paired end and spliced reads. Has mainly been tested with TopHat output but
 * reports of success with other programs have been received.
 */
public class BAMFileParser extends DataParser {

    private SAMFileReader reader = null;
    private String bamPath = null;

    public BAMFileParser() {
    }

    public BAMFileParser(File bamFile) {
        this.bamPath = bamFile.getPath();
        init(bamFile);
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
        this.bamPath = bamFile.getPath();
        System.out.println(this.getClass().getName() + ":samFiles:" + bamFile.getName());
        DataSet newData;
        SAMRecordIterator iter = null;
        String lastChr = null;
        int forwardReadCount = 0;
        int reverseReadCount = 0;
        int totalReadCount = 0;
        long totalReadLength = 0;
        try {

            init(bamFile);

            newData = new DataSet(bamFile.getName(), bamFile.getCanonicalPath());
            newData.setDataParser(this);

            // Now process the file

            iter = reader.iterator();

            if (iter.hasNext()) {
                SAMRecord samRecord = iter.next();
                String sequenceName = samRecord.getReferenceName();
                if (!ChromosomeUtils.isStandardChromosomeName(sequenceName)) {
                    newData.setStandardChromosomeName(false);
                }
                if (samRecord.getReadNegativeStrandFlag()) {
                    reverseReadCount++;
                } else {
                    forwardReadCount++;
                }
                totalReadCount++;
            }

            while (iter.hasNext()) {
                SAMRecord samRecord = iter.next();

                if (cancel) {
                    reader.close();
                    progressCancelled();
                    return;
                }
                if (samRecord.getReadUnmappedFlag()) {
                    // There was no match
                    continue;
                }

                ++lineCount;
                String sequenceName = samRecord.getReferenceName();
                if (lineCount % 100000 == 0) {
                    progressUpdated("Read " + lineCount + " lines from " + lastChr + ", " + bamFile.getName(), 0, 0);
                }
                if (!sequenceName.equals(lastChr)) {
                    lastChr = sequenceName;
                    lineCount = 0;
                }
                if (samRecord.getReadNegativeStrandFlag()) {
                    reverseReadCount++;
                } else {
                    forwardReadCount++;
                }
                totalReadCount++;
                totalReadLength += samRecord.getReadLength();
            }
            newData.setTotalReadCount(totalReadCount);
            newData.setForwardReadCount(forwardReadCount);
            newData.setReverseReadCount(reverseReadCount);
            newData.setTotalReadLength(totalReadLength);
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

    private void createBAMIndexFile(File bamFileInput, File bamIndexoutput) {
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

//    public List<SequenceRead> query(String sequence, int start, int end, boolean contained) throws IOException {
//        if (reader == null) {
//            throw new IOException("BAM file has not been loaded.");
//        }
//        SAMRecordIterator iterator = reader.query(sequence, start, end, contained);
//        List<SequenceRead> samRecords = new ArrayList<SequenceRead>();
//        while (iterator.hasNext()) {
//            samRecords.add(new SequenceRead(iterator.next()));
//        }
//        iterator.close();
//        return samRecords;
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

    public List<Site> getDepth(String chr, int startPosition, int endPosition) {
        SAMFileReader samFileReader = new SAMFileReader(new File(bamPath));
        Interval interval = new Interval(chr, startPosition, endPosition);
        IntervalList intervalList = new IntervalList(samFileReader.getFileHeader());
        intervalList.add(interval);
        SamLocusIterator samLocusIterator = new SamLocusIterator(samFileReader, intervalList, true);
        List<Site> siteList = new ArrayList<Site>();
        for (SamLocusIterator.LocusInfo locusInfo : samLocusIterator) {
            siteList.add(new Site(locusInfo.getSequenceName(), locusInfo.getPosition(), locusInfo.getRecordAndPositions().size()));
        }
        samLocusIterator.close();
        samFileReader.close();
        return siteList;
    }

//    public int getTotalReadCount() {
//        if (reader == null) {
//            return 0;
//        }
//        int count = 0;
//        AbstractBAMFileIndex index = (AbstractBAMFileIndex) reader.getIndex();
//        int nRefs = index.getNumberOfReferences();
//        for (int i = 0; i < nRefs; i++) {
//            BAMIndexMetaData meta = index.getMetaData(i);
//            count += meta.getAlignedRecordCount();
//        }
//        return count;
//    }

    public List<SequenceRead> getReadsForSite(Site site) {
        SAMFileReader samFileReader = new SAMFileReader(new File(bamPath));
        Interval interval = new Interval(site.getChr(), site.getStart(), site.getEnd());
        IntervalList intervalList = new IntervalList(samFileReader.getFileHeader());
        intervalList.add(interval);
        SamLocusIterator samLocusIterator = new SamLocusIterator(samFileReader, intervalList, true);
        List<SequenceRead> siteList = new ArrayList<SequenceRead>();
        for (SamLocusIterator.LocusInfo locusInfo : samLocusIterator) {
            List<SamLocusIterator.RecordAndOffset> list = locusInfo.getRecordAndPositions();
            for (SamLocusIterator.RecordAndOffset recordAndOffset : list) {
                siteList.add(new SequenceRead(recordAndOffset.getRecord()));
            }
        }
        samLocusIterator.close();
        samFileReader.close();
        return siteList;
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

    public boolean readyToParse() {
        return true;
    }

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
