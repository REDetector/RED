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

package com.xl.filter.filterpanel;

import com.xl.datatypes.DataStore;
import com.xl.display.dataviewer.DataTreeRenderer;
import com.xl.display.dataviewer.SiteSetTreeModel;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;

/**
 * Created by Xing Li on 2014/10/11.
 * <p/>
 * The Class AbstractOptionPanel represents a filter panel before performing the filter and provides some parameters for user if there is any choice.
 */
abstract class AbstractOptionPanel extends JPanel implements TreeSelectionListener {
    /**
     * The site set tree model.
     */
    protected JTree siteTree;

    /**
     * Initiate a new option panel. The left is the site set tree and the right is the option panel.
     *
     * @param dataStore The data store, refer to a sample relative to this site set
     */
    public AbstractOptionPanel(DataStore dataStore) {
        setLayout(new BorderLayout());
        JPanel dataPanel = new JPanel();
        dataPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        dataPanel.setLayout(new BorderLayout());
        dataPanel.add(new JLabel("Site Lists", JLabel.CENTER), BorderLayout.NORTH);
        SiteSetTreeModel siteSetTreeModel = new SiteSetTreeModel(dataStore);
        siteTree = new JTree(siteSetTreeModel);
        siteTree.setCellRenderer(new DataTreeRenderer());
        siteTree.addTreeSelectionListener(this);

        dataPanel.add(new JScrollPane(siteTree), BorderLayout.CENTER);
        add(dataPanel, BorderLayout.WEST);

        add(new JScrollPane(getOptionPanel()), BorderLayout.CENTER);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 300);
    }

    /**
     * The right part of the whole option panel.
     *
     * @return the option panel.
     */
    private JPanel getOptionPanel() {
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JTextArea textField = new JTextArea();
        textField.setText(getPanelDescription());
        textField.setEditable(false);
        textField.setLineWrap(true);
        textField.setWrapStyleWord(true);
        totalPanel.add(textField, BorderLayout.NORTH);

        if (hasChoicePanel()) {
            totalPanel.add(getChoicePanel(), BorderLayout.CENTER);
        }
        return totalPanel;
    }

    /**
     * If there is any choice that can be selected or entered by user, then we have a choice panel.
     *
     * @return true if there is any choice that can be selected or entered by user
     */
    protected abstract boolean hasChoicePanel();

    /**
     * Get the choice panel.
     *
     * @return the choice panel.
     */
    protected abstract JPanel getChoicePanel();

    /**
     * We have a filter description to describe how filter works and how to use it.
     *
     * @return the description.
     */
    protected abstract String getPanelDescription();
}
