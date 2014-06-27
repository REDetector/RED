package com.xl.datatypes;

import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.exception.REDException;
import com.xl.interfaces.DataChangeListener;
import com.xl.main.REDApplication;
import com.xl.utils.MessageUtils;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The Class DataCollection is the main data storage object through
 * which all of the data in a project can be accessed.
 */
public class DataCollection {

    /**
     * The probe set.
     */
    private ProbeSet probeSet = null;

    /**
     * The data sets.
     */
    private Vector<DataSet> dataSets = new Vector<DataSet>();

    /**
     * The data groups.
     */
    private Vector<DataGroup> dataGroups = new Vector<DataGroup>();

    /**
     * The active data store.
     */
    private DataStore activeDataStore = null;

    /**
     * The data change listeners.
     */
    private Vector<DataChangeListener> listeners = new Vector<DataChangeListener>();

    /**
     * The genome.
     */
    private Genome genome;

    /**
     * Instantiates a new data collection.
     *
     * @param g the g
     */
    public DataCollection(Genome g) {
        if (g == null) {
            throw new NullPointerException("Genome can't be null when creating a data collection");
        }
        this.genome = g;
    }

    /**
     * Genome.
     *
     * @return the genome
     */
    public Genome genome() {
        return genome;
    }

    /**
     * Gets the data set.
     *
     * @param position the position
     * @return the data set
     */
    public DataSet getDataSet(int position) {

        if (position >= 0 && position < dataSets.size()) {
            return dataSets.elementAt(position);
        }
        return null;
    }

    /**
     * Gets the data group.
     *
     * @param position the position
     * @return the data group
     */
    public DataGroup getDataGroup(int position) {
        if (position >= 0 && position < dataGroups.size()) {
            return dataGroups.elementAt(position);
        }
        return null;
    }

    /**
     * Checks if is quantitated.
     *
     * @return true, if is quantitated
     */
    public boolean isQuantitated() {

        if (probeSet == null || probeSet.getAllProbes().length == 0) return false;

        DataStore[] stores = getAllDataStores();

        for (int i = 0; i < stores.length; i++) {
            if (stores[i].isQuantitated()) return true;
        }

        return false;
    }

    /**
     * Sets the probe set.
     *
     * @param newProbeSet the new probe set
     */
    public void setProbeSet(ProbeSet newProbeSet) {
        if (probeSet != null) {
            probeSet.delete();
        }
        probeSet = newProbeSet;
        probeSet.setCollection(this);
        Enumeration<DataChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().probeSetReplaced(probeSet);
        }

