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

package com.xl.display.dataviewer;

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.sites.SiteList;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * The Class DataTreeRenderer sets the look of the DataViewer.
 */
public class DataTreeRenderer extends DefaultTreeCellRenderer {

    /**
     * The data set icon.
     */
    private Icon dataSetIcon = new ImageIcon(ClassLoader.getSystemResource("resources/dataset_icon.png"));

    /**
     * The data group icon.
     */
    private Icon dataGroupIcon = new ImageIcon(ClassLoader.getSystemResource("resources/datagroup_icon.png"));

    /**
     * The site list icon.
     */
    private Icon siteListIcon = new ImageIcon(ClassLoader.getSystemResource("resources/sitelist_icon.png"));

    /**
     * The annotation set icon.
     */
    private Icon annotationSetIcon = new ImageIcon(ClassLoader.getSystemResource("resources/annotation_set_icon.png"));

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent (javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int,
     * boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        if (value instanceof DataSet) {
            JLabel label = new JLabel(value.toString(), dataSetIcon, JLabel.LEFT);

            if (selected) {
                label.setOpaque(true);
                label.setBackground(Color.LIGHT_GRAY);
            }
            return label;
        } else if (value instanceof DataGroup) {
            JLabel label = new JLabel(value.toString(), dataGroupIcon, JLabel.LEFT);

            if (selected) {
                label.setOpaque(true);
                label.setBackground(Color.LIGHT_GRAY);
            }
            return label;
        } else if (value instanceof SiteList) {
            JLabel label = new JLabel(value.toString(), siteListIcon, JLabel.LEFT);
            if (selected) {
                label.setOpaque(true);
                label.setBackground(Color.LIGHT_GRAY);
            }
            return label;
        } else if (value instanceof AnnotationSet) {
            JLabel label = new JLabel(value.toString(), annotationSetIcon, JLabel.LEFT);
            if (selected) {
                label.setOpaque(true);
                label.setBackground(Color.LIGHT_GRAY);
            }
            return label;
        } else {
            return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
    }

}
