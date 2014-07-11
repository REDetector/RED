package com.xl.parsers.annotationparsers;

import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.annotation.CoreAnnotationSet;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.display.featureviewer.Feature;
import com.xl.exception.REDException;
import com.xl.utils.ChromosomeUtils;
import com.xl.utils.GeneType;
import com.xl.utils.Strand;
import com.xl.utils.filefilters.FileFilterExt;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

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

    private List<String> nameList = new LinkedList<String>();

    public UCSCRefGeneParser(Genome genome) {
        super(genome);
    }

    @Override
    public boolean requiresFile() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    protected AnnotationSet[] parseAnnotation(GeneType type, File file)
            throws Exception {
        // TODO Auto-generated method stub
        BufferedReader br = null;
        try {
            if (file.getName().toLowerCase().endsWith(".gz")) {
                br = new BufferedReader(new InputStreamReader(
                        new GZIPInputStream(new FileInputStream(file))));
            } else {
                br = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parseAnnotation(type, br, genome);
    }

    protected AnnotationSet[] parseAnnotation(GeneType type, BufferedReader br, Genome genome) throws IOException {
        System.out.println(this.getClass().getName() + ":parseAnnotation()");
        parseGeneType(type);
        Vector<AnnotationSet> annotationSets = new Vector<AnnotationSet>();
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
                progressUpdated(
                        "Read " + lineCount + " lines from "
                                + genome.getDisplayName(), 0, 1);
            }
            if (lineCount == 1000000) {
                currentAnnotation.finalise();
                annotationSets.add(currentAnnotation);
                currentAnnotation = new AnnotationSet(genome,
                        genome.getGenomeId() + "[" + annotationSets.size()
                                + "]");
            }

            if (lineCount > 1000000 && lineCount % 1000000 == 0) {
                progressUpdated("Caching...", 0, 1);
                currentAnnotation.finalise();
                currentAnnotation = new AnnotationSet(genome,
                        genome.getGenomeId() + "[" + annotationSets.size()
                                + "]");
                annotationSets.add(currentAnnotation);
            }
            String[] sections = line.split("\t");
            try {
                // int index = Integer.parseInt(sections[binColumn]);
                String name = sections[nameColumn].trim();
                String aliasName = sections[aliasNameColumn];
                String chr = sections[chromColumn];
                if (ChromosomeUtils.isStandardChromosomeName(chr)) {
                    //Get the strand of the feature.
                    Strand strand = Strand.parseStrand(sections[strandColumn]);

                    //Get the transcription start and end of the feature.
                    int txStart = Integer.parseInt(sections[txStartColumn]);
                    int txEnd = Integer.parseInt(sections[txEndColumn]);
                    Location txLocation = new SequenceRead(txStart, txEnd);

                    //Get the coding region's start and end of the feature.
                    int cdsStart = Integer.parseInt(sections[cdsStartColumn]);
                    int cdsEnd = Integer.parseInt(sections[cdsEndColumn]);
                    Location cdsLocation = new SequenceRead(cdsStart, cdsEnd);

                    //Get all exons' start and end of the feature.
                    int exonCount = Integer.parseInt(sections[exonCountColumn]);
                    String[] exonStarts = sections[exonStartsBufferColumn].split(",");
                    String[] exonEnds = sections[exonEndsBufferColumn].split(",");
                    Location[] exonLocations = new Location[exonCount];
                    if (exonStarts.length == exonEnds.length) {
                        for (int i = 0; i < exonCount; i++) {
                            exonLocations[i] = new SequenceRead(
                                    Integer.parseInt(exonStarts[i]),
                                    Integer.parseInt(exonEnds[i]));
                        }
                    }
                    Feature newFeature = new Feature(name, chr, strand,
                            txLocation, cdsLocation, exonLocations, aliasName);
                    if (nameList.contains(aliasName) || nameList.contains(name)) {
                        Feature originFeature = currentAnnotation.getFeaturesForName(chr, aliasName);
                        if (originFeature != null && newFeature.getTotalLength() > originFeature.getTotalLength()) {
                            currentAnnotation.deleteFeature(chr, originFeature);
                        }
                        currentAnnotation.addFeature(newFeature);
                    } else {
                        nameList.add(aliasName);
                        nameList.add(name);
                        currentAnnotation.addFeature(newFeature);
                    }
                }
            } catch (NumberFormatException e) {
                progressWarningReceived(new REDException("Location "
                        + sections[txStartColumn] + "-" + sections[txEndColumn]
                        + " was not an integer"));
                e.printStackTrace();
            }
        }

        if (lineCount < 1000000)

        {
            currentAnnotation.finalise();
            annotationSets.add(currentAnnotation);
        }
        System.out.println(this.getClass().getName() + ":parseAnnotation() end");
        br.close();
        progressComplete("annotation_loaded", annotationSets);
        return annotationSets.toArray(new AnnotationSet[0]);
    }

    @Override
    public String name() {
        // TODO Auto-generated method stub
        return "UCSC Gene Parser";
    }

    @Override
    public FileFilterExt fileFilter() {
        // TODO Auto-generated method stub
        return new FileFilterExt(".txt");
    }

