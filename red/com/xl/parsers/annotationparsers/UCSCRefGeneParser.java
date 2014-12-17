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

package com.xl.parsers.annotationparsers;

import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.annotation.CoreAnnotationSet;
import com.xl.datatypes.feature.Feature;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.sequence.Location;
import com.xl.exception.REDException;
import com.xl.utils.GeneType;
import com.xl.utils.NameRetriever;
import com.xl.utils.ParsingUtils;
import com.xl.utils.Strand;
import com.xl.utils.filefilters.FileFilterExt;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * The Class UCSCRefGeneParser is a parser to parse RefSeq gene annotation file from UCSC.
 */
public class UCSCRefGeneParser extends AnnotationParser {

    private int nameColumn = 0;
    private int chromColumn = 0;
    private int strandColumn = 0;
    private int txStartColumn = 0;
    private int txEndColumn = 0;
    private int cdsStartColumn = 0;
    private int cdsEndColumn = 0;
    private int exonCountColumn = 0;
    private int exonStartsBufferColumn = 0;
    private int exonEndsBufferColumn = 0;
    private int aliasNameColumn = 0;

    public UCSCRefGeneParser(Genome genome) {
        super(genome);
    }

    protected AnnotationSet parseAnnotation(GeneType type, BufferedReader br, Genome genome) throws IOException {
        parseGeneType(type);
        AnnotationSet currentAnnotation = new CoreAnnotationSet(genome);
        int lineCount = 0;
        String line;
        while ((line = br.readLine()) != null) {
            lineCount++;
            if (cancel) {
                progressCancelled();
                return null;
            }

            if (lineCount % 1000 == 0) {
                progressUpdated("Read " + lineCount + " lines from " + genome.getDisplayName(), 0, 1);
            }

            String[] sections = line.split("\t");
            try {
                // int index = Integer.parseInt(sections[binColumn]);
                String name = sections[nameColumn].trim();
                String aliasName = sections[aliasNameColumn];
                String chr = sections[chromColumn];
                List<Location> allLocations = new ArrayList<Location>();
                if (NameRetriever.isStandardChromosomeName(chr)) {
                    //Get the strand of the feature.
                    Strand strand = Strand.parseStrand(sections[strandColumn]);

                    //Get the transcription start and end of the feature.
                    int txStart = Integer.parseInt(sections[txStartColumn]);
                    int txEnd = Integer.parseInt(sections[txEndColumn]);
                    allLocations.add(new Location(txStart, txEnd));

                    //Get the coding region's start and end of the feature.
                    int cdsStart = Integer.parseInt(sections[cdsStartColumn]);
                    int cdsEnd = Integer.parseInt(sections[cdsEndColumn]);
                    allLocations.add(new Location(cdsStart, cdsEnd));

                    //Get all exons' start and end of the feature.
                    int exonCount = Integer.parseInt(sections[exonCountColumn]);
                    String[] exonStarts = sections[exonStartsBufferColumn].split(",");
                    String[] exonEnds = sections[exonEndsBufferColumn].split(",");
                    if (exonStarts.length == exonEnds.length) {
                        for (int i = 0; i < exonCount; i++) {
                            allLocations.add(new Location(Integer.parseInt(exonStarts[i]), Integer.parseInt(exonEnds[i])));
                        }
                    }
                    Feature newFeature = new Feature(name, chr, strand, allLocations, aliasName);
                    currentAnnotation.addFeature(newFeature);

                }
            } catch (NumberFormatException e) {
                progressWarningReceived(new REDException("Location " + sections[txStartColumn] + "-" + sections[txEndColumn] + " was not an integer"));
                e.printStackTrace();
            }
        }
        currentAnnotation.finalise();
        br.close();
        progressComplete("annotation_loaded", currentAnnotation);
        return currentAnnotation;
    }

    @Override
    public FileFilterExt fileFilter() {
        return new FileFilterExt(".txt");
    }

    @Override
    protected AnnotationSet parseAnnotation(File file) throws Exception {
        GeneType geneType = ParsingUtils.parseGeneType(file.getName());

        BufferedReader br = null;
        try {
            if (file.getName().toLowerCase().endsWith(".gz")) {
                br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            } else {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parseAnnotation(geneType, br, genome);
    }

    @Override
    public String name() {
        return "UCSC Gene Parser";
    }

    public void parseGeneType(GeneType type) {
        switch (type) {
            case REFFLAT:
                aliasNameColumn = 0;
                nameColumn = 1;
                chromColumn = 2;
                strandColumn = 3;
                txStartColumn = 4;
                txEndColumn = 5;
                cdsStartColumn = 6;
                cdsEndColumn = 7;
                exonCountColumn = 8;
                exonStartsBufferColumn = 9;
                exonEndsBufferColumn = 10;
                break;
            case GENEPRED:
                nameColumn = 1;
                chromColumn = 2;
                strandColumn = 3;
                txStartColumn = 4;
                txEndColumn = 5;
                cdsStartColumn = 6;
                cdsEndColumn = 7;
                exonCountColumn = 8;
                exonStartsBufferColumn = 9;
                exonEndsBufferColumn = 10;
                aliasNameColumn = 12;
                break;
            case UCSCGENE:
                nameColumn = 0;
                chromColumn = 1;
                strandColumn = 2;
                txStartColumn = 3;
                txEndColumn = 4;
                cdsStartColumn = 5;
                cdsEndColumn = 6;
                exonCountColumn = 7;
                exonStartsBufferColumn = 8;
                exonEndsBufferColumn = 9;
                aliasNameColumn = 10;
                break;
        }
    }

}
