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
import com.dw.dnarna.LikelihoodRatioFilter;
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
public class LikelihoodRatioFilterMenu extends AbstractSiteFilter {

    private double threshold = 4d;
    private JTextField thresholdField = null;
    private LLRFilterMenuOptionPanel optionsPanel = new LLRFilterMenuOptionPanel();

    /**
     * Instantiates a new values filter with default values
     *
     * @param collection The dataCollection to filter
     * @throws com.xl.exception.REDException if the dataCollection isn't quantitated.
     */
    public LikelihoodRatioFilterMenu(DataCollection collection) throws REDException {
        super(collection);
    }

    @Override
    public String description() {
        return "Filter editing bases by statistic method (LLR).";
    }

    @Override
    protected void generateSiteList() {
        progressUpdated("Filtering RNA-editing sites by statistic method (LLR), please wait...", 0, 0);
        LikelihoodRatioFilter lf = new LikelihoodRatioFilter(databaseManager);
        lf.establishLLRResultTable(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME);
        lf.executeLLRFilter(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME, DatabaseManager.DNA_VCF_RESULT_TABLE_NAME, parentTable, threshold);
        DatabaseManager.getInstance().distinctTable(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME);

        Vector<Site> sites = Query.queryAllEditingSites(DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME);
        SiteList newList = new SiteList(parentList, DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME, description(),
                DatabaseManager.LLR_FILTER_RESULT_TABLE_NAME);
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
        return stores.length != 0 && thresholdField.getText().length() != 0;
    }

    @Override
    public String name() {
        return "Likelihood Ratio Test Filter";
    }

    @Override
    protected String listName() {
        return "LLR filter";
    }


    /**
     * The ValuesFilterOptionPanel.
     */
    private class LLRFilterMenuOptionPanel extends AbstractOptionPanel implements KeyListener {

        /**
         * Instantiates a new values filter option panel.
         */
        public LLRFilterMenuOptionPanel() {
            super(collection);
        }

        public void valueChanged(ListSelectionEvent lse) {
            System.out.println(LikelihoodRatioFilterMenu.class.getName() + ":valueChanged()");
            Object[] objects = dataList.getSelectedValues();
            stores = new DataStore[objects.length];
            for (int i = 0; i < stores.length; i++) {
                stores[i] = (DataStore) objects[i];
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
            choicePanel.setLayout(new BorderLayout());

            JPanel ratioPanel = new JPanel();
            ratioPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.gridy = 0;
            c.gridx = 0;
            c.weightx = 0.5;
            c.weighty = 0.5;
            c.fill = GridBagConstraints.HORIZONTAL;
            ratioPanel.add(new JLabel("The ratio of a site whether it is RNA editing or not :"), c);

            c.gridx = 1;
            c.weightx = 0.1;
            thresholdField = new JTextField(3);
            thresholdField.setText(threshold + "");
            thresholdField.addKeyListener(this);
            ratioPanel.add(thresholdField, c);

            choicePanel.add(ratioPanel, BorderLayout.CENTER);

            JTextArea textArea = new JTextArea();
            textArea.setText("If the ratio is 4, it means that the probability of happening RNA editing is 10000(10e4) times more than that of not happening.");
            textArea.setLineWrap(true);
            textArea.setEditable(false);
            choicePanel.add(textArea, BorderLayout.SOUTH);
            return choicePanel;
        }

        @Override
        protected String getPanelDescription() {
            return "Likelihood Ratio test is used to reduce the errors in detecting RNA editing sites caused by technical artifacts (e.g., sequencing errors).";
        }

        @Override
        public void keyTyped(KeyEvent e) {
            int keyChar = e.getKeyChar();
            if (!((keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9) || (keyChar == KeyEvent.VK_PERIOD)))
                e.consume();
        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            JTextField f = (JTextField) e.getSource();
            if (f.getText().length() == 0) {
                thresholdField.setText("");
            } else if (f == thresholdField) {
                threshold = Double.parseDouble(thresholdField.getText());
            }
            optionsChanged();
        }

    }
}
