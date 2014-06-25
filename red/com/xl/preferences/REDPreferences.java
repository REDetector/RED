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
package com.xl.preferences;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A set of preferences, both temporary and permanent which are used throughout
 * SeqMonk. Permanent preferences can be loaded from and saved to a preferences
 * file allowing persistence between sessions.
 */
public class REDPreferences {

    /**
     * The single instantiated instance of preferences
     */
    private static REDPreferences preferences = new REDPreferences();

    /**
     * The directory under which to look for genome files
     */
    private File genomeBase = null;

    /**
     * The default data location.
     */
    private File dataLocation = new File(System.getProperty("user.home"));

    /**
     * The last used data location.
     */
    private File lastUsedDataLocation = null;

    /**
     * The default save location.
     */
    private File saveLocation = new File(System.getProperty("user.home"));

    /**
     * The last used save location.
     */
    private File lastUsedSaveLocation = null;

    /**
     * The preferences file.
     */
    private File preferencesFile = null;

    /**
     * The directory in which to save temporary cache files
     */
    private File tempDirectory = null;

    /**
     * Whether we've opted to compress our output files
     */
    private boolean compressOutput = false;

    /**
     * The network address from where we can download new genomes
     */
//	private String genomeDownloadLocation = "http://www.bioinformatics.babraham.ac.uk/seqmonk/genomes/";
    private String genomeDownloadLists = "http://igv.broadinstitute.org/genomes/";

    private String genomeDownloadLocation = null;
    /**
     * Whether we're using a network proxy
     */
    private boolean useProxy = false;

    /**
     * The proxy host.
     */
    private String proxyHost = "";

    /**
     * The proxy port.
     */
    private int proxyPort = 0;

    /**
     * Whether we should check for updates every time we're launched.
     */
    private boolean checkForUpdates = true;

    /**
     * The email address we should attach to crash reports
     */
    private String crashEmail = null;

    /**
     * The recently opened files list
     */
    private LinkedList<String> recentlyOpenedFiles = new LinkedList<String>();

    /**
     * The project root directory
     */
    private String projectRootDirectory = null;

