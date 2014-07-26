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
import java.util.Properties;

/**
 * A set of redPreferences, both temporary and permanent which are used throughout
 * SeqMonk. Permanent redPreferences can be loaded from and saved to a redPreferences
 * file allowing persistence between sessions.
 */
public class REDPreferences {
    public static final String PROXY = "Proxy";
    public static final String CRASH_EMAIL = "CrashEmail";
    public static final String COMPRESS_OUTPUT = "CompressOutput";
    public static final String CHECK_FOR_UPDATE = "CheckForUpdate";
    public static final String DATA_LOADED_TO_DATABASE = "DataLoaded";
    public static final String DATABASE_HOST = "Host";
    public static final String DATABASE_PORT = "Port";
    public static final String DATABASE_USER = "User";
    public static final String DATABASE_PASSWORD = "Password";
    public static final String DENOVO = "Denovo";

    /**
     * The single instantiated instance of redPreferences
     */
    private static REDPreferences redPreferences = new REDPreferences();

    private LocationPreferences locationPreferences = LocationPreferences.getInstance();

    /**
     * The redPreferences file.
     */
    private File preferencesFile = null;

    /**
     * Whether we've opted to compress our output files
     */
    private boolean compressOutput = false;


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
    private String crashEmail = "";


    private boolean dataLoadedToDatabase = false;


    private String databaseHost = "";
    private String databasePort = "";
    private String databaseUser = "";
    private String databasePassword = "";


    private boolean isDenovo = false;


    /**
     * Instantiates a redPreferences object. Only ever called once from inside this
     * class. External access is via the getInstnace() method.
     */
    private REDPreferences() {
        try {
            preferencesFile = new File(locationPreferences.getProjectSaveLocation() + File.separator + "red_prefs.txt");
            if (preferencesFile.exists()) {
                /** Loading redPreferences from file... */
                loadPreferences();
            } else {
                savePreferences();
            }
            updateProxyInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the single instance of SeqMonkPreferences.
     *
     * @return single instance of SeqMonkPreferences
     */
    public static REDPreferences getInstance() {
        return redPreferences;
    }

    /**
     * Load redPreferences from a saved file
     */
    private void loadPreferences() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(preferencesFile));
        setCrashEmail(properties.getProperty(CRASH_EMAIL));
        setCompressOutput(Boolean.parseBoolean(properties.getProperty(COMPRESS_OUTPUT)));
        setCheckForUpdates(Boolean.parseBoolean(properties.getProperty(CHECK_FOR_UPDATE)));
        setDataLoadedToDatabase(Boolean.parseBoolean(properties.getProperty(DATA_LOADED_TO_DATABASE)));
        setDatabaseHost(properties.getProperty(DATABASE_HOST));
        setDatabasePort(properties.getProperty(DATABASE_PORT));
        setDatabaseUser(properties.getProperty(DATABASE_USER));
        setDatabasePassword(properties.getProperty(DATABASE_PASSWORD));
        setDenovo(Boolean.parseBoolean(properties.getProperty(DENOVO)));
        String[] proxys = properties.getProperty(PROXY).split(",");
        if (proxys.length == 2) {
            setProxy(proxys[0], Integer.parseInt(proxys[1]));
        } else {
            setProxy(proxyHost, proxyPort);
        }
        locationPreferences.loadPreferences(properties);
    }

    /**
     * Save redPreferences.
     *
     * @throws IOException
     */
    public void savePreferences() throws IOException {
        PrintWriter p = new PrintWriter(new FileWriter(preferencesFile));

//        p.println("# RED Preferences file.  Do not edit individually.");
        Properties properties = new Properties();
        properties.setProperty(PROXY, proxyHost + "," + proxyPort);
        properties.setProperty(CRASH_EMAIL, crashEmail);
        properties.setProperty(COMPRESS_OUTPUT, Boolean.toString(compressOutput));
        properties.setProperty(CHECK_FOR_UPDATE, Boolean.toString(checkForUpdates));
        properties.setProperty(DATA_LOADED_TO_DATABASE, Boolean.toString(dataLoadedToDatabase));
        properties.setProperty(DENOVO, Boolean.toString(isDenovo));
        properties.setProperty(DATABASE_HOST, databaseHost);
        properties.setProperty(DATABASE_PORT, databasePort);
        properties.setProperty(DATABASE_USER, databaseUser);
        properties.setProperty(DATABASE_PASSWORD, databasePassword);
        locationPreferences.savePreferences(properties);
        properties.store(p, "RED Preferences. DO NOT Edit This File Individually.");
        p.close();
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
     * Sets the flag to say if we should check for updates
     *
     * @param checkForUpdates Check if there is any updated version.
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

    public boolean isDataLoadedToDatabase() {
        return dataLoadedToDatabase;
    }

    public void setDataLoadedToDatabase(boolean dataLoadedToDatabase) {
        this.dataLoadedToDatabase = dataLoadedToDatabase;
    }

    public String getDatabaseHost() {
        return databaseHost;
    }

    public void setDatabaseHost(String databaseHost) {
        this.databaseHost = databaseHost;
    }

    public String getDatabasePort() {
        return databasePort;
    }

    public void setDatabasePort(String databasePort) {
        this.databasePort = databasePort;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public boolean isDenovo() {
        return isDenovo;
    }

    public void setDenovo(boolean isDenovo) {
        this.isDenovo = isDenovo;
    }
}
