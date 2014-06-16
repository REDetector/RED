package com.xl.parsers.dataparsers;

/**
 * Copyright Copyright 2007-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import com.xl.datatypes.*;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.dialog.CrashReporter;
import com.xl.dialog.ProgressDialog;
import com.xl.display.featureviewer.Feature;
import com.xl.exception.REDException;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;
import com.xl.parsers.annotationparsers.IGVGenomeParser;
import com.xl.preferences.DisplayPreferences;
import com.xl.preferences.REDPreferences;
import com.xl.utils.GenomeUtils;
import com.xl.utils.MessageUtils;
import com.xl.utils.Strand;
import net.xl.genomes.GenomeDownloader;

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * The SeqMonkParser reads the main SeqMonk file format. It is different to all
 * other data parsers in that it does not extend DataParser since it not only
 * contains data, but can also load genomes and probes and can set visual
 * preferences.
 */
public class REDParser implements Runnable, ProgressListener {

    /**
     * The Constant MAX_DATA_VERSION says what is the highest version of the
     * SeqMonk file format this parser can understand. If the file to be loaded
     * has a version higher than this then the parser won't attempt to load it.
     */

    public static final int MAX_DATA_VERSION = 1;

    private REDApplication application;
    private BufferedReader br;
    private Vector<ProgressListener> listeners = new Vector<ProgressListener>();
    private DataSet[] dataSets;
    private DataGroup[] dataGroups;
    private boolean genomeLoaded = false;
    private Exception exceptionReceived = null;
    private boolean pauseWhilstLoadingGenome = false;
    private Probe[] probes;
    private int thisDataVersion = -1;

    /**
     * Instantiates a new red parser.
     *
     * @param application The application which we're loading this file into
     */
    public REDParser(REDApplication application) {
        this.application = application;
    }

    /**
     * Application.
     *
     * @return the red application
     */
    public REDApplication application() {
        return application;
    }

    /**
     * Adds a progress listener.
     *
     * @param l The listener to add
     */
    public void addProgressListener(ProgressListener l) {
        if (l != null && !listeners.contains(l))
            listeners.add(l);
    }

