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

package com.xl.menu;

import com.xl.database.DatabaseListener;
import com.xl.database.DatabaseManager;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.SiteList;
import com.xl.display.dialog.DataZoomSelector;
import com.xl.main.REDApplication;
import com.xl.utils.namemanager.MenuUtils;

import javax.swing.*;
import java.awt.*;

public class MainREDToolbar extends REDToolbar implements DatabaseListener {

    /**
     * The jump to position button.
     */
    private JButton jumpToPositionButton;

    /**
     * The find feature button.
     */
    private JButton findFeatureButton;

    private JButton readsOnlyButton;
    private JButton sitesOnlyButton;
    private JButton sitesAndReadsButton;

    private JButton switchMode;

    private JButton databaseConnection;

    private JButton preferences;

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
        sitesOnlyButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/sites_only.png")));
        sitesOnlyButton.setActionCommand(MenuUtils.SHOW_PROBES_ONLY);
        sitesOnlyButton.setToolTipText("Show only Sites");
        sitesOnlyButton.addActionListener(menu);
        add(sitesOnlyButton);
        sitesAndReadsButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/sites_and_reads.png")));
        sitesAndReadsButton.setActionCommand(MenuUtils.SHOW_READS_AND_PROBES);
        sitesAndReadsButton.setToolTipText("Show Reads and Sites");
        sitesAndReadsButton.addActionListener(menu);
        add(sitesAndReadsButton);

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

        databaseConnection = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/database_connection.png")));
        databaseConnection.setActionCommand(MenuUtils.CONNECT_TO_MYSQL);
        databaseConnection.setToolTipText("Connect to database");
        databaseConnection.addActionListener(menu);
        add(databaseConnection);

        switchMode = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/switch.png")));
        switchMode.setActionCommand(MenuUtils.SWITCH_SAMPLES_OR_MODE);
        switchMode.setToolTipText("Switch samples or mode");
        switchMode.addActionListener(menu);
        add(switchMode);

        addSeparator();

        preferences = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/preferences.png")));
        preferences.setActionCommand(MenuUtils.PREFERENCES);
        preferences.setToolTipText("Set preferences as you like");
        preferences.addActionListener(menu);
        add(preferences);

        addSeparator();
        DatabaseManager.getInstance().addDatabaseListener(this);
        reset();
    }

    /**
     * Genome loaded.
     */
    public void genomeLoaded() {
        // Enable the buttons relating only to the genome
        System.out.println(this.getClass().getName() + ":genomeLoaded()");
        readsOnlyButton.setEnabled(true);
        sitesOnlyButton.setEnabled(true);
        sitesAndReadsButton.setEnabled(true);
        jumpToPositionButton.setEnabled(true);
        jumpToPositionButton.setFocusable(false);
        findFeatureButton.setEnabled(true);
        databaseConnection.setEnabled(true);
        preferences.setEnabled(true);
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

        Component[] components = getComponents();
        for (Component component : components) {
            component.setEnabled(true);
            component.setFocusable(false);
        }

    }

    public String name() {
        return "Main Toolbar";
    }

    @Override
    public void databaseChanged(String databaseName, String sampleName) {
    }

    @Override
    public void databaseConnected() {
        switchMode.setEnabled(true);
    }

    @Override
    public void activeDataChanged(DataStore d, SiteList l) {
        if (d != null) {
            // Now we can enable everything on the toolbar.
            Component[] components = getComponents();
            for (Component component : components) {
                component.setEnabled(true);
                component.setFocusable(false);
            }
        } else {
            reset();
            genomeLoaded();
        }
    }
}
