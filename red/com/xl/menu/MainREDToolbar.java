package com.xl.menu;

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.dialog.DataZoomSelector;
import com.xl.main.REDApplication;
import com.xl.utils.namemanager.MenuUtils;

import javax.swing.*;
import java.awt.*;

public class MainREDToolbar extends REDToolbar {

    /**
     * The jump to position button.
     */
    private JButton jumpToPositionButton;

    /**
     * The find feature button.
     */
    private JButton findFeatureButton;

    private JButton readsOnlyButton;
    private JButton probesOnlyButton;
    private JButton probesAndReadsButton;

    private DataZoomSelector dataZoomSelector;

    /**
     * Instantiates a new red toolbar.
     *
     * @param menu the menu
     */
    public MainREDToolbar(REDMenu menu) {

        super(menu);

        readsOnlyButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/reads_only.png")));
        readsOnlyButton.setActionCommand(MenuUtils.SHOW_READS_ONLY);
        readsOnlyButton.setToolTipText("Show only Reads");
        readsOnlyButton.addActionListener(menu);
        add(readsOnlyButton);
        probesOnlyButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/probes_only.png")));
        probesOnlyButton.setActionCommand(MenuUtils.SHOW_PROBES_ONLY);
        probesOnlyButton.setToolTipText("Show only Probes");
        probesOnlyButton.addActionListener(menu);
        add(probesOnlyButton);
        probesAndReadsButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/probes_and_reads.png")));
        probesAndReadsButton.setActionCommand(MenuUtils.SHOW_READS_AND_PROBES);
        probesAndReadsButton.setToolTipText("Show Reads and Probes");
        probesAndReadsButton.addActionListener(menu);
        add(probesAndReadsButton);
        addSeparator();

        findFeatureButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/find_feature.png")));
        findFeatureButton.setActionCommand(MenuUtils.FIND);
        findFeatureButton.setToolTipText("Find Feature");
        findFeatureButton.addActionListener(menu);

        add(findFeatureButton);

        jumpToPositionButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/jump_to_position.png")));
        jumpToPositionButton.setActionCommand(MenuUtils.GOTO_POSITION);
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
        System.out.println(this.getClass().getName() + ":genomeLoaded()");
        readsOnlyButton.setEnabled(true);
        probesOnlyButton.setEnabled(true);
        probesAndReadsButton.setEnabled(true);
        jumpToPositionButton.setEnabled(true);
        jumpToPositionButton.setFocusable(false);
        findFeatureButton.setEnabled(true);
        findFeatureButton.setFocusable(false);
        if (dataZoomSelector == null) {
            dataZoomSelector = new DataZoomSelector(REDApplication.getInstance());
            add(dataZoomSelector.getContentPane());
        }

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
