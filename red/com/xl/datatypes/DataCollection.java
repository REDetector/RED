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

package com.xl.datatypes;

import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.sites.SiteList;
import com.xl.exception.RedException;
import com.xl.interfaces.ActiveDataChangedListener;
import com.xl.interfaces.DataStoreChangedListener;
import com.xl.main.RedApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The Class DataCollection is the main data storage object through which all of the data in a project can be accessed.
 */
public class DataCollection {
    private static final Logger logger = LoggerFactory.getLogger(DataCollection.class);

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
     * The active site list.
     */
    private SiteList activeSiteList = null;

    /**
     * The data change listeners.
     */
    private Vector<DataStoreChangedListener> dataStoreChangedListeners = new Vector<DataStoreChangedListener>();

    /**
     * The active data store change listeners.
     */
    private Vector<ActiveDataChangedListener> activeDataChangedListeners = new Vector<ActiveDataChangedListener>();

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
            logger.error("Genome can't be null when creating a data collection", new NullPointerException());
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
     * Adds the data set.
     *
     * @param dataStore the data
     */
    public void addDataStore(DataStore dataStore) {
        if (dataStore instanceof DataSet) {
            dataSets.add((DataSet) dataStore);
        } else {
            dataGroups.add((DataGroup) dataStore);
        }
        dataStore.setCollection(this);
        Enumeration<DataStoreChangedListener> e = dataStoreChangedListeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().dataStoreAdded(dataStore);
        }
    }

    /**
     * Removes the data groups.
     *
     * @param dataStore the data store
     */
    public void removeDataStore(DataStore dataStore) {
        if (dataStore instanceof DataSet) {
            Enumeration<DataGroup> e = dataGroups.elements();
            while (e.hasMoreElements()) {
                DataGroup g = e.nextElement();
                if (g.containsDataSet((DataSet) dataStore)) {
                    g.removeDataSet((DataSet) dataStore);
                }
            }
            dataSets.remove(dataStore);
        } else if (dataStore instanceof DataGroup) {
            dataGroups.remove(dataStore);
        }
        dataStore.setCollection(null);

        Enumeration<DataStoreChangedListener> e = dataStoreChangedListeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().dataStoreRemoved(dataStore);
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
        System.arraycopy(groups, 0, stores, 0, groups.length);
        System.arraycopy(sets, 0, stores, groups.length, groups.length + sets.length);
        return stores;
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
        } else if (RedApplication.getInstance().drawnDataStores().length == 1) {
            return RedApplication.getInstance().drawnDataStores()[0];
        } else {
            return null;
        }
    }

    /**
     * Get current active site list. Only one active list can be shown on the data viewer tree.
     *
     * @return the active site list
     */
    public SiteList getActiveSiteList() {
        if (activeSiteList == null || activeDataStore == null || activeDataStore.siteSet() == null) {
            return null;
        }
        if (activeSiteList != null) {
            return activeSiteList;
        } else {
            return activeDataStore.siteSet().getActiveList();
        }
    }

    /**
     * A top way to tell all ActiveDataChangedListeners the changed data store or site list.
     *
     * @param dataStore The active data store
     * @param siteList  The active site list.
     * @throws RedException If there is wrong data store or site list, then throw this exception.
     */
    public void setActiveData(DataStore dataStore, SiteList siteList) throws RedException {
        if (dataStore == null) {
            activeDataStore = null;
            activeSiteList = null;
        } else if ((dataStore instanceof DataSet && dataSets.contains(dataStore)) || (dataStore instanceof DataGroup && dataGroups.contains(dataStore))) {
            activeDataStore = dataStore;
            if (dataStore.siteSet() == null) {
                activeSiteList = null;
            } else {
                List<SiteList> siteLists = new ArrayList<SiteList>(Arrays.asList(dataStore.siteSet().getAllSiteLists()));
                if (siteList == null) {
                    activeSiteList = null;
                } else if (siteLists.contains(siteList)) {
                    activeSiteList = siteList;
                    dataStore.siteSet().setActiveList(siteList);
                } else {
                    throw new RedException("Data store '" + dataStore.name() + "' does not have this site list " + siteList.getListName());
                }
            }

            Enumeration<ActiveDataChangedListener> e = activeDataChangedListeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().activeDataChanged(dataStore, siteList);
            }
        } else {
            throw new RedException("Data store " + dataStore.name() + " could not be found in the data collection");
        }

    }

    /**
     * Adds the data change listener.
     *
     * @param l the l
     */
    public void addDataChangeListener(DataStoreChangedListener l) {
        if (l != null && !dataStoreChangedListeners.contains(l)) {
            dataStoreChangedListeners.add(l);
        }
    }

    /**
     * Removes the data change listener.
     *
     * @param l the l
     */
    public void removeDataChangeListener(DataStoreChangedListener l) {
        if (l != null && dataStoreChangedListeners.contains(l)) {
            dataStoreChangedListeners.remove(l);
        }
    }

    /**
     * Adds the data change listener.
     *
     * @param l the l
     */
    public void addActiveDataListener(ActiveDataChangedListener l) {
        if (l != null && !activeDataChangedListeners.contains(l)) {
            activeDataChangedListeners.add(l);
        }
    }

    /**
     * Removes the data change listener.
     *
     * @param l the l
     */
    public void removeActiveDataListener(ActiveDataChangedListener l) {
        if (l != null && activeDataChangedListeners.contains(l)) {
            activeDataChangedListeners.remove(l);
        }
    }

    /**
     * Data group renamed.
     *
     * @param d the new active data store name
     */
    public void dataStoreRenamed(DataStore d) {
        Enumeration<DataStoreChangedListener> e = dataStoreChangedListeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().dataStoreRenamed(d);
        }
    }

    /**
     * Data group samples changed.
     *
     * @param g the g
     */
    public void dataGroupSamplesChanged(DataGroup g) {
        Enumeration<DataStoreChangedListener> e = dataStoreChangedListeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().dataGroupSamplesChanged(g);
        }
    }

}
