package com.xl.filter;

import com.dw.dbutils.DatabaseManager;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.SiteList;
import com.xl.exception.REDException;
import com.xl.interfaces.Cancellable;
import com.xl.interfaces.OptionsListener;
import com.xl.interfaces.ProgressListener;
import com.xl.preferences.REDPreferences;

import javax.swing.*;
import java.util.ArrayList;

/**
 * A class representing a generic filter from which all of the actual filters derive
 */
public abstract class AbstractSiteFilter implements Runnable, Cancellable {

    public final String parentTable;
    protected final DataCollection collection;
    protected final SiteList parentList;
    protected final DatabaseManager databaseManager;
    protected boolean cancel = false;
    protected DataStore[] stores = new DataStore[0];
    private ArrayList<ProgressListener> listeners = new ArrayList<ProgressListener>();
    private ArrayList<OptionsListener> optionsListeners = new ArrayList<OptionsListener>();

    /**
     * Instantiates a new site filter.
     *
     * @param collection The dataCollection
     * @throws REDException if the collection isn't loaded
     */
    public AbstractSiteFilter(DataCollection collection) throws REDException {
        if (!REDPreferences.getInstance().isDataLoadedToDatabase()) {
            throw new REDException("You must importing your data into database before running filters.");
        }
        this.collection = collection;
        parentList = collection.siteSet().getActiveList();
        parentTable = parentList.getTableName();
        databaseManager = DatabaseManager.getInstance();
        if (REDPreferences.getInstance().isDenovo()) {
            databaseManager.useDatabase(DatabaseManager.DENOVO_DATABASE_NAME);
        } else {
            databaseManager.useDatabase(DatabaseManager.NON_DENOVO_DATABASE_NAME);
        }
    }


    public void cancel() {
        cancel = true;
    }

    /**
     * Starts the filter running.  This will start a new thread implemented
     * by the filter and return immediately.  Further progress will only be
     * reported via the listeners.
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

        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Removes a progress listener.
     *
     * @param l The progress listener to remove
     */
    public void removeProgressListener(ProgressListener l) {
        if (l != null && listeners.contains(l)) {
            listeners.remove(l);
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
     * A shortcut method if you're processing one site at a time.  This allows
     * you to call this method with every site and it will put up progress at
     * suitable points and add a suitable message
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
        for (ProgressListener listener : listeners) {
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
        for (ProgressListener listener : listeners) {
            listener.progressCancelled();
        }
    }

    /**
     * Passes on Progress exception received message to all listeners
     *
     * @param e The exception
     */
    protected void progressExceptionReceived(Exception e) {
        for (ProgressListener listener : listeners) {
            listener.progressExceptionReceived(e);
        }
    }

    /**
     * Passes on Filter finished message to all listeners
     *
     * @param newList The newly created site list
     */
    protected void filterFinished(SiteList newList) {
        newList.setName(listName());
        newList.setDescription(listDescription());
        for (ProgressListener listener : listeners) {
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
     * List name. This just needs to be a short reasonable name
     * for the newly created list.
     *
     * @return A suitable name for the newly generated site list.
     */
    protected abstract String listName();

    /**
     * List description.  This should provide a complete but concise summary
     * of all of the options selected when the filter was run.  This description
     * doesn't have to be computer parsable but it should be able to be interpreted
     * by a human.
     *
     * @return A suitable description for the newly generated site list
     */
    protected String listDescription() {
        StringBuilder b = new StringBuilder();

        b.append("Filter on potential RNA editing sites in ");
        b.append(collection.siteSet().getActiveList().name()).append(" ");

        for (int s = 0; s < stores.length; s++) {
            b.append(stores[s].name());
            if (s < stores.length - 1) {
                b.append(" , ");
            }
        }
        return b.toString();
    }

    /**
     * Start the generation of the site list.  This will be called from within
     * a new thread so you don't need to implemet threading within the filter.
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
