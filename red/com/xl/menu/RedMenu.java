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

/*
 * Created by JFormDesigner on Tue Nov 12 11:20:42 GMT 2013
 */

package com.xl.menu;

import com.xl.database.DatabaseListener;
import com.xl.database.DatabaseManager;
import com.xl.database.DatabaseSelector;
import com.xl.database.UserPasswordDialog;
import com.xl.datatypes.DataStore;
import com.xl.display.chromosomeviewer.ChromosomeViewer;
import com.xl.display.dialog.*;
import com.xl.display.dialog.gotodialog.GoToDialog;
import com.xl.display.dialog.gotodialog.GoToWindowDialog;
import com.xl.display.panel.ToolbarPanel;
import com.xl.display.report.FilterReports;
import com.xl.display.report.ReportOptions;
import com.xl.display.report.SitesDistributionHistogram;
import com.xl.display.report.VariantDistributionHistogram;
import com.xl.exception.NetworkException;
import com.xl.exception.RedException;
import com.xl.filter.filterpanel.*;
import com.xl.help.HelpDialog;
import com.xl.main.Global;
import com.xl.main.RedApplication;
import com.xl.net.genomes.UpdateChecker;
import com.xl.parsers.annotationparsers.AnnotationParserRunner;
import com.xl.parsers.annotationparsers.UcscRefGeneParser;
import com.xl.parsers.dataparsers.BamFileParser;
import com.xl.parsers.dataparsers.FastaFileParser;
import com.xl.preferences.DisplayPreferences;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.imagemanager.ImageSaver;
import com.xl.utils.namemanager.MenuUtils;
import com.xl.utils.ui.OptionDialogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

/**
 * The Class RedMenu is the main menu in RED.
 */
public class RedMenu extends JMenuBar implements ActionListener, DatabaseListener {
    private final Logger logger = LoggerFactory.getLogger(RedMenu.class);
    /**
     * The application.
     */
    private RedApplication application;
    /**
     * The toolbar panel;
     */
    private ToolbarPanel toolbarPanel;
    /**
     * Main RED toolbar in the toolbar panel.
     */
    private AbstractToolbar redToolbar;

    /**
     * File menu.
     */
    private JMenuItem newProject;
    private JMenuItem openProject;
    private JMenuItem saveProject;
    private JMenuItem saveProjectAs;
    private JMenuItem connectToDatabase;
    private JMenu importDataMenu;
    private JMenuItem importToDatabase;
    private JMenuItem importRna;
    private JMenuItem importDna;
    private JMenuItem importFasta;
    private JMenuItem importAnnotation;
    private JMenu exportImage;
    private JMenuItem genomeView;
    private JMenuItem chromosomeView;
    private JMenu reopenProject;
    private JMenuItem clearAboveLists;
    private JMenuItem exit;

    /**
     * Edit menu.
     */
    private JMenu editMenu;
    private JCheckBoxMenuItem showToolbar;
    private JCheckBoxMenuItem showDirectoryPanel;
    private JCheckBoxMenuItem showGenomePanel;
    private JCheckBoxMenuItem showChromosomePanel;
    private JCheckBoxMenuItem showFeaturePanel;
    private JCheckBoxMenuItem showStatusPanel;
    private JMenuItem setDataTracks;
    private JMenuItem find;
    private JMenuItem preference;

    /**
     * View menu.
     */
    private JMenu viewMenu;
    private JMenuItem zoomIn;
    private JMenuItem zoomOut;
    private JMenuItem setZoomLevel;
    private JMenuItem moveLeft;
    private JMenuItem moveRight;
    private JMenu gotoMenu;
    private JMenuItem gotoPosition;
    private JMenuItem gotoWindow;

    /**
     * Filter menu.
     */
    private JMenu filterMenu;
    private JMenuItem qualityControlFilter;
    private JMenuItem editingTypeFilter;
    private JMenuItem knownSNPFilter;
    private JMenuItem dnaRnaFilter;
    private JMenuItem repeatRegionsFilter;
    private JMenuItem spliceJunctionFilter;
    private JMenu statisticalFilterMenu;
    private JMenuItem fetFilter;
    private JMenuItem llrFilter;

