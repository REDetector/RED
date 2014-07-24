package com.xl.filter;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.probes.ProbeList;
import com.xl.exception.REDException;
import com.xl.interfaces.Cancellable;
import com.xl.interfaces.OptionsListener;
import com.xl.interfaces.ProgressListener;
import com.xl.preferences.REDPreferences;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A class representing a generic filter from which all
 * of the actual filters derive
 */
public abstract class ProbeFilter implements Runnable, Cancellable {

    protected final DataCollection collection;
    protected final ProbeList startingList;
    private ArrayList<ProgressListener> listeners = new ArrayList<ProgressListener>();
    private ArrayList<OptionsListener> optionsListeners = new ArrayList<OptionsListener>();
    protected boolean cancel = false;

    /**
     * Instantiates a new probe filter.
     *
     * @param collection The dataCollection
     * @throws REDException if the dataCollection isn't quantitated
     */
    public ProbeFilter(DataCollection collection) throws REDException {
        if (!REDPreferences.getInstance().isDataLoadedToDatabase()) {
            throw new REDException("You must importing your data into database before running filters.");
        }
        this.collection = collection;
        startingList = collection.probeSet().getActiveList();
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Dialogs.Cancellable#cancel()
     */
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
     * A shortcut method if you're processing one probe at a time.  This allows
     * you to call this method with every probe and it will put up progress at
     * suitable points and add a suitable message
     *
     * @param current The current number of probes processed
     * @param total   The progress value at completion
     */
    protected void progressUpdated(int current, int total) {
        if (current % ((total / 100) + 1) == 0) {
            progressUpdated("Processed " + current + " out of " + total + " probes", current, total);
        }
    }

    /**
     * Passes on Progress updated messages to all listeners
     *
     * @param message The message to display
     * @param current The current progress value
     * @param total   The progress value at completion
     */
    protected void progressUpdated(String message, int current, int total) {

        Iterator<ProgressListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().progressUpdated(message, current, total);
        }
    }


    /**
     * Passes on Options changed message to all listeners
     */
    protected void optionsChanged() {
        Iterator<OptionsListener> i = optionsListeners.iterator();
        while (i.hasNext()) {
            i.next().optionsChanged();
        }
    }

    /**
     * Passes on Progress cancelled message to all listeners
     */
    protected void progressCancelled() {
        Iterator<ProgressListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().progressCancelled();
        }
    }

    /**
     * Passes on Progress exception received message to all listeners
     *
     * @param e The exception
     */
    protected void progressExceptionReceived(Exception e) {
        Iterator<ProgressListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().progressExceptionReceived(e);
        }
    }

    /**
     * Passes on Filter finished message to all listeners
     *
     * @param newList The newly created probe list
     */
    protected void filterFinished(ProbeList newList) {
        newList.setName(listName());
        newList.setDescription(listDescription());
        Iterator<ProgressListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().progressComplete("new_probe_list", newList);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        generateProbeList();
    }

    /**
     * List name. This just needs to be a short reasonable name
     * for the newly created list.
     *
     * @return A suitable name for the newly generated probe list.
     */
    abstract protected String listName();

    /**
     * List description.  This should provide a complete but concise summary
     * of all of the options selected when the filter was run.  This description
     * doesn't have to be computer parsable but it should be able to be interpreted
     * by a human.
     *
     * @return A suitable description for the newly generated probe list
     */
    abstract protected String listDescription();

    /**
     * Start the generation of the probe list.  This will be called from within
     * a new thread so you don't need to implemet threading within the filter.
     */
    abstract protected void generateProbeList();

    /**
     * Checks if the currently set options allow the filter to be run
     *
     * @return true, if the filter is ready to run.
     */
    abstract public boolean isReady();

    /**
     * Checks if this filter has an options panel.
     *
     * @return true, if the filter has an options panel
     */
    abstract public boolean hasOptionsPanel();

    /**
     * Gets the options panel.
     *
     * @return The options panel
     */
    abstract public JPanel getOptionsPanel();

    /**
     * Name.
     *
     * @return The name of this filter
     */
    abstract public String name();

    /**
     * Description.
     *
     * @return A longer description describing what this filter does.
     */
    abstract public String description();


}
