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

import com.xl.database.DatabaseManager;
import com.xl.database.Query;
import com.xl.database.TableCreator;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.exception.REDException;
import com.xl.filter.denovo.KnownSNPFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.Vector;

/**
 * The Class KnownSNPFilterPanel is a rule-based filter panel to provide some parameters to be set as user's preference if there is any choice.
 */
public class KnownSNPFilterPanel extends AbstractSiteFilter {
    private final Logger logger = LoggerFactory.getLogger(KnownSNPFilterPanel.class);
    /**
     * The known SNP filter option panel.
     */
    private KnownSNPFilterOptionPanel optionsPanel = new KnownSNPFilterOptionPanel();

    /**
     * Instantiates a new known SNP filter.
     *
     * @param dataStore The data store to filter.
     */
    public KnownSNPFilterPanel(DataStore dataStore) throws REDException {
        super(dataStore);
    }

    @Override
    protected String listName() {
        return "Known SNP Filter";
    }

    @Override
    protected void generateSiteList() throws SQLException {
        progressUpdated("Filtering RNA editing sites by dbSNP filter, please wait...", 0, 0);
        logger.info("Filtering RNA editing sites by dbSNP filter");
        String linearTableName = currentSample + "_" + parentList.getFilterName() + "_" + DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME;
        if (!TableCreator.createFilterTable(parentList.getTableName(), linearTableName)) {
            progressCancelled();
            return;
        }
        KnownSNPFilter dbsnpFilter = new KnownSNPFilter(databaseManager);
        dbsnpFilter.executeDbSNPFilter(DatabaseManager.DBSNP_DATABASE_TABLE_NAME, linearTableName, parentList.getTableName());
        DatabaseManager.getInstance().distinctTable(linearTableName);

        Vector<Site> sites = Query.queryAllEditingSites(linearTableName);
        SiteList newList = new SiteList(parentList, listName(), DatabaseManager.DBSNP_FILTER_RESULT_TABLE_NAME, linearTableName, description());
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
    public boolean isReady() {
        return parentList != null;
    }

    @Override
    public boolean hasOptionsPanel() {
        return true;
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    public String name() {
        return "Known SNP Filter";
    }

    @Override
    public String description() {
        return "Filter RNA editing sites by dbSNP database.";
    }

    /**
     * The known SNP filter option panel.
     */
    private class KnownSNPFilterOptionPanel extends AbstractOptionPanel implements KeyListener {

        /**
         * Instantiates a new known SNP filter option panel.
         */
        public KnownSNPFilterOptionPanel() {
            super(dataStore);
        }

        @Override
        public void valueChanged(TreeSelectionEvent tse) {
            Object selectedItem = siteTree.getSelectionPath().getLastPathComponent();
            if (selectedItem instanceof SiteList) {
                parentList = (SiteList) selectedItem;
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
            if (f.getText().length() == 0) {
                return;
            }
            optionsChanged();
        }

        @Override
        protected boolean hasChoicePanel() {
            return false;
        }

        @Override
        protected JPanel getChoicePanel() {
            return null;
        }

        @Override
        protected String getPanelDescription() {
            return "RNA-seq variants that were known SNPs in DNA level are excluded for eliminating germline variants from dbSNP database " +
                    "(e.g., db-snp_138.hg19.vcf).";
        }
    }
}
