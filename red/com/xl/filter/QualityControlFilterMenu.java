/**
 * Copyright Copyright 2007-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.xl.filter;

import com.dw.dbutils.DatabaseManager;
import com.dw.dbutils.Query;
import com.dw.denovo.QualityControlFilter;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.exception.REDException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * The ValuesFilter filters sites based on their associated values
 * from quantiation.  Each site is filtered independently of all
 * other sites.
 */
public class QualityControlFilterMenu extends AbstractSiteFilter {

    private int qualityThres = 20;
    private int depthThres = 6;
    private JTextField qualityField;
    private JTextField depthField;
    private QCFilterOptionPanel optionsPanel = new QCFilterOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws REDException if the dataCollection isn't quantitated.
     */
    public QualityControlFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter RNA-editing bases by quality and depth.";
    }

    @Override
    protected void generateSiteList() {
        progressUpdated("Filtering RNA-editing sites by quality and coverage, please wait...", 0, 0);
        QualityControlFilter bf = new QualityControlFilter(databaseManager);
        bf.establishQCTable(DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
        // The first parameter means quality and the second means depth
        bf.executeQCFilter(parentTable, DatabaseManager.QC_FILTER_RESULT_TABLE_NAME, qualityThres, depthThres);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
        Vector<Site> sites = Query.queryAllEditingSites(DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
        SiteList newList = new SiteList(parentList, DatabaseManager.QC_FILTER_RESULT_TABLE_NAME, description(),
                DatabaseManager.QC_FILTER_RESULT_TABLE_NAME);
        int index = 0;
        int sitesLength = sites.size();
        for (Site site : sites) {
            progressUpdated(index++, sitesLength);
            if (cancel) {
                cancel = false;
                progressCancelled();
                return;
            }
            newList.addSite(site);
        }
        filterFinished(newList);
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    public boolean hasOptionsPanel() {
        return true;
    }

    @Override
    public boolean isReady() {
        return stores.length != 0 && qualityField.getText().length() != 0 && depthField.getText().length() != 0;
    }

    @Override
    public String name() {
        return "Quality Control Filter";
    }

    @Override
    protected String listName() {
        return "Q>=" + qualityThres + " & DP>=" + depthThres;
    }

    /**
     * The ValuesFilterOptionPanel.
     */
    private class QCFilterOptionPanel extends AbstractOptionPanel implements KeyListener {


        /**
         * Instantiates a new values filter option panel.
         */
        public QCFilterOptionPanel() {
            super(collection);
        }

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            System.out.println(QualityControlFilterMenu.class.getName() + ":valueChanged()");
            Object[] objects = dataList.getSelectedValues();
            stores = new DataStore[objects.length];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = (DataStore) objects[i];
            }
            optionsChanged();
        }

        @Override
        public void keyTyped(KeyEvent e) {
            int keyChar = e.getKeyChar();
            if (!(keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9))
                e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            JTextField f = (JTextField) e.getSource();
            if (f == qualityField) {
                if (f.getText().length() == 0) {
                    qualityField.setText("");
                } else {
                    qualityThres = Integer.parseInt(qualityField.getText());
                }
            } else if (f == depthField) {
                if (f.getText().length() == 0) {
                    depthField.setText("");
                } else {
                    depthThres = Integer.parseInt(depthField.getText());
                }
            }
            optionsChanged();
        }

        @Override
        protected boolean hasChoicePanel() {
            return true;
        }

        @Override
        protected JPanel getChoicePanel() {
            JPanel choicePanel = new JPanel();
            choicePanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = 0;
            c.gridx = 0;
            c.weightx = 0.5;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(new JLabel("Quality >= "), c);
            c.gridx = 1;
            c.weightx = 0.1;
            qualityField = new JTextField(3);
            qualityField.addKeyListener(this);
            choicePanel.add(qualityField, c);
            c.gridy++;
            c.gridx = 0;
            c.weightx = 0.5;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            choicePanel.add(new JLabel("Depth of coverage >= "), c);
            c.gridx = 1;
            c.weightx = 0.1;
            depthField = new JTextField(3);
            depthField.addKeyListener(this);
            choicePanel.add(depthField, c);
            return choicePanel;
        }

        @Override
        protected String getPanelDescription() {
            return "Two measures of base quality (Q, range of 1-255) and depth of coverage (DP, range of 1-255) are used in the QC filter. For example, " +
                    "a given site will be removed if it was of a low quality (Q< 20) or with a low depth of coverage (DP< 6).";
        }
    }
}
