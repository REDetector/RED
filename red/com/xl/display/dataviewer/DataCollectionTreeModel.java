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

package com.xl.display.dataviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.sites.SiteList;
import com.xl.interfaces.AnnotationCollectionListener;
import com.xl.interfaces.DataStoreChangeListener;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The Class DataCollectionTreeModel provides a tree model which describes the data sets, data groups and annotation sets in a data collection
 */
public class DataCollectionTreeModel implements TreeModel, DataStoreChangeListener, AnnotationCollectionListener {

    /**
     * The collection.
     */
    private DataCollection collection;

    /**
     * The listeners.
     */
    private Vector<TreeModelListener> listeners = new Vector<TreeModelListener>();

    /**
     * The root node.
     */
    private FolderNode rootNode;

    /**
     * The annotation node.
     */
    private FolderNode annotationNode;

    /**
     * The data set node.
     */
    private FolderNode dataSetNode;

    /**
     * The data group node.
     */
    private FolderNode dataGroupNode;

    /**
     * Instantiates a new data collection tree model.
     *
     * @param collection the collection
     */
    public DataCollectionTreeModel(DataCollection collection) {
        if (collection == null) {
            throw new NullPointerException("Data collection can not be null.");
        }
        this.collection = collection;
        collection.addDataChangeListener(this);
        collection.genome().getAnnotationCollection().addAnnotationCollectionListener(this);
        rootNode = new FolderNode(collection.genome().toString());
        annotationNode = new FolderNode("Annotation Sets");
        dataSetNode = new FolderNode("Data Sets");
        dataGroupNode = new FolderNode("Data Groups");
    }

    public void addTreeModelListener(TreeModelListener tl) {
        if (tl != null && !listeners.contains(tl)) {
            listeners.add(tl);
        }
    }

    public void removeTreeModelListener(TreeModelListener tl) {
        if (tl != null && listeners.contains(tl)) {
            listeners.remove(tl);
        }
    }

    @Override
    public Object getChild(Object node, int index) {
        if (node.equals(rootNode)) {
            switch (index) {
                case 0:
                    return annotationNode;
                case 1:
                    return dataSetNode;
                case 2:
                    return dataGroupNode;
            }
        } else if (node.equals(annotationNode)) {
            return collection.genome().getAnnotationCollection().annotationSets()[index];
        } else if (node.equals(dataSetNode)) {
            return collection.getAllDataSets()[index];
        } else if (node.equals(dataGroupNode)) {
            return collection.getAllDataGroups()[index];
        }
        throw new NullPointerException("Null child from " + node + " at index " + index);
    }

    @Override
    public int getChildCount(Object node) {
        if (node.equals(annotationNode)) {
            return collection.genome().getAnnotationCollection().annotationSets().length;
        } else if (node.equals(rootNode)) {
            return 3; // Annotation sets, DataSets, DataGroups
        } else if (node.equals(dataSetNode)) {
            return collection.getAllDataSets().length;
        } else if (node.equals(dataGroupNode)) {
            return collection.getAllDataGroups().length;
        } else {
            return 0;
        }
    }

    @Override
    public int getIndexOfChild(Object node, Object child) {
        if (node instanceof SiteList) {
            SiteList[] children = ((SiteList) node).children();
            for (int i = 0; i < children.length; i++) {
                if (children[i].equals(child)) {
                    return i;
                }
            }
        } else if (node.equals(annotationNode)) {
            AnnotationSet[] sets = collection.genome().getAnnotationCollection().annotationSets();
            for (int s = 0; s < sets.length; s++) {
                if (sets[s] == child) return s;
            }
        } else if (node.equals(rootNode)) {
            if (child.equals(annotationNode)) return 0;
            if (child.equals(dataSetNode)) return 1;
            if (child.equals(dataGroupNode)) return 2;
        } else if (node.equals(dataSetNode)) {
            DataSet[] sets = collection.getAllDataSets();
            for (int i = 0; i < sets.length; i++) {
                if (sets[i] == child) return i;
            }
        } else if (node.equals(dataGroupNode)) {
            DataGroup[] groups = collection.getAllDataGroups();
            for (int i = 0; i < groups.length; i++) {
                if (groups[i] == child) return i;
            }
        }
        System.err.println("Couldn't find valid index for " + node + " and " + child);
        return 0;

    }

