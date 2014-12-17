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

import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.SiteList;

/**
 * Created by Xing Li on 2014/11/17.
 * <p/>
 * The listener interface for receiving ActiveDataChanged events. The class that is interested in processing a ActiveDataChanged event implements this
 * interface, and the object created with that class is registered with a component using the component's <code>addActiveDataChangedListener<code> method.
 * When the ActiveDataChanged event occurs, that object's appropriate method is invoked.
 */
public interface ActiveDataChangedListener {
    /**
     * When the active data is changed, then we tell all to make some changes at the same time.
     *
     * @param dataStore The data store to be changed to.
     * @param siteList  The site list to be changed to.
     */
    public void activeDataChanged(DataStore dataStore, SiteList siteList);
}
