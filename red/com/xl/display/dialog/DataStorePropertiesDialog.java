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

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.main.RedApplication;
import com.xl.utils.Strand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The Class DataStorePropertiesDialog shows some basic stats about a data store
 */
public class DataStorePropertiesDialog extends JDialog implements ActionListener {
    /**
     * Instantiates a new data store properties dialog.
     *
     * @param dataStore the data store
     */
    public DataStorePropertiesDialog(DataStore dataStore) {

        super(RedApplication.getInstance(), "DataStore Properties");
        getContentPane().setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        infoPanel.add(new JLabel("Name"), gbc);
        gbc.gridx = 2;
        infoPanel.add(new JLabel(dataStore.name()), gbc);

        gbc.gridx = 1;
        gbc.gridy++;

        if (dataStore instanceof DataSet) {
            infoPanel.add(new JLabel("File Name"), gbc);
            gbc.gridx = 2;
            infoPanel.add(new JLabel(((DataSet) dataStore).fileName()), gbc);
        } else if (dataStore instanceof DataGroup) {
            infoPanel.add(new JLabel("Data Sets"), gbc);
            gbc.gridx = 2;
            infoPanel.add(new JLabel(((DataGroup) dataStore).dataSets().length + ""), gbc);

        }

        gbc.gridx = 1;
        gbc.gridy++;

        infoPanel.add(new JLabel("Total Reads"), gbc);
        gbc.gridx = 2;
        JLabel totalCount = new JLabel("" + dataStore.getTotalReadCount());
        infoPanel.add(totalCount, gbc);

        gbc.gridx = 1;
        gbc.gridy++;

        infoPanel.add(new JLabel("Forward Count"), gbc);
        gbc.gridx = 2;
        JLabel forwardCount = new JLabel("" + dataStore.getReadCountForStrand(Strand.POSITIVE));
        infoPanel.add(forwardCount, gbc);

        gbc.gridx = 1;
        gbc.gridy++;

        infoPanel.add(new JLabel("Reverse Count"), gbc);
        gbc.gridx = 2;
        JLabel reverseCount = new JLabel("" + dataStore.getReadCountForStrand(Strand.NEGATIVE));
        infoPanel.add(reverseCount, gbc);

        gbc.gridx = 1;
        gbc.gridy++;

        infoPanel.add(new JLabel("Total Read Length"), gbc);
        gbc.gridx = 2;
        JLabel totalLength = new JLabel("" + dataStore.getTotalReadLength());
        infoPanel.add(totalLength, gbc);

        getContentPane().add(new JScrollPane(infoPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(500, 250);
        setLocationRelativeTo(RedApplication.getInstance());
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        // The only action is to close
        setVisible(false);
        dispose();
    }
}
