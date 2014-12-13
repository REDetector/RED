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

import com.xl.datatypes.feature.Feature;
import com.xl.utils.Strand;

import javax.swing.table.AbstractTableModel;

/**
 * The Class FeatureAttributeTable provides a table to view some properties for a feature, which is shown when double click the activated feature on Feature
 * Track.
 */
public class FeatureAttributeTable extends AbstractTableModel {
    /**
     * The feature.
     */
    private Feature feature = null;

    /**
     * Initiate a new feature attribute table.
     *
     * @param feature the feature.
     */
    public FeatureAttributeTable(Feature feature) {
        this.feature = feature;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        return 7;
    }


    @Override
    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return "Attribute";
            case 1:
                return "Parameter";
            default:
                return null;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {

        switch (col) {
            case 0:
                switch (row) {
                    case 0:
                        return "Name";
                    case 1:
                        return "Chromosome";
                    case 2:
                        return "Strand";
                    case 3:
                        return "Transcription";
                    case 4:
                        return "Coding Region";
                    case 5:
                        return "Exon Numbers";
                    case 6:
                        return "Exons";
                    default:
                        return null;
                }
            case 1:
                switch (row) {
                    case 0:
                        return feature.getAliasName();
                    case 1:
                        return feature.getChr();
                    case 2:
                        return Strand.parseStrand(feature.getStrand());
                    case 3:
                        return feature.getTxLocation().toString();
                    case 4:
                        return feature.getCdsLocation().toString();
                    case 5:
                        return feature.getExonLocations().size();
                    case 6:
                        return feature.getExonLocations();
                    default:
                        return null;
                }
            default:
                return null;
        }

    }

}
