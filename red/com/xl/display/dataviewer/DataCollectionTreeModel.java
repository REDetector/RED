package com.xl.display.dataviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.interfaces.AnnotationCollectionListener;
import com.xl.interfaces.DataChangeListener;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.*;

/**
 * The Class DataCollectionTreeModel provides a tree model which describes
 * the data sets, data groups and annotation sets in a data collection
 */
public class DataCollectionTreeModel implements TreeModel, DataChangeListener, AnnotationCollectionListener {

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
        this.collection = collection;
        if (collection != null) {
            collection.addDataChangeListener(this);
            collection.genome().getAnnotationCollection().addAnnotationCollectionListener(this);
        }
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

    public Object getChild(Object node, int index) {
        if (node instanceof ProbeList) {
            return ((ProbeList) node).children()[index];
        } else if (node.equals(rootNode)) {
            switch (index) {
                case 0:
                    return annotationNode;
                case 1:
                    return dataSetNode;
                case 2:
                    return dataGroupNode;
            }
        } else if (node.equals(annotationNode)) {
            return collection.genome().getAnnotationCollection().anotationSets()[index];
        } else if (node.equals(dataSetNode)) {
            return collection.getAllDataSets()[index];
        } else if (node.equals(dataGroupNode)) {
            return collection.getAllDataGroups()[index];
        }

        throw new NullPointerException("Null child from " + node + " at index " + index);
    }

    public int getChildCount(Object node) {
        if (node instanceof ProbeList) {
            return ((ProbeList) node).children().length;
        } else if (node.equals(annotationNode)) {
            return collection.genome().getAnnotationCollection().anotationSets().length;
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

    public int getIndexOfChild(Object node, Object child) {
        if (node instanceof ProbeList) {
            ProbeList[] children = ((ProbeList) node).children();
            for (int i = 0; i < children.length; i++) {
                if (children[i].equals(child)) {
                    return i;
                }
            }
        } else if (node.equals(annotationNode)) {
            AnnotationSet[] sets = collection.genome().getAnnotationCollection().anotationSets();
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

    public Object getRoot() {
        return rootNode;
    }


    public boolean isLeaf(Object node) {
        return !(node instanceof FolderNode);
    }

    public void valueForPathChanged(TreePath tp, Object node) {
        // This only applies to editable trees - which this isn't.
        System.out.println("Value for path changed called on node " + node);
    }

    public void dataGroupAdded(DataGroup g) {
        TreeModelEvent me = new TreeModelEvent(g, getPathToRoot(dataGroupNode), new int[]{getIndexOfChild(dataGroupNode, g)}, new DataGroup[]{g});

        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesInserted(me);
        }
    }

    public void dataGroupsRemoved(DataGroup[] g) {

        // Find the indices of each of these datagroups and sort them low to high before telling the listeners
        Map<Integer, DataGroup> indices = new LinkedHashMap<Integer, DataGroup>();

        for (DataGroup group : g) {
            indices.put(getIndexOfChild(dataGroupNode, group), group);
        }

        // We have to make an Integer object array before we can convert this
        // to a primitive int array
        Integer[] deleteIndices = (Integer[]) indices.keySet().toArray();
        Arrays.sort(deleteIndices);

        DataGroup[] deleteGroups = new DataGroup[deleteIndices.length];
        for (int i = 0; i < deleteIndices.length; i++) {
            deleteGroups[i] = indices.get(deleteIndices[i]);
        }

        int[] delInd = new int[deleteIndices.length];
        for (int i = 0; i < deleteIndices.length; i++) {
            delInd[i] = deleteIndices[i];
        }

        TreeModelEvent me = new TreeModelEvent(g, getPathToRoot(dataGroupNode), delInd, deleteGroups);
        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesRemoved(me);
        }
    }

    public void dataGroupRenamed(DataGroup g) {
        TreeModelEvent me = new TreeModelEvent(g, getPathToRoot(g));
        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesChanged(me);
        }

        // We also need to let the tree know that the structure may have
        // changed since the new name may sort differently and therefore
        // appear in a different position.
        me = new TreeModelEvent(dataGroupNode, getPathToRoot(dataGroupNode));
        e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeStructureChanged(me);
        }

    }

