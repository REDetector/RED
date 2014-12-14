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

import com.xl.utils.NameRetriever;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The Class SiteList stores RNA editing sites which are remained for each step of filtration. After performing one filter, a SiteList will be generated to tell
 * the filtration result. It is like that a leaf has been grown out with the tree root or associated tree branch.
 */
public class SiteList {

    // This vector stores all of the sites currently in the list and keeps them sorted for convenience.
    /**
     * The table name which stands for this site list in database. And RED can retrieve the site set tree by this linear table name.
     */
    protected String tableName;
    /**
     * The description which defines this site list and could not be modified by user.
     */
    protected String description;
    /**
     * The comments added by user.
     */
    protected String comments = "";
    /**
     * The sorted sites.
     */
    private Vector<Site> sortedSites = new Vector<Site>();
    /**
     * This flag says whether the list of sites is actually sorted at the moment
     */
    private boolean isSorted = false;
    /**
     * The list name which is used for display in DataViewer.
     */
    private String listName;
    /**
     * The name which can only obtain from DatabaseManager.XXX. It is convenient to set a standard filter name for this site list to tell RED and database what
     * filter had user run just.
     */
    private String filterName;
    /**
     * The parent (i.e., SiteList or SiteSet).
     */
    private SiteList parent;

    /**
     * The children of this site list. See parent.
     */
    private Vector<SiteList> children = new Vector<SiteList>();

    /**
     * * Instantiates a new site list.
     *
     * @param parent      The parent of this site list
     * @param listName    the listName
     * @param tableName   The table name which stores in database
     * @param description the description
     */
    public SiteList(SiteList parent, String listName, String tableName, String description) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
        this.listName = listName;
        this.tableName = tableName;
        this.description = description;
        if (tableName != null) {
            filterName = NameRetriever.getFilterName(tableName);
        }
        siteListAdded(this);
    }

    /**
     * * Instantiates a new site list.
     *
     * @param parent      The parent of this site list
     * @param listName    the listName
     * @param filterName  The filter name for this site list
     * @param tableName   The table name which stores in database
     * @param description the description
     */
    public SiteList(SiteList parent, String listName, String filterName, String tableName, String description) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
        this.listName = listName;
        this.filterName = filterName;
        this.tableName = tableName;
        this.description = description;
        siteListAdded(this);
    }

    /**
     * Get the parent list or set.
     *
     * @return The parent
     */
    public SiteList getParent() {
        return parent;
    }

    /**
     * Returns this site list and all lists below this point in the tree
     *
     * @return the all site lists
     */
    public SiteList[] getAllSiteLists() {
        Vector<SiteList> v = new Vector<SiteList>();
        v.add(this);
        getAllSiteLists(v);
        return v.toArray(new SiteList[0]);
    }

    /**
     * Gets the all site lists.
     *
     * @param v the v
     */
    synchronized protected void getAllSiteLists(Vector<SiteList> v) {
        // This recursive function iterates through the tree of lists building up a complete flattened list of Site Lists.  If called from a particular node
        // it will return all lists at or below that node

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
     * @param chr the chromosome name
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
     * This method should be called when this list is to be removed from the tree of lists.  It disconnects itself from its parent leaving it free to be garbage
     * collected
     */
    synchronized public void delete() {

        // We need to fire this event before actually doing the delete or our data view can't use the tree connections to remove the node from the existing
        // tree cleanly
        siteListRemoved(this);

        // This actually breaks the link between this node and the rest of the tree.
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
     * Removes the child. Should only be called from within the SiteList class as part of the public delete() method.  Breaks a node away from the rest of the
     * tree
     *
     * @param child the child
     */
    private void removeChild(SiteList child) {
        children.remove(child);
    }

    /**
     * Adds the child. Should only be called from within the SiteList class as part of the constructor. Creates a two way link between nodes and their parents
     *
     * @param child the child
     */
    private void addChild(SiteList child) {
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
     * Sets the description. We replace '\t' with ' ' and '`' with '\n' to make it convenient store in the RED project file.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description.replaceAll("[\\t]", " ").replaceAll("`", "\n");
    }

    /**
     * Sets the comments. We replace '\t' with ' ' and '`' with '\n' to make it convenient store in the RED project file.
     *
     * @param comments The new comment.
     */
    public void setComments(String comments) {
        this.comments = comments.replaceAll("[\\t]", " ").replaceAll("`", "\n");
    }

    /**
     * Description.
     *
     * @return the description.
     */
    public String description() {
        return description;
    }

    /**
     * Comments
     *
     * @return The comment
     */
    public String comments() {
        return comments;
    }

    /**
     * Sort sites by position. If there exists the duplicate Site or the same Site, a Exception will be thrown.
     */
    private synchronized void sortSites() {
        if (!isSorted) {
            Collections.sort(sortedSites);
            isSorted = true;
        }

        try {
            // Do a sanity check to ensure we don't have any duplication here
            for (int i = 1, len = sortedSites.size(); i < len; i++) {
                if (sortedSites.elementAt(i) == sortedSites.elementAt(i - 1)) {
                    throw new Exception("Duplicate site " + sortedSites.elementAt(i) + " and " + sortedSites.elementAt(i - 1) + " in " + getListName());
                }
                if (sortedSites.elementAt(i).compareTo(sortedSites.elementAt(i - 1)) == 0) {
                    throw new Exception("Unable to sort site " + sortedSites.elementAt(i) + " and " + sortedSites.elementAt(i - 1) + " in " + getListName
                            ());
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
         * We had all kinds of problems with this. Because the sorted list has to stay sorted we ended up with a method which took the list from this method
         * and resorted it a different way. That affected the ordering of the list in here, and breakage ensued.
		 *
		 * The only way we can ensure that this doesn't happen is to return a copy of this array rather than the original.
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
     * Gets the list listName.
     *
     * @return the list listName
     */
    public String getListName() {
        return listName;
    }

    /**
     * Sets the listName.
     *
     * @param s the new listName
     */
    public void setListName(String s) {
        this.listName = s;
        siteListRenamed(this);
    }

    /**
     * Get the filter name.
     *
     * @return The filter name.
     */
    public String getFilterName() {
        return filterName;
    }

    public String getTableName() {
        return tableName;
    }

    public String toString() {
        return listName + " (" + sortedSites.size() + ")";
    }

    public String toWrite() {
        return listName + "\t" + filterName + "\t" + tableName + "\t" + description.replaceAll("[\\t]", " ").replaceAll("[\\r\\n]",
                "`") + "\t" + comments.replaceAll("[\\t]", " ").replaceAll("[\\r\\n]", "`");
    }

    // We use the following methods to notify up the tree about changes which have occurred somewhere in the tree. They are private versions of the methods
    // in the SiteListChangeListener

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
