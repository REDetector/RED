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

import com.xl.net.crashreport.CrashReporter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


/**
 * The Class HelpSearchPanel provides a search box and results panel to allow easy searching through help documents.
 */
public class HelpSearchPanel extends JPanel implements ActionListener, ListSelectionListener, Runnable {

    /**
     * The root.
     */
    private HelpIndexRoot root;

    /**
     * The query field.
     */
    private JTextField queryField;

    /**
     * The result list.
     */
    private JList resultList;

    /**
     * The list model.
     */
    private DefaultListModel listModel;

    /**
     * The search button.
     */
    private JButton searchButton;

    /**
     * The dialog.
     */
    private HelpDialog dialog;

    /**
     * The results scroll pane.
     */
    private JScrollPane resultsScrollPane;

    /**
     * Instantiates a new help search panel.
     *
     * @param root   the root
     * @param dialog the dialog
     */
    public HelpSearchPanel(HelpIndexRoot root, HelpDialog dialog) {
        this.root = root;
        this.dialog = dialog;

        setLayout(new BorderLayout());

        JPanel queryPanel = new JPanel();
        queryPanel.setLayout(new BorderLayout());
        queryPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        queryField = new JTextField();
        queryField.setActionCommand("search");
        queryField.addActionListener(this);
        queryPanel.add(queryField, BorderLayout.CENTER);
        searchButton = new JButton("Search");
        searchButton.setActionCommand("search");
        searchButton.addActionListener(this);
        queryPanel.add(searchButton, BorderLayout.EAST);
        add(queryPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel();
        listModel.addElement("[No search results]");
        resultList = new JList(listModel);
        resultList.addListSelectionListener(this);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsScrollPane = new JScrollPane(resultList);
        add(resultsScrollPane, BorderLayout.CENTER);

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        Thread t = new Thread(this);
        t.start();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent lse) {
        Object o = resultList.getSelectedValue();
        if (o != null && o instanceof HelpPage) {
            dialog.DisplayPage((HelpPage) o);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        searchButton.setEnabled(false);
        listModel.removeAllElements();
        if (queryField.getText().trim().length() > 0) {
            HelpPage[] results;
            try {
                results = root.findPagesForTerm(queryField.getText().trim());
            } catch (IOException e) {
                new CrashReporter(e);
                searchButton.setEnabled(true);
                return;
            }
            if (results.length > 0) {
                for (HelpPage helpPage : results) {
                    listModel.addElement(helpPage);
                }
            } else {
                listModel.addElement("[No search results]");
            }
        }

        // This stupid rigmarole is because on OSX the updated list just won't show up for some reason. Removing the list and re-adding it forces it to
        // always show up.
        // It's not even enough to remake the scroll pane.  You have to replace the entire JList.
        remove(resultsScrollPane);
        revalidate();
        resultList = new JList(listModel);
        resultList.addListSelectionListener(this);
        resultsScrollPane = new JScrollPane(resultList);
        add(resultsScrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();

        searchButton.setEnabled(true);
    }
}
