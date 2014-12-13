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

package com.xl.display.featureviewer;

import com.sun.java.TableSorter;
import com.xl.datatypes.feature.Feature;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.Strand;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * The Class FeatureListViewer displays the hits when searching through the features.
 */

public class FeatureListViewer extends JTable implements MouseListener {

    private REDApplication application;

    /**
     * Instantiates a new feature list viewer.
     *
     * @param features the features
     */
    public FeatureListViewer(Feature[] features) {

        this.application = REDApplication.getInstance();

        String[] headers = new String[]{"ID", "Name", "Strand", "Chromosome", "Transcription", "CDS", "EXON"};
        Class[] classes = new Class[]{String.class, String.class, String.class, String.class, String.class, String.class, String.class};

        Object[][] rowData = new Object[features.length][headers.length];

        for (int i = 0; i < features.length; i++) {
            rowData[i][0] = features[i];
            rowData[i][1] = features[i].getAliasName();
            rowData[i][2] = Strand.parseStrand(features[i].getStrand());
            rowData[i][3] = features[i].getChr();
            rowData[i][4] = features[i].getTxLocation().toString();
            rowData[i][5] = features[i].getCdsLocation().toString();
            rowData[i][6] = features[i].getExonLocations();
        }

        TableSorter sorter = new TableSorter(new FeatureTableModel(rowData, headers, classes));
        setModel(sorter);
        addMouseListener(this);
        sorter.setTableHeader(getTableHeader());
    }

    public Feature[] getSelectedFeatures() {

        int[] selectedIndices = getSelectedRows();
        Feature[] features = new Feature[selectedIndices.length];

        for (int i = 0; i < features.length; i++) {
            features[i] = (Feature) getValueAt(selectedIndices[i], 0);
        }

        return features;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {
        // We're only interested in double clicks
        if (me.getClickCount() != 2)
            return;
        // This is only linked from the report JTable
        JTable t = (JTable) me.getSource();
        int r = t.getSelectedRow();
        Feature f = (Feature) t.getValueAt(r, 0);

        DisplayPreferences.getInstance().setLocation(application.dataCollection().genome().getChromosome(f.getChr()), f.getTxLocation().getStart(),
                f.getTxLocation().getEnd());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent arg0) {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent arg0) {
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent arg0) {
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent arg0) {
    }

    /**
     * The Class FeatureTableModel.
     */
    private class FeatureTableModel extends AbstractTableModel {

        /**
         * The data.
         */
        private Object[][] data;

        /**
         * The headers.
         */
        private String[] headers;

        /**
         * The classes.
         */
        private Class[] classes;

        /**
         * Instantiates a new feature table model.
         *
         * @param data    the data
         * @param headers the headers
         * @param classes the classes
         */
        public FeatureTableModel(Object[][] data, String[] headers, Class[] classes) {
            this.data = data;
            this.headers = headers;
            this.classes = classes;
        }

        /*
         * (non-Javadoc)
         *
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            return data.length;
        }

        /*
         * (non-Javadoc)
         *
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return data[0].length;
        }

        /*
         * (non-Javadoc)
         *
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int r, int c) {
            return data[r][c];
        }

        /*
         * (non-Javadoc)
         *
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        public String getColumnName(int c) {
            return headers[c];
        }

        /*
         * (non-Javadoc)
         *
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        public Class getColumnClass(int c) {
            return classes[c];
        }

        /*
         * (non-Javadoc)
         *
         * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
         */
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    }
}
