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
import com.xl.net.genomes.DownloadableGenomeSet;
import com.xl.net.genomes.DownloadableGenomeTreeModel;
import com.xl.net.genomes.GenomeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * The Class GenomeDownloadSelector provides a dialog which can be used to select a genome to download.
 */
public class GenomeDownloadSelector extends JDialog implements ActionListener, TreeSelectionListener {
    private final Logger logger = LoggerFactory.getLogger(GenomeDownloadSelector.class);
    /**
     * The application.
     */
    private REDApplication application;
    /**
     * The tree.
     */
    private JTree tree;
    /**
     * The model behind the tree
     */
    private DownloadableGenomeTreeModel treeModel;
    /**
     * The download button.
     */
    private JButton downloadButton;

    /**
     * Instantiates a new genome download selector.
     *
     * @param application the application
     * @throws IOException if there was a problem getting or parsing the list of genomes
     */
    public GenomeDownloadSelector(REDApplication application) throws IOException, NetworkException {
        super(application, "Select Genome to Download...");
        this.application = application;
        setSize(380, 340);
        setLocationRelativeTo(application);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        getContentPane().setLayout(new BorderLayout());

        treeModel = new DownloadableGenomeTreeModel(new DownloadableGenomeSet());
        tree = new JTree(treeModel);
        tree.addTreeSelectionListener(this);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);

        // Create the buttons at the bottom.

        JPanel buttonPanel = new JPanel();

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        downloadButton = new JButton("Download");
        downloadButton.setActionCommand("download");
        downloadButton.setEnabled(false);
        downloadButton.addActionListener(this);
        getRootPane().setDefaultButton(downloadButton);
        buttonPanel.add(downloadButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("download")) {
            setVisible(false);
            GenomeList selectedGenome = (GenomeList) tree.getSelectionPath().getLastPathComponent();
            application.downloadGenome(selectedGenome.getId(), selectedGenome.getDisplayName());
            logger.info("Downloading the genome file: " + selectedGenome.getDisplayName());
            dispose();
        } else if (ae.getActionCommand().equals("cancel")) {
            setVisible(false);
            dispose();
        }

    }

    @Override
    public void valueChanged(TreeSelectionEvent tse) {
        if (tree.getSelectionPath() != null && tree.getSelectionPath().getLastPathComponent() instanceof GenomeList) {
            downloadButton.setEnabled(true);
        } else {
            downloadButton.setEnabled(false);
        }
    }
}
