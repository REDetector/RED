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
package com.xl.datawriters;

import com.xl.datatypes.*;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.annotation.CoreAnnotationSet;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.datatypes.probes.Probe;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.datatypes.sequence.HiCHitCollection;
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.display.featureviewer.Feature;
import com.xl.exception.REDException;
import com.xl.interfaces.Cancellable;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;
import com.xl.preferences.REDPreferences;
import com.xl.utils.ParsingUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

/**
 * The Class SeqMonkDataWriter serialises a SeqMonk project to a single file.
 */
public class REDDataWriter implements Runnable, Cancellable {

    /**
     * The Constant DATA_VERSION.
     */
    public static final int DATA_VERSION = 1;

    // If you make ANY changes to the format written by this class
    // you MUST increment this value to stop older parsers from
    // trying to parse it. Once you have updated the parser to
    // read the new format you can then update the corresponding
    // value in the parser so that it will work.

	/*
     * TODO: Some of these data sets take a *long* time to save due to the
	 * volume of data. Often when people are saving they're just saving display
	 * preferences. In these cases it would be nice to have a mode where the
	 * display preferences were just appended to the end of an existing file,
	 * rather than having to put out the whole thing again. Since the size of
	 * the preferences section is pretty small it won't affect overall file size
	 * much.
	 * 
	 * If the data (probes, groups or quantitation) changes then we'll have to
	 * do a full rewrite.
	 */

    /**
     * The listeners.
     */
    private Vector<ProgressListener> listeners = new Vector<ProgressListener>();

    /**
     * The data.
     */
    private DataCollection data;

    /**
     * The genome.
     */
    private Genome genome;

    /**
     * The final file to save to file.
     */
    private File file;

    /**
     * The temporary file to work with
     */
    private File tempFile;

    /**
     * The visible stores.
     */
    private DataStore[] visibleStores;

    /**
     * Whether to cancel
     */
    private boolean cancel = false;

    /**
     * Instantiates a new seq monk data writer.
     */
    public REDDataWriter() {
    }

    /**
     * Adds the progress listener.
     *
     * @param l the l
     */
    public void addProgressListener(ProgressListener l) {
        if (l != null && !listeners.contains(l))
            listeners.add(l);
    }

    /**
     * Removes the progress listener.
     *
     * @param l the l
     */
    public void removeProgressListener(ProgressListener l) {
        if (l != null && listeners.contains(l))
            listeners.remove(l);
    }

    /**
     * Write data.
     *
     * @param application the application
     * @param file        the file
     */
    public void writeData(REDApplication application, File file) {
        data = application.dataCollection();
        // System.out.println(this.getClass().getDisplayName()+":"+data.getGenome().species());
        this.genome = data.genome();
        this.file = file;
        visibleStores = application.drawnDataStores();
        Thread t = new Thread(this);
        t.start();
    }

    public void cancel() {
        cancel = true;
    }

