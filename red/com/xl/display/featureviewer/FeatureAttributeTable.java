package com.xl.display.featureviewer;

/**
 * Copyright 2010-13 Simon Andrews
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

import com.xl.datatypes.sequence.Location;
import com.xl.utils.Strand;

import javax.swing.table.AbstractTableModel;

public class FeatureAttributeTable extends AbstractTableModel {

    private Feature feature = null;

    public FeatureAttributeTable(Feature feature) {
        this.feature = feature;
    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return 7;
    }

    public Class<?> getColumnClass(int col) {
        return String.class;
    }

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
                        return feature.getExonLocations().length;
                    case 6:
                        StringBuffer str = new StringBuffer();
                        for (Location exon : feature.getExonLocations()) {
                            str.append(exon.toString() + ",");
                        }
                        if (str.length() != 0) {
                            str.deleteCharAt(str.length() - 1);
                        }
                        return str.toString();
                    default:
                        return null;
                }
            default:
                return null;
        }

    }

}
