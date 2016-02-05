/*
 * RED: RNA Editing Detector Copyright (C) <2014> <Xing Li>
 *
 * RED is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * RED is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.xl.database;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Xing Li on 2014/11/16.
 * <p/>
 * The Class DatabaseTreeModel provides a tree model which describes the RNA editing detected mode and samples in
 * database.
 */
public class DatabaseTreeModel {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseTreeModel.class);

    private final Set<String> allSamples;

    /**
     * We have two modes which are able to be chosen: DENOVO_MODE and DNA_RNA_MODE
     */
    private final DefaultMutableTreeNode root;

    public DatabaseTreeModel() {
        List<String> databaseNames = DatabaseManager.getInstance().getAllDatabase();
        root = new DefaultMutableTreeNode("Samples in MySQL Database");
        allSamples = new HashSet<String>();
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        for (String databaseName : databaseNames) {
            DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode(databaseName);
            treeModel.insertNodeInto(parentNode, root, root.getChildCount());
            List<String> tables = DatabaseManager.getInstance().getCurrentTables(databaseName);
            for (String table : tables) {
                // We only detect RNA VCF file and exclude all filters relative to this sample.
                if (table.contains(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME)
                    && !table.contains(DatabaseManager.FILTER)) {
                    TableNode tableNode = new TableNode(table);
                    DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(tableNode);
                    treeModel.insertNodeInto(leafNode, parentNode, parentNode.getChildCount());
                    allSamples.add(tableNode.getSampleName());
                }

            }
        }
    }

    public DefaultMutableTreeNode getRootTreeNode() {
        return root;
    }

    public Set<String> getAllSamples() {
        return allSamples;
    }
}
