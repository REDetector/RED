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
package com.xl.datawriters;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.annotation.CoreAnnotationSet;
import com.xl.datatypes.feature.Feature;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.datatypes.sites.SiteSet;
import com.xl.interfaces.Cancellable;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.ParsingUtils;
import com.xl.utils.Strand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * The Class REDDataWriter serialises a RED project to a single file. It contains only configurations but not the real data.
 */
public class REDDataWriter implements Runnable, Cancellable {
    /**
     * The constant data version for RED project.
     */
    public static final int DATA_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(REDDataWriter.class);
    /**
     * The listeners.
     */
    private Vector<ProgressListener> listeners = new Vector<ProgressListener>();

    /**
     * The data.
     */
    private DataCollection data;

    /**
     * The final file to save to.
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
     * Instantiates a new RED data writer.
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
        this.file = file;
        visibleStores = application.drawnDataStores();
        Thread t = new Thread(this);
        logger.info("Start writing data into the file '" + file.getAbsolutePath() + "'");
        t.start();
    }

    /**
     * Cancel the saving progress.
     */
    public void cancel() {
        cancel = true;
    }

    /**
     * If cancel==true, then this method will be called to deal with the remaining work.
     *
     * @param p The print stream to be cancelled.
     * @throws IOException If the temp file can not be deleted, then throw this IOException.
     */
    private void cancelled(PrintStream p) throws IOException {
        p.close();

        if (!tempFile.delete()) {
            logger.error("Couldn't delete temp file", new IOException());
        }
        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().progressCancelled();
        }
    }

    public void run() {
        try {
            // Generate a temp file in the same directory as the final destination
            tempFile = File.createTempFile("red", ".temp", file.getParentFile());

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
            PrintStream p = new PrintStream(bos);

            // Print data version.
            printDataVersion(p);
            //Print the genome annotation file.
            printGenome(p);
            //Print information of current data sets and data group.
            DataSet[] dataSets = data.getAllDataSets();
            DataGroup[] dataGroups = data.getAllDataGroups();
            printDataSets(dataSets, p);
            printDataGroups(dataSets, dataGroups, p);
            //Print annotation file, NOT including core annotation set(i.e., RefSeq Gene).
            AnnotationSet[] annotationSets = data.genome().getAnnotationCollection().annotationSets();
            for (AnnotationSet annotationSet : annotationSets) {
                if (annotationSet instanceof CoreAnnotationSet) {
                    continue;
                }
                if (!printAnnotationSet(annotationSet, p)) {
                    // They cancelled
                    return;
                }
            }
            //Print visible data stores, including visible data sets and visible data groups.
            printVisibleDataStores(dataSets, dataGroups, p);
            //Print display preferences.
            printDisplayPreferences(p);
            //Flush and close the stream.
            p.flush();
            p.close();

            // We can now overwrite the original file
            if (file.exists()) {
                if (!file.delete()) {
                    throw new IOException("Couldn't delete old project file when making new one");
                }
            }

            if (!tempFile.renameTo(file)) {
                throw new IOException("Failed to rename temporary file");
            }

            Enumeration<ProgressListener> e = listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().progressComplete("data_written", null);
            }
        } catch (IOException ex) {
            Enumeration<ProgressListener> e = listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().progressExceptionReceived(ex);
            }
            ex.printStackTrace();
        }
    }

    /**
     * Print the data version. The first line of the file will be the version of the data format we're using. This will help us out should we need to update the
     * format in the future.
     *
     * @param p the p
     */
    private void printDataVersion(PrintStream p) {
        logger.info("Print data version: {}", DATA_VERSION);
        p.println(ParsingUtils.RED_DATA_VERSION + "\t" + DATA_VERSION);
    }

    /**
     * Print the genome information, output the details of the genome we're using. We set the start and end flag to make it easy retrieve.
     *
     * @param p the p
     */
    private void printGenome(PrintStream p) {
        logger.info("Print genome information.");
        p.println(ParsingUtils.GENOME_INFORMATION_START);
        p.println(GenomeDescriptor.getInstance().toString());
        p.println(ParsingUtils.GENOME_INFORMATION_END);
    }

    /**
     * Prints the data sets. The first line is the sample flag and the count of data set. Then for each data set, we print the basic information and the site
     * set tree if the data set has.
     *
     * @param dataSets the data sets
     * @param p        the p
     */
    private void printDataSets(DataSet[] dataSets, PrintStream p) throws IOException {
        logger.info("Print data sets: {}", Arrays.asList(dataSets));
        p.println(ParsingUtils.SAMPLES + "\t" + dataSets.length);
        for (DataSet dataSet : dataSets) {
            p.println(dataSet.name() + "\t" + dataSet.fileName() + "\t" + dataSet.isStandardChromosomeName() + "\t" + dataSet.getTotalReadCount() + "\t" + dataSet
                    .getTotalReadLength() + "\t" + dataSet.getForwardReadCount() + "\t" + dataSet.getReverseReadCount() + "\t" + (dataSet.siteSet() != null));
            if (dataSet.siteSet() != null) {
                printSiteSetTree(p, dataSet.siteSet());
            }
        }
    }

    /**
     * Prints the data groups.
     *
     * @param dataSets   the data sets
     * @param dataGroups the data groups
     * @param p          the p
     */
    private void printDataGroups(DataSet[] dataSets, DataGroup[] dataGroups, PrintStream p) {
        logger.info("Print data groups: {}", Arrays.asList(dataGroups));
        p.println(ParsingUtils.DATA_GROUPS + "\t" + dataGroups.length);
        for (DataGroup dataGroup : dataGroups) {
            DataSet[] groupSets = dataGroup.dataSets();
            // We used to use the name of the data set to populate the group but this caused problems when we had duplicated data set names we therefore have
            // to figure out the index of each data set in each group

            StringBuffer b = new StringBuffer();
            b.append(dataGroup.name());
            for (DataSet groupSet : groupSets) {
                for (int d = 0; d < dataSets.length; d++) {
                    if (groupSet == dataSets[d]) {
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
    private boolean printAnnotationSet(AnnotationSet a, PrintStream p) throws IOException {
        logger.info("Print annotation set: {}", a.name());
        List<Feature> features = a.getAllFeatures();
        p.println(ParsingUtils.ANNOTATION + "\t" + a.name() + "\t" + features.size());

        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().progressUpdated("Writing annotation set " + a.name(), 0, 1);
        }

        for (Feature feature : features) {
            if (cancel) {
                cancelled(p);
                return false;
            }
            String name = feature.getName();
            String chr = feature.getChr();
            String strand = Strand.parseStrand(feature.getStrand());
            String aliasName = feature.getAliasName();
            List<Location> allLocations = feature.getAllLocations();
            p.println(name + "\t" + chr + "\t" + strand + "\t" + aliasName);
            p.println(allLocations);
        }
        return true;
    }

    /**
     * Prints the site set.
     *
     * @param p       the p
     * @param siteSet the site set
     */
    private void printSiteSetTree(PrintStream p, SiteSet siteSet) throws IOException {
        logger.info("Print the site set tree for a sample: {}" + siteSet.getListName());
        // We need the saved string to be linear so we replace the line breaks with ` (which we've replaced with ' in the comment. We put back the line
        // breaks when we load the comments back.

        Site[] allSites = siteSet.getAllSites();
        p.println(ParsingUtils.SITES + "\t" + allSites.length + "\t" + siteSet.toWrite());

        // Next we print out the data

        for (int i = 0; i < allSites.length; i++) {

            if (cancel) {
                cancelled(p);
                return;
            }

            if (i % 1000 == 0) {
                Enumeration<ProgressListener> e = listeners.elements();
                while (e.hasMoreElements()) {
                    e.nextElement().progressUpdated("Written data for " + i + " sites out of " + allSites.length, i, allSites.length);
                }
            }

            p.println(allSites[i]);
        }

        // Now we print out the list of site lists
        /*
         * We rely on this list coming in tree order, that is to say that when we see a node at depth n we assume that all subsequent nodes at depth n+1 are
         * children of the first node, until we see another node at depth n.
		 *
		 * This should be how the nodes are created anyway.
		 */
        SiteList[] lists = siteSet.getAllSiteLists();

        // We start at the second list since the first list will always be "All sites" which we'll sort out some other way.
        p.println(ParsingUtils.LISTS + "\t" + (lists.length - 1));

        for (int i = 1, len = lists.length; i < len; i++) {
            Site[] sites = lists[i].getAllSites();
            int siteLength = sites.length;
            p.println(getListDepth(lists[i]) + "\t" + siteLength + "\t" + lists[i].toWrite());

            for (int j = 0; j < siteLength; j++) {
                if (j % 1000 == 0) {
                    if (cancel) {
                        cancelled(p);
                        return;
                    }
                    Enumeration<ProgressListener> e = listeners.elements();
                    while (e.hasMoreElements()) {
                        e.nextElement().progressUpdated("Written lists for " + j + " sites out of " + siteLength, j, siteLength);
                    }
                }
                p.println(sites[j]);
            }
        }

    }

    /**
     * Prints the visible data stores.
     *
     * @param dataSets   the data sets
     * @param dataGroups the data groups
     * @param p          the p
     */
    private void printVisibleDataStores(DataSet[] dataSets, DataGroup[] dataGroups, PrintStream p) {
        logger.info("Print the visible data stores");
        // Now we can put out the list of visible stores We have to refer to these by position rather than name since names are not guaranteed to be unique.
        p.println(ParsingUtils.VISIBLE_STORES + "\t" + visibleStores.length);
        for (DataStore visibleStore : visibleStores) {
            if (visibleStore instanceof DataSet) {
                for (int s = 0; s < dataSets.length; s++) {
                    if (visibleStore == dataSets[s]) {
                        p.println(s + "\t" + "set");
                    }
                }
            } else if (visibleStore instanceof DataGroup) {
                for (int g = 0; g < dataGroups.length; g++) {
                    if (visibleStore == dataGroups[g]) {
                        p.println(g + "\t" + "group");
                    }
                }
            }
        }
    }

    /**
     * Prints the display preferences.
     *
     * @param p the print stream to write the preferences to
     */
    private void printDisplayPreferences(PrintStream p) {
        logger.info("Print the display preferences");
        DisplayPreferences.getInstance().writeConfiguration(p);
    }

    /**
     * Gets the list depth.
     *
     * @param p the p
     * @return the list depth
     */
    private int getListDepth(SiteList p) {
        int depth = 0;

        while (p.getParent() != null) {
            depth++;
            p = p.getParent();
        }
        return depth;
    }
}
