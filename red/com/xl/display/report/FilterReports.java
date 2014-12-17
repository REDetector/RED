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

package com.xl.display.report;

import com.xl.database.Query;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.SiteBean;
import com.xl.datatypes.sites.SiteList;
import com.xl.display.dataviewer.DataTreeRenderer;
import com.xl.display.dataviewer.SiteSetTreeModel;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.util.Vector;

/**
 * Created by Xing Li on 2014/10/5.
 * <p/>
 * The Class FilterReports provides a table output for all information derived from a table in database.
 */
public class FilterReports extends Report implements TreeSelectionListener {
    /**
     * The option panel.
     */
    private JPanel optionsPanel = null;
    /**
     * The site set tree.
     */
    private JTree siteSetTree;
    /**
     * The selected site list.
     */
    private Object currentSiteList;

    /**
     * Instantiates a new report.
     *
     * @param dataStore Data Store to use for the report
     */
    public FilterReports(DataStore dataStore) {
        super(dataStore);
    }

    @Override
    public String name() {
        return "Filter Reporter";
    }

    @Override
    public JPanel getOptionsPanel() {
        if (optionsPanel != null) return optionsPanel;
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BorderLayout());

        JPanel siteViewer = new JPanel();
        siteViewer.setLayout(new GridBagLayout());
        siteViewer.setBackground(Color.WHITE);
        GridBagConstraints con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 0;
        con.weightx = 0.1;
        con.weighty = 0.01;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.anchor = GridBagConstraints.FIRST_LINE_START;
        SiteSetTreeModel siteModel = new SiteSetTreeModel(dataStore);
        siteSetTree = new UnfocusableTree(siteModel);
        siteSetTree.addTreeSelectionListener(this);
        siteSetTree.setCellRenderer(new DataTreeRenderer());
        siteViewer.add(siteSetTree, con);
        // This nasty bit just makes the trees squash up to the top of the display area.
        con.gridy++;
        con.weighty = 1;
        con.fill = GridBagConstraints.BOTH;
        siteViewer.add(new JLabel(" "), con);

        optionsPanel.add(siteViewer, BorderLayout.CENTER);

        return optionsPanel;
    }

    @Override
    public void generateReport() {
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public boolean isReady() {
        return currentSiteList != null;
    }

    @Override
    public void run() {
        SiteList selectedSiteList = (SiteList) currentSiteList;
        Vector<SiteBean> sites = Query.queryAllEditingInfo(selectedSiteList.getTableName());
        SiteBeanTableModel model = new SiteBeanTableModel(sites.toArray(new SiteBean[0]));
        reportComplete(model);
    }

    @Override
    public void valueChanged(TreeSelectionEvent tse) {
        if (tse.getSource() == siteSetTree) {
            if (siteSetTree.getSelectionPath() == null) {
                currentSiteList = null;
            } else {
                currentSiteList = siteSetTree.getSelectionPath().getLastPathComponent();
            }
            optionsChanged();
        }
    }

    /**
     * An extension of JTree which is unable to take keyboard focus.
     * <p/>
     * This class is needed to make sure the arrow key navigation always works in the chromosome view. If either of the JTrees can grab focus they will
     * intercept the arrow key events and just move the selections on the tree.
     */
    private class UnfocusableTree extends JTree {

        /**
         * Instantiates a new unfocusable tree.
         *
         * @param m the tree model
         */
        public UnfocusableTree(TreeModel m) {
            super(m);
            this.setFocusable(false);
        }
    }

    /**
     * A TableModel representing the results of the AnnotatedListReport.
     */
    private class SiteBeanTableModel extends AbstractTableModel {

        private SiteBean[] siteBeans;

        /**
         * Instantiates a new annotation table model.
         *
         * @param siteBeans The starting site list
         */
        public SiteBeanTableModel(SiteBean[] siteBeans) {
            this.siteBeans = siteBeans;
        }

        @Override
        public int getRowCount() {
            return siteBeans.length;
        }

        @Override
        public int getColumnCount() {
            return 9;
        }

        @Override
        public Object getValueAt(int r, int c) {
            switch (c) {
                case 0:
                    return siteBeans[r].getChr();
                case 1:
                    return siteBeans[r].getPos();
                case 2:
                    return siteBeans[r].getId();
                case 3:
                    return siteBeans[r].getRef();
                case 4:
                    return siteBeans[r].getAlt();
                case 5:
                    return siteBeans[r].getQual();
                case 6:
                    return siteBeans[r].getLevel();
                case 7:
                    return siteBeans[r].getPvalue();
                case 8:
                    return siteBeans[r].getFdr();
                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int c) {
            switch (c) {
                case 0:
                    return "Chromosome";
                case 1:
                    return "Position";
                case 2:
                    return "ID";
                case 3:
                    return "Reference Base";
                case 4:
                    return "Alternative Base";
                case 5:
                    return "Quality";
                case 6:
                    return "Editing Level";
                case 7:
                    return "p-value";
                case 8:
                    return "FDR";
                default:
                    return null;
            }
        }

        @Override
        public Class getColumnClass(int c) {
            switch (c) {
                case 0:
                    return String.class;
                case 1:
                    return Integer.class;
                case 2:
                    return String.class;
                case 3:
                    return Character.class;
                case 4:
                    return Character.class;
                case 5:
                    return Double.class;
                case 6:
                    return Double.class;
                case 7:
                    return Double.class;
                case 8:
                    return Double.class;
                default:
                    return null;
            }
        }

    }
}
