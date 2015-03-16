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
package com.xl.help;

import com.xl.main.REDApplication;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;

/**
 * The Class HelpDialog shows the contents of the help system and allows searching and navigation within it.
 */
public class HelpDialog extends JDialog implements TreeSelectionListener {
    /**
     * The tree.
     */
    private JTree tree;
    /**
     * The current page.
     */
    private HelpPageDisplay currentPage = null;
    /**
     * The main split.
     */
    private JSplitPane mainSplit;

    /**
     * Instantiates a new help dialog.
     *
     * @param startingLocation the folder containing the html help documentation
     */
    public HelpDialog(File startingLocation) {
        super(REDApplication.getInstance(), "Help Contents");

        HelpIndexRoot root = new HelpIndexRoot(startingLocation);

        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        setContentPane(mainSplit);

        tree = new JTree(new DefaultTreeModel(root));

        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplit.setTopComponent(new JScrollPane(tree));
        leftSplit.setBottomComponent(new HelpSearchPanel(root, this));

        mainSplit.setLeftComponent(leftSplit);
        currentPage = new HelpPageDisplay(null);
        mainSplit.setRightComponent(currentPage);

        tree.addTreeSelectionListener(this);

        setSize(1000, 600);
        setLocationRelativeTo(REDApplication.getInstance());
        setVisible(true);

        leftSplit.setDividerLocation(0.7);
        mainSplit.setDividerLocation(0.3);
        findStartingPage();
    }

    /**
     * Find starting page.
     */
    private void findStartingPage() {
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) tree.getModel().getRoot();

        try {
            displayPage((HelpPage) currentNode.getFirstLeaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Display page.
     *
     * @param page the page
     */
    public void displayPage(HelpPage page) {
        if (currentPage != null) {
            int d = mainSplit.getDividerLocation();
            mainSplit.remove(currentPage);
            currentPage = new HelpPageDisplay(page);
            mainSplit.setRightComponent(currentPage);
            mainSplit.setDividerLocation(d);
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent tse) {

        if (tse.getNewLeadSelectionPath() == null) return;

        Object o = tse.getNewLeadSelectionPath().getLastPathComponent();
        if (o instanceof HelpPage && ((HelpPage) o).isLeaf()) {
            displayPage((HelpPage) o);
        }
    }

}