    /**
     * Reports menu.
     */
    private JMenu reportsMenu;
    private JMenuItem variantDistribution;
    private JMenuItem sitesDistribution;
    private JMenuItem filterReports;

    /**
     * Help menu.
     */
    private JMenu helpMenu;
    private JMenuItem helpContents;
    private JMenuItem helpOnline;
    private JMenuItem checkForUpdates;
    private JMenuItem aboutRED;

    /**
     * Initiate a new RED menu.
     *
     * @param application the application.
     */
    public RedMenu(RedApplication application) {
        this.application = application;
        DatabaseManager.getInstance().addDatabaseListener(this);
        initComponents();
    }

    /**
     * Init all components.
     */
    private void initComponents() {
        toolbarPanel = new ToolbarPanel();
        redToolbar = new RedToolbar(this);

        JMenu fileMenu = new JMenu();
        newProject = new JMenuItem();
        openProject = new JMenuItem();
        saveProject = new JMenuItem();
        saveProjectAs = new JMenuItem();
        importDataMenu = new JMenu();
        connectToDatabase = new JMenuItem();
        importToDatabase = new JMenuItem();
        importRna = new JMenuItem();
        importDna = new JMenuItem();
        importFasta = new JMenuItem();
        importAnnotation = new JMenuItem();
        exportImage = new JMenu();
        genomeView = new JMenuItem();
        chromosomeView = new JMenuItem();
        reopenProject = new JMenu();
        clearAboveLists = new JMenuItem();
        exit = new JMenuItem();

        editMenu = new JMenu();
        showToolbar = new JCheckBoxMenuItem(MenuUtils.SHOW_TOOLBAR, redToolbar.shown());
        showDirectoryPanel = new JCheckBoxMenuItem(MenuUtils.SHOW_DIRECTORY_PANEL, true);
        showGenomePanel = new JCheckBoxMenuItem(MenuUtils.SHOW_GENOME_PANEL, true);
        showChromosomePanel = new JCheckBoxMenuItem(MenuUtils.SHOW_CHROMOSOME_PANEL, true);
        showFeaturePanel = new JCheckBoxMenuItem(MenuUtils.SHOW_FEATURE_PANEL, true);
        showStatusPanel = new JCheckBoxMenuItem(MenuUtils.SHOW_STATUS_PANEL, true);

        gotoMenu = new JMenu();
        gotoPosition = new JMenuItem();
        gotoWindow = new JMenuItem();
        find = new JMenuItem();
        preference = new JMenuItem();
        viewMenu = new JMenu();
        zoomIn = new JMenuItem();
        zoomOut = new JMenuItem();
        setZoomLevel = new JMenuItem();
        setDataTracks = new JMenuItem();
        moveLeft = new JMenuItem();
        moveRight = new JMenuItem();

        filterMenu = new JMenu();
        qualityControlFilter = new JMenuItem();
        editingTypeFilter = new JMenuItem();
        knownSNPFilter = new JMenuItem();
        dnaRnaFilter = new JMenuItem();
        repeatRegionsFilter = new JMenuItem();
        spliceJunctionFilter = new JMenuItem();
        statisticalFilterMenu = new JMenu();
        fetFilter = new JMenuItem();
        llrFilter = new JMenuItem();

        reportsMenu = new JMenu();
        variantDistribution = new JMenuItem();
        sitesDistribution = new JMenuItem();
        filterReports = new JMenuItem();
        helpMenu = new JMenu();
        helpContents = new JMenuItem();
        helpOnline = new JMenuItem();
        checkForUpdates = new JMenuItem();
        aboutRED = new JMenuItem();

        // ======== fileMenu ========
        {
            fileMenu.setText(MenuUtils.FILE_MENU);
            fileMenu.setMnemonic('F');

            addJMenuItem(fileMenu, newProject, MenuUtils.NEW_PROJECT, KeyEvent.VK_N, true);
            addJMenuItem(fileMenu, openProject, MenuUtils.OPEN_PROJECT, KeyEvent.VK_O, true);
            addJMenuItem(fileMenu, saveProject, MenuUtils.SAVE_PROJECT, KeyEvent.VK_S, false);
            addJMenuItem(fileMenu, saveProjectAs, MenuUtils.SAVE_PROJECT_AS, KeyEvent.VK_W, false);
            fileMenu.addSeparator();
            // ======== import data ========
            {
                addJMenuItem(fileMenu, connectToDatabase, MenuUtils.CONNECT_TO_DATABASE, KeyEvent.VK_C, false);
                importDataMenu.setText(MenuUtils.IMPORT_DATA);
                addJMenuItem(importDataMenu, importToDatabase, MenuUtils.DATABASE, -1, false);
                addJMenuItem(importDataMenu, importFasta, MenuUtils.FASTA, -1, true);
                addJMenuItem(importDataMenu, importRna, MenuUtils.RNA, -1, true);
                importRna.setToolTipText("It's usually a BAM file for a RNA data set");
                addJMenuItem(importDataMenu, importDna, MenuUtils.DNA, -1, true);
                importDna.setToolTipText("It's usually a BAM file for a DNA data set");
                // We don't support annotation import now.
                addJMenuItem(importDataMenu, importAnnotation, MenuUtils.ANNOTATION, -1, false);
                fileMenu.add(importDataMenu);
                importDataMenu.setEnabled(false);
            }

            // ======== export image ========
            {
                exportImage.setText(MenuUtils.EXPORT_IMAGE);
                exportImage.setEnabled(false);
                addJMenuItem(exportImage, genomeView, MenuUtils.GENOME_VIEW, -1, true);
                addJMenuItem(exportImage, chromosomeView, MenuUtils.CHROMOSOME_VIEW, -1, true);
                fileMenu.add(exportImage);
            }

            fileMenu.addSeparator();

            // ======== Recently Opened Files ========
            {
                reopenProject.setText(MenuUtils.REOPEN_PROJECT);
                List<String> recentPaths = LocationPreferences.getInstance().getRecentlyOpenedFiles();
                for (String recentPath : recentPaths) {
                    File f = new File(recentPath);
                    if (f.exists()) {
                        JMenuItem menuItem2 = new JMenuItem(f.getName());
                        menuItem2.addActionListener(new FileOpener(application, f));
                        reopenProject.add(menuItem2);
                    }
                }
                if (recentPaths.size() != 0) {
                    reopenProject.addSeparator();
                    addJMenuItem(reopenProject, clearAboveLists, MenuUtils.CLEAR_LISTS, -1, true);
                }
                fileMenu.add(reopenProject);
            }

            addJMenuItem(fileMenu, exit, MenuUtils.EXIT, KeyEvent.VK_Q, true);
        }
        add(fileMenu);

        // ======== editMenu ========
        {
            editMenu.setText(MenuUtils.EDIT_MENU);

            addJMenuItem(editMenu, showToolbar, MenuUtils.SHOW_TOOLBAR, -1, false);
            addJMenuItem(editMenu, showDirectoryPanel, MenuUtils.SHOW_DIRECTORY_PANEL, -1, false);
            addJMenuItem(editMenu, showGenomePanel, MenuUtils.SHOW_GENOME_PANEL, -1, false);
            addJMenuItem(editMenu, showChromosomePanel, MenuUtils.SHOW_CHROMOSOME_PANEL, -1, false);
            addJMenuItem(editMenu, showFeaturePanel, MenuUtils.SHOW_FEATURE_PANEL, -1, false);
            addJMenuItem(editMenu, showStatusPanel, MenuUtils.SHOW_STATUS_PANEL, -1);

            editMenu.addSeparator();
            addJMenuItem(editMenu, setDataTracks, MenuUtils.SET_DATA_TRACKS, -1, false);
            addJMenuItem(editMenu, find, MenuUtils.FIND, KeyEvent.VK_F, false);

            editMenu.addSeparator();
            addJMenuItem(editMenu, preference, MenuUtils.PREFERENCES, KeyEvent.VK_P, true);
        }
        add(editMenu);

        // ======== viewMenu ========
        {
            viewMenu.setText(MenuUtils.VIEW_MENU);
            addJMenuItem(viewMenu, zoomIn, MenuUtils.ZOOM_IN, KeyEvent.VK_I);
            addJMenuItem(viewMenu, zoomOut, MenuUtils.ZOOM_OUT, KeyEvent.VK_O);
            addJMenuItem(viewMenu, setZoomLevel, MenuUtils.SET_ZOOM_LEVEL, -1);
            viewMenu.addSeparator();
            addJMenuItem(viewMenu, moveLeft, MenuUtils.MOVE_LEFT, -1);
            addJMenuItem(viewMenu, moveRight, MenuUtils.MOVE_RIGHT, -1);
            viewMenu.addSeparator();
            {
                gotoMenu.setText(MenuUtils.GOTO);
                addJMenuItem(gotoMenu, gotoPosition,
                        MenuUtils.GOTO_POSITION, -1);
                addJMenuItem(gotoMenu, gotoWindow, MenuUtils.GOTO_WINDOW,
                        -1);
                viewMenu.add(gotoMenu);
            }
        }
        add(viewMenu);
        viewMenu.setEnabled(false);

        // ======== filterMenu ========
        {
            filterMenu.setText(MenuUtils.FILTER_MENU);
            addJMenuItem(filterMenu, qualityControlFilter, MenuUtils.QC_FILTER, -1);
            addJMenuItem(filterMenu, editingTypeFilter, MenuUtils.SPECIFIC_FILTER, -1);
            addJMenuItem(filterMenu, knownSNPFilter, MenuUtils.KNOWN_SNVS_FILTER, -1);
            addJMenuItem(filterMenu, dnaRnaFilter, MenuUtils.DNA_RNA_FILTER, -1);
            addJMenuItem(filterMenu, repeatRegionsFilter, MenuUtils.REPEATED_FILTER, -1);
            addJMenuItem(filterMenu, spliceJunctionFilter, MenuUtils.SPLICE_JUNCTION_FILTER, -1);
            {
                statisticalFilterMenu.setText(MenuUtils.STATISTICAL_FILTER);
                addJMenuItem(statisticalFilterMenu, fetFilter, MenuUtils.PVALUE_FILTER, -1);
                addJMenuItem(statisticalFilterMenu, llrFilter, MenuUtils.LLR_FILTER, -1);
                filterMenu.add(statisticalFilterMenu);
            }
        }
        add(filterMenu);
        filterMenu.setEnabled(false);

        // ======== plotsFilter ========
        {
            reportsMenu.setText(MenuUtils.REPORTS_MENU);
            addJMenuItem(reportsMenu, variantDistribution, MenuUtils.VARIANT_DISTRIBUTION, -1);
            addJMenuItem(reportsMenu, sitesDistribution, MenuUtils.RNA_EDITING_SITES_DISTRIBUTION, -1);
            addJMenuItem(reportsMenu, filterReports, MenuUtils.FILTER_REPORTS, -1, false);
        }
        add(reportsMenu);
        reportsMenu.setEnabled(false);

        // ======== helpMenu ========
        {
            helpMenu.setText(MenuUtils.HELP_MENU);
            addJMenuItem(helpMenu, helpContents, MenuUtils.HELP_CONTENTS, -1);
            addJMenuItem(helpMenu, helpOnline, MenuUtils.HELP_ONLINE, -1);
            addJMenuItem(helpMenu, checkForUpdates, MenuUtils.CHECK_FOR_UPDATES, -1);
            addJMenuItem(helpMenu, aboutRED, MenuUtils.ABOUT_RED, -1);
        }
        add(helpMenu);
    }

