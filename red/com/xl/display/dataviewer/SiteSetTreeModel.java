package com.xl.display.dataviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.SiteList;
import com.xl.datatypes.sites.SiteSet;
import com.xl.datatypes.sites.SiteSetChangeListener;
import com.xl.interfaces.DataChangeListener;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

/**
 * The Class SiteSetTreeModel provides a tree model which describes the
 * relationships between site sets.
 */
public class SiteSetTreeModel implements TreeModel, SiteSetChangeListener, DataChangeListener {

    /**
     * The sites.
     */
    private SiteSet sites;

    /**
     * The listeners.
     */
    private Vector<TreeModelListener> listeners = new Vector<TreeModelListener>();

    /**
     * The root node.
     */
    private FolderNode rootNode;

    /**
     * Instantiates a new site set tree model.
     *
     * @param collection the collection
     */
    public SiteSetTreeModel(DataCollection collection) {
        if (collection != null) {
            collection.addDataChangeListener(this);
            sites = collection.siteSet();
        }
        if (sites != null) {
            collection.siteSet().addSiteSetChangeListener(this);
        }
        rootNode = new FolderNode("Site Lists");
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
        if (node instanceof SiteList) {
            return ((SiteList) node).children()[index];
        } else if (node.equals(rootNode)) {
            if (index == 0) return sites;
        }

        throw new NullPointerException("Null child from " + node + " at index " + index);
    }

    public int getChildCount(Object node) {
        if (node instanceof SiteList) {
            return ((SiteList) node).children().length;
        } else if (node.equals(rootNode)) {
            if (sites == null) return 0;
            return 1;
        } else {
            return 0;
        }
    }

    public int getIndexOfChild(Object node, Object child) {
        if (node instanceof SiteList) {
            SiteList[] children = ((SiteList) node).children();
            for (int i = 0; i < children.length; i++) {
                if (children[i].equals(child)) {
                    return i;
                }
            }
        } else if (node.equals(rootNode)) {
            if (child == sites) {
                return 0;
            }
        }
        System.err.println("Couldn't find valid index for " + node + " and " + child);
        return 0;

    }

    public Object getRoot() {
        return rootNode;
    }


    public boolean isLeaf(Object node) {
        if (node instanceof SiteList) {
            return ((SiteList) node).children().length == 0;
        } else if (node instanceof FolderNode) {
            return false;
        }
        return true;
    }

    public void valueForPathChanged(TreePath tp, Object node) {
        // This only applies to editable trees - which this isn't.
        System.out.println("Value for path changed called on node " + node);
    }

    public void siteListAdded(SiteList l) {
        Object[] pathToRoot = getPathToRoot(l.getParent());
        TreeModelEvent me = new TreeModelEvent(l, pathToRoot, new int[]{getIndexOfChild(l.getParent(), l)}, new SiteList[]{l});

        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesInserted(me);
        }
    }

    public void siteListRemoved(SiteList l) {
        TreeModelEvent me;
        if (l instanceof SiteSet) {
            me = new TreeModelEvent(l, getPathToRoot(l.getParent()), new int[]{0}, new SiteList[]{l});
        } else {
            me = new TreeModelEvent(l, getPathToRoot(l.getParent()), new int[]{getIndexOfChild(l.getParent(), l)}, new SiteList[]{l});
        }
        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesRemoved(me);
        }
    }

    public void siteListRenamed(SiteList l) {
        TreeModelEvent me = new TreeModelEvent(l, getPathToRoot(l));
        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesChanged(me);
        }
    }

    public void dataGroupAdded(DataGroup g) {
    }

    public void dataGroupsRemoved(DataGroup[] g) {
    }

    public void dataGroupRenamed(DataGroup g) {
    }

    public void dataGroupSamplesChanged(DataGroup g) {
    }

    public void dataSetAdded(DataSet d) {
    }

    public void dataSetsRemoved(DataSet[] d) {
    }

    public void dataSetRenamed(DataSet d) {
    }

    public void siteSetReplaced(SiteSet sites) {
        if (this.sites != null) {
            this.sites.removeSiteSetChangeListener(this);
        }
        this.sites = sites;

        if (sites != null) {
            sites.addSiteSetChangeListener(this);
        }

        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeStructureChanged(new TreeModelEvent(rootNode, new SiteList[]{sites}));
        }
    }

    public void activeDataStoreChanged(DataStore s) {

    }

    public void activeSiteListChanged(SiteList l) {

    }

    /**
     * Gets the path to root.
     *
     * @param l the l
     * @return the path to root
     */
    private Object[] getPathToRoot(SiteList l) {

        LinkedList<Object> nodes = new LinkedList<Object>();

        if (l != null) {
            nodes.add(l);

            while (l.getParent() != null) {
                l = l.getParent();
                nodes.addFirst(l);
            }
        }

        nodes.addFirst(rootNode);

        // Now make this into an array
        Object[] pathToNode = nodes.toArray(new Object[0]);

        return pathToNode;
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
