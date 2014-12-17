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

package com.xl.interfaces;

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataStore;

/**
 * The listener interface for receiving DataStoreChanged events. The class that is interested in processing a DataStoreChanged event implements this interface, and the
 * object created with that class is registered with a component using the component's <code>addDataStoreChangedListener<code> method. When the DataStoreChanged event
 * occurs, that object's appropriate method is invoked.
 */
public interface DataStoreChangedListener {

    /**
     * Data set added.
     *
     * @param d the d
     */
    public void dataStoreAdded(DataStore d);

    /**
     * Data set removed.
     *
     * @param d the d
     */
    public void dataStoreRemoved(DataStore d);

    /**
     * Data set renamed.
     *
     * @param d the d
     */
    public void dataStoreRenamed(DataStore d);

    /**
     * Data group samples changed.
     *
     * @param g the g
     */
    public void dataGroupSamplesChanged(DataGroup g);

}
