package com.xl.display.dataviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.annotation.CoreAnnotationSet;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.dialog.AnnotationSetPropertiesDialog;
import com.xl.dialog.CrashReporter;
import com.xl.dialog.DataStorePropertiesDialog;
import com.xl.dialog.ProbeListCommentEditDialog;
import com.xl.display.HistogramPlot.ProbeLengthHistogramPlot;
import com.xl.display.HistogramPlot.ReadLengthHistogramPlot;
import com.xl.exception.REDException;
import com.xl.main.REDApplication;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.*;


/**
 * The DataViewer is a panel which shows a tree based overview of a data
 * collection.  It also provides a mechanism to select DataStores and
 * ProbeLists and can launch various tools via popup menus.
 */
public class DataViewer extends JPanel implements MouseListener, TreeSelectionListener {

    private DataCollection collection;
    private REDApplication application;
    private JTree dataTree;
    private JTree probeSetTree;

    /**
     * Instantiates a new data viewer.
     *
     * @param application
     */
    public DataViewer(REDApplication application) {
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
        add(dataTree, con);

        con.gridy++;

        ProbeSetTreeModel probeModel = new ProbeSetTreeModel(collection);
        probeSetTree = new UnfocusableTree(probeModel);
        probeSetTree.addMouseListener(this);
        probeSetTree.addTreeSelectionListener(this);
        probeSetTree.setCellRenderer(new DataTreeRenderer());
        add(probeSetTree, con);


        // This nasty bit just makes the trees squash up to the top of the display
        // area.
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
            } else if (clickedItem instanceof ProbeList) {
                new ProbePopupMenu((ProbeList) clickedItem).show(probeSetTree, me.getX(), me.getY());
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
            } else if (clickedItem instanceof ProbeList) {
                new ProbePopupMenu((ProbeList) clickedItem).actionPerformed(new ActionEvent(this, 0, "view"));
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
                    collection.setActiveDataStore(null);
                } else {
                    Object selectedItem = dataTree.getSelectionPath().getLastPathComponent();
                    if (selectedItem instanceof DataStore) {
                        collection.setActiveDataStore((DataStore) (selectedItem));
                    } else {
                        collection.setActiveDataStore(null);
                    }
                }

            } else if (tse.getSource() == probeSetTree) {
                if (probeSetTree.getSelectionPath() == null) {
                    collection.probeSet().setActiveList(null);
                } else {
                    Object selectedItem = probeSetTree.getSelectionPath().getLastPathComponent();
                    if (selectedItem instanceof ProbeList) {
                        collection.probeSet().setActiveList((ProbeList) selectedItem);
                    } else {
                        if (collection.probeSet() != null) {
                            collection.probeSet().setActiveList(null);
                        }
                    }
                }
            }
        } catch (REDException e) {
            new CrashReporter(e);
        }

    }

    /**
     * Provides a small popup dialog which can be used when renaming
     * an object.
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
         * @param d
         */
        public DataPopupMenu(DataSet d) {
            this.d = d;

            JCheckBoxMenuItem displayTrack = new JCheckBoxMenuItem("Show Track in Chromosome View");
            displayTrack.setActionCommand("display_track");
            displayTrack.addActionListener(this);
            if (application.dataStoreIsDrawn(d)) {
                displayTrack.setState(true);
            } else {
                displayTrack.setState(false);
            }
            add(displayTrack);

            JMenuItem readLenHistogram = new JMenuItem("Show Read Length Histogram");
            readLenHistogram.setActionCommand("readlen_histogram");
            readLenHistogram.addActionListener(this);
            add(readLenHistogram);

            JMenuItem rename = new JMenuItem("Rename");
            rename.setActionCommand("rename");
            rename.addActionListener(this);
            add(rename);

            JMenuItem properties = new JMenuItem("Properties");
            properties.setActionCommand("properties");
            properties.addActionListener(this);
            add(properties);


            // I'm not sure at the moment whether I should allow
            // deletion of a data set.  There are lots of places
            // this would affect - I'm not going to implement this
            // at the moment.
//			JMenuItem delete = new JMenuItem("Delete");
//			delete.setActionCommand("delete");
//			delete.addActionListener(this);
//			add(delete);


        }


        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("scatter")) {
//				application.showScatterPlot(d);
            } else if (ae.getActionCommand().equals("display_track")) {
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
         * @param d
         */
        public GroupPopupMenu(DataGroup d) {
            this.d = d;
            JCheckBoxMenuItem displayTrack = new JCheckBoxMenuItem("Show Track in Chromosome View");
            displayTrack.setActionCommand("display_track");
            displayTrack.addActionListener(this);
            if (application.dataStoreIsDrawn(d)) {
                displayTrack.setState(true);
            } else {
                displayTrack.setState(false);
            }
            add(displayTrack);

            JMenuItem readLenHistogram = new JMenuItem("Show Read Length Histogram");
            readLenHistogram.setActionCommand("readlen_histogram");
            readLenHistogram.addActionListener(this);
            add(readLenHistogram);

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
            if (ae.getActionCommand().equals("readlen_histogram")) {
                new ReadLengthHistogramPlot(d);
            } else if (ae.getActionCommand().equals("hiclen_histogram")) {
//				new HiCLengthHistogramPlot(d, null);
            } else if (ae.getActionCommand().equals("display_track")) {
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
                collection.removeDataGroups(new DataGroup[]{d});
            } else if (ae.getActionCommand().equals("properties")) {
                new DataStorePropertiesDialog(d);
            } else {
                System.err.println("Unknown menu option '" + ae.getActionCommand() + "'");
            }
        }
    }

    /**
     * The popup menu which appears when the user right-clicks on a ProbeList
     */
    private class ProbePopupMenu extends JPopupMenu implements ActionListener {

        private ProbeList p;

        /**
         * Instantiates a new probe popup menu.
         *
         * @param p
         */
        public ProbePopupMenu(ProbeList p) {
            this.p = p;

            JMenuItem view = new JMenuItem("View List");
            view.setActionCommand("view");
            view.addActionListener(this);
            add(view);

            JMenuItem report = new JMenuItem("Make List Report");
            report.setActionCommand("report");
            report.addActionListener(this);
            add(report);

            JMenuItem similar = new JMenuItem("Similar Lists");
            similar.setActionCommand("similar");
            similar.addActionListener(this);
            add(similar);

            JMenuItem length = new JMenuItem("Show Probe Length Histogram");
            length.setActionCommand("length");
            length.addActionListener(this);
            add(length);

            JMenuItem rename = new JMenuItem("Rename");
            rename.setActionCommand("rename");
            rename.addActionListener(this);
            if (p instanceof ProbeSet) {
                rename.setEnabled(false);
            }
            add(rename);

            JMenuItem comments = new JMenuItem("Edit comments");
            comments.setActionCommand("comments");
            comments.addActionListener(this);
            add(comments);

            JMenuItem delete = new JMenuItem("Delete");
            delete.setActionCommand("delete");
            delete.addActionListener(this);
            if (p instanceof ProbeSet) {
                delete.setEnabled(false);
            }
            add(delete);
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("rename")) {
                String name = getNewName(p.name());
                if (name != null) {
                    p.setName(name);
                }
            } else if (ae.getActionCommand().equals("comments")) {
                new ProbeListCommentEditDialog(p, this);
            } else if (ae.getActionCommand().equals("delete")) {
                p.delete();
            } else if (ae.getActionCommand().equals("report")) {
//				new ProbeListReportCreator(p);
            } else if (ae.getActionCommand().equals("length")) {
                new ProbeLengthHistogramPlot(p);
            } else if (ae.getActionCommand().equals("view")) {
//				new ProbeListViewer(p, application);
            } else if (ae.getActionCommand().equals("similar")) {
//				new SimilarProbeListsDialog(application.dataCollection());
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
         * @param annotation
         */
        public AnnotationPopupMenu(AnnotationSet annotation) {
            this.annotationSet = annotation;

            JMenuItem properties = new JMenuItem("Properties");
            properties.setActionCommand("properties");
            properties.addActionListener(this);
            add(properties);

            JMenuItem rename = new JMenuItem("Rename");
            rename.setActionCommand("rename");
            rename.addActionListener(this);
            add(rename);

            JMenu renameFeature = new JMenu("Rename Feature");

            if (annotation instanceof CoreAnnotationSet) {
                rename.setEnabled(false);
                renameFeature.setEnabled(false);
            } else {
                JMenuItem renameFeatureItem = new JMenuItem(annotation.name());
                renameFeatureItem
                        .setActionCommand("rename_feature_" + annotation.name());
                renameFeatureItem.addActionListener(this);
                renameFeature.add(renameFeatureItem);
            }

            add(renameFeature);

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
            if (ae.getActionCommand().equals("rename")) {
                String name = getNewName(annotationSet.name());
                if (name != null) {
                    annotationSet.setName(name);
                }
            } else if (ae.getActionCommand().equals("delete")) {
                annotationSet.delete();
            } else if (ae.getActionCommand().equals("properties")) {
                new AnnotationSetPropertiesDialog(annotationSet);
            } else if (ae.getActionCommand().startsWith("rename_feature_")) {
                String featureName = ae.getActionCommand().replaceFirst("rename_feature_", "");

                String newName = getNewName(featureName);
                if (newName != null) {
//					annotationSet.renameFeatures(featureName, newName);
                }
            } else {
                System.err.println("Unknown menu option '" + ae.getActionCommand() + "'");
            }

        }
    }


    /**
     * An extension of JTree which is unable to take keyboard focus.
     * <p/>
     * This class is needed to make sure the arrow key navigation
     * always works in the chromosome view.  If either of the JTrees
     * can grab focus they will intercept the arrow key events and
     * just move the selections on the tree.
     */
    private class UnfocusableTree extends JTree {

        // This class is needed to make sure the arrow key navigation
        // always works in the chromosome view.  If either of the JTrees
        // can grab focus they will intercept the arrow key events and
        // just move the selections on the tree.

        /**
         * Instantiates a new unfocusable tree.
         *
         * @param m
         */
        public UnfocusableTree(TreeModel m) {
            super(m);
            this.setFocusable(false);
        }

    }

}
