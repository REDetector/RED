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

import net.sf.picard.util.Interval;
import net.sf.picard.util.IntervalList;
import net.sf.picard.util.SamLocusIterator;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Administrator on 2014/11/1.
 */
public class BamFileParserTest {

    public static void main(String[] args) throws IOException {
        System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + "MB");
        DateFormat format = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        System.out.println("Start:" + format.format(new Date(System.currentTimeMillis())));
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

        int count = 0;
        SAMRecordIterator iterator = reader.query("8", 87669314, 87670835, true);
        while (iterator.hasNext()) {
            SAMRecord record = iterator.next();
            System.out.println(record.getSAMString());
            count++;
//            List<AlignmentBlock> blocks = record.getAlignmentBlocks();
//            builder.append(record.getAlignmentStart() + "\t" + record.getAlignmentEnd() + "\t" + record.getCigarString() + "\t" + record.getCigar().getReadLength()
//                    + "\t" + record.getCigar().getReferenceLength());
//            builder.append(record.getBaseQualityString() + "\t" + record.getReadString() + "\t");
//            builder.append(record.getMappingQuality() + "\t" + record.getReadLength() + "\t");
//            builder.append(record.getReadName() + "\t" + record.getReadNameLength() + "\t" + record.getReferenceName() + "\t");
//            builder.append(record.getMappingQuality());
//              builder.append(record.format());
//            builder.append(record.getSAMString());
//            builder.append(record.toString());
//            for (AlignmentBlock block : blocks) {
//                System.out.println(block.getReadStart() + "\t" + block.getReferenceStart() + "\t" + block.getLength());
//            }
        }
        iterator.close();
        System.out.println(count + " -----------------------------------------------");
//        iterator = reader.query("8", 44587, 45587, true);
//        while (iterator.hasNext()) {
//            SAMRecord record = iterator.next();
//            System.out.println(record.getSAMString());
//
//        }
        System.out.println("Start:" + format.format(new Date(System.currentTimeMillis())));
        System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + "MB");
    }

    public static void getDepth() {
        SAMFileReader samFileReader = new SAMFileReader(new File("E:\\Master\\ChongQing\\Data\\BJ22N_DNA_RNA\\BJ22N.chr8.sam"));
        DateFormat format = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        System.out.println("Start:" + format.format(new Date(System.currentTimeMillis())));
        String chr = 8 + "";
        Random random = new Random(47);
        int startPosition = random.nextInt(1000000);
        int endPosition = random.nextInt(1000000);
        if (startPosition > endPosition) {
            int tmp = startPosition;
            startPosition = endPosition;
            endPosition = tmp;
        }
        Interval interval = new Interval(chr, startPosition, endPosition);
        IntervalList iL = new IntervalList(samFileReader.getFileHeader());
        iL.add(interval);
        SamLocusIterator sli = new SamLocusIterator(samFileReader, iL, true);
        int count = 0;
        for (SamLocusIterator.LocusInfo locusInfo : sli) {
//            System.out.println("POS=" + locusInfo.getPosition() + " depth:" + locusInfo.getRecordAndPositions().size());
            count++;
        }
        System.out.println(count);
        System.out.println("End:" + format.format(new Date(System.currentTimeMillis())));
        sli.close();
        samFileReader.close();
    }
}
