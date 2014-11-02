package com.xl.datatypes.sites;

import com.xl.datatypes.DataCollection;
import com.xl.exception.REDException;

import java.util.Enumeration;
import java.util.Vector;

/**
 * The Class SiteSet is a special instance of site list which represents a
 * full set of sites as created by a site generator. All site lists are
 * therefore subsets of the containing SiteSet.
 */
public class SiteSet extends SiteList {

    /**
     * The active list.
     */
    private SiteList activeList = null;

    /**
     * The listeners.
     */
    private Vector<SiteSetChangeListener> listeners = new Vector<SiteSetChangeListener>();

    /**
     * The expected total count.
     */
    private int expectedTotalCount = 0;

    /**
     * The containing data collection
     */
    private DataCollection collection = null;

    /**
     * Instantiates a new site set.
     *
     * @param description the description
     * @param sites       the sites
     */
    public SiteSet(String description, Site[] sites, String tableName) {
        super(null, "All Sites", description, tableName);
        setSites(sites);
    }

    /**
     * Instantiates a new site set.
     *
     * @param description  the description
     * @param expectedSize the expected size
     */
    public SiteSet(String description, int expectedSize, String tableName) {
        /**
         * This constructor should only be called by the SeqMonkParser since it
         * relies on the correct number of sites eventually being added.
         * Ideally we'd go back to sort out this requirement by changing the
         * SeqMonk file format, but for now we're stuck with this work round
         */
        super(null, "All Sites", description, tableName);
        expectedTotalCount = expectedSize;
    }

    public void addSite(Site p) {

        /**
         * This method is only used by the SeqMonk parser. All other site
         * generators add their sites in bulk using the setSites method which
         * is more efficient.
         */

        // Call the super method so we can still be treated like a
        // normal site list
        super.addSite(p);
    }

    public void setCollection(DataCollection collection) {
        this.collection = collection;
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

    public String justDescription() {
        return super.description();
    }

    /**
     * Size.
     *
     * @return the int
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
     * @throws REDException the red exception
     */
    public void setActiveList(SiteList list) throws REDException {

        if (list == null) {
            activeList = null;
            return;
        }
        activeList = list;

        if (collection != null) {
            collection.activeSiteListChanged(list);
        }
    }

    /**
     * Adds the site set change listener.
     *
     * @param l the l
     */
    public void addSiteSetChangeListener(SiteSetChangeListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Removes the site set change listener.
     *
     * @param l the l
     */
    public void removeSiteSetChangeListener(SiteSetChangeListener l) {
        if (l != null && listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    synchronized public void delete() {
        // This is overridden from SiteList and is called as the
        // list is removed.
        super.delete();
        // Now we can get rid of our list of listeners
        listeners.removeAllElements();
        // Drop the link to the collection
        collection = null;
    }

    // These methods propogate up through the tree of site lists
    // to here where we override them to pass the messages on to
    // any listeners we have
    protected void siteListAdded(SiteList l) {
        if (listeners == null)
            return;
        Enumeration<SiteSetChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            SiteSetChangeListener pl = e.nextElement();
            pl.siteListAdded(l);
        }
    }

    protected void siteListRemoved(SiteList l) {
        Enumeration<SiteSetChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().siteListRemoved(l);
        }
    }

    protected void siteListRenamed(SiteList l) {
        Enumeration<SiteSetChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().siteListRenamed(l);
        }
    }

}
