package com.xl.datatypes.probes;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The Class ProbeList stores as set of probes and associated quantiation values
 */
public class ProbeList {

    // This vector stores all of the probes currently in the list and keeps
    // them sorted for convenience.
    /**
     * The sorted probes.
     */
    private Vector<Probe> sortedProbes = new Vector<Probe>();

    /**
     * This flag says whether the list of probes is actually sorted at the
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

    public ProbeList getParent() {
        return parent;
    }

    /**
     * Gets the all probe lists.
     *
     * @return the all probe lists
     */
    public ProbeList[] getAllProbeLists() {
        /**
         * Returns this probe list and all lists below this point in the tree
         */

        Vector<ProbeList> v = new Vector<ProbeList>();
        v.add(this);
        getAllProbeLists(v);
        return v.toArray(new ProbeList[0]);
    }

    /**
     * Gets the all probe lists.
     *
     * @param v the v
     * @return the all probe lists
     */
    synchronized protected void getAllProbeLists(Vector<ProbeList> v) {
        // This recursive function iterates through the tree
        // of lists building up a complete flattened list
        // of ProbeLists.  If called from a particular node
        // it will return all lists at or below that node

        // For the SeqMonkDataWriter to work it is essential that the
        // lists in this vector are never reordered otherwise we can
        // lose the linkage when we save and reopen the data.

        Enumeration<ProbeList> e = children.elements();
        while (e.hasMoreElements()) {
            ProbeList l = e.nextElement();
            v.add(l);
            l.getAllProbeLists(v);
        }
    }

    /**
     * The parent.
     */
    private ProbeList parent;

    /**
     * The children.
     */
    private Vector<ProbeList> children = new Vector<ProbeList>();

    /**
     * Instantiates a new probe list.
     *
     * @param name        the name
     * @param description the description
     */
    public ProbeList(ProbeList parent, String name, String description) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
        this.name = name;
        this.description = description;
        probeListAdded(this);
    }

    /**
     * Gets the probes for chromosome.
     *
     * @param chr the c
     * @return the probes for chromosome
     */
    public Probe[] getProbesForChromosome(String chr) {
        if (!isSorted) {
            sortProbes();
        }
        Enumeration<Probe> en = sortedProbes.elements();
        Vector<Probe> tempChr = new Vector<Probe>();

        while (en.hasMoreElements()) {
            Probe p = en.nextElement();
            if (p.getChr().equals(chr)) {
                tempChr.add(p);
            }
        }
        return tempChr.toArray(new Probe[0]);
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
        probeListRemoved(this);

        // This actually breaks the link between this node and the rest
        // of the tree.
        if (parent != null) {
            parent.removeChild(this);
        }
        parent = null;
        sortedProbes.clear();
    }

    /**
     * Children.
     *
     * @return the probe list[]
     */
    public ProbeList[] children() {
        return children.toArray(new ProbeList[0]);
    }

    /**
     * Removes the child.
     *
     * @param child the child
     */
    private void removeChild(ProbeList child) {
        /**
         * Should only be called from within the ProbeList class as part of the
         * public delete() method.  Breaks a node away from the rest of the tree
         */
        children.remove(child);
    }

    /**
     * Adds the child.
     *
     * @param child the child
     */
    private void addChild(ProbeList child) {
        /**
         * Should only be called from within the ProbeList class as part of the
         * constructor. Creates a two way link between nodes and their parents
         */
        children.add(child);
    }

    /**
     * Adds the probe.
     *
     * @param p the p
     */
    public synchronized void addProbe(Probe p) {
        sortedProbes.add(p);
        isSorted = false;

    }

    /**
     * Sets the name.
     *
     * @param s the new name
     */
    public void setName(String s) {
        this.name = s;
        probeListRenamed(this);
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

    private synchronized void sortProbes() {
        if (!isSorted) {
            Collections.sort(sortedProbes);
            isSorted = true;
        }

        try {
            // Do a sanity check to ensure we don't have any duplication here
            for (int i = 1, len = sortedProbes.size(); i < len; i++) {
                if (sortedProbes.elementAt(i) == sortedProbes.elementAt(i - 1)) {
                    throw new Exception("Duplicate probe "
                            + sortedProbes.elementAt(i) + " and "
                            + sortedProbes.elementAt(i - 1) + " in " + name());
                }
                if (sortedProbes.elementAt(i).compareTo(
                        sortedProbes.elementAt(i - 1)) == 0) {
                    throw new Exception("Unsortable probe "
                            + sortedProbes.elementAt(i) + " and "
                            + sortedProbes.elementAt(i - 1) + " in " + name());
                }
            }
        } catch (Exception ex) {
            // There are duplicate probes and we need to remove them.
            Vector<Probe> dedup = new Vector<Probe>();
            Probe lastProbe = null;
            Enumeration<Probe> en = sortedProbes.elements();
            while (en.hasMoreElements()) {
                Probe p = en.nextElement();
                if (p == lastProbe)
                    continue;
                dedup.add(p);
                lastProbe = p;
            }

            sortedProbes = dedup;
        }

        sortedProbes.trimToSize();
    }

    /**
     * Gets the all probes.
     *
     * @return the all probes
     */
    public Probe[] getAllProbes() {
        if (!isSorted) {
            sortProbes();
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

        Probe[] returnArray = new Probe[sortedProbes.size()];
        Enumeration<Probe> en = sortedProbes.elements();

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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name + " (" + sortedProbes.size() + ")";
    }

    // We use the following methods to notify up the tree about
    // changes which have occurred somewhere in the tree. They
    // are private versions of the methods in the ProbeSetChangeListener

    /**
     * Probe list added.
     *
     * @param l the l
     */
    protected void probeListAdded(ProbeList l) {
        parent.probeListAdded(l);
    }

    /**
     * Probe list removed.
     *
     * @param l the l
     */
    protected void probeListRemoved(ProbeList l) {
        parent.probeListRemoved(l);
    }

    /**
     * Probe list renamed.
     *
     * @param l the l
     */
    protected void probeListRenamed(ProbeList l) {
        parent.probeListRenamed(l);
    }

}
