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

package com.xl.display.dialog;

import com.xl.exception.NetworkException;
import com.xl.main.REDApplication;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.ChromosomeNameComparator;
import com.xl.utils.FileUtils;
import com.xl.utils.namemanager.MenuUtils;
import com.xl.utils.namemanager.SuffixUtils;
import com.xl.utils.ui.OptionDialogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * The Class GenomeSelector shows a tree of the currently available genomes
 */
public class GenomeSelector extends JDialog implements ActionListener, TreeSelectionListener {
    private final Logger logger = LoggerFactory.getLogger(GenomeSelector.class);
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
            OptionDialogUtils.showWarningDialog(application, "<html>The default Genome directory is " + LocationPreferences.getInstance().getGenomeDirectory() + "." +
                            "<br>There is nothing in the default genome directory." +
                            "<br>You can move your genome files into the default genome directory or select <i>Edit->Preferences...</i> to change genome " +
                            "directory." +
                            "<br>If you don't have any genome file, you can download one by selecting <i>Import New</i> after you press OK button.",
                    "Genome Directory Warning");
        } else {
            Set<GenomeNode> genomeFile = new TreeSet<GenomeNode>(new GenomeNodeComparator());
            for (File genome : genomes) {
                if (genome.isFile()) {
                    if (genome.getName().endsWith(SuffixUtils.GENOME)) {
                        genomeFile.add(new GenomeNode(genome));
                    }
                } else {
                    File[] genomeId = genome.listFiles();
                    if (genomeId == null || genomeId.length == 0) {
                        continue;
                    }
                    for (File file : genomeId) {
                        if (!file.isDirectory() && file.getName().endsWith(SuffixUtils.GENOME)) {
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
        tree.addTreeSelectionListener(this);
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);

        getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);

        // Create the buttons at the bottom.
        JPanel buttonPanel = new JPanel();

        JButton cancelButton = new JButton(MenuUtils.CANCEL_BUTTON);
        cancelButton.setActionCommand(MenuUtils.CANCEL_BUTTON);
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        JButton deleteButton = new JButton(MenuUtils.DELETE_BUTTON);
        deleteButton.setActionCommand(MenuUtils.DELETE_BUTTON);
        deleteButton.addActionListener(this);
        buttonPanel.add(deleteButton);

        JButton importButton = new JButton(MenuUtils.IMPORT_BUTTON);
        importButton.setActionCommand(MenuUtils.IMPORT_BUTTON);
        importButton.addActionListener(this);
        buttonPanel.add(importButton);

        okButton = new JButton(MenuUtils.OK_BUTTON);
        okButton.setActionCommand(MenuUtils.OK_BUTTON);
        okButton.setEnabled(false);
        okButton.addActionListener(this);
        getRootPane().setDefaultButton(okButton);
        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals(MenuUtils.OK_BUTTON)) {
            setVisible(false);

            // Remove any currently loaded data
            application.wipeAllData();

            // Now load the new genome.
            File genomeFie = ((GenomeNode) tree.getSelectionPath().getLastPathComponent()).file();
            application.loadGenome(genomeFie);
            logger.info("Loading genome: " + genomeFie.getName());
            dispose();
        } else if (ae.getActionCommand().equals(MenuUtils.IMPORT_BUTTON)) {
            try {
                new GenomeDownloadSelector(application);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (NetworkException e) {
                new CrashReporter(e);
                logger.error(e.getMessage(), e);
            }
            setVisible(false);
            dispose();
        } else if (ae.getActionCommand().equals(MenuUtils.DELETE_BUTTON)) {
            Object lastComponent = tree.getSelectionPath().getLastPathComponent();
            if (lastComponent instanceof GenomeNode) {
                String path = ((GenomeNode) lastComponent).file().getAbsolutePath();
                FileUtils.deleteFile(path);
            }
            dispose();
            new GenomeSelector(application);
        } else if (ae.getActionCommand().equals(MenuUtils.CANCEL_BUTTON)) {
            setVisible(false);
            dispose();
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent tse) {
        if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() instanceof GenomeNode) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
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

    /**
     * A name comparator between two genome node.
     */
    private class GenomeNodeComparator implements Comparator<GenomeNode> {

        @Override
        public int compare(GenomeNode o1, GenomeNode o2) {
            String name1 = o1.f.getName();
            String name2 = o2.f.getName();
            return ChromosomeNameComparator.getInstance().compare(name1, name2);
        }
    }
}
