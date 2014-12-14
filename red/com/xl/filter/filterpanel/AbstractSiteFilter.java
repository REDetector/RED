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

package com.xl.filter.filterpanel;

import com.xl.database.DatabaseManager;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.SiteList;
import com.xl.exception.REDException;
import com.xl.filter.FilterNameRetriever;
import com.xl.interfaces.Cancellable;
import com.xl.interfaces.OptionsListener;
import com.xl.interfaces.ProgressListener;

import javax.swing.*;
import java.util.ArrayList;

/**
 * The Class AbstractSiteFilter represents a generic filter from which all of the actual filters derive
 */
abstract class AbstractSiteFilter implements Runnable, Cancellable {
    /**
     * The data store.
     */
    protected final DataStore dataStore;
    /**
     * The database manager.
     */
    protected final DatabaseManager databaseManager;
    /**
     * The sample name derive from the site set.
     */
    protected final String currentSample;
    /**
     * The parent list selected from the left option panel by user.
     */
    protected SiteList parentList = null;
    /**
     * A flag to cancel the progress.
     */
    protected boolean cancel = false;
    /**
     * The progress listeners.
     */
    private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
    /**
     * The options listeners.
     */
    private ArrayList<OptionsListener> optionsListeners = new ArrayList<OptionsListener>();

    /**
     * Instantiates a new site filter.
     *
     * @param dataStore The dataCollection
     */
    public AbstractSiteFilter(DataStore dataStore) {
        this.dataStore = dataStore;
        databaseManager = DatabaseManager.getInstance();
        currentSample = FilterNameRetriever.getSampleName(dataStore.siteSet().getFilterName());
    }

    public void cancel() {
        cancel = true;
    }

    /**
     * Starts the filter running.  This will start a new thread implemented by the filter and return immediately.  Further progress will only be reported via
     * the listeners.
     *
     * @throws REDException if the filter is not ready to run.
     */
    public void runFilter() throws REDException {
        if (!isReady()) {
            throw new REDException("Filter is not ready to run");
        }

        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Adds a progress listener.
     *
     * @param l The progress listener to add
     */
    public void addProgressListener(ProgressListener l) {
        if (l == null) {
            throw new NullPointerException("ProgressListener can't be null");
        }

        if (!progressListeners.contains(l)) {
            progressListeners.add(l);
        }
    }

    /**
     * Removes a progress listener.
     *
     * @param l The progress listener to remove
     */
    public void removeProgressListener(ProgressListener l) {
        if (l != null && progressListeners.contains(l)) {
            progressListeners.remove(l);
        }
    }

    /**
     * Adds an options listener.
     *
     * @param l The options listener to add
     */
    public void addOptionsListener(OptionsListener l) {
        if (l == null) {
            throw new NullPointerException("OptionsListener can't be null");
        }

        if (!optionsListeners.contains(l)) {
            optionsListeners.add(l);
        }
    }

    /**
     * Removes an options listener.
     *
     * @param l The options listener to remove
     */
    public void removeOptionsListener(OptionsListener l) {
        if (l != null && optionsListeners.contains(l)) {
            optionsListeners.remove(l);
        }
    }


    /**
     * A shortcut method if you're processing one site at a time.  This allows you to call this method with every site and it will put up progress at suitable
     * points and add a suitable message
     *
     * @param current The current number of sites processed
     * @param total   The progress value at completion
     */
    protected void progressUpdated(int current, int total) {
        if (current % ((total / 100) + 1) == 0) {
            progressUpdated("Processed " + current + " out of " + total + " sites", current, total);
        }
    }

    /**
     * Passes on Progress updated messages to all listeners
     *
     * @param message The message to display
     * @param current The current progress value
     * @param total   The progress value at completion
     */
    public void progressUpdated(String message, int current, int total) {
        for (ProgressListener listener : progressListeners) {
            listener.progressUpdated(message, current, total);
        }
    }


    /**
     * Passes on Options changed message to all listeners
     */
    protected void optionsChanged() {
        for (OptionsListener optionsListener : optionsListeners) {
            optionsListener.optionsChanged();
        }
    }

    /**
     * Passes on Progress cancelled message to all listeners
     */
    protected void progressCancelled() {
        for (ProgressListener listener : progressListeners) {
            listener.progressCancelled();
        }
    }

    /**
     * Passes on Progress exception received message to all listeners
     *
     * @param e The exception
     */
    protected void progressExceptionReceived(Exception e) {
        for (ProgressListener listener : progressListeners) {
            listener.progressExceptionReceived(e);
        }
    }

    /**
     * Passes on Filter finished message to all listeners
     *
     * @param newList The newly created site list
     */
    protected void filterFinished(SiteList newList) {
        newList.setListName(listName());
        newList.setDescription(listDescription());
        for (ProgressListener listener : progressListeners) {
            listener.progressComplete("new_site_list", newList);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        generateSiteList();
    }

    /**
     * List name. This just needs to be a short reasonable name for the newly created list.
     *
     * @return A suitable name for the newly generated site list.
     */
    protected abstract String listName();

    /**
     * List description.  This should provide a complete but concise summary of all of the options selected when the filter was run. This description doesn't
     * have to be parsed by computer but it should be able to be interpreted by a human.
     *
     * @return A suitable description for the newly generated site list
     */
    protected String listDescription() {
        return "Filter on potential RNA editing sites in " + parentList.getListName();
    }

    /**
     * Start the generation of the site list.  This will be called from within a new thread so you don't need to implement threading within the filter.
     */
    protected abstract void generateSiteList();

    /**
     * Checks if the currently set options allow the filter to be run
     *
     * @return true, if the filter is ready to run.
     */
    public abstract boolean isReady();

    /**
     * Checks if this filter has an options panel.
     *
     * @return true, if the filter has an options panel
     */
    public abstract boolean hasOptionsPanel();

    /**
     * Gets the options panel.
     *
     * @return The options panel
     */
    public abstract JPanel getOptionsPanel();

    /**
     * Name.
     *
     * @return The name of this filter
     */
    public abstract String name();

    /**
     * Description.
     *
     * @return A longer description describing what this filter does.
     */
    public abstract String description();


}