    public void dataGroupSamplesChanged(DataGroup g) {
        // This can affect the name we display if the group changes from being HiC
        // to non-hiC (or vice versa) so we treat this like a name change.
        dataGroupRenamed(g);

    }

    public void dataSetAdded(DataSet d) {
        TreeModelEvent me = new TreeModelEvent(d, getPathToRoot(dataSetNode), new int[]{getIndexOfChild(dataSetNode, d)}, new DataSet[]{d});

        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesInserted(me);
        }
    }

    public void dataSetsRemoved(DataSet[] d) {

        // Find the indices of each of these datasets and sort them low to high
        // before telling the listeners
        Hashtable<Integer, DataSet> indices = new Hashtable<Integer, DataSet>();

        for (int i = 0; i < d.length; i++) {
            indices.put(getIndexOfChild(dataSetNode, d[i]), d[i]);
        }

        // We have to make an Integer object array before we can convert this
        // to a primitive int array
        Integer[] deleteIndices = (Integer[]) indices.keySet().toArray();
        Arrays.sort(deleteIndices);

        DataSet[] deleteSets = new DataSet[deleteIndices.length];
        for (int i = 0; i < deleteIndices.length; i++) {
            deleteSets[i] = indices.get(deleteIndices[i]);
        }

        int[] delInd = new int[deleteIndices.length];
        for (int i = 0; i < deleteIndices.length; i++) {
            delInd[i] = deleteIndices[i];
        }

        TreeModelEvent me = new TreeModelEvent(d, getPathToRoot(dataSetNode), delInd, deleteSets);
        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesRemoved(me);
        }
    }

    public void dataSetRenamed(DataSet d) {
        TreeModelEvent me = new TreeModelEvent(d, getPathToRoot(d));
        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesChanged(me);
        }

        // We also need to let the tree know that the structure may have
        // changed since the new name may sort differently and therefore
        // appear in a different position.
        me = new TreeModelEvent(dataSetNode, getPathToRoot(dataSetNode));
        e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeStructureChanged(me);
        }

    }

    public void probeSetReplaced(ProbeSet probes) {
    }

    public void annotationSetsAdded(AnnotationSet[] annotationSets) {

        int[] indices = new int[annotationSets.length];
        for (int i = 0; i < annotationSets.length; i++) {
            indices[i] = getIndexOfChild(annotationNode, annotationSets[i]);
        }


        TreeModelEvent me = new TreeModelEvent(annotationSets, getPathToRoot(annotationNode), indices, annotationSets);

        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesInserted(me);
        }
    }

    public void annotationSetRemoved(AnnotationSet annotationSet) {
        TreeModelEvent me = new TreeModelEvent(annotationSet, getPathToRoot(annotationNode), new int[]{getIndexOfChild(annotationNode, annotationSet)}, new AnnotationSet[]{annotationSet});

        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesRemoved(me);
        }
    }

    public void annotationSetRenamed(AnnotationSet annotationSet) {
        TreeModelEvent me = new TreeModelEvent(annotationSet, getPathToRoot(annotationSet));
        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesChanged(me);
        }
    }

    public void annotationFeaturesRenamed(AnnotationSet annotationSet, String newName) {
    }

    public void activeDataStoreChanged(DataStore s) {
    }

    public void activeProbeListChanged(ProbeList l) {
    }

    /**
     * Gets the path to root.
     *
     * @param d the d
     * @return the path to root
     */
    private Object[] getPathToRoot(DataSet d) {
        return new Object[]{rootNode, dataSetNode, d};
    }

    /**
     * Gets the path to root.
     *
     * @param s the s
     * @return the path to root
     */
    private Object[] getPathToRoot(AnnotationSet s) {
        return new Object[]{rootNode, annotationNode, s};
    }

    /**
     * Gets the path to root.
     *
     * @param g the g
     * @return the path to root
     */
    private Object[] getPathToRoot(DataGroup g) {
        return new Object[]{rootNode, dataGroupNode, g};
    }

    /**
     * Gets the path to root.
     *
     * @param f the f
     * @return the path to root
     */
    private Object[] getPathToRoot(FolderNode f) {
        if (f == rootNode) return new Object[]{f};
        else return new Object[]{rootNode, f};
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
