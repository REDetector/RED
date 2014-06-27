package com.xl.menu;

/**
 * Copyright 2012-13 Simon Andrews
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

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;

import javax.swing.*;
import java.awt.*;

public class MainREDToolbar extends REDToolbar {

    /**
     * The jump to position button.
     */
    private JButton jumpToPositionButton;

    /**
     * The change annotation button.
     */
    private JButton changeAnnotationButton;

    /**
     * The find feature button.
     */
    private JButton findFeatureButton;

    /**
     * Instantiates a new seq monk toolbar.
     *
     * @param menu the menu
     */
    public MainREDToolbar(REDMenu menu) {

        super(menu);

        JButton posNegButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/above_and_below.png")));
        posNegButton.setActionCommand("scale_negative");
        posNegButton.setToolTipText("Positive and Negative Scale");
        posNegButton.addActionListener(menu);
        add(posNegButton);

        JButton posButton = new JButton(new ImageIcon(
                ClassLoader
                        .getSystemResource("resources/toolbar/above_only.png")));
        posButton.setActionCommand("scale_positive");
        posButton.setToolTipText("Positive Scale");
        posButton.addActionListener(menu);

        add(posButton);

        addSeparator();

        JButton dynamicColoursButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/dynamic_data_colours.png")));
        dynamicColoursButton.setActionCommand("data_colour_dynamic");
        dynamicColoursButton.setToolTipText("Dynamic Data Colours");
        dynamicColoursButton.addActionListener(menu);

        add(dynamicColoursButton);

        JButton staticColoursButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/static_data_colours.png")));
        staticColoursButton.setActionCommand("data_colour_fixed");
        staticColoursButton.setToolTipText("Static Data Colours");
        staticColoursButton.addActionListener(menu);

        add(staticColoursButton);

        addSeparator();

        JButton readsOnlyButton = new JButton(new ImageIcon(
                ClassLoader
                        .getSystemResource("resources/toolbar/reads_only.png")));
        readsOnlyButton.setActionCommand("data_reads");
        readsOnlyButton.setToolTipText("Show only Reads");
        readsOnlyButton.addActionListener(menu);

        add(readsOnlyButton);

        JButton probesOnlyButton = new JButton(
                new ImageIcon(ClassLoader
                        .getSystemResource("resources/toolbar/probes_only.png")));
        probesOnlyButton.setActionCommand("data_probes");
        probesOnlyButton.setToolTipText("Show only Probes");
        probesOnlyButton.addActionListener(menu);

        add(probesOnlyButton);

        JButton probesAndReadsButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/probes_and_reads.png")));
        probesAndReadsButton.setActionCommand("data_reads_probes");
        probesAndReadsButton.setToolTipText("Show Probes and Reads");
        probesAndReadsButton.addActionListener(menu);

        add(probesAndReadsButton);

        addSeparator();

        JButton lowDensityReadsButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/low_density_reads.png")));
        lowDensityReadsButton.setActionCommand("read_density_low");
        lowDensityReadsButton.setToolTipText("Low Density Read Display");
        lowDensityReadsButton.addActionListener(menu);

        add(lowDensityReadsButton);

        JButton mediumDensityReadsButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/medium_density_reads.png")));
        mediumDensityReadsButton.setActionCommand("read_density_medium");
        mediumDensityReadsButton.setToolTipText("Medium Density Read Display");
        mediumDensityReadsButton.addActionListener(menu);

        add(mediumDensityReadsButton);

        JButton highDensityReadsButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/high_density_reads.png")));
        highDensityReadsButton.setActionCommand("read_density_high");
        highDensityReadsButton.setToolTipText("High Density Read Display");
        highDensityReadsButton.addActionListener(menu);

        add(highDensityReadsButton);

        addSeparator();

        JButton combinePackedReadsButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/combine_pack_reads.png")));
        combinePackedReadsButton.setActionCommand("read_pack_combine");
        combinePackedReadsButton.setToolTipText("Combine Packed Reads");
        combinePackedReadsButton.addActionListener(menu);

        add(combinePackedReadsButton);

        JButton splitPackedReadsButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/split_pack_reads.png")));
        splitPackedReadsButton.setActionCommand("read_pack_separate");
        splitPackedReadsButton.setToolTipText("Split Packed Reads");
        splitPackedReadsButton.addActionListener(menu);

        add(splitPackedReadsButton);

        addSeparator();

        changeAnnotationButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/change_annotation.png")));
        changeAnnotationButton.setActionCommand("view_annotation_tracks");
        changeAnnotationButton.setToolTipText("Change Annotation Tracks");
        changeAnnotationButton.addActionListener(menu);

        add(changeAnnotationButton);

        JButton changeDataButton = new JButton(
                new ImageIcon(ClassLoader
                        .getSystemResource("resources/toolbar/change_data.png")));
        changeDataButton.setActionCommand("view_data_tracks");
        changeDataButton.setToolTipText("Change Data Tracks");
        changeDataButton.addActionListener(menu);

        add(changeDataButton);

        addSeparator();

        JButton changeDataZoomButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/change_data_zoom.png")));
        changeDataZoomButton.setActionCommand("view_set_zoom");
        changeDataZoomButton.setToolTipText("Change Data Zoom Level");
        changeDataZoomButton.addActionListener(menu);

        add(changeDataZoomButton);

        addSeparator();

        findFeatureButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/find_feature.png")));
        findFeatureButton.setActionCommand("find_feature");
        findFeatureButton.setToolTipText("Find Feature");
        findFeatureButton.addActionListener(menu);

        add(findFeatureButton);

        jumpToPositionButton = new JButton(
                new ImageIcon(
                        ClassLoader
                                .getSystemResource("resources/toolbar/jump_to_position.png")));
        jumpToPositionButton.setActionCommand("goto_position");
        jumpToPositionButton.setToolTipText("Go to Position");
        jumpToPositionButton.addActionListener(menu);

        add(jumpToPositionButton);

        addSeparator();

        reset();
    }

    /**
     * Genome loaded.
     */
    public void genomeLoaded() {
        // Enable the buttons relating only to the genome
        jumpToPositionButton.setEnabled(true);
        jumpToPositionButton.setFocusable(false);
        changeAnnotationButton.setEnabled(true);
        changeAnnotationButton.setFocusable(false);
        findFeatureButton.setEnabled(true);
        findFeatureButton.setFocusable(false);
    }

    public boolean showByDefault() {
        return true;
    }

    public void reset() {
        // We can disable everything on the toolbar.
        Component[] c = getComponents();
        for (int i = 0; i < c.length; i++) {
            c[i].setEnabled(false);
            c[i].setFocusable(false);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(boolean enable) {

		/*
         * Disabling the toolbar still leaves the buttons active which isn't
		 * what we want so we'll disable everything inside it as well.
		 * 
		 * We also don't want the toolbar taking focus as it breaks the
		 * navigation using arrow keys so we explicitly disable this.
		 */

        super.setEnabled(enable);

        Component[] c = getComponents();
        for (int i = 0; i < c.length; i++) {
            c[i].setEnabled(enable);
            c[i].setFocusable(false);
        }

    }

    public void dataSetAdded(DataSet d) {

        // Now we can enable everything on the toolbar.
        Component[] c = getComponents();
        for (int i = 0; i < c.length; i++) {
            c[i].setEnabled(true);
            c[i].setFocusable(false);
        }

    }

    public void dataSetsRemoved(DataSet[] d) {

        // If there are no datasets loaded we need to reset everything and
        // just go with the genome loaded defaults
        if (collection().getAllDataSets().length == 0) {
            reset();
            genomeLoaded();
        }
    }

    public void dataGroupAdded(DataGroup g) {
    }

    public void dataGroupsRemoved(DataGroup[] g) {
    }

    public void dataSetRenamed(DataSet d) {
    }

    public void dataGroupRenamed(DataGroup g) {
    }

    public void dataGroupSamplesChanged(DataGroup g) {
    }

    public void probeSetReplaced(ProbeSet p) {
    }

    public void activeDataStoreChanged(DataStore s) {

    }

    public void activeProbeListChanged(ProbeList l) {
    }

    public String name() {
        return "Main Toolbar";
    }

}
