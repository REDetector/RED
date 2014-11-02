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

package com.xl.thread;

import com.dw.dbutils.DatabaseManager;
import com.xl.filter.AbstractSiteFilter;

/**
 * Created by Administrator on 2014/10/20.
 */
public class ThreadCountRow implements Runnable {
    private String tableName;

    private AbstractSiteFilter abstractSiteFilter;

    public ThreadCountRow(AbstractSiteFilter abstractSiteFilter, String tableName) {
        this.abstractSiteFilter = abstractSiteFilter;
        this.tableName = tableName;
    }

    @Override
    public void run() {
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        int totalCount = 0;
        int currentCount;
        while (true) {
            currentCount = databaseManager.calRowCount(tableName);
            if (currentCount == totalCount && currentCount != 0) {
                break;
            } else {
                totalCount = currentCount;
                abstractSiteFilter.progressUpdated("Filtering out " + totalCount + " RNA-editing sites from " + abstractSiteFilter.parentTable, 0, 0);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
