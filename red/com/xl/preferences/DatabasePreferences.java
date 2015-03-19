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

package com.xl.preferences;

import com.xl.database.DatabaseListener;
import com.xl.database.DatabaseManager;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Xing Li on 2014/11/15.
 * <p/>
 * The database preferences stores information relative to database, some preferences will be saved in the red_prefs.txt file.
 */
public class DatabasePreferences implements DatabaseListener {
    /**
     * The key of the host.
     */
    public static final String DATABASE_HOST = "Host";
    /**
     * The key of the port.
     */
    public static final String DATABASE_PORT = "Port";
    /**
     * The key of the user.
     */
    public static final String DATABASE_USER = "User";
    /**
     * A silly design... I'll fix this structure soon.
     */
    public static final String DATABASE_TABLE_BUILDER = "TableBuilder";
    /**
     * Singleton pattern.
     */
    private static DatabasePreferences databasePreferences = new DatabasePreferences();
    /**
     * The value of the host.
     */
    private String databaseHost = "";
    /**
     * The value of the port.
     */
    private String databasePort = "";
    /**
     * The value of the user.
     */
    private String databaseUser = "";
    /**
     * The value of the pwd.
     */
    private String databasePasswd = "";
    /**
     * Current using database.
     */
    private String currentDatabase = "";
    /**
     * Current using sample.
     */
    private String currentSample = "";
    /**
     * A silly design... I'll fix this structure soon.
     */
    private String databaseTableBuilder = "";

    private DatabasePreferences() {
        DatabaseManager.getInstance().addDatabaseListener(this);
    }

    public static DatabasePreferences getInstance() {
        return databasePreferences;
    }

    public String getCurrentSample() {
        return currentSample;
    }

    public void setCurrentSample(String currentSample) {
        this.currentSample = currentSample;
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

    public String getDatabasePasswd() {
        return databasePasswd;
    }

    public void setDatabasePasswd(String databasePasswd) {
        this.databasePasswd = databasePasswd;
    }

    public String getCurrentDatabase() {
        return currentDatabase;
    }

    public void setCurrentDatabase(String currentDatabase) {
        this.currentDatabase = currentDatabase;
    }

    /**
     * Save preferences.
     *
     * @throws IOException
     */
    public void savePreferences(Properties properties) throws IOException {
        properties.setProperty(DATABASE_HOST, databaseHost);
        properties.setProperty(DATABASE_PORT, databasePort);
        properties.setProperty(DATABASE_USER, databaseUser);
        properties.setProperty(DATABASE_TABLE_BUILDER, databaseTableBuilder);
    }

    /**
     * Load preferences.
     *
     * @param properties the properties.
     * @throws IOException
     */
    public void loadPreferences(Properties properties) throws IOException {
        setDatabaseHost(properties.getProperty(DATABASE_HOST));
        setDatabasePort(properties.getProperty(DATABASE_PORT));
        setDatabaseUser(properties.getProperty(DATABASE_USER));
    }

    @Override
    public void databaseChanged(String databaseName, String sampleName) {
        setCurrentDatabase(databaseName);
        setCurrentSample(sampleName);
    }

    @Override
    public void databaseConnected() {
    }

}
