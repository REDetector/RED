package com.xl.dialog;

import com.xl.main.REDApplication;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.ChromosomeNameComparator;
import com.xl.utils.FileUtils;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * The Class GenomeSelector shows a tree of the currently available genomes
 */
public class GenomeSelector extends JDialog {

    /**
     * The application.
     */
    private REDApplication application;

    /**
     * The tree.
     */
    private JTree tree;

    /**
     * The ok button.
     */
    private JButton okButton;

    /**
     * Instantiates a new genome selector.
     *
     * @param application the application
     */
    public GenomeSelector(REDApplication application) {
        super(application, "Select Genome...");
        this.application = application;
        setSize(400, 350);
        setLocationRelativeTo(application);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        getContentPane().setLayout(new BorderLayout());

        // Create the tree of available genomes
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Genomes");

        File genomeDirectory = new File(LocationPreferences.getInstance().getGenomeDirectory());
        File[] genomes = genomeDirectory.listFiles();

        if (genomes == null || genomes.length == 0) {
            JOptionPane.showMessageDialog(application, "<html>The default gGenome directory is " + LocationPreferences
                            .getInstance().getGenomeDirectory() + ".<br>There is nothing in the default genome " +
                            "directory.<br>You can move your genome files into the default genome directory or " +
                            "select menu Edit->Preferences to change genome directory.<br>However, " +
                            "if you don't have any genome file, you can choose download to import new genome.",
                    "Genome Directory Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            Set<GenomeNode> genomeFile = new TreeSet<GenomeNode>(new GenomeNodeComparator());
            for (File genome : genomes) {
                if (genome.isFile()) {
                    if (genome.getName().endsWith(".genome")) {
                        genomeFile.add(new GenomeNode(genome));
                    }
                } else {
                    File[] genomeId = genome.listFiles();
                    if (genomeId == null || genomeId.length == 0) {
                        continue;
                    }
                    for (File file : genomeId) {
                        if (!file.isDirectory() && file.getName().endsWith(".genome")) {
                            genomeFile.add(new GenomeNode(file));
                        }
                    }
                }
            }
            for (GenomeNode node : genomeFile) {
                root.add(node);
            }
        }

        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.addTreeSelectionListener(new TreeListener());
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);

        getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);

        // Create the buttons at the bottom.
        ButtonListener l = new ButtonListener();

        JPanel buttonPanel = new JPanel();

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(l);
        buttonPanel.add(cancelButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setActionCommand("delete");
        deleteButton.addActionListener(l);
        buttonPanel.add(deleteButton);

        JButton importButton = new JButton("Import New");
        importButton.setActionCommand("import");
        importButton.addActionListener(l);
        buttonPanel.add(importButton);

        okButton = new JButton("OK");
        okButton.setActionCommand("ok");
        okButton.setEnabled(false);
        okButton.addActionListener(l);
        getRootPane().setDefaultButton(okButton);
        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * The listener interface for receiving button events. The class that is
     * interested in processing a button event implements this interface, and
     * the object created with that class is registered with a component using
     * the component's <code>addButtonListener<code> method. When
     * the button event occurs, that object's appropriate
     * method is invoked.
     */
    private class ButtonListener implements ActionListener {

        /*
         * (non-Javadoc)
         *
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("ok")) {
                setVisible(false);

                // Remove any currently loaded data
                application.wipeAllData();

                // Now load the new genome.
                application.loadGenome(((GenomeNode) tree.getSelectionPath()
                        .getLastPathComponent()).file());
                System.out.println(this.getClass().getName()
                        + ":GenomeSelector(ok button):"
                        + ((GenomeNode) tree.getSelectionPath()
                        .getLastPathComponent()).file()
                        .getAbsolutePath());
                dispose();
            } else if (ae.getActionCommand().equals("import")) {
                try {
                    new GenomeDownloadSelector(application);
                } catch (Exception e) {
                    new CrashReporter(e);
                }
                setVisible(false);
                dispose();
            } else if (ae.getActionCommand().equals("delete")) {
                Object lastComponent = tree.getSelectionPath()
                        .getLastPathComponent();
                if (lastComponent instanceof GenomeNode) {
                    String path = ((GenomeNode) lastComponent).file()
                            .getAbsolutePath();
                    System.out.println(this.getClass().getName() + "\t" + path);
                    FileUtils.deleteDirectory(path.substring(0,
                            path.lastIndexOf("\\")));
                    System.out.println(this.getClass().getName() + "\t"
                            + path.substring(0, path.lastIndexOf("\\")));
                }
                dispose();
                new GenomeSelector(application);
            } else if (ae.getActionCommand().equals("cancel")) {
                setVisible(false);
                dispose();
            }

        }
    }

    /**
     * The listener interface for receiving tree events. The class that is
     * interested in processing a tree event implements this interface, and the
     * object created with that class is registered with a component using the
     * component's <code>addTreeListener<code> method. When
     * the tree event occurs, that object's appropriate
     * method is invoked.
     */
    private class TreeListener implements TreeSelectionListener {

        /*
         * (non-Javadoc)
         *
         * @see
         * javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.
         * event.TreeSelectionEvent)
         */
        public void valueChanged(TreeSelectionEvent tse) {
            if (tree.getSelectionPath() != null
                    && tree.getSelectionPath().getLastPathComponent() instanceof GenomeNode) {
                okButton.setEnabled(true);
            } else {
                okButton.setEnabled(false);
            }
        }

    }

    /**
     * The Class GenomeNode.
     */
    private class GenomeNode extends DefaultMutableTreeNode {

        /**
         * The f.
         */
        private File f;

        /**
         * Instantiates a new assembly node.
         *
         * @param f the f
         */
        public GenomeNode(File f) {
            super(f.getName());
            this.f = f;
        }

        /**
         * File.
         *
         * @return the file
         */
        public File file() {
            return f;
        }

    }

    private class GenomeNodeComparator implements Comparator<GenomeNode> {

        @Override
        public int compare(GenomeNode o1, GenomeNode o2) {
            String name1 = o1.f.getName();
            String name2 = o2.f.getName();
            System.out.println(name1 + "\t" + name2);
            return ChromosomeNameComparator.getInstance().compare(name1, name2);
        }
    }
}