    /**
     * Removes a progress listener.
     *
     * @param l The listener to remove
     */
    public void removeProgressListener(ProgressListener l) {
        if (l != null && listeners.contains(l))
            listeners.remove(l);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {

        genomeLoaded = false;
        exceptionReceived = null;

        // Loading this data is done in a modular manner. Every line
        // we read should be a key followed by one or more values (tab
        // delimited). Whatever it is we pass it on to a specialised subroutine
        // to deal with.

        try {
            String line;
            String[] sections;

            Vector<AnnotationSet> annotationSets = new Vector<AnnotationSet>();

            while ((line = br.readLine()) != null) {
                sections = line.split("\\t");

                // Now we look where to send this...
                if (sections[0].equals("RED Data Version")) {
                    parseDataVersion(sections);
                } else if (sections[0].equals("Features")) {
                    if (!genomeLoaded) {
                        throw new REDException(
                                "No genome definition found before data");
                    }
                    parseFeatures(sections);
                } else if (sections[0].equals("Samples")) {
                    if (!genomeLoaded) {
                        throw new REDException(
                                "No genome definition found before data");
                    }
                    parseSamples(sections);
                } else if (sections[0].equals("Annotation")) {
                    if (!genomeLoaded) {
                        throw new REDException(
                                "No genome definition found before data");
                    }
                    annotationSets.add(parseAnnotation(sections));
                } else if (sections[0].equals("Data Groups")) {
                    if (!genomeLoaded) {
                        throw new REDException(
                                "No genome definition found before data");
                    }
                    try {
                        parseGroups(sections);
                    } catch (REDException ex) {
                        if (ex.getMessage().contains("ambiguous")) {
                            Enumeration<ProgressListener> e = listeners
                                    .elements();
                            while (e.hasMoreElements()) {
                                e.nextElement().progressWarningReceived(ex);
                            }
                        } else {
                            throw ex;
                        }
                    }
                } else if (sections[0].equals("Replicate Sets")) {
                    if (!genomeLoaded) {
                        throw new REDException(
                                "No genome definition found before data");
                    }
                    parseReplicates(sections);
                } else if (sections[0].equals("Probes")) {
                    if (!genomeLoaded) {
                        throw new REDException(
                                "No genome definition found before data");
                    }
                    parseProbes(sections);
                } else if (sections[0].equals("Lists")) {
                    if (!genomeLoaded) {
                        throw new REDException(
                                "No genome definition found before data");
                    }
                    parseLists(sections);
                } else if (sections[0].equals("Genome")) {
                    parseGenome();
                } else if (sections[0].equals("Visible Stores")) {
                    if (!genomeLoaded) {
                        throw new REDException(
                                "No genome definition found before data");
                    }
                    parseVisibleStores(sections);
                } else if (sections[0].equals("Display Preferences")) {
                    if (!genomeLoaded) {
                        throw new REDException(
                                "No genome definition found before data");
                    }

                    // Add any annotation sets we've parsed at this point
                    application
                            .dataCollection()
                            .genome()
                            .getAnnotationCollection()
                            .addAnnotationSets(
                                    annotationSets
                                            .toArray(new AnnotationSet[0]));

                    parseDisplayPreferences(sections);
                } else {
                    throw new REDException("Didn't recognise section '"
                            + sections[0] + "' in red file");
                }
            }

            // We're finished with the file
            if (br != null) {
                br.close();
            }

        } catch (Exception ex) {
            Enumeration<ProgressListener> e = listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().progressExceptionReceived(ex);
            }
            try {
                br.close();
            } catch (IOException e1) {
                new CrashReporter(e1);
            }
            return;
        }

        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            // In this case we put out a dummy empty dataset since
            // we've already entered the data into the collection by now
            e.nextElement().progressComplete("datasets_loaded", new DataSet[0]);
        }
        application.resetChangesWereMade();

    }

    /**
     * Parses the file.
     *
     * @param file The file to parse
     */
    public void parseFile(File file) {

		/*
         * The attempt to open the file as a GZIP input stream can on some
		 * systems leave an open filehandle to the red file. We therefore create
		 * the lowest fileinputstream as a separate object so we can be
		 * absolutely sure that it's closed before we reopen as a normal input
		 * stream.
		 */
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    fis)));
        } catch (IOException ioe) {

            try {
                if (fis != null) {
                    fis.close();
                }
                br = new BufferedReader(new FileReader(file));
            } catch (IOException ex) {
                Enumeration<ProgressListener> e = listeners.elements();
                while (e.hasMoreElements()) {
                    e.nextElement().progressExceptionReceived(ex);
                }
                return;
            }

        }

        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Parses the data version.
     *
     * @param sections data version line split on tabs
     * @throws REDException
     */
    private void parseDataVersion(String[] sections) throws REDException {
        if (sections.length != 2) {
            throw new REDException(
                    "Data Version line didn't contain 2 sections");
        }

        thisDataVersion = Integer.parseInt(sections[1]);

        if (thisDataVersion > MAX_DATA_VERSION) {
            throw new REDException(
                    "This data file needs a newer verison of RED to read it.");
        }
        System.out.println(this.getClass().getName() + ":parseDataVersion()"
                + "\t" + thisDataVersion);
    }

    /**
     * Parses the genome line.
     *
     * @throws REDException
     */
    private void parseGenome() throws Exception {
        System.out.println(this.getClass().getName() + ":parseGenome()");
        String line;
        String[] sections;
        while ((line = br.readLine()) != null) {
            if (line.equals(GenomeUtils.GENOME_INFORMATION_END)) {
                MessageUtils.showInfo(GenomeUtils.GENOME_INFORMATION_END);
                break;
            }
            sections = line.split("\\t");
            if (sections.length == 2) {
                MessageUtils.showInfo(sections[0] + "\t" + sections[1]);
                setProperties(sections[0], sections[1]);
            }

        }

        File f;
        try {
            f = new File(REDPreferences.getInstance().getGenomeBase()
                    .getAbsoluteFile()
                    + File.separator + GenomeDescriptor.getInstance().getDisplayName() + File.separator + GenomeDescriptor.getInstance().getGenomeId() + ".genome");
            System.out.println(this.getClass().getName() + ":" + f.getAbsolutePath());
        } catch (FileNotFoundException e) {
            throw new REDException(
                    "Couldn't find the folder which should contain the genome files.  Please check your file preferences.");
        }
        if (!f.exists()) {
            // The user doesn't have this genome - yet...
            GenomeDownloader d = new GenomeDownloader();
            d.addProgressListener(this);
            ProgressDialog progressDialog = new ProgressDialog(application,
                    "Downloading genome...");
            d.addProgressListener(progressDialog);
            d.downloadGenome(GenomeDescriptor.getInstance().getGenomeId(), GenomeDescriptor.getInstance().getDisplayName(), true);
            progressDialog.requestFocus();
            pauseWhilstLoadingGenome = true;
            while (pauseWhilstLoadingGenome) {
                if (exceptionReceived != null)
                    throw exceptionReceived;
            }
        }

        IGVGenomeParser parser = new IGVGenomeParser();
        parser.addProgressListener(this);
        parser.parseGenome(f);

        while (!genomeLoaded) {
            if (exceptionReceived != null)
                throw exceptionReceived;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }

    }

    private void setProperties(String key, String value) {
        if (value == null) {
            return;
        }
        if (key.equals(GenomeUtils.KEY_CHR_ALIAS_FILE_NAME)) {
            GenomeDescriptor.getInstance().setChrAliasFileName(value);
        } else if (key.equals(GenomeUtils.KEY_CHR_NAMES_ALTERED)) {
            GenomeDescriptor.getInstance().setChrNamesAltered(Boolean.parseBoolean(value));
        } else if (key.equals(GenomeUtils.KEY_CHROMOSOMES_ARE_ORDERED)) {
            GenomeDescriptor.getInstance().setChromosomesAreOrdered(Boolean.parseBoolean(value));
        } else if (key.equals(GenomeUtils.KEY_CYTOBAND_FILE_NAME)) {
            GenomeDescriptor.getInstance().setCytoBandFileName(value);
        } else if (key.equals(GenomeUtils.KEY_DISPLAY_NAME)) {
            GenomeDescriptor.getInstance().setDisplayName(value);
        } else if (key.equals(GenomeUtils.KEY_FASTA)) {
            GenomeDescriptor.getInstance().setFasta(Boolean.parseBoolean(value));
        } else if (key.equals(GenomeUtils.KEY_FASTA_DIRECTORY)) {
            GenomeDescriptor.getInstance().setFastaDirectory(Boolean.parseBoolean(value));
        } else if (key.equals(GenomeUtils.KEY_FASTA_FILE_NAME_STRING)) {
            GenomeDescriptor.getInstance().setFastaFileNames(value.split(","));
        } else if (key.equals(GenomeUtils.KEY_GENE_FILE_NAME)) {
            GenomeDescriptor.getInstance().setGeneFileName(value);
        } else if (key.equals(GenomeUtils.KEY_GENE_TRACK_NAME)) {
            GenomeDescriptor.getInstance().setGeneTrackName(value);
        } else if (key.equals(GenomeUtils.KEY_GENOME_ID)) {
            GenomeDescriptor.getInstance().setGenomeId(value);
        } else if (key.equals(GenomeUtils.KEY_HAS_CUSTOM_SEQUENCE_LOCATION)) {
            GenomeDescriptor.getInstance().setHasCustomSequenceLocation(Boolean.parseBoolean(value));
        } else if (key.equals(GenomeUtils.KEY_SEQUENCE_LOCATION)) {
            GenomeDescriptor.getInstance().setSequenceLocation(value);
        } else if (key.equals(GenomeUtils.KEY_URL)) {
            GenomeDescriptor.getInstance().setUrl(value);
        }
    }

    /**
     * Parses the set of feature tracks to be displayed
     *
     * @param sections The initial tab split features line
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseFeatures(String[] sections) throws REDException,
            IOException {
        System.out.println(this.getClass().getName() + ":parseFeatures()");
        if (sections.length != 2) {
            throw new REDException("Features line didn't contain 2 sections");
        }
        if (!sections[0].equals("Features")) {
            throw new REDException("Couldn't find expected features line");
        }
        application.setCurrentFeatureTrackName(sections[1]);
    }

    /**
     * Parses an external set of annotations
     *
     * @param sections The tab split initial annotation line
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private AnnotationSet parseAnnotation(String[] sections)
            throws REDException, IOException {
        System.out.println(this.getClass().getName() + ":parseAnnotation()");
        if (sections.length != 3) {
            throw new REDException("Annotation line didn't contain 3 sections");
        }

        AnnotationSet set = new AnnotationSet(application.dataCollection()
                .genome(), sections[1]);

        int featureCount = Integer.parseInt(sections[2]);

        for (int i = 0; i < featureCount; i++) {

            if (i % 1000 == 0) {
                progressUpdated("Parsing annotation in " + set.name(), i,
                        featureCount);
            }
            sections = br.readLine().split("\\t");
            String name = sections[0];
            String chr = sections[1];
            Strand strand = Strand.parseStrand(sections[2]);
            String[] loca = sections[3].split("-");
            Location txLocation = new SequenceRead(Integer.parseInt(loca[0]),
                    Integer.parseInt(loca[1]));
            loca = sections[4].split("-");
            Location cdsLocation = new SequenceRead(Integer.parseInt(loca[0]),
                    Integer.parseInt(loca[1]));
            loca = sections[5].split(",");
            Location[] exonLocations = new Location[loca.length];

            for (int l = 0; l < loca.length; l++) {
                String[] exonLoc = loca[l].split("-");
                exonLocations[l] = new SequenceRead(
                        Integer.parseInt(exonLoc[0]),
                        Integer.parseInt(exonLoc[1]));
            }
            String aliasName = sections[6];
            Feature basicFeature = new Feature(name, chr, strand,
                    txLocation, cdsLocation, exonLocations, aliasName);
            set.addFeature(basicFeature);
        }

        set.finalise();
        return set;
    }

    /**
     * Parses the list of samples.
     *
     * @param sections The tab split initial samples line
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseSamples(String[] sections) throws REDException,
            IOException {
        System.out.println(this.getClass().getName() + ":parseSamples()");
        if (sections.length != 2) {
            throw new REDException("Samples line didn't contain 2 sections");
        }
        if (!sections[0].equals("Samples")) {
            throw new REDException("Couldn't find expected samples line");
        }

        int n = Integer.parseInt(sections[1]);

        // We need to keep the Data Sets around to add data to later.
        dataSets = new DataSet[n];

        for (int i = 0; i < n; i++) {
            sections = br.readLine().split("\\t");
            // Originally there was only one section (the DataSet name). Then
            // there were two names, a user supplied name and the original
            // imported file name. Now there are 3 sections where the third
            // section indicates the type of dataset. The only unusual one is a
            // HiC dataset anything else is assumed to be a normal dataset.
            if (sections.length == 1) {
                dataSets[i] = new DataSet(sections[0], "Not known", false);
            } else if (sections.length == 2) {
                dataSets[i] = new DataSet(sections[0], sections[1], false);
            } else if (sections.length == 3) {
                if (sections[2].equals("HiC")) {
                    dataSets[i] = new PairedDataSet(sections[0], sections[1],
                            false, 0, false);
                } else {
                    dataSets[i] = new DataSet(sections[0], sections[1], false);
                }
            }
        }

        // Immediately after the list of samples comes the lists of reads
        String line = null;

        // Iterate through the number of samples
        for (int i = 0; i < n; i++) {

            Enumeration<ProgressListener> en = listeners.elements();
            while (en.hasMoreElements()) {
                en.nextElement().progressUpdated(
                        "Reading data for " + dataSets[i].name(), i * 10,
                        n * 10);
            }

            // The first line is
            line = br.readLine();
            sections = line.split("\t");
            if (sections.length != 2) {
                throw new REDException("Read line " + i
                        + " didn't contain 2 sections");
            }
            int readCount = Integer.parseInt(sections[0]);

            if (dataSets[i] instanceof PairedDataSet) {
                // Paired Data sets have a different format, with a packed
                // position for the first read, then a chromosome name and
                // packed position for the second read.

                // From v13 onwards we started entering HiC data pairs in both
                // directions so
                // the data would be able to be read back in without sorting it.
                // boolean hicDataIsPresorted = thisDataVersion >= 13;

                // We use this to update the progress bar every time we move to
                // a new chromosome
                int seenChromosomeCount = 0;

                while (true) {
                    // The first line should be the chromosome and a number of
                    // reads
                    line = br.readLine();

                    if (line == null) {
                        throw new REDException(
                                "Ran out of data whilst parsing reads for sample "
                                        + i);
                    }

                    // A blank line indicates the end of the sample
                    if (line.length() == 0)
                        break;

                    sections = line.split("\\t");

                    ++seenChromosomeCount;
                    progressUpdated("Reading data for " + dataSets[i].name(), i
                            * application.dataCollection().genome()
                            .getAllChromosomeCount()
                            + seenChromosomeCount, n
                            * application.dataCollection().genome()
                            .getAllChromosomeCount());

                    String chr = sections[0];
                    int chrReadCount = Integer.parseInt(sections[1]);

                    // System.err.println("Trying to parse "+chrReadCount+" from chr "+c.name());

                    for (int r = 0; r < chrReadCount; r++) {

                        line = br.readLine();
                        if (line == null) {
                            throw new REDException(
                                    "Ran out of data whilst parsing reads for sample "
                                            + i);
                        }

						/*
                         * We used to have a split("\t") here, but this turned
						 * out to be the bottleneck which hugely restricted the
						 * speed at which the data could be read. Switching for
						 * a set of index calls and substring makes this *way*
						 * faster.
						 */
                        sections = line.split(":");
                        String[] sourceReads = sections[0].split("\\t");
                        String[] hitReads = sections[1].split("\\t");
                        SequenceRead sourceSeq = parsePairedDataSetLine(chr,
                                sourceReads);
                        SequenceRead hitSeq = parsePairedDataSetLine(chr,
                                hitReads);
                        try {
                            dataSets[i].addData(sourceSeq);
                            dataSets[i].addData(hitSeq);
                        } catch (REDException ex) {
                            Enumeration<ProgressListener> e = listeners
                                    .elements();
                            while (e.hasMoreElements()) {
                                e.nextElement().progressWarningReceived(ex);
                                continue;
                            }
                        }
                    }

                }
            } else {
                // In versions after 7 we split the section up into chromosomes
                // so we don't put the chromosome on every line, and we put out
                // the packed double value rather than the individual start, end
                // and strand

                // As of version 12 we collapse repeated reads into one line
                // with
                // a count after it, so we need to check for this.

                // We keep count of reads processed to update the progress
                // listeners
                int readsRead = 0;

                while (true) {
                    // The first line should be the chromosome and a number of
                    // reads
                    line = br.readLine();

                    if (line == null) {
                        throw new REDException(
                                "Ran out of data whilst parsing reads for sample "
                                        + i);
                    }

                    // A blank line indicates the end of the sample
                    if (line.length() == 0)
                        break;

                    sections = line.split("\\t");

                    // We don't try to capture this exception since we can't
                    // then process any of the reads which follow.
                    String chr = sections[0];
                    int chrReadCount = Integer.parseInt(sections[1]);

                    for (int r = 0; r < chrReadCount; r++) {
                        if ((readsRead % (1 + (readCount / 10))) == 0) {
                            Enumeration<ProgressListener> en2 = listeners
                                    .elements();
                            while (en2.hasMoreElements()) {
                                en2.nextElement()
                                        .progressUpdated(
                                                "Reading data for "
                                                        + dataSets[i].name(),
                                                i
                                                        * 10
                                                        + (readsRead / (1 + (readCount / 10))),
                                                n * 10);
                            }
                        }

                        line = br.readLine();
                        if (line == null) {
                            throw new REDException(
                                    "Ran out of data whilst parsing reads for sample "
                                            + i);
                        }
                        sections = line.split("\\t");
                        // We use some custom parsing code to efficiently
                        // extract the packed position and count from the line.
                        // This avoids having to use the generic parsing code or
                        // doing a split to determine the tab location.
                        dataSets[i].addData(parsePairedDataSetLine(chr, sections));
                    }

                }

            }

            // We've now read all of the data for this sample so we can compact
            // it
            Enumeration<ProgressListener> en2 = listeners.elements();
            while (en2.hasMoreElements()) {
                en2.nextElement().progressUpdated(
                        "Caching data for " + dataSets[i].name(), (i + 1) * 10,
                        n * 10);
            }
            dataSets[i].finalise();
            System.out.println(this.getClass().getName()
                    + ":application.dataCollection().addDataSet(dataSets[i]);");
            application.dataCollection().addDataSet(dataSets[i]);
        }
    }

    /**
     * Parses the list of sample groups.
     *
     * @param sections The tab split values from the initial groups line
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseGroups(String[] sections) throws REDException,
            IOException {
        if (sections.length != 2) {
            throw new REDException("Data Groups line didn't contain 2 sections");
        }
        if (!sections[0].equals("Data Groups")) {
            throw new REDException("Couldn't find expected data groups line");
        }

        int n = Integer.parseInt(sections[1]);

        dataGroups = new DataGroup[n];

        for (int i = 0; i < n; i++) {
            String[] group = br.readLine().split("\\t");
            DataSet[] groupMembers = new DataSet[group.length - 1];

            if (thisDataVersion < 4) {
                DataSet[] allSets = application.dataCollection()
                        .getAllDataSets();
                // In the bad old days we used to refer to datasets by their
                // name
                for (int j = 1; j < group.length; j++) {

                    boolean seen = false;
                    for (int d = 0; d < allSets.length; d++) {
                        // System.out.println("Comparing "+group[j]+" to "+allSets[d]+" at index "+d);

                        if (allSets[d].name().equals(group[j])) {
                            if (seen) {
                                // System.out.println("Seen this before - abort abort");
                                throw new REDException("Name " + group[j]
                                        + " is ambiguous in group " + group[0]);
                            }
                            // System.out.println("Seen for the first time");
                            groupMembers[j - 1] = allSets[d];
                            seen = true;
                        }
                    }
                }
            } else {
                for (int j = 1; j < group.length; j++) {

                    // The more modern and safer way to make up a group is by
                    // its index
                    groupMembers[j - 1] = application.dataCollection()
                            .getDataSet(Integer.parseInt(group[j]));
                    if (groupMembers[j - 1] == null) {
                        throw new REDException(
                                "Couldn't find dataset at position " + group[j]);
                    }
                }
            }

            DataGroup g = new DataGroup(group[0], groupMembers);
            application.dataCollection().addDataGroup(g);
            dataGroups[i] = g;
        }
    }

    /**
     * Parses the list of replicate sets.
     *
     * @param sections The tab split values from the initial replicates line
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseReplicates(String[] sections) throws REDException,
            IOException {
        if (sections.length != 2) {
            throw new REDException("Data Groups line didn't contain 2 sections");
        }
        if (!sections[0].equals("Replicate Sets")) {
            throw new REDException("Couldn't find expected replicates line");
        }

        int n = Integer.parseInt(sections[1]);

        for (int i = 0; i < n; i++) {
            String[] replicateLine = br.readLine().split("\\t");
            DataStore[] groupMembers = new DataStore[replicateLine.length - 1];

            for (int j = 1; j < replicateLine.length; j++) {

                if (replicateLine[j].startsWith("g")) {
                    replicateLine[j] = replicateLine[j].substring(1);
                    groupMembers[j - 1] = application.dataCollection()
                            .getDataGroup(Integer.parseInt(replicateLine[j]));
                } else if (replicateLine[j].startsWith("s")) {
                    replicateLine[j] = replicateLine[j].substring(1);
                    groupMembers[j - 1] = application.dataCollection()
                            .getDataSet(Integer.parseInt(replicateLine[j]));
                } else {
                    throw new REDException("Replicate member id "
                            + replicateLine[j] + " didn't start with g or s");
                }

                if (groupMembers[j - 1] == null) {
                    throw new REDException(
                            "Couldn't find replicate member from position "
                                    + replicateLine[j]);
                }
            }

            ReplicateSet r = new ReplicateSet(replicateLine[0], groupMembers);
            application.dataCollection().addReplicateSet(r);
        }
    }

    /**
     * Parses the list of probes.
     *
     * @param sections The tab split initial line from the probes section
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseProbes(String[] sections) throws REDException,
            IOException {

        if (sections.length < 2) {
            throw new REDException(
                    "Probe line didn't contain at least 2 sections");
        }
        if (!sections[0].equals("Probes")) {
            throw new REDException("Couldn't find expected probes line");
        }

        int n = Integer.parseInt(sections[1]);

        probes = new Probe[n];

        String description = "No generator description available";

        if (sections.length > 2) {
            description = sections[2];
        }

        ProbeSet probeSet = new ProbeSet(description, n);

        if (sections.length > 3) {
            if (sections[3].length() > 0) {
                probeSet.setCurrentQuantitation(sections[3]);
            }
        }

        if (sections.length > 4) {
            probeSet.setComments(sections[4].replaceAll("`", "\n"));
        }

        // We need to save the probeset to the dataset at this point so we can
        // add the probe
        // lists as we get to them.
        application.dataCollection().setProbeSet(probeSet);

        int positionOffset;

        // We used to store chr start and end
        if (thisDataVersion < 8) {
            positionOffset = 4;
        }
        // We now store chr and packed position (to give start end and strand)
        else {
            positionOffset = 3;

        }

        int expectedSectionLength = 3 + dataSets.length + dataGroups.length;

        String line;
        for (int i = 0; i < n; i++) {
            line = br.readLine();
            if (line == null) {
                throw new REDException("Ran out of probe data at line " + i
                        + " (expected " + n + " probes)");
            }

            // Since the probes section can have blank trailing sections we need
            // to not trim these, hence the -1 limit.
            sections = line.split("\\t", -1);

            if (i == 0) {
                /*
                 * Older versions of this format put down data for just
				 * datasets. Newer versions include data for datagroups as well.
				 * We need to figure out which one we're looking at
				 */
                if (sections.length == positionOffset + dataSets.length) {
                    expectedSectionLength = positionOffset + dataSets.length;
                } else if (sections.length == positionOffset + dataSets.length
                        + dataGroups.length) {
                    expectedSectionLength = positionOffset + dataSets.length
                            + dataGroups.length;
                }
            }

            if (sections.length != expectedSectionLength) {
                throw new REDException("Expected " + expectedSectionLength
                        + " sections in data file for " + sections[0]
                        + " but got " + sections.length);
            }

            if (i % 10000 == 0) {
                Enumeration<ProgressListener> e = listeners.elements();
                while (e.hasMoreElements()) {
                    e.nextElement().progressUpdated(
                            "Processed data for " + i + " probes", i, n);
                }
            }
            String c = sections[1];

            // Sanity check
            if (c == null) {
                throw new REDException("Couldn't find a chromosome called "
                        + sections[1]);
            }

            int start = Integer.parseInt(sections[2]);
            int end = Integer.parseInt(sections[3]);
            Probe p = new Probe(c, start, end);
            if (!sections[0].equals("null")) {
                p.setName(sections[0]);
            }

            probes[i] = p;
            probeSet.addProbe(probes[i], null);

			/*
             * Our probe data lines have all of the dataset values first,
			 * followed by the datagroup values.
			 */

            for (int j = positionOffset; j < sections.length; j++) {

				/*
				 * It's possible that some datasets won't have been quantitated
				 * whilst others have. Rather than putting a 0 in for them we
				 * can skip them altogether saving memory and not introducing
				 * false data.
				 * 
				 * In theory this should only be true for datasets as groups
				 * should always be quantitated. We might want to move the skip
				 * code into the dataset insert section.
				 */

                if (sections[j].length() == 0)
                    continue;

                if ((j - positionOffset) >= dataSets.length) {
                    dataGroups[j - (positionOffset + dataSets.length)]
                            .setValueForProbe(p, Float.parseFloat(sections[j]));
                } else {
                    dataSets[j - positionOffset].setValueForProbe(p,
                            Float.parseFloat(sections[j]));
                }
            }
        }
        application.dataCollection().activeProbeListChanged(probeSet);

        // This rename doesn't actually change the name. We put this in because
        // the All Probes group is drawn in the data view before probes have
        // been
        // added to it. This means that it's name isn't updated when the probes
        // have
        // been added and it appears labelled with 0 probes. This doesn't happen
        // if
        // there are any probe lists under all probes as they cause it to be
        // refreshed,
        // but if you only have the probe set then you need this to make the
        // display show
        // the correct information.
        probeSet.setName("All Probes");

    }

    /**
     * Parses the list of dataStores which should initially be visible
     *
     * @param sections The tab split initial line from the visible stores section
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseVisibleStores(String[] sections) throws REDException,
            IOException {
        System.out.println(this.getClass().getName()
                + ":parseVisibleStores(String[] sections)");
        if (sections.length != 2) {
            throw new REDException(
                    "Visible stores line didn't contain 2 sections");
        }
        int n = Integer.parseInt(sections[1]);

		/*
		 * Collect the drawn stores in an array. We used to add them as we found
		 * them but this was inefficient since we had to redo a calculation for
		 * every one we added. This way we only need to calculate once.
		 */

        DataStore[] drawnStores = new DataStore[n];

        for (int i = 0; i < n; i++) {
            String line = br.readLine();
            if (line == null) {
                throw new REDException("Ran out of visible store data at line "
                        + i + " (expected " + n + " stores)");
            }
            String[] storeSections = line.split("\\t");
            if (storeSections.length != 2) {
                throw new REDException(
                        "Expected 2 sections in visible store line but got "
                                + storeSections.length);
            }
            if (storeSections[1].equals("set")) {
                drawnStores[i] = application.dataCollection().getDataSet(
                        Integer.parseInt(storeSections[0]));
            } else if (storeSections[1].equals("group")) {
                drawnStores[i] = application.dataCollection().getDataGroup(
                        Integer.parseInt(storeSections[0]));
            } else if (storeSections[1].equals("replicate")) {
                drawnStores[i] = application.dataCollection().getReplicateSet(
                        Integer.parseInt(storeSections[0]));
            } else {
                throw new REDException("Didn't recognise data type '"
                        + storeSections[1]
                        + "' when adding visible stores from line '" + line
                        + "'");
            }
        }

        application.addToDrawnDataStores(drawnStores);

    }

    /**
     * Parses the set of probe lists.
     *
     * @param sections The tab split initial line from the probe lists section
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseLists(String[] sections) throws REDException, IOException {
        if (sections.length != 2) {
            throw new REDException("Probe Lists line didn't contain 2 sections");
        }

        int n = Integer.parseInt(sections[1]);
        ProbeList[] lists = new ProbeList[n];

        // We also store the probe lists in their appropriate linkage position
        // to recreate the links between probe lists. The worst case scenario
        // is that we have one big chain of linked lists so we make a linkage
        // list which is the same size as the number of probe lists.
        ProbeList[] linkage = new ProbeList[n + 1];

        // The 0 linkage list will always be the full ProbeSet
        linkage[0] = application.dataCollection().probeSet();

        for (int i = 0; i < n; i++) {
            String line = br.readLine();
            if (line == null) {
                throw new REDException("Ran out of probe data at line " + i
                        + " (expected " + n + " probes)");
            }
            String[] listSections = line.split("\\t", -1);
            // The fields should be linkage, name, value name, description
            //

            // Older versions of the format didn't include a description or
            // linkage so
            // we allow for that not being present.
            if (thisDataVersion < 5) {
                lists[i] = new ProbeList(application.dataCollection()
                        .probeSet(), listSections[0], "", listSections[1]);
                if (listSections.length > 2) {
                    lists[i].setDescription(listSections[2]);
                } else {
                    lists[i].setDescription("No description");
                }
            } else {
                lists[i] = new ProbeList(
                        linkage[Integer.parseInt(listSections[0]) - 1],
                        listSections[1], listSections[3], listSections[2]);
                if (listSections.length > 4) {
                    lists[i].setComments(listSections[4].replaceAll("`", "\n"));
                }
                linkage[Integer.parseInt(listSections[0])] = lists[i];
            }
        }

        // Next we reach the probe list data. These comes as a long list of
        // values
        // the first of which is the probe name, then either a numerical value
        // if
        // the probe is contained in that list, or a blank if it isn't.
        String line = br.readLine();
        if (line == null) {
            throw new REDException("Couldn't find probe line for list data");
        }
        sections = line.split("\\t");
        if (sections.length != 2) {
            throw new REDException("Probe line didn't contain 2 sections");
        }
        if (!sections[0].equals("Probes")) {
            throw new REDException(
                    "Couldn't find expected probe lists probe line");
        }

        n = Integer.parseInt(sections[1]);

        for (int i = 0; i < n; i++) {
            sections = br.readLine().split("\\t", -1);
            if (sections.length != lists.length + 1) {
                throw new REDException("Expected " + (lists.length + 1)
                        + " sections in probe list section of data file for "
                        + sections[0] + " but got " + sections.length);
            }

            if (i % 1000 == 0) {
                Enumeration<ProgressListener> e = listeners.elements();
                while (e.hasMoreElements()) {
                    e.nextElement().progressUpdated(
                            "Processed list data for " + i + " probes", i, n);
                }
            }

            Probe p = probes[i];

            if (p == null) {
                continue;
            }

            for (int j = 0; j < lists.length; j++) {
                if (sections[j + 1].length() > 0) {
                    if (sections[j + 1].equals("NaN")) {
                        lists[j].addProbe(p, null);
                    } else {
                        lists[j].addProbe(p,
                                new Float(Float.parseFloat(sections[j + 1])));
                    }
                }
            }
        }
    }

    /**
     * Parses the display preferences.
     *
     * @param sections The tab split initial display preferences line
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseDisplayPreferences(String[] sections)
            throws REDException, IOException {
        System.out.println(this.getClass().getName()
                + ":parseDisplayPreferences(String[] sections)");
        int linesToParse;
        try {
            linesToParse = Integer.parseInt(sections[1]);
        } catch (Exception e) {
            throw new REDException(
                    "Couldn't see the number of display preference lines to parse");
        }

        String[] prefs;
        for (int i = 0; i < linesToParse; i++) {
            prefs = br.readLine().split("\\t");

            if (prefs[0].equals("DataZoom")) {
                DisplayPreferences.getInstance().setMaxDataValue(
                        Double.parseDouble(prefs[1]));
            } else if (prefs[0].equals("ScaleMode")) {
                DisplayPreferences.getInstance().setScaleType(
                        Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("DisplayMode")) {
                DisplayPreferences.getInstance().setDisplayMode(
                        Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("CurrentView")) {
                DisplayPreferences.getInstance().setLocation(prefs[1],
                        Integer.parseInt(prefs[2]), Integer.parseInt(prefs[3]));
            } else if (prefs[0].equals("ReadDensity")) {
                DisplayPreferences.getInstance().setReadDensity(
                        Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("SplitMode")) {
                DisplayPreferences.getInstance().setReadDisplay(
                        Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("QuantitationColour")) {
                DisplayPreferences.getInstance().setColourType(
                        Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("Gradient")) {
                DisplayPreferences.getInstance().setGradient(
                        Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("GraphType")) {
                DisplayPreferences.getInstance().setGraphType(
                        Integer.parseInt(prefs[1]));
            } else {
                throw new REDException(
                        "Didn't know how to process display preference '"
                                + prefs[0] + "'");
            }
        }
    }

    private SequenceRead parsePairedDataSetLine(String chr, String[] reads) throws REDException {
        if (reads.length != 3) {
            throw new REDException("This line is incomplete.");
        }
        SequenceRead sequence = null;
        byte[] readBases = reads[2].getBytes();
//        byte[] qualities = reads[3].getBytes();
        sequence = new SequenceRead(chr, Integer.parseInt(reads[0]),
                Strand.parseStrand(reads[1]), readBases, null);
        return sequence;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.ac.babraham.SeqMonk.DataTypes.ProgressListener#progressCancelled()
     */
    public void progressCancelled() {
        // Shouldn't be relevant here, but should we pass this on to our
        // listeners?
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.ac.babraham.SeqMonk.DataTypes.ProgressListener#progressComplete(java
     * .lang.String, java.lang.Object)
     */
    public void progressComplete(String command, Object result) {
        if (command == null)
            return;
        if (command.equals("load_genome")) {
            application.progressComplete("load_genome", (Genome) result);
            genomeLoaded = true;
        } else if (command.equals("genome_downloaded")) {
            pauseWhilstLoadingGenome = false;
        } else {
            throw new IllegalArgumentException(
                    "Don't know how to handle command '" + command + "'");
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.ac.babraham.SeqMonk.DataTypes.ProgressListener#progressExceptionReceived
     * (java.lang.Exception)
     */
    public void progressExceptionReceived(Exception ex) {
        if (exceptionReceived != null && ex == exceptionReceived)
            return;
        exceptionReceived = ex;
        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().progressExceptionReceived(ex);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.ac.babraham.SeqMonk.DataTypes.ProgressListener#progressUpdated(java
     * .lang.String, int, int)
     */
    public void progressUpdated(String message, int current, int max) {
        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().progressUpdated(message, current, max);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.ac.babraham.SeqMonk.DataTypes.ProgressListener#progressWarningReceived
     * (java.lang.Exception)
     */
    public void progressWarningReceived(Exception e) {
    }

}