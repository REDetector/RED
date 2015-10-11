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
import com.xl.utils.NameRetriever;
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
 * The Class BamFileParser parses data in the program-independent BAM file format.
 */
public class BamFileParser extends DataParser {
    /**
     * The SAM file reader. Although its name is SAM file reader, but it really parses the BAM file since that the SAM file needs
     */
    private SAMFileReader reader = null;
    private String bamPath = null;

    public BamFileParser() {
    }

    public BamFileParser(File bamFile) {
        this.bamPath = bamFile.getPath();
        init(bamFile);
    }

    @Override
    public void run() {
        int lineCount = 0;
        File bamFile = getFile();
        this.bamPath = bamFile.getPath();
        DataSet newData;
        SAMRecordIterator iterator = null;
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
            iterator = reader.iterator();
            if (iterator.hasNext()) {
                SAMRecord samRecord = iterator.next();
                String sequenceName = samRecord.getReferenceName();
                if (!NameRetriever.isStandardChromosomeName(sequenceName)) {
                    newData.setStandardChromosomeName(false);
                }
                if (samRecord.getReadNegativeStrandFlag()) {
                    reverseReadCount++;
                } else {
                    forwardReadCount++;
                }
                totalReadCount++;
            }

            while (iterator.hasNext()) {
                SAMRecord samRecord = iterator.next();

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
            if (iterator != null) {
                iterator.close();
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

    private void createBAMIndexFile(File bamFileInput, File bamIndexOutput) {
        SAMFileReader reader = new SAMFileReader(bamFileInput);
        reader.enableFileSource(true);
        SAMFileHeader header = reader.getFileHeader();
        BAMIndexer indexer = new BAMIndexer(bamIndexOutput, header);
        SAMRecordIterator iterator = reader.iterator();
        while (iterator.hasNext()) {
            indexer.processAlignment(iterator.next());
        }
        indexer.finish();
        iterator.close();
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

    public boolean readyToParse() {
        return true;
    }

    public String parserName() {
        return "BAM File Parser";
    }

    public String getDescription() {
        return "Imports Standard BAM Format Files";
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

    @Override
    public FileFilter getFileFilter() {
        return new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".bam");
            }

            public String getDescription() {
                return "BAM Files";
            }
        };
    }

}
