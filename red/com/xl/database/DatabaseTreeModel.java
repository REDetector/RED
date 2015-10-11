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

import com.xl.exception.UnknownParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xing Li on 2014/11/16.
 * <p/>
 * The Class DatabaseTreeModel provides a tree model which describes the RNA editing detected mode and samples in database.
 */
public class DatabaseTreeModel implements TreeModel {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseTreeModel.class);
    /**
     * The root of tree model.
     */
    private Object root = "Samples in MySQL Database";
    /**
     * We have two modes which are able to be chosen: DENOVO_MODE and DNA_RNA_MODE
     */
    private String[] modes = null;
    /**
     * Samples from DENOVO_MODE
     */
    private List<TableNode> denovoTableNodes = null;
    /**
     * Samples from DNA_RNA_MODE
     */
    private List<TableNode> dnarnaTableNodes = null;

    public DatabaseTreeModel() {
        modes = new String[]{DatabaseManager.DENOVO_MODE_DATABASE_NAME, DatabaseManager.DNA_RNA_MODE_DATABASE_NAME};
        denovoTableNodes = new ArrayList<TableNode>();
        List<String> denovoTables;
        denovoTables = DatabaseManager.getInstance().getCurrentTables(DatabaseManager.DENOVO_MODE_DATABASE_NAME);
        for (String denovoTable : denovoTables) {
            // We only detect RNA VCF file and exclude all filters relative to this sample.
            if (denovoTable.contains(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME) && !denovoTable.contains(DatabaseManager.FILTER)) {
                denovoTableNodes.add(new TableNode(denovoTable));
            }
        }

        dnarnaTableNodes = new ArrayList<TableNode>();
        List<String> dnarnaTables = DatabaseManager.getInstance().getCurrentTables(DatabaseManager.DNA_RNA_MODE_DATABASE_NAME);
        for (String dnarnaTable : dnarnaTables) {
            // We only detect RNA VCF file and exclude all filters relative to this sample.
            if (dnarnaTable.contains(DatabaseManager.RNA_VCF_RESULT_TABLE_NAME) && !dnarnaTable.contains(DatabaseManager.FILTER)) {
                dnarnaTableNodes.add(new TableNode(dnarnaTable));
            }
        }
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent.equals(root)) {
            return modes[index];
        } else if (parent.equals(modes[0])) {
            return denovoTableNodes.get(index);
        } else if (parent.equals(modes[1])) {
            return dnarnaTableNodes.get(index);
        } else {
            logger.error("Object '" + parent + "' can not be recognized by our program in index " + index, new UnknownParameterException());
            return modes[index];
        }
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent.equals(root)) {
            return modes.length;
        } else if (parent.equals(modes[0])) {
            return denovoTableNodes.size();
        } else if (parent.equals(modes[1])) {
            return dnarnaTableNodes.size();
        } else {
            logger.error("Could not get child count from parent '" + parent + "'", new UnknownParameterException());
            return 0;
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        return !node.equals(modes[0]) && !node.equals(modes[1]) && !node.equals(root);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        logger.warn("Value for path changed called on node " + newValue);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent.equals(root)) {
            if (child.equals(modes[0])) {
                return 0;
            } else {
                return 1;
            }
        } else if (parent.equals(modes[0])) {
            if (child instanceof TableNode) {
                return denovoTableNodes.indexOf(child);
            }
        } else if (parent.equals(modes[1])) {
            if (child instanceof TableNode) {
                return dnarnaTableNodes.indexOf(child);
            }
        } else {
            logger.error("Could not get the index of child '" + child + "'from parent '" + parent + "'", new UnknownParameterException());
        }
        return 0;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {

    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {

    }
}
