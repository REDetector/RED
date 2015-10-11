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
import com.xl.datatypes.annotation.CoreAnnotationSet;
import com.xl.datatypes.sites.SiteList;
import com.xl.datatypes.sites.SiteSet;
import com.xl.display.dialog.*;
import com.xl.display.report.SitesDistributionHistogram;
import com.xl.display.report.VariantDistributionHistogram;
import com.xl.exception.RedException;
import com.xl.main.RedApplication;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.*;


/**
 * The DataViewer is a panel which shows a tree based overview of a data collection.  It also provides a mechanism to select DataStores and SiteLists and can
 * launch various tools via popup menus.
 */
public class DataViewer extends JPanel implements MouseListener, TreeSelectionListener {

    private DataCollection collection;
    private RedApplication application;
    private JTree dataTree;
    private JTree siteSetTree;
    private SiteSetTreeModel siteModel;

    /**
     * Instantiates a new data viewer.
     *
     * @param application the application
     */
    public DataViewer(RedApplication application) {
        this.application = application;
        this.collection = application.dataCollection();
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        GridBagConstraints con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 0;
        con.weightx = 0.1;
        con.weighty = 0.01;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.anchor = GridBagConstraints.FIRST_LINE_START;

        DataCollectionTreeModel model = new DataCollectionTreeModel(collection);
        dataTree = new UnfocusableTree(model);
        dataTree.addMouseListener(this);
        dataTree.addTreeSelectionListener(this);
        dataTree.setCellRenderer(new DataTreeRenderer());
        dataTree.setExpandsSelectedPaths(true);
        add(dataTree, con);

        con.gridy++;

        siteModel = new SiteSetTreeModel(collection.getActiveDataStore());
        siteSetTree = new UnfocusableTree(siteModel);
        siteSetTree.addMouseListener(this);
        siteSetTree.addTreeSelectionListener(this);
        siteSetTree.setCellRenderer(new DataTreeRenderer());
        add(siteSetTree, con);

        // This nasty bit just makes the trees squash up to the top of the display area.
        con.gridy++;
        con.weighty = 1;
        con.fill = GridBagConstraints.BOTH;
        add(new JLabel(" "), con);

    }


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent arg0) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent me) {

        JTree tree = (JTree) me.getSource();
        tree.setSelectionRow(tree.getRowForLocation(me.getX(), me.getY()));

        // Check if they right-clicked
        if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {

            // I'm not sure if this is a timing issue, but we can get the selection path being null
            if (tree.getSelectionPath() == null) return;

            Object clickedItem = tree.getSelectionPath().getLastPathComponent();

            if (clickedItem instanceof DataSet) {
                new DataPopupMenu((DataSet) clickedItem).show(dataTree, me.getX(), me.getY());
            } else if (clickedItem instanceof DataGroup) {
                new GroupPopupMenu((DataGroup) clickedItem).show(dataTree, me.getX(), me.getY());
            } else if (clickedItem instanceof SiteList) {
                new SitePopupMenu((SiteList) clickedItem).show(siteSetTree, me.getX(), me.getY());
            } else if (clickedItem instanceof AnnotationSet) {
                new AnnotationPopupMenu((AnnotationSet) clickedItem).show(dataTree, me.getX(), me.getY());
            }
        }

        // Check if they double clicked
        else if (me.getClickCount() == 2) {

            // I'm not sure if this is a timing issue, but we can get the selection path being null
            if (tree.getSelectionPath() == null) return;

            Object clickedItem = tree.getSelectionPath().getLastPathComponent();

            if (clickedItem instanceof DataSet) {
                new DataPopupMenu((DataSet) clickedItem).actionPerformed(new ActionEvent(this, 0, "properties"));
            } else if (clickedItem instanceof DataGroup) {
                new GroupPopupMenu((DataGroup) clickedItem).actionPerformed(new ActionEvent(this, 0, "properties"));
            } else if (clickedItem instanceof AnnotationSet) {
                new AnnotationPopupMenu((AnnotationSet) clickedItem).actionPerformed(new ActionEvent(this, 0, "properties"));
            }
        }

    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent arg0) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent arg0) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent arg0) {
    }

    /* (non-Javadoc)
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent tse) {
        // Check for a new selected node and act appropriately

        try {
            if (tse.getSource() == dataTree) {
                if (dataTree.getSelectionPath() == null) {
                    collection.setActiveData(null, null);
                } else {
                    Object selectedItem = dataTree.getSelectionPath().getLastPathComponent();
                    if (selectedItem instanceof DataStore) {
                        DataStore d = (DataStore) selectedItem;
                        if (d.siteSet() != null) {
                            siteModel.siteSetReplaced(d.siteSet());
                        }
                        collection.setActiveData(d, d.siteSet());
                    } else {
                        collection.setActiveData(null, null);
                    }
                }

            } else if (tse.getSource() == siteSetTree) {
                if (siteSetTree.getSelectionPath() == null) {
                    collection.setActiveData(collection.getActiveDataStore(), null);
                } else {
                    Object selectedItem = siteSetTree.getSelectionPath().getLastPathComponent();
                    if (selectedItem instanceof SiteList) {
                        collection.setActiveData(collection.getActiveDataStore(), (SiteList) selectedItem);
                    } else {
                        collection.setActiveData(collection.getActiveDataStore(), null);
                    }
                }
            }
        } catch (RedException e) {
            new CrashReporter(e);
        }

    }

    /**
     * Provides a small popup dialog which can be used when renaming an object.
     *
     * @param initialName The objects current name name
     * @return The new name provided by the user.  Null if the user cancelled or didn't change the name.
     */
    public String getNewName(String initialName) {
        String name;
        while (true) {
            name = (String) JOptionPane.showInputDialog(this, "Enter new name", "Rename", JOptionPane.QUESTION_MESSAGE, null, null, initialName);
            if (name == null)
                return null;  // They cancelled

            if (name.length() == 0)
                continue; // Try again

            break;
        }
        if (name.equals(initialName)) {
            return null;
        }
        return name;
    }

    /**
     * The popup menu which appears when the user right-clicks on a DataSet
     */
    private class DataPopupMenu extends JPopupMenu implements ActionListener {

        private DataSet d;

        /**
         * Instantiates a new data popup menu.
         *
         * @param d the data set
         */
        public DataPopupMenu(DataSet d) {
            this.d = d;

            JCheckBoxMenuItem displayTrack = new JCheckBoxMenuItem("Show Track in Chromosome Panel");
            displayTrack.setActionCommand("display_track");
            displayTrack.addActionListener(this);
            if (application.dataStoreIsDrawn(d)) {
                displayTrack.setState(true);
            } else {
                displayTrack.setState(false);
            }
            add(displayTrack);

            JMenuItem rename = new JMenuItem("Rename");
            rename.setActionCommand("rename");
            rename.addActionListener(this);
            add(rename);

            JMenuItem properties = new JMenuItem("Properties");
            properties.setActionCommand("properties");
            properties.addActionListener(this);
            add(properties);
        }


        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("display_track")) {
                if (((JCheckBoxMenuItem) ae.getSource()).getState()) {
                    application.addToDrawnDataStores(new DataStore[]{d});
                } else {
                    application.removeFromDrawnDataStores(d);
                }
            } else if (ae.getActionCommand().equals("rename")) {
                String name = getNewName(d.name());
                if (name != null) {
                    d.setName(name);
                }
            } else if (ae.getActionCommand().equals("properties")) {
                new DataStorePropertiesDialog(d);
            } else {
                System.err.println("Unknown menu option '" + ae.getActionCommand() + "'");
            }
        }
    }

    /**
     * The popup menu which appears when the user right-clicks on a DataGroup
     */
    private class GroupPopupMenu extends JPopupMenu implements ActionListener {

        private DataGroup d;

        /**
         * Instantiates a new group popup menu.
         *
         * @param d the data group
         */
        public GroupPopupMenu(DataGroup d) {
            this.d = d;
            JCheckBoxMenuItem displayTrack = new JCheckBoxMenuItem("Show Track in Chromosome Panel");
            displayTrack.setActionCommand("display_track");
            displayTrack.addActionListener(this);
            if (application.dataStoreIsDrawn(d)) {
                displayTrack.setState(true);
            } else {
                displayTrack.setState(false);
            }
            add(displayTrack);

            JMenuItem rename = new JMenuItem("Rename");
            rename.setActionCommand("rename");
            rename.addActionListener(this);
            add(rename);

            JMenuItem delete = new JMenuItem("Delete");
            delete.setActionCommand("delete");
            delete.addActionListener(this);
            add(delete);

            JMenuItem properties = new JMenuItem("Properties");
            properties.setActionCommand("properties");
            properties.addActionListener(this);
            add(properties);

        }


        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("display_track")) {
                if (((JCheckBoxMenuItem) ae.getSource()).getState()) {
                    application.addToDrawnDataStores(new DataStore[]{d});
                } else {
                    application.removeFromDrawnDataStores(d);
                }
            } else if (ae.getActionCommand().equals("rename")) {
                String name = getNewName(d.name());
                if (name != null) {
                    d.setName(name);
                }
            } else if (ae.getActionCommand().equals("delete")) {
                collection.removeDataStore(d);
            } else if (ae.getActionCommand().equals("properties")) {
                new DataStorePropertiesDialog(d);
            } else {
                System.err.println("Unknown menu option '" + ae.getActionCommand() + "'");
            }
        }
    }

    /**
     * The popup menu which appears when the user right-clicks on a SiteList
     */
    private class SitePopupMenu extends JPopupMenu implements ActionListener {

        private SiteList p;

        /**
         * Instantiates a new site popup menu.
         *
         * @param p the site list
         */
        public SitePopupMenu(SiteList p) {
            this.p = p;

            JMenuItem view = new JMenuItem("Show Sites List");
            view.setActionCommand("view");
            view.addActionListener(this);
            add(view);

            JMenuItem sitesDistribution = new JMenuItem("Show Sites Distribution");
            sitesDistribution.setActionCommand("sites distribution");
            sitesDistribution.addActionListener(this);
            add(sitesDistribution);

            JMenuItem variantDistribution = new JMenuItem("Show Variant Distribution");
            variantDistribution.setActionCommand("variant distribution");
            variantDistribution.addActionListener(this);
            add(variantDistribution);

            JMenuItem rename = new JMenuItem("Rename");
            rename.setActionCommand("rename");
            rename.addActionListener(this);
            if (p instanceof SiteSet) {
                rename.setEnabled(false);
            }
            add(rename);

            JMenuItem comments = new JMenuItem("Edit Comments");
            comments.setActionCommand("comments");
            comments.addActionListener(this);
            add(comments);

            JMenuItem delete = new JMenuItem("Delete");
            delete.setActionCommand("delete");
            delete.addActionListener(this);
            if (p instanceof SiteSet) {
                delete.setEnabled(false);
            }
            add(delete);
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("view")) {
                new SiteListViewer(p, application);
            } else if (ae.getActionCommand().equals("sites distribution")) {
                new SitesDistributionHistogram(collection.getActiveDataStore());
            } else if (ae.getActionCommand().equals("variant distribution")) {
                new VariantDistributionHistogram(collection.getActiveDataStore());
            } else if (ae.getActionCommand().equals("rename")) {
                String name = getNewName(p.getListName());
                if (name != null) {
                    p.setListName(name);
                }
            } else if (ae.getActionCommand().equals("comments")) {
                new SiteListCommentEditDialog(p, this);
            } else if (ae.getActionCommand().equals("delete")) {
                p.delete();
            } else {
                System.err.println("Unknown menu option '" + ae.getActionCommand() + "'");
            }

        }
    }


    /**
     * The popup menu which appears when the user right-clicks on an AnnotationSet
     */
    private class AnnotationPopupMenu extends JPopupMenu implements ActionListener {

        private AnnotationSet annotationSet;

        /**
         * Instantiates a new annotation popup menu.
         *
         * @param annotation the annotation set
         */
        public AnnotationPopupMenu(AnnotationSet annotation) {
            this.annotationSet = annotation;

            JCheckBoxMenuItem displayTrack = new JCheckBoxMenuItem("Show Track in Chromosome Panel");
            displayTrack.setActionCommand("display_track");
            displayTrack.addActionListener(this);
            if (application.chromosomeViewer().getFeatureTrack().isVisible()) {
                displayTrack.setState(true);
            } else {
                displayTrack.setState(false);
            }
            add(displayTrack);

            JMenuItem properties = new JMenuItem("Properties");
            properties.setActionCommand("properties");
            properties.addActionListener(this);
            add(properties);

            JMenuItem rename = new JMenuItem("Rename");
            rename.setActionCommand("rename");
            rename.addActionListener(this);
            add(rename);

            JMenuItem delete = new JMenuItem("Delete");
            delete.setActionCommand("delete");
            delete.addActionListener(this);
            if (annotation instanceof CoreAnnotationSet) {
                delete.setEnabled(false);
            }
            add(delete);
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("display_track")) {
                if (((JCheckBoxMenuItem) ae.getSource()).getState()) {
                    application.chromosomeViewer().getFeatureTrack().setVisible(true);
                } else {
                    application.chromosomeViewer().getFeatureTrack().setVisible(false);
                }
            } else if (ae.getActionCommand().equals("rename")) {
                String name = getNewName(annotationSet.name());
                if (name != null) {
                    annotationSet.setName(name);
                }
            } else if (ae.getActionCommand().equals("delete")) {
                annotationSet.delete();
            } else if (ae.getActionCommand().equals("properties")) {
                new AnnotationSetPropertiesDialog(annotationSet);
            } else {
                System.err.println("Unknown menu option '" + ae.getActionCommand() + "'");
            }

        }
    }


    /**
     * An extension of JTree which is unable to take keyboard focus.
     * <p/>
     * This class is needed to make sure the arrow key navigation always works in the chromosome view. If either of the JTrees can grab focus they will
     * intercept the arrow key events and just move the selections on the tree.
     */
    private class UnfocusableTree extends JTree {

        // This class is needed to make sure the arrow key navigation
        // always works in the chromosome view.  If either of the JTrees
        // can grab focus they will intercept the arrow key events and
        // just move the selections on the tree.

        /**
         * Instantiates a new unfocusable tree.
         *
         * @param m the tree model
         */
        public UnfocusableTree(TreeModel m) {
            super(m);
            this.setExpandsSelectedPaths(true);
            this.setFocusable(false);
        }

    }

}
