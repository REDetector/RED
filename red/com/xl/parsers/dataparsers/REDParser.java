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

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.feature.Feature;
import com.xl.datatypes.feature.FeatureLocation;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.datatypes.sites.SiteSet;
import com.xl.display.dialog.CrashReporter;
import com.xl.display.dialog.ProgressDialog;
import com.xl.exception.REDException;
import com.xl.interfaces.ProgressListener;
import com.xl.main.Global;
import com.xl.main.REDApplication;
import com.xl.net.genomes.GenomeDownloader;
import com.xl.parsers.annotationparsers.IGVGenomeParser;
import com.xl.preferences.DisplayPreferences;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.ParsingUtils;
import com.xl.utils.Strand;
import com.xl.utils.namemanager.GenomeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * The REDParser reads the main RED file format. It is different to all other data parsers in that it does not extend DataParser since it not only contains
 * data, but can also load genomes and sites and can set visual preferences.
 */
public class REDParser implements Runnable, ProgressListener {
    private static final Logger logger = LoggerFactory.getLogger(REDParser.class);

    /**
     * The RED application.
     */
    private REDApplication application;
    /**
     * The reader.
     */
    private BufferedReader reader;
    /**
     * The listener.
     */
    private Vector<ProgressListener> listeners = new Vector<ProgressListener>();
    /**
     * Whether the genome is loaded.
     */
    private boolean genomeLoaded = false;
    /**
     * The received exception.
     */
    private Exception exceptionReceived = null;

    private boolean pauseWhilstLoadingGenome = true;