    /**
     * A brief way to add menu item to the menu.
     *
     * @param jMenu     The menu which should add to.
     * @param jMenuItem The menu item to be added to the jMenu.
     * @param text      The menu item name, including the action command name.
     * @param mnemonic  The keyboard shortcuts. Call Java API when using the shortcut letter.
     */
    private void addJMenuItem(JMenu jMenu, JMenuItem jMenuItem, String text, int mnemonic) {
        addJMenuItem(jMenu, jMenuItem, text, mnemonic, true);
    }

    /**
     * A brief way to add menu item to the menu.
     *
     * @param jMenu     The menu which should add to.
     * @param jMenuItem The menu item to be added to the jMenu.
     * @param text      The menu item name, including the action command name.
     * @param mnemonic  The keyboard shortcuts. Call Java API when using the shortcut letter.
     * @param isEnable  Set the item enable or not.
     */
    private void addJMenuItem(JMenu jMenu, JMenuItem jMenuItem, String text, int mnemonic, boolean isEnable) {
        if (text != null) {
            jMenuItem.setText(text);
            jMenuItem.setActionCommand(text);
        }
        if (mnemonic != -1) {
            jMenuItem.setMnemonic(mnemonic);
            jMenuItem.setAccelerator(KeyStroke.getKeyStroke(mnemonic,
                    InputEvent.CTRL_MASK));
        }
        jMenuItem.addActionListener(this);
        jMenu.add(jMenuItem);
        jMenuItem.setEnabled(isEnable);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        String action = arg0.getActionCommand();
        // --------------------File Menu--------------------
        if (action.equals(MenuUtils.NEW_PROJECT)) {
            application.startNewProject();
        } else if (action.equals(MenuUtils.OPEN_PROJECT)) {
            application.loadProject();
        } else if (action.equals(MenuUtils.SAVE_PROJECT)) {
            application.saveProject();
        } else if (action.equals(MenuUtils.SAVE_PROJECT_AS)) {
            application.saveProjectAs();
        } else if (action.equals(MenuUtils.CONNECT_TO_DATABASE)) {
            if (application.dataCollection().getAllDataSets() == null || application.dataCollection().getAllDataSets().length == 0) {
                OptionDialogUtils.showWarningDialog(application, "You must import relative dataset(BAM file) before connecting to database.", "Unable to " +
                        "connect to database now...");
                return;
            }
            new UserPasswordDialog(application);
        } else if (action.equals(MenuUtils.DATABASE)) {
            new DataImportDialog(application);
        } else if (action.equals(MenuUtils.FASTA)) {
            application.importData(new FastaFileParser(application.dataCollection()));
        } else if (action.equals(MenuUtils.RNA)) {
            application.importData(new BamFileParser());
        } else if (action.equals(MenuUtils.DNA)) {
            application.importData(new BamFileParser());
        } else if (action.equals(MenuUtils.ANNOTATION)) {
            AnnotationParserRunner.RunAnnotationParser(application, new UcscRefGeneParser(application.dataCollection().genome()));
            //            throw new UnsupportedOperationException("We only support .genome file from IGV server now...");
        } else if (action.equals(MenuUtils.CHROMOSOME_VIEW)) {
            ChromosomeViewer viewer = application.chromosomeViewer();
            ImageSaver.saveImage(viewer, "chr_view_" + viewer.chromosome().getName() + "_" + viewer.currentStart() + "_" + viewer.currentEnd());
        } else if (action.equals(MenuUtils.GENOME_VIEW)) {
            application.genomeViewer().setExportImage(true);
            application.genomeViewer().displayPreferencesUpdated(DisplayPreferences.getInstance());
            ImageSaver.saveImage(application.genomeViewer(), "genome_view");
            application.genomeViewer().setExportImage(false);
            application.genomeViewer().displayPreferencesUpdated(DisplayPreferences.getInstance());
        } else if (action.equals(MenuUtils.CLEAR_LISTS)) {
            LocationPreferences.getInstance().clearRecentlyOpenedFiles();
            OptionDialogUtils.showMessageDialog(application, "The recently opened file lists will be cleared before RED is restarted.",
                    "Clear Recently Opened Files.");
        } else if (action.equals(MenuUtils.EXIT)) {
            application.dispose();
            System.exit(0);
        }
        // --------------------EditMenu--------------------
        else if (action.equals(MenuUtils.SHOW_TOOLBAR)) {
            toolbarPanel.setVisible(showToolbar.isSelected());
        } else if (action.equals(MenuUtils.SHOW_DIRECTORY_PANEL)) {
            if (showDirectoryPanel.isSelected() && showGenomePanel.isSelected()) {
                application.topPane().setDividerLocation(0.2);
            } else {
                application.topPane().setDividerLocation(0);
            }
            application.dataViewer().setVisible(showDirectoryPanel.isSelected());
        } else if (action.equals(MenuUtils.SHOW_GENOME_PANEL)) {
            if (showDirectoryPanel.isSelected() && showGenomePanel.isSelected()) {
                application.topPane().setDividerLocation(0.2);
            } else {
                application.topPane().setDividerLocation(0);
            }
            application.genomeViewer().setVisible(showGenomePanel.isSelected());
        } else if (action.equals(MenuUtils.SHOW_CHROMOSOME_PANEL)) {
            application.chromosomeViewer().setVisible(showChromosomePanel.isSelected());
        } else if (action.equals(MenuUtils.SHOW_FEATURE_PANEL)) {
            application.chromosomeViewer().getFeatureTrack().setVisible(showFeaturePanel.isSelected());
        } else if (action.equals(MenuUtils.SHOW_STATUS_PANEL)) {
            application.statusPanel().setVisible(showStatusPanel.isSelected());
        } else if (action.equals(MenuUtils.SET_DATA_TRACKS)) {
            new DataTrackSelector(application);
        } else if (action.equals(MenuUtils.FIND)) {
            new FindFeatureDialog(application.dataCollection());
        } else if (action.equals(MenuUtils.PREFERENCES)) {
            new EditPreferencesDialog();
        }
        // --------------------ViewMenu--------------------
        else if (action.equals(MenuUtils.ZOOM_IN)) {
            application.chromosomeViewer().zoomIn();
        } else if (action.equals(MenuUtils.ZOOM_OUT)) {
            application.chromosomeViewer().zoomOut();
        } else if (action.equals(MenuUtils.SET_ZOOM_LEVEL)) {
            new DataZoomSelectorDialog(application);
        } else if (action.equals(MenuUtils.MOVE_LEFT)) {
            application.chromosomeViewer().moveLeft();
        } else if (action.equals(MenuUtils.MOVE_RIGHT)) {
            application.chromosomeViewer().moveRight();
        } else if (action.equals(MenuUtils.GOTO_POSITION)) {
            new GoToDialog(application);
        } else if (action.equals(MenuUtils.GOTO_WINDOW)) {
            new GoToWindowDialog(application);
        }
        // --------------------FilterMenu--------------------
        else if (action.endsWith("Filter...")) {
            try {
                DataStore activeDataStore = application.dataCollection().getActiveDataStore();
                if (action.equals(MenuUtils.QC_FILTER)) {
                    new FilterOptionDialog(new QualityControlFilterPanel(activeDataStore));
                } else if (action.equals(MenuUtils.SPECIFIC_FILTER)) {
                    new FilterOptionDialog(new EditingTypeFilterPanel(activeDataStore));
                } else if (action.equals(MenuUtils.KNOWN_SNVS_FILTER)) {
                    new FilterOptionDialog(new KnownSnpFilterPanel(activeDataStore));
                } else if (action.equals(MenuUtils.REPEATED_FILTER)) {
                    new FilterOptionDialog(new RepeatRegionsFilterPanel(activeDataStore));
                } else if (action.equals(MenuUtils.DNA_RNA_FILTER)) {
                    new FilterOptionDialog(new DnaRnaFilterPanel(activeDataStore));
                } else if (action.equals(MenuUtils.SPLICE_JUNCTION_FILTER)) {
                    new FilterOptionDialog(new SpliceJunctionFilterPanel(activeDataStore));
                }
            } catch (RedException e) {
                e.printStackTrace();
            }
        } else if (action.contains("test")) {
            try {
                DataStore activeDataStore = application.dataCollection().getActiveDataStore();
                if (action.equals(MenuUtils.PVALUE_FILTER)) {
                    new FilterOptionDialog(new FisherExactTestFilterPanel(activeDataStore));
                } else if (action.equals(MenuUtils.LLR_FILTER)) {
                    new FilterOptionDialog(new LikelihoodRatioFilterPanel(activeDataStore));
                }
            } catch (RedException e) {
                e.printStackTrace();
            }
        }
        // --------------------ReportsMenu------------------
        else if (action.equals(MenuUtils.VARIANT_DISTRIBUTION)) {
            if (application.dataCollection().getActiveDataStore() == null) {
                OptionDialogUtils.showMessageDialog(application, "You need to select a data store in the Data panel before viewing this plot",
                        "No data selected...");
            } else if (application.dataCollection().getActiveSiteList() == null) {
                OptionDialogUtils.showMessageDialog(application, "You need to select a site set/site list in the Data panel before viewing this plot",
                        "No data selected...");
            } else {
                new VariantDistributionHistogram(application.dataCollection().getActiveDataStore());
            }
        } else if (action.equals(MenuUtils.RNA_EDITING_SITES_DISTRIBUTION)) {
            if (application.dataCollection().getActiveDataStore() == null) {
                OptionDialogUtils.showMessageDialog(application, "You need to select a data store in the Data panel before viewing this plot",
                        "No data selected...");
            } else if (application.dataCollection().getActiveSiteList() == null) {
                OptionDialogUtils.showMessageDialog(application, "You need to select a site set/site list in the Data panel before viewing this plot",
                        "No data selected...");
            } else {
                new SitesDistributionHistogram(application.dataCollection().getActiveDataStore());
            }
        } else if (action.equals(MenuUtils.FILTER_REPORTS)) {
            new ReportOptions(application, new FilterReports(application.dataCollection().getActiveDataStore()));
        }
        // --------------------HelpMenu---------------------
        else if (action.equals(MenuUtils.HELP_CONTENTS)) {
            try {
                new HelpDialog(new File(ClassLoader.getSystemResource("Help").getFile().replaceAll("%20", " ")));
            } catch (RedException e) {
                OptionDialogUtils.showMessageDialog(application, "<html>The embedded help content did not work, please use online help at <a href=\"" + Global
                        .HELP_ONLINE + "\">" + Global.HELP_ONLINE + "</a> instead");
                try {
                    Desktop.getDesktop().browse(new URI(Global.HELP_ONLINE));
                } catch (IOException e1) {
                    logger.error("I/O exception.", e);
                } catch (URISyntaxException e1) {
                    logger.error("Syntax exception.", e);
                }
                logger.warn("The embedded help content did not work.");
                helpContents.setEnabled(false);
            }
        } else if (action.equals(MenuUtils.HELP_ONLINE)) {
            try {
                Desktop.getDesktop().browse(new URI(Global.HELP_ONLINE));
            } catch (IOException e) {
                logger.error("I/O exception.", e);
            } catch (URISyntaxException e) {
                logger.error("Syntax exception.", e);
            }
        } else if (action.equals(MenuUtils.CHECK_FOR_UPDATES)) {
            UpdateChecker.isUpdateAvailable(new UpdateChecker.IUpdateCheck() {
                @Override
                public void onSuccess(boolean isUpdateAvailable) {
                    if (isUpdateAvailable) {
                        String latestVersion;
                        try {
                            latestVersion = UpdateChecker.getLatestVersionNumber();
                            OptionDialogUtils.showMessageDialog(application, "<html>A newer version of RED (v" + latestVersion + ") is available, " +
                                            "<br>please go to  <a href=\"" + Global.HOME_PAGE + "\">" + Global.HOME_PAGE + "</a> for the latest version",
                                    "Update available");
                        } catch (NetworkException e) {
                            OptionDialogUtils.showErrorDialog(application, "Network is unavailable, please have a check.");
                        }
                    } else {
                        OptionDialogUtils.showMessageDialog(application, "<html>You are running the latest version of RED.", "Latest version of RED");
                    }
                }

                @Override
                public void onFailed() {
                    OptionDialogUtils.showErrorDialog(application, "Network is unavailable, please have a check.");
                }
            });
        } else if (action.equals(MenuUtils.ABOUT_RED)) {
            new AboutDialog();
        }
        //--------------------Main Toolbar---------------------
        else if (action.equals(MenuUtils.SHOW_READS_ONLY)) {
            DisplayPreferences.getInstance().setDisplayMode(DisplayPreferences.DISPLAY_MODE_READS_ONLY);
        } else if (action.equals(MenuUtils.SHOW_PROBES_ONLY)) {
            DisplayPreferences.getInstance().setDisplayMode(DisplayPreferences.DISPLAY_MODE_PROBES_ONLY);
        } else if (action.equals(MenuUtils.SHOW_READS_AND_PROBES)) {
            DisplayPreferences.getInstance().setDisplayMode(DisplayPreferences.DISPLAY_MODE_READS_AND_PROBES);
        } else if (action.equals(MenuUtils.SWITCH_SAMPLES_OR_MODE)) {
            new DatabaseSelector(application);
        }
    }

