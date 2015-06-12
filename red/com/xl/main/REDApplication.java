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

package com.xl.main;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.xl.database.DatabaseListener;
import com.xl.database.DatabaseManager;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.sites.SiteList;
import com.xl.datatypes.sites.SiteListChangeListener;
import com.xl.datawriters.REDDataWriter;
import com.xl.display.chromosomeviewer.ChromosomePositionScrollBar;
import com.xl.display.chromosomeviewer.ChromosomeViewer;
import com.xl.display.dataviewer.DataViewer;
import com.xl.display.dialog.*;
import com.xl.display.dialog.gotodialog.GoToDialog;
import com.xl.display.genomeviewer.GenomeViewer;
import com.xl.display.panel.REDPreviewPanel;
import com.xl.display.panel.StatusPanel;
import com.xl.display.panel.WelcomePanel;
import com.xl.exception.ErrorCatcher;
import com.xl.exception.REDException;
import com.xl.interfaces.AnnotationCollectionListener;
import com.xl.interfaces.DataStoreChangedListener;
import com.xl.interfaces.ProgressListener;
import com.xl.menu.REDMenu;
import com.xl.net.genomes.GenomeDownloader;
import com.xl.parsers.annotationparsers.IGVGenomeParser;
import com.xl.parsers.dataparsers.DataParser;
import com.xl.parsers.dataparsers.REDParser;
import com.xl.preferences.DisplayPreferences;
import com.xl.preferences.LocationPreferences;
import com.xl.preferences.REDPreferences;
import com.xl.utils.ui.OptionDialogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * The Class REDApplication is the first appeared panel when program starts. It contains and manages all components for the common functions.
 */
public class REDApplication extends JFrame implements ProgressListener, DataStoreChangedListener, SiteListChangeListener, AnnotationCollectionListener, DatabaseListener {
    private static final Logger logger = LoggerFactory.getLogger(REDApplication.class);
    /**
     * The static instance of RED.
     */
    private static REDApplication application;

    /**
     * The root menu of RED
     */
    private REDMenu menu;

    /**
     * The DataViewer is the set of folders shown on the top left.
     */
    private DataViewer dataViewer = null;

    /**
     * The genome viewer is the panel on the top right.
     */
    private GenomeViewer genomeViewer = null;

    /**
     * The chromosome viewer is the interactive view at the bottom.
     */
    private ChromosomeViewer chromosomeViewer;

    /**
     * The welcome panel is the status panel shown when the program is first launched
     */
    private WelcomePanel welcomePanel;

    /**
     * This is the split pane which separates the chromosome panel from the top panels
     */
    private JSplitPane mainPane;

    /**
     * This is the split pane which separates the genome and data views *
     */
    private JSplitPane topPane;

    /**
     * This is the small strip at the bottom of the main display.*
     */
    private StatusPanel statusPanel;

    /**
     * The data collection is the main data model
     */
    private DataCollection dataCollection = null;

    /**
     * A list of data stores which are currently displayed in the chromosome view
     */
    private Vector<DataStore> drawnDataStores = new Vector<DataStore>();

    /**
     * The last opened / saved file. *
     */
    private File currentFile = null;

    /**
     * Flag to check if anything substantial has changed since the file was last loaded/saved.
     */
    private boolean changesWereMade = false;

    /**
     * Flag used only when saving before shutting down (save on exit) *
     */
    private boolean shuttingDown = false;

    /**
     * Flag used when saving before loading a new file *
     */
    private File fileToLoad = null;

