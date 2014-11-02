package com.xl.datatypes.sites;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The Class SiteList stores as set of sites and associated quantiation values
 */
public class SiteList {

    // This vector stores all of the sites currently in the list and keeps
    // them sorted for convenience.
    /**
     * The sorted sites.
     */
    private Vector<Site> sortedSites = new Vector<Site>();

    /**
     * This flag says whether the list of sites is actually sorted at the
     * moment
     */
    private boolean isSorted = false;

    /**
     * The name.
     */
    private String name;

    /**
     * The description.
     */
    private String description;

    /**
     * The comments
     */
    private String comments = "";
    /**
     * The parent.
     */
    private SiteList parent;


    private String tableName;

    /**
     * The children.
     */
    private Vector<SiteList> children = new Vector<SiteList>();

    /**
     * Instantiates a new site list.
     *
     * @param name        the name
     * @param description the description
     */
    public SiteList(SiteList parent, String name, String description, String tableName) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
        this.name = name;
        this.description = description;
        this.tableName = tableName;
        siteListAdded(this);
    }

    public SiteList getParent() {
        return parent;
    }

    /**
     * Gets the all site lists.
     *
     * @return the all site lists
     */
    public SiteList[] getAllSiteLists() {
        /**
         * Returns this site list and all lists below this point in the tree
         */

        Vector<SiteList> v = new Vector<SiteList>();
        v.add(this);
        getAllSiteLists(v);
        return v.toArray(new SiteList[0]);
    }

    /**
     * Gets the all site lists.
     *
     * @param v the v
     * @return the all site lists
     */
    synchronized protected void getAllSiteLists(Vector<SiteList> v) {
        // This recursive function iterates through the tree
        // of lists building up a complete flattened list
        // of SiteLists.  If called from a particular node
        // it will return all lists at or below that node

        // For the SeqMonkDataWriter to work it is essential that the
        // lists in this vector are never reordered otherwise we can
        // lose the linkage when we save and reopen the data.

        Enumeration<SiteList> e = children.elements();
        while (e.hasMoreElements()) {
            SiteList l = e.nextElement();
            v.add(l);
            l.getAllSiteLists(v);
        }
    }


    /**
     * Gets the sites for chromosome.
     *
     * @param chr the c
     * @return the sites for chromosome
     */
    public Site[] getSitesForChromosome(String chr) {
        if (!isSorted) {
            sortSites();
        }
        Enumeration<Site> en = sortedSites.elements();
        Vector<Site> tempChr = new Vector<Site>();

        while (en.hasMoreElements()) {
            Site p = en.nextElement();
            if (p.getChr().equals(chr)) {
                tempChr.add(p);
            }
        }
        return tempChr.toArray(new Site[0]);
    }

    /**
     * Delete.
     */
    synchronized public void delete() {
        /**
         * This method should be called when this list is to be
         * removed from the tree of lists.  It disconnects itself
         * from its parent leaving it free to be garbage collected
         */

        // We need to fire this event before actually doing the delete
        // or our Data view can't use the tree connections to remove
        // the node from the existing tree cleanly
        siteListRemoved(this);

        // This actually breaks the link between this node and the rest
        // of the tree.
        if (parent != null) {
            parent.removeChild(this);
        }
        parent = null;
        sortedSites.clear();
    }

    /**
     * Children.
     *
     * @return the site list[]
     */
    public SiteList[] children() {
        return children.toArray(new SiteList[0]);
    }

    /**
     * Removes the child.
     *
     * @param child the child
     */
    private void removeChild(SiteList child) {
        /**
         * Should only be called from within the siteList class as part of the
         * public delete() method.  Breaks a node away from the rest of the tree
         */
        children.remove(child);
    }

    /**
     * Adds the child.
     *
     * @param child the child
     */
    private void addChild(SiteList child) {
        /**
         * Should only be called from within the SiteList class as part of the
         * constructor. Creates a two way link between nodes and their parents
         */
        children.add(child);
    }

    /**
     * Adds the site.
     *
     * @param p the p
     */
    public synchronized void addSite(Site p) {
        sortedSites.add(p);
        isSorted = false;

    }

    /**
     * Sets the name.
     *
     * @param s the new name
     */
    public void setName(String s) {
        this.name = s;
        siteListRenamed(this);
    }

    /**
     * Sets the description.
     *
     * @param d the new description
     */
    public void setDescription(String d) {
        this.description = d.replaceAll("[\\t\\n\\r]", " ");
    }

    public void setComments(String comments) {
        this.comments = comments.replaceAll("[\\t]", " ").replaceAll("`", "'");
    }

    /**
     * Description.
     *
     * @return the string
     */
    public String description() {
        return description;
    }

    public String comments() {
        return comments;
    }

    private synchronized void sortSites() {
        if (!isSorted) {
            Collections.sort(sortedSites);
            isSorted = true;
        }

        try {
            // Do a sanity check to ensure we don't have any duplication here
            for (int i = 1, len = sortedSites.size(); i < len; i++) {
                if (sortedSites.elementAt(i) == sortedSites.elementAt(i - 1)) {
                    throw new Exception("Duplicate site "
                            + sortedSites.elementAt(i) + " and "
                            + sortedSites.elementAt(i - 1) + " in " + name());
                }
                if (sortedSites.elementAt(i).compareTo(
                        sortedSites.elementAt(i - 1)) == 0) {
                    throw new Exception("Unsortable site "
                            + sortedSites.elementAt(i) + " and "
                            + sortedSites.elementAt(i - 1) + " in " + name());
                }
            }
        } catch (Exception ex) {
            // There are duplicate sites and we need to remove them.
            Vector<Site> dedup = new Vector<Site>();
            Site lastSite = null;
            Enumeration<Site> en = sortedSites.elements();
            while (en.hasMoreElements()) {
                Site p = en.nextElement();
                if (p == lastSite)
                    continue;
                dedup.add(p);
                lastSite = p;
            }

            sortedSites = dedup;
        }

        sortedSites.trimToSize();
    }

    /**
     * Gets the all sites.
     *
     * @return the all sites
     */
    public Site[] getAllSites() {
        if (!isSorted) {
            sortSites();
        }

		/*
         * We had all kinds of problems with this. Because the sorted list has
		 * to stay sorted we ended up with a method which took the list from
		 * this method and resorted it a different way. That affected the
		 * ordering of the list in here, and breakage ensued.
		 * 
		 * The only way we can ensure that this doesn't happen is to return a
		 * copy of this array rather than the original.
		 */

        Site[] returnArray = new Site[sortedSites.size()];
        Enumeration<Site> en = sortedSites.elements();

        int i = 0;
        while (en.hasMoreElements()) {
            returnArray[i] = en.nextElement();
            i++;
        }

        return returnArray;
    }

    /**
     * Gets the list name.
     *
     * @return the list name
     */
    public String name() {
        return name;
    }

    public String getTableName() {
        return tableName;
    }

    public String toString() {
        return name + " (" + sortedSites.size() + ")";
    }

    // We use the following methods to notify up the tree about
    // changes which have occurred somewhere in the tree. They
    // are private versions of the methods in the SiteSetChangeListener

    /**
     * Site list added.
     *
     * @param l the l
     */
    protected void siteListAdded(SiteList l) {
        parent.siteListAdded(l);
    }

    /**
     * Site list removed.
     *
     * @param l the l
     */
    protected void siteListRemoved(SiteList l) {
        parent.siteListRemoved(l);
    }

    /**
     * Site list renamed.
     *
     * @param l the l
     */
    protected void siteListRenamed(SiteList l) {
        parent.siteListRenamed(l);
    }

}
