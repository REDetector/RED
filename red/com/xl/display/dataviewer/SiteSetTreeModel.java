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

import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.SiteList;
import com.xl.datatypes.sites.SiteListChangeListener;
import com.xl.datatypes.sites.SiteSet;
import com.xl.exception.UnknownParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

/**
 * The Class SiteSetTreeModel provides a tree model which describes the relationships between site sets.
 */
public class SiteSetTreeModel implements TreeModel, SiteListChangeListener {
    private final Logger logger = LoggerFactory.getLogger(SiteSetTreeModel.class);
    /**
     * The listeners.
     */
    private Vector<TreeModelListener> listeners = new Vector<TreeModelListener>();

    /**
     * The site set.
     */
    private SiteSet siteSet;

    /**
     * The root node.
     */
    private FolderNode rootNode;

    /**
     * Instantiates a new site set tree model.
     *
     * @param dataStore the data store
     */
    public SiteSetTreeModel(DataStore dataStore) {
        if (dataStore != null) {
            siteSet = dataStore.siteSet();
            if (siteSet != null) {
                dataStore.siteSet().addSiteSetChangeListener(this);
            }
            rootNode = new FolderNode(dataStore.name());
        } else {
            rootNode = new FolderNode("Site Lists");
        }
    }

    @Override
    public Object getRoot() {
        return rootNode;
    }

    @Override
    public Object getChild(Object node, int index) {
        if (node instanceof SiteList) {
            return ((SiteList) node).children()[index];
        } else if (node.equals(rootNode)) {
            return siteSet;
        } else {
            logger.error("Object '" + node + "' can not be recognized by our program in index " + index, new UnknownParameterException());
            return siteSet;
        }
    }

    @Override
    public int getChildCount(Object node) {
        if (node instanceof SiteList) {
            return ((SiteList) node).children().length;
        } else if (node.equals(rootNode)) {
            if (siteSet == null) return 0;
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        if (node instanceof SiteList) {
            return ((SiteList) node).children().length == 0;
        } else if (node instanceof FolderNode) {
            return false;
        }
        return true;
    }

    @Override
    public void valueForPathChanged(TreePath tp, Object node) {
        // This only applies to editable trees - which this isn't.
        logger.warn("Value for path changed called on node " + node);
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
        } else if (node.equals(rootNode)) {
            if (child == siteSet) {
                return 0;
            }
        } else {
            logger.error("Could not get the index of child '" + child + "'from parent '" + node + "'", new UnknownParameterException());
        }
        return 0;

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
    public void siteListAdded(SiteList l) {
        Object[] pathToRoot = getPathToRoot(l.getParent());
        TreeModelEvent me = new TreeModelEvent(l, pathToRoot, new int[]{getIndexOfChild(l.getParent(), l)}, new SiteList[]{l});

        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesInserted(me);
        }
    }

    @Override
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

    @Override
    public void siteListRenamed(SiteList l) {
        TreeModelEvent me = new TreeModelEvent(l, getPathToRoot(l));
        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeNodesChanged(me);
        }
    }

    public void siteSetReplaced(SiteSet sites) {
        if (this.siteSet != null) {
            this.siteSet.removeSiteSetChangeListener(this);
        }
        this.siteSet = sites;

        if (sites != null) {
            sites.addSiteSetChangeListener(this);
            rootNode = new FolderNode(sites.getDataStore().name());
        }

        Enumeration<TreeModelListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().treeStructureChanged(new TreeModelEvent(rootNode, new SiteList[]{sites}));
        }
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

        // Make this into an array
        return nodes.toArray(new Object[0]);
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
