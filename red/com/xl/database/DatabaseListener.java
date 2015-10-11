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

package com.xl.database;

/**
 * Created by Xing Li on 2014/11/15.
 * <p/>
 * The listener interface for receiving database information events. The class that is interested in processing a database event implements this interface, and
 * the object created with that class is registered with a component using the component's <code>addDatabaseListener<code> method. When the database event
 * occurs, that object's appropriate method is invoked.
 */
public interface DatabaseListener {
    /**
     * No matter database or sample is changed, the method is called to tell RED to handle this event. We need to synchronize the site list in a data set or
     * data group with the site in database.
     *
     * @param databaseName Database name which is being used or changed to.
     * @param sampleName   Sample name which is being used or changed to.
     */
    void databaseChanged(String databaseName, String sampleName);

    /**
     * When database is connected, we need to tell RED to handle this event, such as enabling buttons in toolbar, enabling data importing, synchronizing data
     * between database and data store, etc.
     */
    void databaseConnected();

}