//    private Feature mergeTwoFeatures(Feature originalFeature, Feature newFeature) {
//        String name = originalFeature.getName().length() > newFeature.getName().length() ? newFeature.getName() : originalFeature.getName();
//        String aliasName = originalFeature.getAliasName();
//        String chr = originalFeature.getChr();
//        Strand strand = originalFeature.getStrand();
//        int txStart = originalFeature.getTxLocation().getStart() <= newFeature.getTxLocation().getStart() ? originalFeature.getTxLocation().getStart() : newFeature.getTxLocation().getStart();
//        int txEnd = originalFeature.getTxLocation().getEnd() >= newFeature.getTxLocation().getEnd() ? originalFeature.getTxLocation().getEnd() : newFeature.getTxLocation().getEnd();
//
//        Location txLocation = new SequenceRead(txStart, txEnd);
//        int cdsStart = originalFeature.getCdsLocation().getStart() <= newFeature.getCdsLocation().getStart() ? originalFeature.getCdsLocation().getStart() : newFeature.getCdsLocation().getStart();
//        int cdsEnd = originalFeature.getCdsLocation().getEnd() >= newFeature.getCdsLocation().getEnd() ? originalFeature.getCdsLocation().getEnd() : newFeature.getCdsLocation().getEnd();
//        Location cdsLocation = new SequenceRead(cdsStart, cdsEnd);
//
//        Vector<Location> exonLocations = new Vector<Location>();
//        Location[] originalExons = originalFeature.getExonLocations();
//        Location[] newExons = newFeature.getExonLocations();
//        for (int i = 0; i < originalExons.length; i++) {
//            for (int j = 0; j < newExons.length; j++) {
//                if (SequenceReadUtils.duplicate(originalExons[i], newExons[j])) {
//                    exonLocations.add(originalExons[i]);
//                    j = newExons.length;
//                } else if (SequenceReadUtils.overlaps(originalExons[i], newExons[j])) {
//                    int exonStart = originalExons[i].getStart() <= newExons[j].getStart() ? originalExons[i].getStart() : newExons[j].getStart();
//                    int exonEnd = originalExons[i].getEnd() >= newExons[j].getEnd() ? originalExons[i].getEnd() : newExons[j].getEnd();
//                    Location tmpExon = new SequenceRead(exonStart, exonEnd);
//                    exonLocations.add(tmpExon);
//                    j = newExons.length;
//                }
//            }
//        }
//        Location[] exonLocation = exonLocations.toArray(new Location[0]);
//        return new Feature(name, chr, strand, txLocation, cdsLocation, exonLocation, aliasName);
//    }

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
