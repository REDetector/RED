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
import com.xl.display.dialog.SearchCommandDialog;
import com.xl.main.RedApplication;
import com.xl.utils.namemanager.MenuUtils;

import javax.swing.*;
import java.awt.*;

/**
 * The Class RedToolbar is an implementation of the abstract toolbar.
 */
public class RedToolbar extends AbstractToolbar implements DatabaseListener {

    /**
     * The jump to position button.
     */
    private JButton jumpToPositionButton;
    /**
     * The find feature button.
     */
    private JButton findFeatureButton;
    /**
     * Show only reads button.
     */
    private JButton readsOnlyButton;
    /**
     * Show only RNA editing sites button.
     */
    private JButton sitesOnlyButton;
    /**
     * Show reads and RNA editing sites button.
     */
    private JButton sitesAndReadsButton;
    /**
     * Switch sample button.
     */
    private JButton switchSampleButton;
    /**
     * Database connection button.
     */
    private JButton databaseConnection;
    /**
     * Preference button.
     */
    private JButton preferencesButton;
    /**
     * The data zoom selector.
     */
    private DataZoomSelector dataZoomSelector;

    private SearchCommandDialog searchCommand;

    /**
     * Instantiates a new red toolbar.
     *
     * @param menu the menu
     */
    public RedToolbar(RedMenu menu) {

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
        databaseConnection.setActionCommand(MenuUtils.CONNECT_TO_DATABASE);
        databaseConnection.setToolTipText("Connect to database");
        databaseConnection.addActionListener(menu);
        add(databaseConnection);

        switchSampleButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/switch.png")));
        switchSampleButton.setActionCommand(MenuUtils.SWITCH_SAMPLES_OR_MODE);
        switchSampleButton.setToolTipText("Switch samples or mode");
        switchSampleButton.addActionListener(menu);
        add(switchSampleButton);

        addSeparator();

        preferencesButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("resources/toolbar/preferences.png")));
        preferencesButton.setActionCommand(MenuUtils.PREFERENCES);
        preferencesButton.setToolTipText("Set preferences as you like");
        preferencesButton.addActionListener(menu);
        add(preferencesButton);

        addSeparator();
        DatabaseManager.getInstance().addDatabaseListener(this);
        reset();
    }

    @Override
    public void reset() {
        // We can disable everything on the toolbar.
        Component[] components = getComponents();
        for (Component component : components) {
            component.setEnabled(false);
            component.setFocusable(false);
        }
    }

    /**
     * Genome loaded.
     */
    public void genomeLoaded() {
        // Enable the buttons relating only to the genome
        readsOnlyButton.setEnabled(true);
        sitesOnlyButton.setEnabled(true);
        sitesAndReadsButton.setEnabled(true);
        jumpToPositionButton.setEnabled(true);
        jumpToPositionButton.setFocusable(false);
        findFeatureButton.setEnabled(true);
        databaseConnection.setEnabled(true);
        preferencesButton.setEnabled(true);
        if (dataZoomSelector == null) {
            dataZoomSelector = new DataZoomSelector(RedApplication.getInstance());
            add(dataZoomSelector.getContentPane());
        }
        if (searchCommand == null) {
            addSeparator();
            searchCommand = new SearchCommandDialog();
            add(searchCommand.getContentPane());
        }
    }

    @Override
    public boolean showByDefault() {
        return true;
    }

    /**
     * The name.
     *
     * @return the name.
     */
    public String name() {
        return "Main Toolbar";
    }

    @Override
    public void setEnabled(boolean enable) {

		/*
         * Disabling the toolbar still leaves the buttons active which isn't what we want so we'll disable everything inside it as well.
		 *
		 * We also don't want the toolbar taking focus as it breaks the navigation using arrow keys so we explicitly disable this.
		 */
        super.setEnabled(enable);

        Component[] components = getComponents();
        for (Component component : components) {
            component.setEnabled(true);
            component.setFocusable(false);
        }

    }

    @Override
    public void databaseChanged(String databaseName, String sampleName) {
    }

    @Override
    public void databaseConnected() {
        switchSampleButton.setEnabled(true);
    }

    @Override
    public void activeDataChanged(DataStore dataStore, SiteList siteList) {
        if (dataStore != null) {
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
