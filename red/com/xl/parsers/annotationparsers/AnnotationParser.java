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

package com.xl.parsers.annotationparsers;

import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.genome.Genome;
import com.xl.interfaces.Cancellable;
import com.xl.interfaces.ProgressListener;
import com.xl.utils.filefilters.FileFilterExt;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The Class AnnotationParser provides the core methods which must be implemented by a class wanting to be able to import features into a RED genome.
 */
public abstract class AnnotationParser implements Cancellable, Runnable {

    /**
     * Cancel flag.
     */
    protected boolean cancel = false;
    /**
     * The genome.
     */
    protected Genome genome = null;
    /**
     * The listeners.
     */
    private Vector<ProgressListener> listeners = new Vector<ProgressListener>();
    /**
     * The file.
     */
    private File file = null;

    /**
     * Instantiates a new annotation parser.
     *
     * @param genome the genome
     */
    public AnnotationParser(Genome genome) {
        this.genome = genome;
    }

    /**
     * File filter.
     *
     * @return the file filter
     */
    public abstract FileFilterExt fileFilter();

    /**
     * Parses the annotation.
     *
     * @param file the file
     * @return the annotation set
     * @throws Exception the exception
     */
    protected abstract AnnotationSet parseAnnotation(File file) throws Exception;

    /**
     * Name.
     *
     * @return the string
     */
    public abstract String name();

    /**
     * Genome.
     *
     * @return the genome
     */
    protected Genome genome() {
        return genome;
    }

    /**
     * Adds the progress listener.
     *
     * @param l the l
     */
    public void addProgressListener(ProgressListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Removes the progress listener.
     *
     * @param l the l
     */
    public void removeProgressListener(ProgressListener l) {
        if (l != null && listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    public void cancel() {
        cancel = true;
    }

    /**
     * Parses the file.
     *
     * @param file the files
     */
    public void parseFile(File file) {
        this.file = file;
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        try {
            AnnotationSet set = parseAnnotation(file);
            if (set == null) {
                // They cancelled or had an error which will be reported through the other methods here
                return;
            }
            // Here we have to add the new sets to the annotation collection before we say that we're finished otherwise this object can get destroyed
            // before the program gets chance to execute the operation which adds the sets to the annotation collection.
            genome.getAnnotationCollection().addAnnotationSet(set);
            progressComplete("annotation_loaded", set);
        } catch (Exception e) {
            e.printStackTrace();
            progressExceptionReceived(e);
        }
    }

	/*
     * These are the methods we use to communicate with out listeners. Some of these can be accessed by the implementing class directly but the big ones
	 * need to go back through this class.
	 */
    /**
     * Progress exception received.
     *
     * @param e the e
     */
    private void progressExceptionReceived(Exception e) {
        Enumeration<ProgressListener> en = listeners.elements();
        while (en.hasMoreElements()) {
            en.nextElement().progressExceptionReceived(e);
        }
    }

    /**
     * Progress warning received.
     *
     * @param e the e
     */
    protected void progressWarningReceived(Exception e) {
        Enumeration<ProgressListener> en = listeners.elements();
        while (en.hasMoreElements()) {
            en.nextElement().progressWarningReceived(e);
        }
    }

    /**
     * Progress updated.
     *
     * @param message the message
     * @param current the current
     * @param max     the max
     */
    protected void progressUpdated(String message, int current, int max) {
        Enumeration<ProgressListener> en = listeners.elements();
        while (en.hasMoreElements()) {
            en.nextElement().progressUpdated(message, current, max);
        }
    }

    /**
     * Progress cancelled.
     */
    protected void progressCancelled() {
        Enumeration<ProgressListener> en = listeners.elements();
        while (en.hasMoreElements()) {
            en.nextElement().progressCancelled();
        }
    }

    /**
     * Progress complete.
     *
     * @param command the command
     * @param result  the result
     */
    protected void progressComplete(String command, Object result) {
        Enumeration<ProgressListener> en = listeners.elements();
        while (en.hasMoreElements()) {
            en.nextElement().progressComplete(command, result);
        }
    }

}