    private void cancelled(PrintStream p) throws IOException {
        p.close();

        if (!tempFile.delete()) {
            throw new IOException("Couldn't delete temp file");
        }
        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().progressCancelled();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            // System.out.println("run:"+file.getAbsolutePath());
            // Generate a temp file in the same directory as the final
            // destination
            tempFile = File
                    .createTempFile("red", ".temp", file.getParentFile());

            BufferedOutputStream bos;

            if (REDPreferences.getInstance().compressOutput()) {
                bos = new BufferedOutputStream(new GZIPOutputStream(
                        new FileOutputStream(tempFile), 2048));
            } else {
                bos = new BufferedOutputStream(new FileOutputStream(tempFile));
            }
            PrintStream p = new PrintStream(bos);

            printDataVersion(p);

            printGenome(p);

            DataSet[] dataSets = data.getAllDataSets();
            DataGroup[] dataGroups = data.getAllDataGroups();

            if (!printDataSets(dataSets, p)) {
                return; // They cancelled
            }

            printDataGroups(dataSets, dataGroups, p);

            AnnotationSet[] annotationSets = data.genome()
                    .getAnnotationCollection().anotationSets();
            for (int a = 0; a < annotationSets.length; a++) {
                if (annotationSets[a] instanceof CoreAnnotationSet) {
                    continue;
                }
                if (!printAnnotationSet(annotationSets[a], p)) {
                    // They cancelled
                    return;
                }
            }

            Probe[] probes = null;

            if (data.probeSet() != null) {
                probes = data.probeSet().getAllProbes();
            }

            if (probes != null) {
                if (!printProbeSet(data.probeSet(), probes, dataSets, p)) {
                    return; // They cancelled
                }
            }

            printVisibleDataStores(dataSets, dataGroups, p);

            if (probes != null) {
                if (!printProbeLists(p)) {
                    return; // They cancelled
                }
            }

            printDisplayPreferences(p);

            p.close();

            // We can now overwrite the original file
            if (file.exists()) {
                if (!file.delete()) {
                    throw new IOException(
                            "Couldn't delete old project file when making new one");
                }

            }

            if (!tempFile.renameTo(file)) {
                throw new IOException("Failed to rename temporary file");
            }

            Enumeration<ProgressListener> e = listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().progressComplete("data_written", null);
            }
        } catch (Exception ex) {
            Enumeration<ProgressListener> e = listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().progressExceptionReceived(ex);
            }
            ex.printStackTrace();
        }

    }

    /**
     * Prints the data version.
     *
     * @param p the p
     */
    private void printDataVersion(PrintStream p) {
        // The first line of the file will be the version of the data
        // format we're using. This will help us out should we need
        // to update the format in the future.
        p.println(ParsingUtils.RED_DATA_VERSION + "\t" + DATA_VERSION);
    }

    /**
     * Prints the assembly.
     *
     * @param p the p
     */
    private void printGenome(PrintStream p) {
        // The next thing we need to do is to output the details of the genome
        // we're using
        p.println(ParsingUtils.GENOME_INFORMATION_START);
        p.println(GenomeDescriptor.getInstance().toString());
        p.println(ParsingUtils.GENOME_INFORMATION_END);
    }

    /**
     * Prints the data sets.
     *
     * @param dataSets the data sets
     * @param p        the p
     * @return false if cancelled, else true
     */
    private boolean printDataSets(DataSet[] dataSets, PrintStream p)
            throws IOException {
        p.println(ParsingUtils.SAMPLES + "\t" + dataSets.length);
        for (int i = 0; i < dataSets.length; i++) {
            p.println(dataSets[i].name() + "\t" + dataSets[i].fileName() + "\t");
        }

        // We now need to print the data for each data set
        for (int i = 0; i < dataSets.length; i++) {
            Enumeration<ProgressListener> e = listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().progressUpdated(
                        "Writing data for " + dataSets[i].name(), i * 10,
                        dataSets.length * 10);
            }

            if (dataSets[i] instanceof PairedDataSet) {
                boolean returnValue = printPairedDataSet(
                        (PairedDataSet) dataSets[i], p, i, dataSets.length);
                if (!returnValue)
                    return false; // They cancelled
            } else {
                boolean returnValue = printStandardDataSet(dataSets[i], p, i,
                        dataSets.length);
                if (!returnValue)
                    return false; // They cancelled
            }
        }

        return true;
    }

    private boolean printPairedDataSet(PairedDataSet set, PrintStream p,
                                       int index, int indexTotal) throws IOException {

        p.println(set.getTotalReadCount() * 2 + "\t" + set.name());

        // Go through one chromosome at a time.
        String[] chrs = data.genome().getAllChromosomeNames();
        for (int c = 0; c < chrs.length; c++) {

            HiCHitCollection hiCHits = set.getHiCReadsForChromosome(chrs[c]);

            // Work out how many of these reads we're actually going to output
            int validReadCount = 0;
            for (int c2 = 0; c2 < chrs.length; c2++) {
                validReadCount += hiCHits
                        .getSourcePositionsForChromosome(chrs[c2]).length;
            }

            p.println(chrs[c] + "\t" + validReadCount);

            for (int c2 = 0; c2 < chrs.length; c2++) {

                SequenceRead[] sourceReads = hiCHits
                        .getSourcePositionsForChromosome(chrs[c2]);
                SequenceRead[] hitReads = hiCHits
                        .getHitPositionsForChromosome(chrs[c2]);

                for (int j = 0; j < sourceReads.length; j++) {

                    if (cancel) {
                        cancelled(p);
                        return false;
                    }

                    // TODO: Fix the progress bar
                    if ((j % (1 + (validReadCount / 10))) == 0) {
                        Enumeration<ProgressListener> e2 = listeners.elements();
                        while (e2.hasMoreElements()) {
                            e2.nextElement().progressUpdated(
                                    "Writing data for " + set.name(),
                                    index * chrs.length + c,
                                    indexTotal * chrs.length);
                        }

                    }

                    p.println(sourceReads[j].toWrite() + ":" + hitReads[j].toWrite());
                }
            }
        }
        // Print a blank line after the last chromosome
        p.println("");

        return true;
    }

    private boolean printStandardDataSet(DataSet set, PrintStream p, int index,
                                         int indexTotal) throws IOException {

        p.println(set.getTotalReadCount() + "\t" + set.name());

        // Go through one chromosome at a time.
        String[] chrs = data.genome().getAllChromosomeNames();
        for (int c = 0; c < chrs.length; c++) {
            SequenceRead[] reads = set.getReadsForChromosome(chrs[c]);
            p.println(chrs[c] + "\t" + reads.length);

            SequenceRead lastRead = null;
            int lastReadCount = 0;

            for (int j = 0; j < reads.length; j++) {

                if (cancel) {
                    cancelled(p);
                    return false;
                }

                if ((j % (1 + (reads.length / 10))) == 0) {
                    Enumeration<ProgressListener> e2 = listeners.elements();
                    while (e2.hasMoreElements()) {
                        e2.nextElement().progressUpdated(
                                "Writing data for " + set.name(),
                                index * chrs.length + c,
                                indexTotal * chrs.length);
                    }

                }

                if (lastReadCount == 0 || reads[j] == lastRead) {
                    lastRead = reads[j];
                    ++lastReadCount;
                } else {
                    if (lastReadCount > 1) {
                        p.println(lastRead.toWrite() + "\t" + lastReadCount);
                    } else if (lastReadCount == 1) {
                        p.println(lastRead.toWrite());
                    } else {
                        throw new IllegalStateException(
                                "Shouldn't have zero count ever, read is "
                                        + reads[j] + " last read is "
                                        + lastRead + " count is "
                                        + lastReadCount);
                    }
                    lastRead = reads[j];
                    lastReadCount = 1;
                }
            }
            if (lastReadCount > 1) {
                p.println(lastRead.toWrite() + "\t" + lastReadCount);
            }

            // If there are no reads on a chromosome then this value could be
            // zero
            else if (lastReadCount == 1) {
                p.println(lastRead.toWrite());
            }

        }
        // Print a blank line after the last chromosome
        p.println("");

        return true;
    }

    /**
     * Prints the data groups.
     *
     * @param dataSets   the data sets
     * @param dataGroups the data groups
     * @param p          the p
     */
    private void printDataGroups(DataSet[] dataSets, DataGroup[] dataGroups,
                                 PrintStream p) {

        p.println(ParsingUtils.DATA_GROUPS + "\t" + dataGroups.length);
        for (int i = 0; i < dataGroups.length; i++) {
            DataSet[] groupSets = dataGroups[i].dataSets();

            // We used to use the name of the dataset to populate the group
            // but this caused problems when we had duplicated dataset names
            // we therefore have to figure out the index of each dataset in
            // each group

            StringBuffer b = new StringBuffer();
            b.append(dataGroups[i].name());
            for (int j = 0; j < groupSets.length; j++) {
                for (int d = 0; d < dataSets.length; d++) {
                    if (groupSets[j] == dataSets[d]) {
                        b.append("\t");
                        b.append(d);
                    }
                }
            }

            p.println(b);
        }
    }

    /**
     * Prints the annotation set.
     *
     * @param a the a
     * @param p the p
     * @return false if cancelled, else true;
     */
    private boolean printAnnotationSet(AnnotationSet a, PrintStream p)
            throws IOException {
        Feature[] features = a.getAllFeatures();
        p.println(ParsingUtils.ANNOTATION + "\t" + a.name() + "\t" + features.length);

        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().progressUpdated(
                    "Writing annotation set " + a.name(), 0, 1);
        }

        for (int f = 0; f < features.length; f++) {

            if (cancel) {
                cancelled(p);
                return false;
            }

            p.println(features[f].toString());

        }

        return true;

    }

    /**
     * Prints the probe set.
     *
     * @param probeSet the probe set
     * @param probes   the probes
     * @param dataSets the data sets
     * @param p        the p
     * @return false if cancelled, else true
     */
    private boolean printProbeSet(ProbeSet probeSet, Probe[] probes,
                                  DataSet[] dataSets, PrintStream p) throws IOException {

        // We need the saved string to be linear so we replace the line breaks
        // with ` (which we've replaced with ' in the
        // comment. We put back the line breaks when we load the comments back.

        String comments = probeSet.comments().replaceAll("[\\r\\n]", "`");

        p.println(ParsingUtils.PROBES + "\t" + probes.length + "\t" + probeSet.justDescription() + "\t" + comments);

        // Next we print out the data

        for (int i = 0; i < probes.length; i++) {

            if (cancel) {
                cancelled(p);
                return false;
            }

            if (i % 1000 == 0) {
                Enumeration<ProgressListener> e = listeners.elements();
                while (e.hasMoreElements()) {
                    e.nextElement().progressUpdated(
                            "Written data for " + i + " probes out of "
                                    + probes.length, i, probes.length);
                }
            }

            p.println(probes[i].getChr() + "\t" + probes[i].getStart() + "\t" + probes[i].getEditingBase());
//            for (DataSet dataSet : dataSets) {
//                SequenceRead[] sequenceReads = dataSet.getReadsForProbe(probes[i]);
//                for (SequenceRead sequenceRead : sequenceReads) {
//                    p.println(sequenceRead.toWrite());
//                }
//            }
        }
        return true;
    }

    /**
     * Prints the visible data stores.
     *
     * @param dataSets   the data sets
     * @param dataGroups the data groups
     * @param p          the p
     */
    private void printVisibleDataStores(DataSet[] dataSets,
                                        DataGroup[] dataGroups, PrintStream p) {
        // Now we can put out the list of visible stores
        // We have to refer to these by position rather than name
        // since names are not guaranteed to be unique.
        p.println(ParsingUtils.VISIBLE_STORES + "\t" + visibleStores.length);
        for (int i = 0; i < visibleStores.length; i++) {
            if (visibleStores[i] instanceof DataSet) {
                for (int s = 0; s < dataSets.length; s++) {
                    if (visibleStores[i] == dataSets[s]) {
                        p.println(s + "\t" + "set");
                    }
                }
            } else if (visibleStores[i] instanceof DataGroup) {
                for (int g = 0; g < dataGroups.length; g++) {
                    if (visibleStores[i] == dataGroups[g]) {
                        p.println(g + "\t" + "group");
                    }
                }
            }
        }
    }

    /**
     * Prints the probe lists.
     *
     * @param p the p
     */
    private boolean printProbeLists(PrintStream p) throws REDException, IOException {
        // Now we print out the list of probe lists

		/*
         * We rely on this list coming in tree order, that is to say that when
		 * we see a node at depth n we assume that all subsequent nodes at depth
		 * n+1 are children of the first node, until we see another node at
		 * depth n.
		 * 
		 * This should be how the nodes are created anyway.
		 */
        ProbeList[] lists = data.probeSet().getAllProbeLists();

        // We start at the second list since the first list will always
        // be "All probes" which we'll sort out some other way.

        p.println(ParsingUtils.LISTS + "\t" + (lists.length - 1));

        for (ProbeList probeList : lists) {
            String listComments = probeList.comments().replaceAll("[\\r\\n]", "`");
            Probe[] currentListProbes = probeList.getAllProbes();
            int probeLength = currentListProbes.length;
            p.println(getListDepth(probeList) + "\t" + probeList.name() + "\t" + probeList.description()
                    + "\t" + probeLength + "\t" + listComments);

            for (int j = 0; j < probeLength; j++) {
                if (j % 1000 == 0) {
                    if (cancel) {
                        cancelled(p);
                        return false;
                    }
                    Enumeration<ProgressListener> e = listeners.elements();
                    while (e.hasMoreElements()) {
                        e.nextElement().progressUpdated(
                                "Written lists for " + j + " probes out of "
                                        + probeLength, j, probeLength);
                    }
                }
                p.println(currentListProbes[j].getChr() + "\t" + currentListProbes[j].getStart() + "\t" +
                        currentListProbes[j].getEditingBase());
            }
        }

        return true;
    }

    /**
     * Prints the display preferences.
     *
     * @param p the print stream to write the preferences to
     */
    private void printDisplayPreferences(PrintStream p) {
        // Now write out some display preferences
        DisplayPreferences.getInstance().writeConfiguration(p);
    }

    /**
     * Gets the list depth.
     *
     * @param p the p
     * @return the list depth
     */
    private int getListDepth(ProbeList p) {
        int depth = 0;

        while (p.getParent() != null) {
            depth++;
            p = p.getParent();
        }
        return depth;
    }
}
