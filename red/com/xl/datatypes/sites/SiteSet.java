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

package com.xl.datatypes.sites;

import com.xl.database.DatabaseManager;
import com.xl.datatypes.DataStore;
import com.xl.utils.NameRetriever;

import java.util.Enumeration;
import java.util.Vector;

/**
 * The Class SiteSet is a special instance of site list which represents a full set of sites as created by a site generator. All site lists are therefore
 * subsets of the containing SiteSet. The site set tree can only be generated from RED project file or database.
 */
public class SiteSet extends SiteList {

    /**
     * The active list.
     */
    private SiteList activeList = null;

    /**
     * The listeners.
     */
    private Vector<SiteListChangeListener> listeners = new Vector<SiteListChangeListener>();

    /**
     * The expected total count.
     */
    private int expectedTotalCount = 0;

    /**
     * The containing data collection
     */
    private DataStore dataStore = null;

    /**
     * * Instantiates a new site set.
     *
     * @param sampleName  The sample name relative to this site set.
     * @param description the description
     * @param sites       the sites
     */
    public SiteSet(String sampleName, String description, Site[] sites) {
        super(null, sampleName + "_" + DatabaseManager.RNA_VCF_RESULT_TABLE_NAME, sampleName + "_" + DatabaseManager.RNA_VCF_RESULT_TABLE_NAME,
                description);
        setSites(sites);
    }

    /**
     * Instantiates a new site set.
     *
     * @param sampleName   The sample name relative to this site set.
     * @param description  the description
     * @param expectedSize the expected size
     */
    public SiteSet(String sampleName, String description, int expectedSize) {
        /**
         * This constructor should only be called by the RedParser since it relies on the correct number of sites eventually being added.
         * Ideally we'd go back to sort out this requirement by changing the RED file format, but for now we're stuck with this work round
         */
        super(null, sampleName + "_" + DatabaseManager.RNA_VCF_RESULT_TABLE_NAME, sampleName + "_" + DatabaseManager.RNA_VCF_RESULT_TABLE_NAME,
                description);
        expectedTotalCount = expectedSize;
    }

    /**
     * * This method is only used by the RED parser. All other site generators add their sites in bulk using the setSites method which is more efficient.
     *
     * @param site the site
     */
    public void addSite(Site site) {
        // Call the super method so we can still be treated like a normal site list
        super.addSite(site);
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public void setDataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Sets the sites.
     *
     * @param sites the new sites
     */
    public void setSites(Site[] sites) {

        expectedTotalCount = sites.length;

        for (Site site : sites) {
            addSite(site);
        }

    }

    /**
     * Size.
     *
     * @return the count of total sites.
     */
    public int size() {
        return expectedTotalCount;
    }

    /**
     * Gets the active list.
     *
     * @return the active list
     */
    public SiteList getActiveList() {
        if (activeList != null) {
            return activeList;
        } else {
            return this;
        }
    }

    /**
     * Sets the active list.
     *
     * @param list the new active list
     */
    public void setActiveList(SiteList list) {

        if (list == null) {
            activeList = null;
            return;
        }
        activeList = list;
    }

    /**
     * Provide a method to store the core information of this site set in a .red file, including sample name, description and comments. RED can retrieve the
     * list name and table name by only sample name.
     *
     * @return the core information.
     */
    @Override
    public String toWrite() {
        String sampleName = NameRetriever.getSampleName(tableName);
        return sampleName + "\t" + description.replaceAll("[\\t]", " ").replaceAll("[\\r\\n]", "`") + "\t" + comments.replaceAll("[\\t]",
                " ").replaceAll("[\\r\\n]", "`");
    }

    /**
     * Adds the site set change listener.
     *
     * @param l the l
     */
    public void addSiteSetChangeListener(SiteListChangeListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Removes the site set change listener.
     *
     * @param l the l
     */
    public void removeSiteSetChangeListener(SiteListChangeListener l) {
        if (l != null && listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    /**
     * Delete this site set tree. DON'T DO this if you won't know what you are doing. It will lost connection with the data store.
     */
    synchronized public void delete() {
        // This is overridden from SiteList and is called as the list is removed.
        super.delete();
        // Now we can get rid of our list of listeners
        listeners.removeAllElements();
        // Drop the link to the collection
        dataStore = null;
    }


    /**
     * These methods propagate up through the tree of site lists to here where we override them to pass the messages on to any listeners we have
     *
     * @param l the l
     */
    protected void siteListAdded(SiteList l) {
        if (listeners == null)
            return;
        Enumeration<SiteListChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().siteListAdded(l);
        }
    }

    /**
     * These methods propagate up through the tree of site lists to here where we override them to pass the messages on to any listeners we have
     *
     * @param l the l
     */
    protected void siteListRemoved(SiteList l) {
        Enumeration<SiteListChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().siteListRemoved(l);
        }
    }

    /**
     * These methods propagate up through the tree of site lists to here where we override them to pass the messages on to any listeners we have
     *
     * @param l the l
     */
    protected void siteListRenamed(SiteList l) {
        Enumeration<SiteListChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().siteListRenamed(l);
        }
    }

}