    static {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
            configurator.doConfigure(ClassLoader.getSystemResource("com/xl/preferences/logbackConfig.xml"));
        } catch (JoranException e) {
            e.printStackTrace();
        }
    }

    private REDApplication() {
        setTitle("RED");
        setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 3 * 2,
                Toolkit.getDefaultToolkit().getScreenSize().height / 3 * 2);
        setLocationRelativeTo(null);
        setExtendedState(getExtendedState() | MAXIMIZED_BOTH);

        //We need to initiate the preferences first.
        REDPreferences.getInstance();
        menu = new REDMenu(this);
        setJMenuBar(menu);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/logo.png")).getImage());

        mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        mainPane.setTopComponent(topPane);

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(menu.toolbarPanel(), BorderLayout.NORTH);

        welcomePanel = new WelcomePanel(this);
        getContentPane().add(welcomePanel, BorderLayout.CENTER);

        statusPanel = new StatusPanel();
        getContentPane().add(statusPanel, BorderLayout.SOUTH);

    }

    /**
     * Get the singleton instance of RED application.
     *
     * @return the instance.
     */
    public static REDApplication getInstance() {
        return application;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            Thread.setDefaultUncaughtExceptionHandler(new ErrorCatcher());
            application = new REDApplication();
            application.setVisible(true);

            if (args.length > 0) {
                File f = new File(args[0]);
                application.loadProject(f);
            }
        } catch (Exception e) {
            new CrashReporter(e);
            e.printStackTrace();
        }

    }

    /**
     * Adds a set of dataStores to the set of currently visible data stores in the chromosome view. If any data store is already visible it won't be added
     * again.
     *
     * @param dataStores An array of dataStores to add
     */
    public void addToDrawnDataStores(DataStore[] dataStores) {
        changesWereMade();
        for (DataStore dataStore : dataStores) {
            if (dataStore != null && !drawnDataStores.contains(dataStore)) {
                drawnDataStores.add(dataStore);
            }
        }
        chromosomeViewer.tracksUpdated();
    }

    /**
     * Adds a loaded genome to the main display
     *
     * @param genome The Genome which has just been loaded.
     */
    private void addNewLoadedGenome(Genome genome) {
        // We've had a trace where the imported genome contained no chromosomes. No idea how that happened but we can check that here.
        if (genome.getAllChromosomes() == null || genome.getAllChromosomes().length == 0) {
            OptionDialogUtils.showErrorDialog(this, "No data was present in the imported genome");
            return;
        }

        dataCollection = new DataCollection(genome);
        dataCollection.addDataChangeListener(this);
        dataCollection.genome().getAnnotationCollection().addAnnotationCollectionListener(this);
        // We need to get rid of the welcome panel if that's still showing and replace it with the proper RED display.
        remove(welcomePanel);
        remove(mainPane);
        add(mainPane, BorderLayout.CENTER);

        genomeViewer = new GenomeViewer(dataCollection.genome(), this);
        DisplayPreferences.getInstance().addListener(genomeViewer);
        dataCollection.addActiveDataListener(genomeViewer);
        topPane.setRightComponent(genomeViewer);

        dataViewer = new DataViewer(this);
        topPane.setLeftComponent(new JScrollPane(dataViewer));
        topPane.setDividerLocation(0.2d);

        validate();

        chromosomeViewer = new ChromosomeViewer(this, dataCollection.genome().getAllChromosomes()[0]);
        DisplayPreferences.getInstance().addListener(chromosomeViewer);
        dataCollection.addActiveDataListener(chromosomeViewer);

        // Add chromosome view to the main panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(chromosomeViewer, BorderLayout.CENTER);
        bottomPanel.add(new ChromosomePositionScrollBar(), BorderLayout.SOUTH);
        mainPane.setBottomComponent(bottomPanel);
        mainPane.setDividerLocation(0.3d);
        validate();

        DisplayPreferences.getInstance().setChromosome(dataCollection.genome().getAllChromosomes()[0]);
        menu.genomeLoadedMenu();

        DatabaseManager.getInstance().addDatabaseListener(this);
    }

    /**
     * Adds new dataSets to the existing dataCollection and adds them to the main chromosome view
     *
     * @param newData The new dataSets to add
     */
    private void addNewDataSets(DataSet[] newData) {
        // We need to add the data to the data collection
        if (newData == null || newData.length == 0) {
            return;
        }

        ArrayList<DataStore> storesToAdd = new ArrayList<DataStore>();
        for (DataSet dataset : newData) {
            // Can we leave this out as this should be handled by the data collection listener?
            dataCollection.addDataStore(dataset);
            storesToAdd.add(dataset);
        }

        if (dataCollection.getAllDataSets().length > 0)
            menu.dataLoaded();

        addToDrawnDataStores(storesToAdd.toArray(new DataStore[0]));

    }

    public void cacheFolderChecked() {
        menu.cacheFolderChecked();
    }

    /**
     * Chromosome viewer.
     *
     * @return the Chromosome viewer
     */
    public ChromosomeViewer chromosomeViewer() {
        return chromosomeViewer;
    }

    /**
     * Sets a flag which causes the UI to prompt the user to save when closing the program.
     */
    private void changesWereMade() {
        changesWereMade = true;
        if (!getTitle().endsWith("*")) {
            setTitle(getTitle() + "*");
        }
    }

    /**
     * Data collection.
     *
     * @return The currently used data collection.
     */
    public DataCollection dataCollection() {
        return dataCollection;
    }

    /**
     * Checks to see if a specified dataStore is currently being displayed
     *
     * @param d The dataStore to check
     * @return Is this dataStore currently visible?
     */
    public boolean dataStoreIsDrawn(DataStore d) {
        return drawnDataStores.contains(d);
    }

    public DataViewer dataViewer() {
        return dataViewer;
    }

    @Override
    public void dispose() {
        // We're overriding this so we can catch the application being closed by the X in the corner. We need to offer the opportunity
        // to save if they've changed anything.

        // We'll already have been made invisible by this stage, so make us visible again in case we're hanging around.
        setVisible(true);

        // Check to see if the user has made any changes they might want to save
        if (changesWereMade) {
            int answer = OptionDialogUtils.showSaveBeforeExitDialog(this);
            switch (answer) {
                case 0:
                    shuttingDown = true;
                    saveProject();
                    return;
                case 1:
                    break;
                case 2:
                    return;
            }
        }

        setVisible(false);
        super.dispose();
        System.exit(0);
    }

    /**
     * This method is usually called from data gathered by the genome selector which will provide the required values for the assembly name. This does not
     * actually load the specified genome, but just downloads it from the online genome repository.
     *
     * @param id          Species name
     * @param displayName Assembly name
     */
    public void downloadGenome(String id, String displayName) {
        GenomeDownloader genomeDownload;
        ProgressDialog progressDialog;
        genomeDownload = new GenomeDownloader();
        genomeDownload.addProgressListener(this);
        progressDialog = new ProgressDialog(this, "Downloading genome: " + displayName);
        genomeDownload.addProgressListener(progressDialog);
        genomeDownload.downloadGenome(id, true);
        progressDialog.requestFocus();
        progressDialog.setDefaultCloseOperation(ProgressDialog.DISPOSE_ON_CLOSE);
    }

    public DataStore[] drawnDataStores() {
        return drawnDataStores.toArray(new DataStore[0]);
    }

    public GenomeViewer genomeViewer() {
        return genomeViewer;
    }

    /**
     * Begins the import of new SequenceRead data
     *
     * @param parser A DataParser which will actually do the importing.
     */
    public void importData(DataParser parser) {
        logger.info("Loading data...");
        parser.addProgressListener(this);

        JFileChooserExt chooser = new JFileChooserExt(LocationPreferences.getInstance().getProjectDataDirectory(), null);
        chooser.setMultiSelectionEnabled(false);

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.CANCEL_OPTION || chooser.getSelectedFile() == null) {
            return;
        }

        LocationPreferences.getInstance().setProjectSaveLocation(chooser.getSelectedFile().getParent());
        parser.setFile(chooser.getSelectedFile());

        // See if we need to display any options
        if (parser.hasOptionsPanel()) {
            DataParserOptionsDialog optionsDialog = new DataParserOptionsDialog(parser);
            optionsDialog.setLocationRelativeTo(this);
            boolean goAhead = optionsDialog.view();

            if (!goAhead) {
                logger.warn("Not ready to go ahead.");
                return;
            }
        }

        ProgressDialog progressDialog = new ProgressDialog(this, "Loading data...", parser);
        parser.addProgressListener(progressDialog);

        try {
            parser.parseData();
        } catch (REDException ex) {
            new CrashReporter(ex);
            logger.error("", ex);
        }
    }

    /**
     * Loads a genome assembly. This will fail if the genome isn't currently in the local cache and downloadGenome should be set first in this case.
     *
     * @param baseLocation The folder containing the requested genome.
     */
    public void loadGenome(File baseLocation) {
        GoToDialog.clearRecentLocations();
        IGVGenomeParser parser = new IGVGenomeParser();
        ProgressDialog progressDialog = new ProgressDialog(this, "Loading genome...");
        parser.addProgressListener(progressDialog);
        parser.addProgressListener(this);
        parser.parseGenome(baseLocation);
        progressDialog.requestFocus();
        progressDialog.setDefaultCloseOperation(ProgressDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Launches a FileChooser to select a project file to open
     */
    public void loadProject() {
        JFileChooser chooser = new JFileChooserExt(LocationPreferences.getInstance().getProjectSaveLocation(), "red");
        chooser.setMultiSelectionEnabled(false);
        REDPreviewPanel previewPanel = new REDPreviewPanel();
        chooser.setAccessory(previewPanel);
        chooser.addPropertyChangeListener(previewPanel);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        File file = chooser.getSelectedFile();
        LocationPreferences.getInstance().setProjectSaveLocation(file.getParent());

        loadProject(file);
    }

    /**
     * Loads an existing project from a file. This method will wipe all existing data and prompt to save if the currently loaded project has changed.
     *
     * @param file The file to load
     */
    public void loadProject(File file) {
        if (file == null)
            return;

		/*
         * Before we wipe all of the data we need to check to see if we need to save the existing project.
		 */

        if (changesWereMade) {
            int answer = OptionDialogUtils.showSaveBeforeExitDialog(this);
            switch (answer) {
                case 0:
                    fileToLoad = file;
                    saveProject();
                    return;
                case 1:
                    break;
                case 2:
                    return;
            }
        }

        LocationPreferences.getInstance().setProjectSaveLocation(file.getAbsolutePath());

        wipeAllData();

        currentFile = file;

        REDParser parser = new REDParser(this);
        parser.addProgressListener(this);
        ProgressDialog progressDialog = new ProgressDialog(this, "Loading data...");
        parser.addProgressListener(progressDialog);
        parser.parseFile(file);
        progressDialog.requestFocus();
        setTitle("RED [" + file.getName() + "]");

        LocationPreferences.getInstance().addRecentlyOpenedFile(file.getAbsolutePath());

    }

    /**
     * Removes a dataStore from the chromosome view
     *
     * @param d The dataStore to remove
     */
    public void removeFromDrawnDataStores(DataStore d) {
        removeFromDrawnDataStores(new DataStore[]{d});
    }

    /**
     * Removes several dataStores from the chromosome view
     *
     * @param stores The dataStores to remove
     */
    public void removeFromDrawnDataStores(DataStore[] stores) {
        // Remember that we changed something
        changesWereMade();
        for (DataStore store : stores) {
            if (drawnDataStores.contains(store)) {
                drawnDataStores.remove(store);
            }
        }
        chromosomeViewer.tracksUpdated();
    }

    /**
     * Resets the changesWereMade flag so that the user will not be prompted to save even if the data has changed.
     */
    public void resetChangesWereMade() {
        changesWereMade = false;
        if (getTitle().endsWith("*")) {
            setTitle(getTitle().replaceAll("\\*$", ""));
        }
    }

    /**
     * Saves the current project under the same name as it was loaded. If no file is associated with the project will call saveProjectAs
     */
    public void saveProject() {
        if (currentFile == null) {
            saveProjectAs();
        } else {
            saveProject(currentFile);
        }
    }

    /**
     * Saves the current project into the specified file.
     *
     * @param file The file into which the project will be saved
     */
    public void saveProject(File file) {
        REDDataWriter writer = new REDDataWriter();

        writer.addProgressListener(new ProgressDialog(this, "Saving Project...", writer));
        writer.addProgressListener(this);

        writer.writeData(this, file);

        setTitle("RED [" + file.getName() + "]");
        LocationPreferences.getInstance().addRecentlyOpenedFile(file.getAbsolutePath());
    }

    /**
     * Launches a FileChooser to allow the user to select a new file name under which to save
     */
    public void saveProjectAs() {
        JFileChooser chooser = new JFileChooserExt(LocationPreferences.getInstance().getProjectSaveLocation(), "red");
        chooser.setMultiSelectionEnabled(false);
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.CANCEL_OPTION)
            return;
        File file = chooser.getSelectedFile();
        LocationPreferences.getInstance().setProjectSaveLocation(file.getParent());
        if (!file.getPath().toLowerCase().endsWith(".red")) {
            file = new File(file.getPath() + ".red");
        }

        // Check if we're stepping on anyone's toes...
        if (file.exists()) {
            int answer = OptionDialogUtils.showFileExistDialog(this, file.getName());
            if (answer > 0) {
                saveProjectAs(); // Let them try again
                return;
            }
        }
        currentFile = file;
        saveProject(file);
    }

    /**
     * Sets the text in the status bar at the bottom of the main window
     *
     * @param text The text to display in the status bar
     */
    public void setStatusText(String text) {
        statusPanel.setText(text);
    }

    /**
     * Replaces the current set of drawn data stores with a new list
     *
     * @param d The set of dataStores to display
     */
    public void setDrawnDataStores(DataStore[] d) {
        // Remember that we changed something
        changesWereMade();

        drawnDataStores.removeAllElements();
        drawnDataStores.addAll(Arrays.asList(d));
        chromosomeViewer.tracksUpdated();
    }

    /**
     * Launches the genome selector to begin a new project.
     */
    public void startNewProject() {
        new GenomeSelector(this);
    }

    /**
     * Clears all stored data and blanks the UI.
     */
    public void wipeAllData() {

        setTitle("RED");
        currentFile = null;
        topPane.setRightComponent(null);
        topPane.setLeftComponent(null);
        mainPane.setBottomComponent(null);
        genomeViewer = null;
        dataViewer = null;
        chromosomeViewer = null;
        dataCollection = null;
        drawnDataStores = new Vector<DataStore>();
        menu.resetMenus();
        DisplayPreferences.getInstance().reset();
    }

    @Override
    public void dataStoreAdded(DataStore d) {
        menu.dataLoaded();
    }

    @Override
    public void dataStoreRemoved(DataStore d) {
        removeFromDrawnDataStores(d);
        changesWereMade();
    }

    @Override
    public void dataStoreRenamed(DataStore d) {
        chromosomeViewer.repaint();
        changesWereMade();
    }

    @Override
    public void dataGroupSamplesChanged(DataGroup g) {
        changesWereMade();
    }

    @Override
    public void annotationSetAdded(AnnotationSet annotationSets) {
        changesWereMade();
    }

    @Override
    public void annotationSetRemoved(AnnotationSet annotationSet) {
        changesWereMade();
    }

    @Override
    public void annotationSetRenamed(AnnotationSet annotationSet) {
        chromosomeViewer.repaint();
        changesWereMade();
    }

    @Override
    public void progressExceptionReceived(Exception e) {
    }

    @Override
    public void progressWarningReceived(Exception e) {
    }

    @Override
    public void progressUpdated(String message, int current, int max) {
    }

    @Override
    public void progressCancelled() {
    }

    @Override
    public void progressComplete(String command, Object result) {
        if (command == null)
            return;
        if (command.equals("load_genome")) {
            addNewLoadedGenome((Genome) result);
        } else if (command.equals("genome_downloaded")) {
            // No result is returned
            startNewProject();
        } else if (command.equals("datasets_loaded")) {
            addNewDataSets((DataSet[]) result);
            changesWereMade();
        } else if (command.equals("project_loaded")) {
            addNewDataSets((DataSet[]) result);
            resetChangesWereMade();
        } else if (command.equals("fasta_loaded")) {
            DisplayPreferences.getInstance().setFastaEnable(true);
            OptionDialogUtils.showMessageDialog(this, "The fasta file has been loaded. Please zoom out to make it visible" +
                    "...", "Load fasta file completed");
            changesWereMade();
        } else if (command.equals("data_written")) {
            // Since we've just saved we can reset the changes flag
            resetChangesWereMade();

            // We might have been called by a previous shutdown operation, in which case we need to send them back to shut down.
            if (shuttingDown) {
                shuttingDown = false;
                dispose();
            }

            // We might have been called by a previous load operation in which case we need to resume this load
            if (fileToLoad != null) {
                loadProject(fileToLoad);
                fileToLoad = null;
            }
        } else if (command.equals("annotation_loaded")) {
        } else {
            throw new IllegalArgumentException("Don't know how to handle progress command '" + command + "'");
        }
    }

    @Override
    public void siteListAdded(SiteList l) {
        changesWereMade();
    }

    @Override
    public void siteListRemoved(SiteList l) {
        changesWereMade();
    }

    @Override
    public void siteListRenamed(SiteList l) {
        changesWereMade();
    }

    public JSplitPane mainPanel() {
        return mainPane;
    }

    public JSplitPane topPane() {
        return topPane;
    }

    public StatusPanel statusPanel() {
        return statusPanel;
    }

    @Override
    public void databaseChanged(String databaseName, String sampleName) {
        String mode;
        if (databaseName.equals(DatabaseManager.DENOVO_DATABASE_NAME)) {
            mode = "denovo mode";
        } else {
            mode = "DNA-RNA mode";
        }
        OptionDialogUtils.showMessageDialog(this, "Database has been changed to " + mode + ", " + sampleName, mode);
        DisplayPreferences.getInstance().setDisplayMode(DisplayPreferences.DISPLAY_MODE_READS_AND_PROBES);
        changesWereMade();
    }

    @Override
    public void databaseConnected() {
        changesWereMade();
    }

}