        // We need to tell all of the DataSets and Groups
        // about the new probe set
        DataStore[] stores = getAllDataStores();
        for (int i = 0; i < stores.length; i++) {
            stores[i].probeSetReplaced(newProbeSet);
        }
    }

    /**
     * Probe set.
     *
     * @return the probe set
     */
    public ProbeSet probeSet() {
        return probeSet;
    }

    /**
     * Adds the data set.
     *
     * @param data the data
     */
    public void addDataSet(DataSet data) {
        dataSets.add(data);
        data.setCollection(this);
        MessageUtils.showInfo(DataCollection.class, "addDataSet (DataSet data)" + dataSets.size());

        // We need to let this dataset know about the
        // current probset.
        data.probeSetReplaced(probeSet());

        Enumeration<DataChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().dataSetAdded(data);
        }
    }

    /**
     * Adds the data group.
     *
     * @param group the group
     */
    public void addDataGroup(DataGroup group) {
        dataGroups.add(group);
        group.setCollection(this);

        // We need to let this datagroup know about the
        // current probset.
        group.probeSetReplaced(probeSet());

        Enumeration<DataChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().dataGroupAdded(group);
        }
    }

    /**
     * Removes the data groups.
     *
     * @param groups the groups
     */
    public void removeDataGroups(DataGroup[] groups) {

        // We inform the listeners first to give
        // a chance for the tree to update.
        Enumeration<DataChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().dataGroupsRemoved(groups);
        }

        for (int g = 0; g < groups.length; g++) {
            dataGroups.remove(groups[g]);
            groups[g].setCollection(null);
        }

    }

    /**
     * Removes the data set.
     *
     * @param data the data
     */
    public void removeDataSets(DataSet[] data) {

        for (int d = 0; d < data.length; d++) {

            Enumeration<DataGroup> e = dataGroups.elements();
            while (e.hasMoreElements()) {
                DataGroup g = e.nextElement();

                if (g.containsDataSet(data[d])) {
                    g.removeDataSet(data[d]);
                }
            }
        }

        // Notify listeners before actually removing to allow the
        // tree to pick up the changes correctly
        Enumeration<DataChangeListener> e3 = listeners.elements();
        while (e3.hasMoreElements()) {
            (e3.nextElement()).dataSetsRemoved(data);
        }

        for (int d = 0; d < data.length; d++) {
            dataSets.remove(data[d]);
            data[d].setCollection(null);
        }

    }

    /**
     * Gets the all data groups.
     *
     * @return the all data groups
     */
    public DataGroup[] getAllDataGroups() {
        DataGroup[] groups = dataGroups.toArray(new DataGroup[0]);
        Arrays.sort(groups);
        return groups;
    }

    /**
     * Gets the all data sets.
     *
     * @return the all data sets
     */
    public DataSet[] getAllDataSets() {
        DataSet[] sets = dataSets.toArray(new DataSet[0]);
        Arrays.sort(sets);
        return sets;
    }

    /**
     * Gets the all data stores.
     *
     * @return the all data stores
     */
    public DataStore[] getAllDataStores() {
        DataSet[] sets = getAllDataSets();
        DataGroup[] groups = getAllDataGroups();

        DataStore[] stores = new DataStore[sets.length + groups.length];

        for (int i = 0; i < groups.length; i++) {
            stores[i] = groups[i];
        }
        for (int i = 0; i < sets.length; i++) {
            stores[groups.length + i] = sets[i];
        }
        return stores;
    }

    public void activeProbeListChanged(ProbeList list) {
        Enumeration<DataChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().activeProbeListChanged(list);
        }
    }

    /**
     * Sets the active data store.
     *
     * @param d the new active data store
     * @throws REDException the seq monk exception
     */
    public void setActiveDataStore(DataStore d) throws REDException {
        if (d == null || dataSets.contains(d) || dataGroups.contains(d)) {
            activeDataStore = d;

            Enumeration<DataChangeListener> e = listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().activeDataStoreChanged(d);
            }

        } else {
            throw new REDException("Data store " + d.name() + " could not be found in the data collection");
        }
    }

    /**
     * Gets the active data store.
     *
     * @return the active data store
     */
    public DataStore getActiveDataStore() {
        if (activeDataStore != null) {
            return activeDataStore;
        } else if (getAllDataStores().length == 1) {
            return getAllDataStores()[0];
        } else if (REDApplication.getInstance().drawnDataStores().length == 1) {
            return REDApplication.getInstance().drawnDataStores()[0];
        } else {
            return null;
        }
    }

    /**
     * Adds the data change listener.
     *
     * @param l the l
     */
    public void addDataChangeListener(DataChangeListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Removes the data change listener.
     *
     * @param l the l
     */
    public void removeDataChangeListener(DataChangeListener l) {
        if (l != null && listeners.contains(l)) {
            listeners.remove(l);
        }
    }


    /**
     * Data group renamed.
     *
     * @param g the g
     */
    public void dataGroupRenamed(DataGroup g) {
        Enumeration<DataChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().dataGroupRenamed(g);
        }
    }

    /**
     * Data set renamed.
     *
     * @param s the s
     */
    public void dataSetRenamed(DataSet s) {
        Enumeration<DataChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().dataSetRenamed(s);
        }
    }

    /**
     * Data group samples changed.
     *
     * @param g the g
     */
    public void dataGroupSamplesChanged(DataGroup g) {
        Enumeration<DataChangeListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().dataGroupSamplesChanged(g);
        }
    }

}
