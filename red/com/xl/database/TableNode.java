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

import com.xl.utils.NameRetriever;

/**
 * Created by Xing Li on 2014/11/16.
 * <p/>
 * A table node which uses in DatabaseTreeMode.
 */
public class TableNode {
    /**
     * The table name.
     */
    private String tableName;
    /**
     * The sample name.
     */
    private String sampleName;

    public TableNode(String tableName) {
        this.tableName = tableName;
        sampleName = NameRetriever.getSampleName(tableName);
    }

    public String getTableName() {
        return tableName;
    }

    public String getSampleName() {
        return sampleName;
    }

    @Override
    public String toString() {
        return sampleName;
    }
}
