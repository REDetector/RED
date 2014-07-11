package com.xl.interfaces;

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;

/**
 * The listener interface for receiving dataChange events.
 * The class that is interested in processing a dataChange
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addDataChangeListener<code> method. When
 * the dataChange event occurs, that object's appropriate
 * method is invoked.
 */
public interface DataChangeListener {

    /**
     * Data set added.
     *
     * @param d the d
     */
    public void dataSetAdded(DataSet d);

    /**
     * Data set removed.
     *
     * @param d the d
     */
    public void dataSetsRemoved(DataSet[] d);

    /**
     * Data group added.
     *
     * @param g the g
     */
    public void dataGroupAdded(DataGroup g);

    /**
     * Data group removed.
     *
     * @param g the g
     */
    public void dataGroupsRemoved(DataGroup[] g);

    /**
     * Data set renamed.
     *
     * @param d the d
     */
    public void dataSetRenamed(DataSet d);

    /**
     * Data group renamed.
     *
     * @param g the g
     */
    public void dataGroupRenamed(DataGroup g);

    /**
     * Data group samples changed.
     *
     * @param g the g
     */
    public void dataGroupSamplesChanged(DataGroup g);

    /**
     * Probe set replaced.
     *
     * @param p the p
     */
    public void probeSetReplaced(ProbeSet p);

    public void activeDataStoreChanged(DataStore s);

    public void activeProbeListChanged(ProbeList l);

}
