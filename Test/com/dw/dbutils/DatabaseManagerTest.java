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

package com.dw.dbutils;

import com.xl.database.DatabaseManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Administrator on 2014/11/15.
 */
public class DatabaseManagerTest {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        databaseManager.connectDatabase("127.0.0.1", "3306", "root", "root");
        databaseManager.useDatabase(DatabaseManager.DNA_RNA_DATABASE_NAME);
        List<String> tableNames = databaseManager.getCurrentTables(DatabaseManager.DENOVO_DATABASE_NAME);
        System.out.println(tableNames);
    }
}