    /**
     * Check the cache folder.
     */
    public void cacheFolderChecked() {
        newProject.setEnabled(true);
        openProject.setEnabled(true);
    }

    /**
     * Data loaded.
     */
    public void dataLoaded() {
        viewMenu.setEnabled(true);
        setDataTracks.setEnabled(true);
        reportsMenu.setEnabled(true);
    }

    @Override
    public void databaseChanged(String databaseName, String sampleName) {
        filterMenu.setEnabled(true);
        filterReports.setEnabled(true);
        if (databaseName.equals(DatabaseManager.DENOVO_MODE_DATABASE_NAME)) {
            llrFilter.setEnabled(false);
            dnaRnaFilter.setEnabled(false);
        } else {
            llrFilter.setEnabled(true);
            dnaRnaFilter.setEnabled(true);
        }
    }

    @Override
    public void databaseConnected() {
        importToDatabase.setEnabled(true);
        filterReports.setEnabled(true);
    }

    /**
     * Genome loaded.
     */
    public void genomeLoadedMenu() {
        updateVisibleToolBars();
        importDataMenu.setEnabled(true);
        viewMenu.setEnabled(true);
        saveProject.setEnabled(true);
        saveProjectAs.setEnabled(true);
        exportImage.setEnabled(true);
        find.setEnabled(true);
        gotoMenu.setEnabled(true);
        gotoPosition.setEnabled(true);
        showToolbar.setEnabled(true);
        showDirectoryPanel.setEnabled(true);
        showGenomePanel.setEnabled(true);
        showChromosomePanel.setEnabled(true);
        showFeaturePanel.setEnabled(true);
        showStatusPanel.setEnabled(true);
        connectToDatabase.setEnabled(true);
        redToolbar.genomeLoaded();
    }

    /**
     * Resets the menu availability to its default state. Should be called when a new genome is loaded.
     */
    public void resetMenus() {
        saveProject.setEnabled(false);
        saveProjectAs.setEnabled(false);
        importDataMenu.setEnabled(false);
        exportImage.setEnabled(false);
        viewMenu.setEnabled(false);
        filterMenu.setEnabled(false);
        reportsMenu.setEnabled(false);
        redToolbar.reset();
    }

    public JPanel toolbarPanel() {
        return toolbarPanel;
    }

    private void updateVisibleToolBars() {
        Vector<JToolBar> visibleToolBars = new Vector<JToolBar>();

        if (redToolbar.shown()) {
            visibleToolBars.add(redToolbar);
        }

        toolbarPanel.setToolBars(visibleToolBars.toArray(new JToolBar[0]));
    }

    /**
     * The Class FileOpener.
     */
    private class FileOpener implements ActionListener {

        /**
         * The application.
         */
        private final RedApplication application;

        /**
         * The file.
         */
        private final File file;

        /**
         * Instantiates a new file opener.
         *
         * @param application the application
         * @param file        the file
         */
        public FileOpener(RedApplication application, File file) {
            this.application = application;
            this.file = file;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            application.loadProject(file);
        }
    }
}