    /**
     * Instantiates a preferences object. Only ever called once from inside this
     * class. External access is via the getInstnace() method.
     */
    private REDPreferences() {
        try {
            projectRootDirectory = new File("").getCanonicalPath();
            preferencesFile = new File(projectRootDirectory
                    + File.separator + "red_prefs.txt");
            if (preferencesFile.exists()) {
                /** Loading preferences from file... */
                loadPreferences();
//			System.out.println("loadPreferences();");
            } else {
                savePreferences();
            }
            updateProxyInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load preferences from a saved file
     */
    private void loadPreferences() {

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(preferencesFile));

            String line;
            String[] sections;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#"))
                    continue; // It's a comment
                sections = line.split("\\t", -1);

                if (sections[0].equals("GenomeBase")) {

					/*
                     * We used to save the genome base even if it was the same
					 * as the default genome directory in the install. To make
					 * life easier if the install location moves we now only
					 * store this if the location is something other than the
					 * default.
					 */
                    if (sections.length == 1) {
                        genomeBase = getGenomeBase();
                    } else {
                        File customBase = new File(sections[1]);
                        genomeBase = customBase;
                    }
                } else if (sections[0].equals("DataLocation")) {
                    dataLocation = new File(sections[1]);
                } else if (sections[0].equals("SaveLocation")) {
                    saveLocation = new File(sections[1]);
                } else if (sections[0].equals("TempDirectory")) {
                    tempDirectory = new File(sections[1]);
                } else if (sections[0].equals("CompressOutput")) {
                    compressOutput = sections[1].equals("1");
                } else if (sections[0].equals("CrashEmail")) {
                    crashEmail = sections[1];
                } else if (sections[0].equals("GenomeDownloadLocation")) {
                    genomeDownloadLists = sections[1];
                } else if (sections[0].equals("Proxy")) {
                    proxyHost = sections[1];
                    if (sections.length > 2 && sections[2].length() > 0) {
                        proxyPort = Integer.parseInt(sections[2]);
                    }
                    if (proxyHost.length() > 0) {
                        useProxy = true;
                    }
                } else if (sections[0].equals("RecentFile")) {
                    File f = new File(sections[1]);
                    if (f.exists()) {
                        recentlyOpenedFiles.add(sections[1]);
                    }
                } else if (sections[0].equals("CheckForUpdates")) {
                    if (sections[1].equals("0")) {
                        checkForUpdates = false;
                    } else {
                        checkForUpdates = true;
                    }
                } else {
                    System.err.println("Unknown preference '" + sections[0]
                            + "'");
                }

            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Save preferences.
     *
     * @throws IOException
     */
    public void savePreferences() throws IOException {
        PrintWriter p = new PrintWriter(new FileWriter(preferencesFile));

        p.println("# RED Preferences file.  Do not edit individually.");

        // First write out the GenomeBase if they've specified a custom location
        p.print("GenomeBase\t");
        if (genomeBase != null) {
            p.println(genomeBase.getAbsolutePath());
        } else {
            p.println();
        }

        // Then the dataLocation
        p.println("DataLocation\t" + dataLocation.getAbsolutePath());

        // Then the saveLocation
        p.println("SaveLocation\t" + saveLocation.getAbsolutePath());

        // Then the proxy information
        p.println("Proxy\t" + proxyHost + "\t" + proxyPort);

        // The genome download URL
        p.println("GenomeDownloadLocation\t" + genomeDownloadLists);

        // The temp directory
        if (tempDirectory != null) {
            p.println("TempDirectory\t" + tempDirectory.getAbsolutePath());
        }

        // The crash email
        if (crashEmail != null) {
            p.println("CrashEmail\t" + crashEmail);
        }

        // Whether to compress our output
        if (compressOutput) {
            p.println("CompressOutput\t1");
        } else {
            p.println("CompressOutput\t0");
        }

        // Whether we want to check for updates
        if (checkForUpdates) {
            p.println("CheckForUpdates\t1");
        } else {
            p.println("CheckForUpdates\t0");
        }

        // Save the recently opened file list
        Iterator<String> rof = recentlyOpenedFiles.iterator();
        while (rof.hasNext()) {
            p.println("RecentFile\t" + rof.next());
        }

        p.close();

    }

    /**
     * Gets the single instance of SeqMonkPreferences.
     *
     * @return single instance of SeqMonkPreferences
     */
    public static REDPreferences getInstance() {
        return preferences;
    }

    /**
     * Gets the list of recently opened files.
     *
     * @return the recently opened files
     */
    public String[] getRecentlyOpenedFiles() {
        return recentlyOpenedFiles.toArray(new String[0]);
    }

    /**
     * Adds a path to the recently opened files list. We store up to 5 recently
     * used files on a rotating basis. Adding a new one pushes out the oldest
     * one.
     *
     * @param filePath The new file location to add
     */
    public void addRecentlyOpenedFile(String filePath) {
        // I know this is inefficient in a linked list but
        // it's only going to contain 5 elements so who cares
        if (recentlyOpenedFiles.contains(filePath)) {
            recentlyOpenedFiles.remove(filePath);
        }
        recentlyOpenedFiles.add(0, filePath);

        // Only keep 9 items
        while (recentlyOpenedFiles.size() > 9) {
            recentlyOpenedFiles.remove(9);
        }
        try {
            savePreferences();
        } catch (IOException e) {
            // In this case we don't report this error since
            // the user isn't explicitly asking us to save.
            e.printStackTrace();
        }
    }

    /**
     * Asks whether we should check for updated versions of SeqMonk
     *
     * @return true, if we should check for updates
     */
    public boolean checkForUpdates() {
        return checkForUpdates;
    }

    /**
     * Should seqmonk format output files be gzip compressed
     *
     * @return true if output should be compressed
     */
    public boolean compressOutput() {
        return compressOutput;
    }

    /**
     * Sets whether seqmonk output should be compressed
     *
     * @param compressOutput if output should be compressed
     */
    public void setCompressOutput(boolean compressOutput) {
        this.compressOutput = compressOutput;
    }

    /**
     * The location of the directory to use to cache data
     *
     * @return A file representing the temp directory. Null if none is set.
     */
    public File tempDirectory() {
        return tempDirectory;
    }

    /**
     * Sets the flag to say if we should check for updates
     *
     * @param checkForUpdates
     */
    public void setCheckForUpdates(boolean checkForUpdates) {
        this.checkForUpdates = checkForUpdates;
    }

    /**
     * Flag to say if network access should go through a proxy
     *
     * @return true, if a proxy should be used
     */
    public boolean useProxy() {
        return useProxy;
    }

    /**
     * Proxy host.
     *
     * @return The name of the proxy to use. Only use this if the useProxy flag
     * is set.
     */
    public String proxyHost() {
        return proxyHost;
    }

    /**
     * Proxy port.
     *
     * @return The port to access the proxy on. Only use this if the useProxy
     * flag is set
     */
    public int proxyPort() {
        return proxyPort;
    }

    /**
     * Sets proxy information
     *
     * @param host The name of the proxy
     * @param port The port to access the proxy on
     */
    public void setProxy(String host, int port) {
        proxyHost = host;
        proxyPort = port;
        updateProxyInfo();
    }

    /**
     * Sets the temp directory.
     *
     * @param f The new temp directory
     */
    public void setTempDirectory(File f) {
        tempDirectory = f;
    }

    /**
     * Custom genome base used.
     *
     * @return true, a non-default genome folder has been selected
     */
    public boolean customGenomeBaseUsed() {
        /**
         * Says whether the user has specified a genome base location of if
         * we're using the default one.
         */

        // genomeBase will be null if we're using the default

        return genomeBase != null;
    }

    /**
     * Gets the genome base.
     *
     * @return The folder under which genomes are stored
     * @throws Exception
     */
    public File getGenomeBase() throws FileNotFoundException {

		/*
         * This method returns a file which represents the directory under which
		 * the genomes are stored. If a custom location has not been specified
		 * then the default Genomes folder in the install dir is returned. If
		 * that can't be found then a FileNotFound exception is thrown
		 * 
		 * If a custom location has been set then this is returned regardless of
		 * it it exists or can be used.
		 */

        File f = null;

        if (genomeBase == null) {
            // Check for the default genomes folder. This should always be
            // present, but you can't be too careful!
            try {
                // System.out.println(ClassLoader.getSystemResource("REDPreferences").toString());
                f = new File(projectRootDirectory + File.separator + "Genomes");
                if (!f.exists()) {
                    // throw new FileNotFoundException(
                    // "Couldn't find default Genomes folder");
                    if (f.mkdirs()) {
                        System.err.println("Couldn't find default Genomes folder, We have created your Genomes folder on your project directory.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            f = genomeBase;
        }

        return f;
    }

    /**
     * Gets the genome download location.
     *
     * @return The URL under which new genomes can be downloaded
     */
    public String getGenomeDownloadLists() {
        return genomeDownloadLists;
    }

    public String getGenomeDownloadLocation() {
        return genomeDownloadLocation;
    }

    /**
     * Sets the genome download location.
     *
     * @param url The URL under which new genomes can be downloaded
     */
    public void setGenomeDownloadLocation(String url) {
        this.genomeDownloadLocation = url;
    }

    /**
     * Sets the genome base location
     *
     * @param f The folder under which new genomes should be stored
     */
    public void setGenomeBase(File f) {
        // If this file is the same as the default then
        // we leave the default in place
        try {
            if (!f.equals(getGenomeBase())) {
                genomeBase = f;
            }
        } catch (FileNotFoundException e) {
            genomeBase = f;
        }
    }

    /**
     * Gets the default data location. This will initially be the data location
     * saved in the preferneces file, but will be updated during use with the
     * last actual location where data was imported. If you definitely want the
     * location stored in the preferences file then use
     * getDataLocationPreference()
     *
     * @return The default location to look for new data
     */
    public File getDataLocation() {
        if (lastUsedDataLocation != null)
            return lastUsedDataLocation;
        return dataLocation;
    }

    /**
     * Gets the data location saved in the preferences file. This value is not
     * updated during use except by explicity changing the saved preference. To
     * get the last used folder use getDataLocation().
     *
     * @return The default data location
     */
    public File getDataLocationPreference() {
        return dataLocation;
    }

    /**
     * Sets the default data location which will be saved in the preferences
     * file.
     *
     * @param f The new data location
     */
    public void setDataLocation(File f) {
        dataLocation = f;
    }

    /**
     * Sets the last used data location. This value is only stored until the
     * program exits, and won't be saved in the preferences file.
     *
     * @param f The new last used data location
     */
    public void setLastUsedDataLocation(File f) {
        if (f.isDirectory()) {
            lastUsedDataLocation = f;
        } else {
            lastUsedDataLocation = f.getParentFile();
        }
    }

    /**
     * Gets the default save location for projects / images / reports etc. This
     * will initially be the location in the preferences file but will be
     * updated during use to reflect the last actually used location.
     *
     * @return The default save location.
     */
    public File getSaveLocation() {
        if (lastUsedSaveLocation != null)
            return lastUsedSaveLocation;
        return saveLocation;
    }

    /**
     * Gets the default save location from the preferences file. This value will
     * always match the preferences file and will not update to reflect actual
     * usage within the current session.
     *
     * @return The default save location
     */
    public File getSaveLocationPreference() {
        /**
         * Always returns the save location saved in the preferences file. Used
         * by the preferences editing dialog. Everywhere else should use
         * getSaveLocation()
         */
        return saveLocation;
    }

    /**
     * Sets the save location to record in the preferences file
     *
     * @param f The new save location
     */
    public void setSaveLocation(File f) {
        saveLocation = f;
    }

    /**
     * Sets the last used save location. This is a temporary setting and will
     * not be recorded in the preferences file.
     *
     * @param f The new last used save location
     */
    public void setLastUsedSaveLocation(File f) {
        if (f.isDirectory()) {
            lastUsedSaveLocation = f;
        } else {
            lastUsedSaveLocation = f.getParentFile();
        }
    }

    /**
     * Gets the stored email address which should be attached to crash reports.
     *
     * @return The stored email address, or an empty string
     */
    public String getCrashEmail() {
        if (crashEmail != null)
            return crashEmail;
        return "";
    }

    /**
     * Stores the email address used in a crash report so that this is
     * automatically added the next time a crash report happens and they don't
     * have to fill it out each time.
     *
     * @param email The email address to store
     */
    public void setCrashEmail(String email) {
        // We're not even going to try to validate this
        crashEmail = email;
    }

    /**
     * Applies the stored proxy information to the environment of the current
     * session so it is picked up automatically by any network calls made within
     * the program. No further configuration is required within classes
     * requiring network access.
     */
    private void updateProxyInfo() {
        if (useProxy) {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", proxyHost);
            System.getProperties().put("proxyPort", "" + proxyPort);
        } else {
            System.getProperties().put("proxySet", "false");
        }
    }

}