    @Override
    public Object getRoot() {
        return rootNode;
    }

    @Override
    public boolean isLeaf(Object node) {
        return !(node instanceof FolderNode);
    }

    @Override
    public void valueForPathChanged(TreePath tp, Object node) {
        // This only applies to editable trees - which this isn't.
        System.out.println("Value for path changed called on node " + node);
    }

    @Override
    public void dataStoreAdded(DataStore d) {
        TreeModelEvent me = null;
        if (d instanceof DataSet) {
            me = new TreeModelEvent(d, getPathToRoot(dataSetNode), new int[]{getIndexOfChild(dataSetNode, d)}, new DataSet[]{(DataSet) d});
        } else if (d instanceof DataGroup) {
            me = new TreeModelEvent(d, getPathToRoot(dataGroupNode), new int[]{getIndexOfChild(dataGroupNode, d)}, new DataGroup[]{(DataGroup) d});
        }
        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesInserted(me);
        }
    }

    @Override
    public void dataStoreRemoved(DataStore d) {
        TreeModelEvent me = null;
        if (d instanceof DataSet) {
            DataSet set = (DataSet) d;
            me = new TreeModelEvent(d, getPathToRoot(dataSetNode), new int[]{getIndexOfChild(dataSetNode, d)}, new DataSet[]{set});
        } else if (d instanceof DataGroup) {
            DataGroup group = (DataGroup) d;
            me = new TreeModelEvent(d, getPathToRoot(dataGroupNode), new int[]{getIndexOfChild(dataGroupNode, d)}, new DataGroup[]{group});
        }

        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesRemoved(me);
        }
    }

    @Override
    public void dataStoreRenamed(DataStore d) {
        TreeModelEvent me = new TreeModelEvent(d, getPathToRoot(d));
        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesChanged(me);
        }

        // We also need to let the tree know that the structure may have changed since the new name may sort differently and therefore appear in a different
        // position.
        if (d instanceof DataSet) {
            me = new TreeModelEvent(dataSetNode, getPathToRoot(dataSetNode));
        } else {
            me = new TreeModelEvent(dataGroupNode, getPathToRoot(dataGroupNode));
        }
        e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeStructureChanged(me);
        }
    }

    @Override
    public void dataGroupSamplesChanged(DataGroup g) {
        dataStoreRenamed(g);
    }

    @Override
    public void annotationSetAdded(AnnotationSet annotationSets) {
        TreeModelEvent me = new TreeModelEvent(annotationSets, getPathToRoot(annotationNode), new int[]{getIndexOfChild(annotationNode, annotationSets)},
                new AnnotationSet[]{annotationSets});

        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesInserted(me);
        }
    }

    @Override
    public void annotationSetRemoved(AnnotationSet annotationSet) {
        TreeModelEvent me = new TreeModelEvent(annotationSet, getPathToRoot(annotationNode), new int[]{getIndexOfChild(annotationNode, annotationSet)}, new AnnotationSet[]{annotationSet});

        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesRemoved(me);
        }
    }

    @Override
    public void annotationSetRenamed(AnnotationSet annotationSet) {
        TreeModelEvent me = new TreeModelEvent(annotationSet, getPathToRoot(annotationSet));
        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesChanged(me);
        }
    }

    /**
     * Gets the path to root.
     *
     * @param o the object
     * @return the path to root
     */
    private Object[] getPathToRoot(Object o) {
        if (o instanceof DataSet) {
            return new Object[]{rootNode, dataSetNode, o};
        } else if (o instanceof AnnotationSet) {
            return new Object[]{rootNode, annotationNode, o};
        } else if (o instanceof DataGroup) {
            return new Object[]{rootNode, dataGroupNode, o};
        } else if (o instanceof FolderNode) {
            if (o == rootNode) return new Object[]{o};
            else return new Object[]{rootNode, o};
        } else {
            System.err.println(this.getClass().getName() + ":Error path");
            return null;
        }
    }


    /**
     * The Class folderNode.
     */
    private class FolderNode {

        /**
         * The name.
         */
        private String name;

        /**
         * Instantiates a new folder node.
         *
         * @param name the name
         */
        public FolderNode(String name) {
            this.name = name;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return name;
        }
    }

}
