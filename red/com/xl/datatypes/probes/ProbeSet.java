package com.xl.datatypes.probes;

import com.xl.datatypes.DataCollection;
import com.xl.exception.REDException;

import java.util.Enumeration;
import java.util.Vector;

/**
 * The Class ProbeSet is a special instance of probe list which represents a
 * full set of probes as created by a probe generator. All probe lists are
 * therefore subsets of the containing probeset.
 */
public class ProbeSet extends ProbeList {

    /**
     * The active list.
     */
    private ProbeList activeList = null;

    /**
     * The listeners.
     */
    private Vector<ProbeSetChangeListener> listeners = new Vector<ProbeSetChangeListener>();

    /**
     * The expected total count.
     */
    private int expectedTotalCount = 0;

    /**
     * The containing data collection
     */
    private DataCollection collection = null;

    /**
     * Instantiates a new probe set.
     *
     * @param description the description
     * @param probes      the probes
     */
    public ProbeSet(String description, Probe[] probes) {
        super(null, "All Probes", description);
        setProbes(probes);
    }

    /**
     * Instantiates a new probe set.
     *
     * @param description  the description
     * @param expectedSize the expected size
     */
    public ProbeSet(String description, int expectedSize) {
        /**
         * This constructor should only be called by the SeqMonkParser since it
         * relies on the correct number of probes eventually being added.
         * Ideally we'd go back to sort out this requirement by changing the
         * SeqMonk file format, but for now we're stuck with this work round
         */
        super(null, "All Probes", description);
        expectedTotalCount = expectedSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.ac.babraham.SeqMonk.DataTypes.Probes.ProbeList#addProbe(uk.ac.babraham
     * .SeqMonk.DataTypes.Probes.Probe, java.lang.Double)
     */
    public void addProbe(Probe p) {

        /**
         * This method is only used by the SeqMonk parser. All other probe
         * generators add their probes in bulk using the setProbes method which
         * is more efficient.
         */

        // Call the super method so we can still be treated like a
        // normal probe list
        super.addProbe(p);
    }

    public void setCollection(DataCollection collection) {
        this.collection = collection;
    }

    /**
     * Sets the probes.
     *
     * @param probes the new probes
     */
    private void setProbes(Probe[] probes) {

        expectedTotalCount = probes.length;

        for (Probe probe : probes) {
            addProbe(probe);
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
    public ProbeList getActiveList() {
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
    public void setActiveList(ProbeList list) throws REDException {

        if (list == null) {
            activeList = null;
            return;
        }
        activeList = list;

        if (collection != null) {
            collection.activeProbeListChanged(list);
        }
    }

    /**
     * Adds the probe set change listener.
     *
     * @param l the l
     */
    public void addProbeSetChangeListener(ProbeSetChangeListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Removes the probe set change listener.
     *
     * @param l the l
     */
    public void removeProbeSetChangeListener(ProbeSetChangeListener l) {
        if (l != null && listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.babraham.SeqMonk.DataTypes.Probes.ProbeList#delete()
     */
    synchronized public void delete() {
        // This is overridden from ProbeList and is called as the
        // list is removed.
        super.delete();
        // Now we can get rid of our list of listeners
        listeners.removeAllElements();
        // Drop the link to the collection
        collection = null;
    }

    // These methods propogate up through the tree of probe lists
    // to here where we override them to pass the messages on to
    // any listeners we have
    /*
     * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.babraham.SeqMonk.DataTypes.Probes.ProbeList#probeListAdded(uk.ac
	 * .babraham.SeqMonk.DataTypes.Probes.ProbeList)
	 */
    protected void probeListAdded(ProbeList l) {
        if (listeners == null)
            return;
        Enumeration<ProbeSetChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            ProbeSetChangeListener pl = e.nextElement();
            pl.probeListAdded(l);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.ac.babraham.SeqMonk.DataTypes.Probes.ProbeList#probeListRemoved(uk
     * .ac.babraham.SeqMonk.DataTypes.Probes.ProbeList)
     */
    protected void probeListRemoved(ProbeList l) {
        Enumeration<ProbeSetChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().probeListRemoved(l);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * uk.ac.babraham.SeqMonk.DataTypes.Probes.ProbeList#probeListRenamed(uk
     * .ac.babraham.SeqMonk.DataTypes.Probes.ProbeList)
     */
    protected void probeListRenamed(ProbeList l) {
        Enumeration<ProbeSetChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().probeListRenamed(l);
        }
    }

}
