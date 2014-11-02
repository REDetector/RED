package com.xl.parsers.dataparsers;

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.feature.Feature;
import com.xl.datatypes.feature.FeatureLocation;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.datatypes.sequence.Alignment;
import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.datatypes.sites.SiteSet;
import com.xl.dialog.CrashReporter;
import com.xl.dialog.ProgressDialog;
import com.xl.exception.REDException;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;
import com.xl.net.genomes.GenomeDownloader;
import com.xl.parsers.annotationparsers.IGVGenomeParser;
import com.xl.preferences.DisplayPreferences;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.ParsingUtils;
import com.xl.utils.Strand;
import com.xl.utils.namemanager.GenomeUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * The REDParser reads the main RED file format. It is different to all
 * other data parsers in that it does not extend DataParser since it not only
 * contains data, but can also load genomes and sites and can set visual
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
    private BufferedReader reader;
    private Vector<ProgressListener> listeners = new Vector<ProgressListener>();
    private DataSet[] dataSets;
    private DataGroup[] dataGroups;
    private boolean genomeLoaded = false;
    private Exception exceptionReceived = null;
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

            while ((line = reader.readLine()) != null) {
                sections = line.split("\\t");

                // Now we look where to send this...
                if (sections[0].equals(ParsingUtils.RED_DATA_VERSION)) {
                    parseDataVersion(sections);
                } else if (sections[0].equals(ParsingUtils.SAMPLES)) {
                    if (!genomeLoaded) {
                        throw new REDException("No genome definition found before data");
                    }
                    parseSamples(sections);
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
                    try {
                        parseGroups(sections);
                    } catch (REDException ex) {
                        if (ex.getMessage().contains("ambiguous")) {
                            Enumeration<ProgressListener> e = listeners.elements();
                            while (e.hasMoreElements()) {
                                e.nextElement().progressWarningReceived(ex);
                            }
                        } else {
                            throw ex;
                        }
                    }
                } else if (sections[0].equals(ParsingUtils.SITES)) {
                    if (!genomeLoaded) {
                        throw new REDException("No genome definition found before data");
                    }
                    parseSites(sections);
                } else if (sections[0].equals(ParsingUtils.LISTS)) {
                    if (!genomeLoaded) {
                        throw new REDException("No genome definition found before data");
                    }
                    parseLists(sections);
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

            // We're finished with the file
            if (reader != null) {
                reader.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Enumeration<ProgressListener> e = listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().progressExceptionReceived(ex);
            }
            try {
                reader.close();
            } catch (IOException e1) {
                new CrashReporter(e1);
            }
            return;
        }

        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            // In this case we put out a dummy empty dataset since
            // we've already entered the data into the collection by now
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
         * The attempt to open the file as a GZIP input stream can on some
		 * systems leave an open filehandle to the red file. We therefore create
		 * the lowest fileinputstream as a separate object so we can be
		 * absolutely sure that it's closed before we reopen as a normal input
		 * stream.
		 */
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(
                    fis)));
        } catch (IOException ioe) {

            try {
                if (fis != null) {
                    fis.close();
                }
                reader = new BufferedReader(new FileReader(file));
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
        while ((line = reader.readLine()) != null) {
            if (line.equals(ParsingUtils.GENOME_INFORMATION_END)) {
                break;
            }
            sections = line.split("=");
            if (sections.length == 2) {
                setProperties(sections[0], sections[1]);
            }
        }
        File f;
        f = new File(LocationPreferences.getInstance().getGenomeDirectory()
                + File.separator + GenomeDescriptor.getInstance().getGenomeId() + ".genome");
        System.out.println(this.getClass().getName() + ":" + f.getAbsolutePath());
        if (!f.exists()) {
            // The user doesn't have this genome - yet...
            GenomeDownloader d = new GenomeDownloader();
            d.addProgressListener(this);
            ProgressDialog progressDialog = new ProgressDialog(application, "Downloading genome...");
            d.addProgressListener(progressDialog);
            d.downloadGenome(GenomeDescriptor.getInstance().getGenomeId(), GenomeDescriptor.getInstance().getDisplayName(), true);
            progressDialog.requestFocus();
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
     * Parses an external set of annotations
     *
     * @param sections The tab split initial annotation line
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private AnnotationSet parseAnnotation(String[] sections) throws REDException, IOException {
        System.out.println(this.getClass().getName() + ":parseAnnotation()");
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
    private void parseSamples(String[] sections) throws REDException, IOException {
        System.out.println(this.getClass().getName() + ":parseSamples()");
        if (sections.length != 2) {
            throw new REDException("Samples line didn't contain 2 sections");
        }

        int n = Integer.parseInt(sections[1]);

        // We need to keep the Data Sets around to add data to later.
        dataSets = new DataSet[n];

        for (int i = 0; i < n; i++) {
            sections = reader.readLine().split("\\t");
            if (sections.length != 3) {
                throw new REDException("Read line " + i + " didn't contain 3 sections");
            }
            dataSets[i] = new DataSet(sections[0], sections[1]);
            dataSets[i].setDataParser(new BAMFileParser(new File(sections[1])));
            dataSets[i].setStandardChromosomeName(Boolean.parseBoolean(sections[2]));
            sections = reader.readLine().split("\\t");
            if (sections.length != 4) {
                throw new REDException("Read line " + i + " didn't contain 4 sections");
            }
            dataSets[i].setTotalReadCount(Integer.parseInt(sections[0]));
            dataSets[i].setTotalReadLength(Long.parseLong(sections[1]));
            dataSets[i].setForwardReadCount(Integer.parseInt(sections[2]));
            dataSets[i].setReverseReadCount(Integer.parseInt(sections[3]));
            application.dataCollection().addDataSet(dataSets[i]);
            System.out.println(this.getClass().getName() + ":application.dataCollection().addDataSet(dataSets[i]);");
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
        System.out.println(REDParser.class.getName() + ":parseGroups(String[] sections)");
        if (sections.length != 2) {
            throw new REDException("Data Groups line didn't contain 2 sections");
        }
        if (!sections[0].equals("Data Groups")) {
            throw new REDException("Couldn't find expected data groups line");
        }

        int n = Integer.parseInt(sections[1]);

        dataGroups = new DataGroup[n];

        for (int i = 0; i < n; i++) {
            String[] group = reader.readLine().split("\\t");
            DataSet[] groupMembers = new DataSet[group.length - 1];

            if (thisDataVersion < 4) {
                DataSet[] allSets = application.dataCollection().getAllDataSets();
                // In the bad old days we used to refer to datasets by their
                // name
                for (int j = 1; j < group.length; j++) {

                    boolean seen = false;
                    for (int d = 0; d < allSets.length; d++) {
                        // System.out.println("Comparing "+group[j]+" to "+allSets[d]+" at index "+d);

                        if (allSets[d].name().equals(group[j])) {
                            if (seen) {
                                // System.out.println("Seen this before - abort abort");
                                throw new REDException("Name " + group[j] + " is ambiguous in group " + group[0]);
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
                    groupMembers[j - 1] = application.dataCollection().getDataSet(Integer.parseInt(group[j]));
                    if (groupMembers[j - 1] == null) {
                        throw new REDException("Couldn't find dataset at position " + group[j]);
                    }
                }
            }

            DataGroup g = new DataGroup(group[0], groupMembers);
            application.dataCollection().addDataGroup(g);
            dataGroups[i] = g;
        }
    }

    /**
     * Parses the list of sites.
     *
     * @param sections The tab split initial line from the sites section
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseSites(String[] sections) throws REDException,
            IOException {
        System.out.println(REDParser.class.getName() + ":parseSites()");
        if (sections.length < 3) {
            throw new REDException("Site line didn't contain at least 3 sections");
        }
        if (!sections[0].equals(ParsingUtils.SITES)) {
            throw new REDException("Couldn't find expected sites line");
        }

        int n = Integer.parseInt(sections[1]);

        String tableName = sections[2];

        String description;

        if (sections.length > 3) {
            description = sections[3];
        } else {
            description = "No generator description available";
        }

        SiteSet siteSet = new SiteSet(description, n, tableName);

        if (sections.length > 4) {
            siteSet.setComments(sections[4].replaceAll("`", "\n"));
        }

        // We need to save the siteset to the dataset at this point so we can
        // add the site lists as we get to them.
        application.dataCollection().setSiteSet(siteSet);

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
                throw new REDException("Line " + i + "does not contain three sections. We need chr/position/editing " +
                        "base to create the Site now.");
            }
            String chr = sections[0];
            if (chr == null) {
                throw new REDException("Couldn't find a chromosome called " + sections[1]);
            }
            // Chr, Position, Reference, Alternative
            Site p = new Site(chr, Integer.parseInt(sections[1]), sections[2].charAt(0), sections[3].charAt(0));
            siteSet.addSite(p);
        }
        application.dataCollection().activeSiteListChanged(siteSet);

        // This rename doesn't actually change the name. We put this in because the All Sites group is drawn in the
        // data view before sites have been added to it. This means that it's name isn't updated when the sites
        // have been added and it appears labelled with 0 sites. This doesn't happen if there are any site lists
        // under all sites as they cause it to be refreshed, but if you only have the site set then you need this
        // to make the display show the correct information.
        siteSet.setName("All Sites");

    }

    /**
     * Parses the list of dataStores which should initially be visible
     *
     * @param sections The tab split initial line from the visible stores section
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseVisibleStores(String[] sections) throws REDException, IOException {
        System.out.println(this.getClass().getName() + ":parseVisibleStores(String[] sections)");
        if (sections.length != 2) {
            throw new REDException("Visible stores line didn't contain 2 sections");
        }
        int n = Integer.parseInt(sections[1]);

		/*
         * Collect the drawn stores in an array. We used to add them as we found
		 * them but this was inefficient since we had to redo a calculation for
		 * every one we added. This way we only need to calculate once.
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
     * Parses the set of site lists.
     *
     * @param sections The tab split initial line from the site lists section
     * @throws REDException
     * @throws IOException  Signals that an I/O exception has occurred.
     */
    private void parseLists(String[] sections) throws REDException, IOException {
        System.out.println(REDParser.class.getName() + ":parseLists()");
        if (sections.length != 2) {
            throw new REDException("Site Lists line didn't contain 2 sections");
        }

        int n = Integer.parseInt(sections[1]);
        SiteList[] lists = new SiteList[n];

        // We also store the site lists in their appropriate linkage position
        // to recreate the links between site lists. The worst case scenario
        // is that we have one big chain of linked lists so we make a linkage
        // list which is the same size as the number of site lists.
        SiteList[] linkage = new SiteList[n + 1];

        // The 0 linkage list will always be the full SiteSet
        linkage[0] = application.dataCollection().siteSet();
        for (int i = 0; i < n; i++) {
            String line = reader.readLine();
            if (line == null) {
                throw new REDException("Ran out of site data at line " + i + " (expected " + n + " sites)");
            }
            String[] listSections = line.split("\\t");
            // The fields should be linkage, name, value name, description

            lists[i] = new SiteList(linkage[Integer.parseInt(listSections[0]) - 1],
                    listSections[1], listSections[2], listSections[3]);
            int currentListSiteLength = Integer.parseInt(listSections[4]);
            if (listSections.length > 5) {
                lists[i].setComments(listSections[5].replaceAll("`", "\n"));
            }
            linkage[Integer.parseInt(listSections[0])] = lists[i];
            // Next we reach the site list data. These comes as a long list of values the first of which is the site
            // name, then either a numerical value if the site is contained in that list, or a blank if it isn't.
            for (int j = 0; j < currentListSiteLength; j++) {
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
                    throw new REDException("Line " + i + "does not contain three sections. We need chr/position/editing " +
                            "base to create the new Site.");
                }
                String chr = sections[0];
                if (chr == null) {
                    throw new REDException("Couldn't find a chromosome called "
                            + sections[0]);
                }
                Site p = new Site(chr, Integer.parseInt(sections[1]), sections[2].toCharArray()[0], sections[3].toCharArray()[0]);
                lists[i].addSite(p);
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
            prefs = reader.readLine().split("\\t");
            if (prefs[0].equals("DisplayMode")) {
                DisplayPreferences.getInstance().setDisplayMode(
                        Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("CurrentView")) {
                DisplayPreferences.getInstance().setLocation(prefs[1],
                        Integer.parseInt(prefs[2]), Integer.parseInt(prefs[3]));
            } else if (prefs[0].equals("Gradient")) {
                DisplayPreferences.getInstance().setGradient(
                        Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("GraphType")) {
                DisplayPreferences.getInstance().setGraphType(
                        Integer.parseInt(prefs[1]));
            } else if (prefs[0].equals("Fasta")) {
                DisplayPreferences.getInstance().setFastaEnable(
                        Boolean.parseBoolean(prefs[1]));
            } else {
                throw new REDException(
                        "Didn't know how to process display preference '"
                                + prefs[0] + "'");
            }
        }
    }

    private Alignment parsePairedDataSetLine(String chr, String[] reads) throws REDException {
        if (reads.length != 3) {
            throw new REDException("This line is incomplete.");
        }
        Alignment sequence;
        int start = Integer.parseInt(reads[0]);
        int end = Integer.parseInt(reads[1]);
        Strand strand = Strand.parseStrand(reads[2]);
        sequence = new Alignment(chr, start, end, strand);
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
            application.progressComplete("load_genome", result);
            genomeLoaded = true;
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
