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

import com.xl.datatypes.DataSet;
import com.xl.datatypes.sequence.Location;
import com.xl.exception.REDException;
import com.xl.interfaces.Cancellable;
import com.xl.interfaces.ProgressListener;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a generic data parser for read data.  Actual data parsers for specific formats will be subclasses of this.
 */
public abstract class DataParser implements Runnable, Cancellable {
    /**
     * The progress listener.
     */
    protected final ArrayList<ProgressListener> listeners;
    /**
     * Cancel flag.
     */
    protected boolean cancel = false;
    /**
     * The file to parse.
     */
    private File file;

    /**
     * Instantiates a new data parser.
     */
    public DataParser() {
        listeners = new ArrayList<ProgressListener>();
    }

    /**
     * Sets a flag which tells the data parser that the user wants to cancel this request.  It's up to the implementing class to notice that this flag has been
     * set.
     */
    public void cancel() {
        cancel = true;
    }

    /**
     * Gets an options panel.
     *
     * @return The options panel
     */
    public abstract JPanel getOptionsPanel();

    /**
     * Checks whether this parser has an options panel
     *
     * @return true, if there is an options panel
     */
    public abstract boolean hasOptionsPanel();

    /**
     * Checks if all options have been set to allow the parser to be run
     *
     * @return true, if the parser is ready to go
     */
    public abstract boolean readyToParse();

    /**
     * A short name for the parser
     *
     * @return A name for the parser
     */
    public abstract String parserName();

    /**
     * A longer description which details what data this parser can read
     *
     * @return A description
     */
    public abstract String getDescription();

    /**
     * Query the reads in the runtime with an efficient way.
     *
     * @param chr   the chromosome
     * @param start the start position
     * @param end   the end position
     * @return a list that contains all reads from the start to the end.
     */
    public abstract List<? extends Location> query(String chr, int start, int end);

    /**
     * Gets the list of files to be parsed
     *
     * @return A list of files to parse
     */
    protected File getFile() {
        return file;
    }

    /**
     * Sets the files which are to be parsed
     *
     * @param file A list of files to parse
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Gets a file filter which will identify all files which could be read by this parser. This is judged solely on the filename so false positives are OK. We
     * should ensure that directories are always allowed when overriding this method.
     *
     * @return A file filter for files which are able to be parsed by this class
     */
    public FileFilter getFileFilter() {
        return new FileFilter() {
            public boolean accept(File pathname) {
                return true;
            }

            public String getDescription() {
                return "All Files";
            }
        };
    }

    @Override
    public String toString() {
        return parserName();
    }

    /**
     * Parses the data.
     *
     * @throws REDException
     */
    public void parseData() throws REDException {

        if (!readyToParse()) {
            throw new REDException("Data Parser is not ready to parse (some options may not have been set)");
        }

        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Adds a progress listener.
     *
     * @param l The listener to add
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
     * @param l The listener to remove
     */
    public void removeProgressListener(ProgressListener l) {
        if (l != null && listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    /**
     * Alerts all listeners to a progress update
     *
     * @param message The message to send
     * @param current The current level of progress
     * @param max     The level of progress at completion
     */
    protected void progressUpdated(String message, int current, int max) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressUpdated(message, current, max);
        }
    }

    /**
     * Alerts all listeners that an exception was received. The parser is not expected to continue after issuing this call.
     *
     * @param e The exception
     */
    protected void progressExceptionReceived(Exception e) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressExceptionReceived(e);
        }
    }

    /**
     * Alerts all listeners that a warning was received.  The parser is expected to continue after issuing this call.
     *
     * @param e The warning exception received
     */
    protected void progressWarningReceived(Exception e) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressWarningReceived(e);
        }
    }

    /**
     * Alerts all listeners that the user cancelled this import.
     */
    protected void progressCancelled() {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressCancelled();
        }
    }

    /**
     * Tells all listeners that the parser has finished parsing the data The list of dataSets should be the same length as the original file list.
     *
     * @param newData An array of completed dataSets.
     */
    protected void processingComplete(DataSet[] newData) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressComplete("datasets_loaded", newData);
        }
    }
}