    /**
     * Instantiates a new RED parser.
     *
     * @param application The application which we're loading this file into
     */
    public REDParser(REDApplication application) {
        this.application = application;
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

    @Override
    public void run() {

        genomeLoaded = false;
        exceptionReceived = null;

        // Loading this data is done in a modular manner. Every line we read should be a key followed by one or more values (tab delimited). Whatever it is
        // we pass it on to a specialised subroutine to deal with.

        try {
            String line;
            String[] sections;

            while ((line = reader.readLine()) != null) {
                sections = line.split("\\t");

                // Now we look where to send this...
                if (sections[0].equals(ParsingUtils.RED_DATA_VERSION)) {
                    parseDataVersion(sections);
                } else if (sections[0].equals(ParsingUtils.SAMPLES)) {
                    if (!genomeLoaded) {
                        throw new REDException("No genome definition found before data");
                    }
                    parseDataSets(sections);
                } else if (sections[0].equals(ParsingUtils.ANNOTATION)) {
                    if (!genomeLoaded) {
                        throw new REDException("No genome definition found before data");
                    }
                    // Add any annotation sets we've parsed at this point
                    application.dataCollection().genome().getAnnotationCollection().addAnnotationSet(parseAnnotation(sections));
                } else if (sections[0].equals(ParsingUtils.DATA_GROUPS)) {
                    if (!genomeLoaded) {
                        throw new REDException("No genome definition found before data");
                    }
                    parseGroups(sections);
                } else if (sections[0].equals(ParsingUtils.GENOME_INFORMATION_START)) {
                    parseGenome();
                } else if (sections[0].equals(ParsingUtils.VISIBLE_STORES)) {
                    if (!genomeLoaded) {
                        throw new REDException("No genome definition found before data");
                    }
                    parseVisibleStores(sections);
                } else if (sections[0].equals(ParsingUtils.DISPLAY_PREFERENCES)) {
                    if (!genomeLoaded) {
                        throw new REDException("No genome definition found before data");
                    }
                    parseDisplayPreferences(sections);
                } else {
                    throw new REDException("Didn't recognise section '" + sections[0] + "' in red file");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Enumeration<ProgressListener> e = listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().progressExceptionReceived(ex);
            }
            return;
        } finally {
            // We're finished with the file
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    new CrashReporter(e1);
                }
            }
        }

        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            // In this case we put out a dummy empty data set since we've already entered the data into the collection by now
            e.nextElement().progressComplete("project_loaded", null);
        }

    }

    /**
     * Parses the file.
     *
     * @param file The file to parse
     */
    public void parseFile(File file) {

		/*
         * The attempt to open the file as a GZIP input stream can on some systems leave an open file handle to the RED file. We therefore create the lowest
         * file input stream as a separate object so we can be absolutely sure that it's closed before we reopen as a normal input stream.
		 */
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(fis)));
        } catch (IOException ioe) {
            try {
                if (fis != null) {
                    fis.close();
                }
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
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
            throw new REDException("Data Version line didn't contain 2 sections");
        }

        int thisDataVersion = Integer.parseInt(sections[1]);

        if (thisDataVersion > Global.DATA_VERSION) {
            throw new REDException("This data file needs a newer version of RED to read it.");
        }
    }

    /**
     * Parses the genome line.
     *
     * @throws REDException
     */
    private synchronized void parseGenome() throws Exception {
        String line;
        String[] sections;
        while ((line = reader.readLine()) != null) {
            if (line.equals(ParsingUtils.GENOME_INFORMATION_END)) {
                break;
            }
            sections = line.split("=");
            if (sections.length == 2) {
                setProperties(sections[0], sections[1]);
            }
        }
        File f = new File(LocationPreferences.getInstance().getGenomeDirectory() + File.separator + GenomeDescriptor.getInstance().getGenomeId() + ".genome");
        if (!f.exists()) {
            // The user doesn't have this genome yet, so we download it automatically...
            GenomeDownloader d = new GenomeDownloader();
            d.addProgressListener(this);
            ProgressDialog progressDialog = new ProgressDialog(application, "Downloading genome...");
            d.addProgressListener(progressDialog);
            d.downloadGenome(GenomeDescriptor.getInstance().getGenomeId(), true);
            progressDialog.requestFocus();
            while (true) {
                // We need to sleep for each detection because the calculation is too fast to modify the loading genome flag...
                Thread.sleep(50);
                if (exceptionReceived != null) {
                    throw exceptionReceived;
                } else if (!pauseWhilstLoadingGenome) {
                    break;
                }
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

    /**
     * Set properties to the genome descriptor.
     *
     * @param key   the key
     * @param value the value
     */
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
     * Parses an external set of annotations
     *
     * @param sections The tab split initial annotation line
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private AnnotationSet parseAnnotation(String[] sections) throws REDException, IOException {
        if (sections.length != 3) {
            throw new REDException("Annotation line didn't contain 3 sections");
        }

        AnnotationSet set = new AnnotationSet(application.dataCollection().genome(), sections[1]);

        int featureCount = Integer.parseInt(sections[2]);

        for (int i = 0; i < featureCount; i++) {

            if (i % 1000 == 0) {
                progressUpdated("Parsing annotation in " + set.name(), i, featureCount);
            }
            sections = reader.readLine().split("\\t");
            if (sections.length != 4) {
                throw new REDException("Feature line didn't contain 4 sections");
            }
            String name = sections[0];
            String chr = sections[1];
            Strand strand = Strand.parseStrand(sections[2]);
            String aliasName = sections[3];

            String secondLine = reader.readLine();
            sections = secondLine.substring(1, secondLine.length()).split(",");
            List<Location> allLocations = new ArrayList<Location>();
            for (String section : sections) {
                allLocations.add(new FeatureLocation(section));
            }
            Feature feature = new Feature(name, chr, strand, allLocations, aliasName);
            set.addFeature(feature);
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
    private void parseDataSets(String[] sections) throws REDException, IOException {
        if (sections.length != 2) {
            throw new REDException("Samples line didn't contain 2 sections");
        }

        int n = Integer.parseInt(sections[1]);

        // We need to keep the Data Sets around to add data to later.
        DataSet[] dataSets = new DataSet[n];

        for (int i = 0; i < n; i++) {
            sections = reader.readLine().split("\\t");
            if (sections.length != 8) {
                System.err.println("Read line " + i + " didn't contain 8 sections");
            }
            dataSets[i] = new DataSet(sections[0], sections[1]);
            dataSets[i].setDataParser(new BAMFileParser(new File(sections[1])));
            dataSets[i].setStandardChromosomeName(Boolean.parseBoolean(sections[2]));
            dataSets[i].setTotalReadCount(Integer.parseInt(sections[3]));
            dataSets[i].setTotalReadLength(Long.parseLong(sections[4]));
            dataSets[i].setForwardReadCount(Integer.parseInt(sections[5]));
            dataSets[i].setReverseReadCount(Integer.parseInt(sections[6]));
            boolean hasSiteSet = Boolean.parseBoolean(sections[7]);
            if (hasSiteSet) {
                parseSiteSetTree(reader.readLine().split("\\t"), dataSets[i]);
            }
            application.dataCollection().addDataStore(dataSets[i]);
        }
    }

    /**
     * Parses the list of sample groups.
     *
     * @param sections The tab split values from the initial groups line
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseGroups(String[] sections) throws REDException, IOException {
        if (sections.length != 2) {
            throw new REDException("Data Groups line didn't contain 2 sections");
        }
        if (!sections[0].equals("Data Groups")) {
            throw new REDException("Couldn't find expected data groups line");
        }

        int n = Integer.parseInt(sections[1]);

        for (int i = 0; i < n; i++) {
            String[] group = reader.readLine().split("\\t");
            DataSet[] groupMembers = new DataSet[group.length - 1];

            for (int j = 1; j < group.length; j++) {

                // The more modern and safer way to make up a group is by
                // its index
                groupMembers[j - 1] = application.dataCollection().getDataSet(Integer.parseInt(group[j]));
                if (groupMembers[j - 1] == null) {
                    throw new REDException("Couldn't find data set at position " + group[j]);
                }
            }

            DataGroup g = new DataGroup(group[0], groupMembers);
            application.dataCollection().addDataStore(g);
        }
    }

    /**
     * Parses the list of sites.
     *
     * @param sections The tab split initial line from the sites section
     * @throws REDException If the line didn't contain the required sections, then throw this exception.
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseSiteSetTree(String[] sections, DataStore d) throws REDException, IOException {
        if (sections.length < 4) {
            System.err.println("Site line didn't contain at least 3 sections");
        }
        if (!sections[0].equals(ParsingUtils.SITES)) {
            throw new REDException("Couldn't find expected sites line");
        }

        int n = Integer.parseInt(sections[1]);
        String sampleName = sections[2];
        String description;

        if (sections.length > 3) {
            description = sections[3];
        } else {
            description = "No generator description available";
        }

        SiteSet siteSet = new SiteSet(sampleName, description, n);

        if (sections.length > 4) {
            siteSet.setComments(sections[4]);
        }

        // We need to save the site set to the data set at this point so we can add the site lists as we get to them.
        d.setSiteSet(siteSet);
        siteSet.setDataStore(d);

        String line;
        for (int i = 0; i < n; i++) {
            line = reader.readLine();
            if (line == null) {
                throw new REDException("Ran out of site data at line " + i + " (expected " + n + " sites)");
            }
            if (i % 1000 == 0) {
                Enumeration<ProgressListener> e = listeners.elements();
                while (e.hasMoreElements()) {
                    e.nextElement().progressUpdated("Processed data for " + i + " sites", i, n);
                }
            }
            sections = line.split("\\t");
            if (sections.length != 4) {
                throw new REDException("Line " + i + "does not contain four sections. We need chr/position/ref/alt to create the Site.");
            }
            String chr = sections[0];
            if (chr == null) {
                throw new NullPointerException("No chromosome is found.");
            }
            // Chr, Position, Reference, Alternative
            Site p = new Site(chr, Integer.parseInt(sections[1]), sections[2].charAt(0), sections[3].charAt(0));
            siteSet.addSite(p);
        }

        // Next we parse the site lists from this site set.
        sections = reader.readLine().split("\\t");
        if (sections.length != 2) {
            throw new REDException("Site Lists line didn't contain 2 sections");
        }

        n = Integer.parseInt(sections[1]);
        SiteList[] lists = new SiteList[n];

        // We also store the site lists in their appropriate linkage position to recreate the links between site lists. The worst case scenario is that we
        // have one big chain of linked lists so we make a linkage list which is the same size as the number of site lists.
        SiteList[] linkage = new SiteList[n + 1];

        // The 0 linkage list will always be the full SiteSet
        linkage[0] = siteSet;
        for (int i = 0; i < n; i++) {
            line = reader.readLine();
            if (line == null) {
                throw new REDException("Ran out of site data at line " + i + " (expected " + n + " sites)");
            }
            sections = line.split("\\t");
            // The fields should be linkage, name, value name, description
            int siteLength = Integer.parseInt(sections[1]);

            if (sections.length > 4) {
                lists[i] = new SiteList(linkage[Integer.parseInt(sections[0]) - 1], sections[2], sections[3], sections[4]);
            } else {
                lists[i] = new SiteList(linkage[Integer.parseInt(sections[0]) - 1], sections[2], sections[3], "");
            }

            if (sections.length > 5) {
                lists[i].setComments(sections[5]);
            }
            linkage[Integer.parseInt(sections[0])] = lists[i];
            // Next we reach the site list data. These comes as a long list of values the first of which is the site name,
            // then either a numerical value if the site is contained in that list, or a blank if it isn't.
            for (int j = 0; j < siteLength; j++) {
                if (j % 1000 == 0) {
                    Enumeration<ProgressListener> e = listeners.elements();
                    while (e.hasMoreElements()) {
                        e.nextElement().progressUpdated("Processed list data for " + i + " sites", i, n);
                    }
                }
                line = reader.readLine();
                if (line == null) {
                    throw new REDException("Couldn't find site line for list data");
                }
                sections = line.split("\\t");
                if (sections.length != 4) {
                    System.err.println("Line " + i + " is not completed. Line information: " + line);
                }
                String chr = sections[0];
                if (chr == null) {
                    throw new REDException("Couldn't find a chromosome called " + sections[0]);
                }
                Site p = new Site(chr, Integer.parseInt(sections[1]), sections[2].toCharArray()[0], sections[3].toCharArray()[0]);
                lists[i].addSite(p);
            }
        }

    }

    /**
     * Parses the list of dataStores which should initially be visible
     *
     * @param sections The tab split initial line from the visible stores section
     * @throws REDException If the line didn't contain the required sections, then throw this exception.
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseVisibleStores(String[] sections) throws REDException, IOException {
        if (sections.length != 2) {
            throw new REDException("Visible stores line didn't contain 2 sections");
        }
        int n = Integer.parseInt(sections[1]);

		/*
         * Collect the drawn stores in an array. We used to add them as we found them but this was inefficient since we had to redo a calculation for every
         * one we added. This way we only need to calculate once.
		 */

        DataStore[] drawnStores = new DataStore[n];

        for (int i = 0; i < n; i++) {
            String line = reader.readLine();
            if (line == null) {
                throw new REDException("Ran out of visible store data at line " + i + " (expected " + n + " stores)");
            }
            String[] storeSections = line.split("\\t");
            if (storeSections.length != 2) {
                throw new REDException("Expected 2 sections in visible store line but got " + storeSections.length);
            }
            if (storeSections[1].equals("set")) {
                drawnStores[i] = application.dataCollection().getDataSet(Integer.parseInt(storeSections[0]));
            } else if (storeSections[1].equals("group")) {
                drawnStores[i] = application.dataCollection().getDataGroup(Integer.parseInt(storeSections[0]));
            } else {
                throw new REDException("Didn't recognise data type '" + storeSections[1] + "' when adding visible stores from line '" + line + "'");
            }
        }

        application.addToDrawnDataStores(drawnStores);
    }

    /**
     * Parses the display preferences.
     *
     * @param sections The tab split initial display preferences line
     * @throws REDException If the line didn't contain the required sections, then throw this exception.
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseDisplayPreferences(String[] sections) throws REDException, IOException {
        int linesToParse;
        try {
            linesToParse = Integer.parseInt(sections[1]);
        } catch (Exception e) {
            throw new REDException("Couldn't see the number of display preference lines to parse");
        }

        String[] prefs;
        for (int i = 0; i < linesToParse; i++) {
            prefs = reader.readLine().split("\\t");
            if (prefs[0].equals("DisplayMode")) {
                DisplayPreferences.getInstance().setDisplayMode(Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("CurrentView")) {
                DisplayPreferences.getInstance().setLocation(prefs[1], Integer.parseInt(prefs[2]), Integer.parseInt(prefs[3]));
            } else if (prefs[0].equals("Gradient")) {
                DisplayPreferences.getInstance().setGradient(Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("GraphType")) {
                DisplayPreferences.getInstance().setGraphType(Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("Fasta")) {
                DisplayPreferences.getInstance().setFastaEnable(Boolean.parseBoolean(prefs[1]));
            } else {
                throw new REDException("Didn't know how to process display preference '" + prefs[0] + "'");
            }
        }
    }

    @Override
    public void progressExceptionReceived(Exception ex) {
        if (exceptionReceived != null && ex == exceptionReceived)
            return;
        exceptionReceived = ex;
        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().progressExceptionReceived(ex);
        }
    }

    @Override
    public void progressWarningReceived(Exception e) {
    }

    @Override
    public void progressUpdated(String message, int current, int max) {
        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().progressUpdated(message, current, max);
        }
    }

    @Override
    public void progressCancelled() {
        // Shouldn't be relevant here, but should we pass this on to our listeners?
    }

    @Override
    public void progressComplete(String command, Object result) {
        if (command == null)
            return;
        if (command.equals("load_genome")) {
            application.progressComplete("load_genome", result);
            genomeLoaded = true;
        } else if (command.equals("genome_downloaded")) {
            pauseWhilstLoadingGenome = false;
        } else {
            throw new IllegalArgumentException("Don't know how to handle command '" + command + "'");
        }

    }

}
