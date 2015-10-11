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

package com.xl.utils;

import com.xl.datatypes.DataStore;
import com.xl.main.RedApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Created by Xing Li on 2014/7/24.
 * <p/>
 * Select the default data store in a JList.
 */
public class ListDefaultSelector {
    private static final Logger logger = LoggerFactory.getLogger(ListDefaultSelector.class);

    public static DataStore selectDefaultStore(JList list) {
        DataStore[] stores = RedApplication.getInstance().drawnDataStores();
        DataStore activeDataStore = RedApplication.getInstance().dataCollection().getActiveDataStore();
        for (int i = 0, len = stores.length; i < len; i++) {
            if (stores[i] == activeDataStore) {
                list.setSelectedIndex(i);
            }
        }
        return activeDataStore;
    }
}
