/*
 * Created by JFormDesigner on Fri Nov 15 01:10:53 GMT 2013
 */

package com.xl.main;

import com.xl.datatypes.*;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.datatypes.probes.ProbeSetChangeListener;
import com.xl.datawriters.REDDataWriter;
import com.xl.dialog.CrashReporter;
import com.xl.dialog.DataParserOptionsDialog;
import com.xl.dialog.GenomeSelector;
import com.xl.dialog.ProgressDialog;
import com.xl.dialog.gotodialog.GotoDialog;
import com.xl.display.chromosomeviewer.ChromosomePositionScrollBar;
import com.xl.display.chromosomeviewer.ChromosomeViewer;
import com.xl.display.dataviewer.DataViewer;
import com.xl.display.genomeviewer.GenomeViewer;
import com.xl.exception.ErrorCatcher;
import com.xl.exception.REDException;
import com.xl.interfaces.AnnotationCollectionListener;
import com.xl.interfaces.CacheListener;
import com.xl.interfaces.DataChangeListener;
import com.xl.interfaces.ProgressListener;
import com.xl.menu.REDMenu;
import com.xl.panel.REDPreviewPanel;
import com.xl.panel.StatusPanel;
import com.xl.panel.WelcomePanel;
import com.xl.parsers.annotationparsers.IGVGenomeParser;
import com.xl.parsers.dataparsers.DataParser;
import com.xl.parsers.dataparsers.REDParser;
import com.xl.preferences.DisplayPreferences;
import com.xl.preferences.REDPreferences;
import com.xl.utils.filefilters.FileFilterExt;
import net.xl.genomes.GenomeDownloader;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Xing Li
 */
public class REDApplication extends JFrame implements ProgressListener,
        DataChangeListener, ProbeSetChangeListener,
        AnnotationCollectionListener {

    private static REDApplication application;

    private static final String WELCOME_CONTENT = "<html>Welcome to RED.<br><br> "
            + "To allow the program to work you need to configure a temporary cache directory.<br><br>"
            + "Use the button on the welcome screen to set this and you can get started";
    private static final String WELCOME_TITLE = "Welcome - cache directory needed";

    /**
     * The version of RED
     */
    public static final String VERSION = "0.0.2";

    /**
     * The root menu of RED
     */
    private REDMenu menu;

    /**
     * The DataViewer is the set of folders shown on the top left *
     */
    private DataViewer dataViewer = null;

    /**
     * The genome viewer is the panel on the top right *
     */
    private GenomeViewer genomeViewer = null;

    /**
     * The chromosome viewer is the interactive view at the bottom *
     */
    private ChromosomeViewer chromosomeViewer;

    /**
     * The welcome panel is the status panel shown when the program is first
     * launched
     */
    // This needs to be able to access the application so we can't initialise it
    // here.
    private WelcomePanel welcomePanel;

    /**
     * This is the split pane which separates the chromosome panel from the top
     * panels
     */
    private JSplitPane mainPane;

    /**
     * This is the split pane which separates the genome and data views *
     */
    private JSplitPane topPane;

    /**
     * This is the small strip at the bottom of the main display *
     */
    private StatusPanel statusPanel;

    /**
     * The data collection is the main data model
     */
    private DataCollection dataCollection = null;

    private String currentFeatureTrackName = null;
    /**
     * A list of data stores which are currently displayed in the chromosome
     * view
     */
    private Vector<DataStore> drawnDataStores = new Vector<DataStore>();

    /**
     * The last opened / saved file. *
     */
    private File currentFile = null;

    /**
     * Flag to check if anything substantial has changed since the file was last
     * loaded/saved.
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

    /**
     * The cache listeners
     */
    private Vector<CacheListener> cacheListeners = new Vector<CacheListener>();

    private REDApplication() {
        setTitle("RED");
        setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 3 * 2,
                Toolkit.getDefaultToolkit().getScreenSize().height / 3 * 2);
        setLocationRelativeTo(null);
        setExtendedState(getExtendedState() | MAXIMIZED_BOTH);

        menu = new REDMenu(this);
        setJMenuBar(menu);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        mainPane.setTopComponent(topPane);

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(menu.toolbarPanel(), BorderLayout.NORTH);

        welcomePanel = new WelcomePanel(this);
        getContentPane().add(welcomePanel, BorderLayout.CENTER);

        statusPanel = new StatusPanel();
        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        mainPane.setDividerLocation((double) 0.25);
        topPane.setDividerLocation((double) 0.25);
    }

    /**
     * Adds a cache listener.
     *
     * @param cacheListener to monitor cache status
     */
    public void addCacheListener(CacheListener cacheListener) {
        if (cacheListener != null && !cacheListeners.contains(cacheListener)) {
            cacheListeners.add(cacheListener);
        }
    }

    /**
     * Adds a set of dataStores to the set of currently visible data stores in
     * the chromosome view. If any data store is already visible it won't be
     * added again.
     *
     * @param dataStore An array of dataStores to add
     */
    public void addToDrawnDataStores(DataStore[] dataStore) {
        System.out.println(this.getClass().getName() + ":addToDrawnDataStores(DataStore[] dataStore)\t" + dataStore.length);
        changesWereMade();
        for (int i = 0; i < dataStore.length; i++) {
            if (dataStore[i] != null && !drawnDataStores.contains(dataStore[i])) {
                drawnDataStores.add(dataStore[i]);
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
        System.out.println(this.getClass().getName() + ":addNewLoadedGenome(Genome genome)");
        if (DisplayPreferences.getInstance().getCurrentChromosome() != null) {
            DisplayPreferences.getInstance().setChromosome(null);
        }
        // We've had a trace where the imported genome contained no
        // chromosomes. No idea how that happened but we can check that
        // here.
        if (genome.getAllChromosomes() == null
                || genome.getAllChromosomes().length == 0) {
            JOptionPane.showMessageDialog(this,
                    "No data was present in the imported genome",
                    "Genome import error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dataCollection = new DataCollection(genome);
        dataCollection.addDataChangeListener(this);
        dataCollection.genome().getAnnotationCollection()
                .addAnnotationCollectionListener(this);
        // We need to get rid of the welcome panel if that's still showing
        // and replace it with the proper RED display.
        remove(welcomePanel);
        remove(mainPane);
        add(mainPane, BorderLayout.CENTER);

        genomeViewer = new GenomeViewer(dataCollection.genome(), this);
        DisplayPreferences.getInstance().addListener(genomeViewer);
        dataCollection.addDataChangeListener(genomeViewer);
        topPane.setRightComponent(genomeViewer);
        dataViewer = new DataViewer(this);
        topPane.setLeftComponent(new JScrollPane(dataViewer));
        topPane.setDividerLocation(0.25d);

        validate();

        chromosomeViewer = new ChromosomeViewer(this, dataCollection.genome()
                .getAllChromosomes()[0]);
        DisplayPreferences.getInstance().addListener(chromosomeViewer);
        dataCollection.addDataChangeListener(chromosomeViewer);

        // Add chromosome view to the main panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(chromosomeViewer, BorderLayout.CENTER);
        bottomPanel.add(new ChromosomePositionScrollBar(), BorderLayout.SOUTH);
        mainPane.setBottomComponent(bottomPanel);
        mainPane.setDividerLocation(0.25d);
        DisplayPreferences.getInstance().setChromosome(
                dataCollection.genome().getAllChromosomes()[0]);
        validate();

        menu.genomeLoadedMenu();

    }

    /**
     * Adds new dataSets to the existing dataCollection and adds them to the
     * main chromosome view
     *
     * @param newData The new dataSets to add
     */
    private void addNewDataSets(DataSet[] newData) {
        // We need to add the data to the data collection
        System.out.println(this.getClass().getName() + ":addNewDataSets(DataSet[] newData)\t" + newData.length);
        ArrayList<DataStore> storesToAdd = new ArrayList<DataStore>();

        for (int i = 0; i < newData.length; i++) {
            if (newData[i].getTotalReadCount() > 0) {
                // Can we leave this out as this should be handled by the
                // data collection listener?
                dataCollection.addDataSet(newData[i]);
                storesToAdd.add(newData[i]);
            }
        }

        if (dataCollection.getAllDataSets().length > 0)
            menu.dataLoaded();

        addToDrawnDataStores(storesToAdd.toArray(new DataStore[0]));

    }

    public void cacheFolderChecked() {
        menu.cacheFolderChecked();
    }

    /**
     * Notifies all listeners that the disk cache was used.
     */
    public void cacheUsed() {
        Enumeration<CacheListener> en = cacheListeners.elements();
        while (en.hasMoreElements()) {
            en.nextElement().cacheUsed();
        }
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
     * Sets a flag which causes the UI to prompt the user to save when closing
     * the program.
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
        if (drawnDataStores.contains(d)) {
            return true;
        }
        return false;
    }

    public DataViewer dataViewer() {
        return dataViewer;
    }

    /**
     * @see java.awt.Window#dispose()
     */
    public void dispose() {
        // We're overriding this so we can catch the application being
        // closed by the X in the corner. We need to offer the opportunity
        // to save if they've changed anything.

        // We'll already have been made invisible by this stage, so make
        // us visible again in case we're hanging around.
        setVisible(true);

        // Check to see if the user has made any changes they might
        // want to save
        if (changesWereMade) {
            int answer = JOptionPane
                    .showOptionDialog(
                            this,
                            "You have made changes which were not saved.  Do you want to save before exiting?",
                            "Save before exit?", 0,
                            JOptionPane.QUESTION_MESSAGE, null, new String[]{
                            "Save and Exit", "Exit without Saving",
                            "Cancel"}, "Save");

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
     * This method is usually called from data gathered by the genome selector
     * which will provide the required values for the assembly name. This does
     * not actually load the specified genome, but just downloads it from the
     * online genome repository.
     *
     * @param displayName Assembly name
     */
    public void downloadGenome(String id, String displayName) {
        GenomeDownloader genomeDownload = null;
        ProgressDialog progressDialog = null;
        try {
            genomeDownload = new GenomeDownloader();
            genomeDownload.addProgressListener(this);
            progressDialog = new ProgressDialog(this, "Downloading genome...");
            genomeDownload.addProgressListener(progressDialog);
            genomeDownload.downloadGenome(id, displayName, true);
            progressDialog.requestFocus();
            progressDialog
                    .setDefaultCloseOperation(ProgressDialog.DISPOSE_ON_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DataStore[] drawnDataStores() {
        return drawnDataStores.toArray(new DataStore[0]);
    }

    public static REDApplication getInstance() {
        return application;
    }

    public String getCurrentFeatureTrackName() {
        return currentFeatureTrackName;
    }

    public void setCurrentFeatureTrackName(String currentFeatureTrackName) {
        this.currentFeatureTrackName = currentFeatureTrackName;
    }

    public GenomeViewer genomeViewer() {
        return genomeViewer;
    }

    public static boolean hasInstance() {
        return application != null;
    }

    /**
     * Begins the import of new SequenceRead data
     *
     * @param parser A DataParser which will actually do the importing.
     */
    public void importData(DataParser parser) {
        parser.addProgressListener(this);

        JFileChooser chooser = new JFileChooser(REDPreferences.getInstance()
                .getDataLocation());
        chooser.setMultiSelectionEnabled(true);
        FileFilter filter = parser.getFileFilter();

        if (filter != null) {
            chooser.setFileFilter(parser.getFileFilter());

            int result = chooser.showOpenDialog(this);

			/*
             * There seems to be a bug in the file chooser which allows the user
			 * to select no files, but not cancel if the control+double click on
			 * a file
			 */
            if (result == JFileChooser.CANCEL_OPTION
                    || chooser.getSelectedFile() == null) {
                return;
            }

            REDPreferences.getInstance().setLastUsedDataLocation(
                    chooser.getSelectedFile());

            parser.setFiles(chooser.getSelectedFiles());
        }

        // See if we need to display any options
        if (parser.hasOptionsPanel()) {
            DataParserOptionsDialog optionsDialog = new DataParserOptionsDialog(
                    parser);
            optionsDialog.setLocationRelativeTo(this);
            boolean goAhead = optionsDialog.view();

            if (!goAhead) {
                return;
            }
        }

        ProgressDialog progressDialog = new ProgressDialog(this,
                "Loading data...", parser);
        parser.addProgressListener(progressDialog);

        try {
            parser.parseData();
        } catch (REDException ex) {
            new CrashReporter(ex);
        }
    }

    /**
     * Loads a genome assembly. This will fail if the genome isn't currently in
     * the local cache and downloadGenome should be set first in this case.
     *
     * @param baseLocation The folder containing the requested genome.
     */
    public void loadGenome(File baseLocation) {
        System.out.println(this.getClass().getName() + ":loadGenome(File baseLocation)");
        try {
            System.out.println(this.getClass().getName() + ":" + baseLocation.getCanonicalPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        GotoDialog.clearRecentLocations();
        IGVGenomeParser parser = new IGVGenomeParser();
        ProgressDialog progressDialog = new ProgressDialog(this,
                "Loading genome...");
        parser.addProgressListener(progressDialog);
        parser.addProgressListener(this);
        parser.parseGenome(baseLocation);
        progressDialog.requestFocus();
        progressDialog
                .setDefaultCloseOperation(ProgressDialog.DISPOSE_ON_CLOSE);
    }

    /**
     * Launches a FileChooser to select a project file to open
     */
    public void loadProject() {
        JFileChooser chooser = new JFileChooser(REDPreferences.getInstance()
                .getSaveLocation());
        chooser.setMultiSelectionEnabled(false);
        REDPreviewPanel previewPanel = new REDPreviewPanel();
        chooser.setAccessory(previewPanel);
        chooser.addPropertyChangeListener(previewPanel);
        chooser.setFileFilter(new FileFilterExt("red"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        File file = chooser.getSelectedFile();
        REDPreferences.getInstance().setLastUsedSaveLocation(file);

        loadProject(file);
    }

    /**
     * Loads an existing project from a file. This method will wipe all existing
     * data and prompt to save if the currently loaded project has changed.
     *
     * @param file The file to load
     */
    public void loadProject(File file) {
        System.out.println(this.getClass().getName() + ":loadProject(File file)");
        if (file == null)
            return;

		/*
		 * Before we wipe all of the data we need to check to see if we need to
		 * save the existing project.
		 */

        if (changesWereMade) {
            int answer = JOptionPane
                    .showOptionDialog(
                            this,
                            "You have made changes which were not saved.  Do you want to save before exiting?",
                            "Save before loading new data?", 0,
                            JOptionPane.QUESTION_MESSAGE, null, new String[]{
                            "Save before Loading",
                            "Load without Saving", "Cancel"}, "Save");

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

        REDPreferences.getInstance().setLastUsedSaveLocation(file);

        wipeAllData();

        currentFile = file;

        REDParser parser = new REDParser(this);
        parser.addProgressListener(this);
        ProgressDialog progressDialog = new ProgressDialog(this,
                "Loading data...");
        parser.addProgressListener(progressDialog);
        parser.parseFile(file);
        progressDialog.requestFocus();
        setTitle("RED [" + file.getName() + "]");

        REDPreferences.getInstance().addRecentlyOpenedFile(
                file.getAbsolutePath());

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
        for (int d = 0; d < stores.length; d++) {
            if (drawnDataStores.contains(stores[d])) {
                drawnDataStores.remove(stores[d]);
            }
        }
        chromosomeViewer.tracksUpdated();
    }

    /**
     * Unsets the changesWereMade flag so that the user will not be prompted to
     * save even if the data has changed.
     */
    public void resetChangesWereMade() {
        changesWereMade = false;
        if (getTitle().endsWith("*")) {
            // setTitle(getTitle().replaceAll("\\*$", ""));
            setTitle(getTitle().substring(0, getTitle().length() - 1));
        }
    }

    /**
     * Saves the current project under the same name as it was loaded. If no
     * file is associated with the project will call saveProjectAs
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

        writer.addProgressListener(new ProgressDialog(this,
                "Saving Project...", writer));
        writer.addProgressListener(this);

        writer.writeData(this, file);

        setTitle("RED [" + file.getName() + "]");
        REDPreferences.getInstance().addRecentlyOpenedFile(
                file.getAbsolutePath());
    }

    /**
     * Launches a FileChooser to allow the user to select a new file name under
     * which to save
     */
    public void saveProjectAs() {
        JFileChooser chooser = new JFileChooser(REDPreferences.getInstance()
                .getSaveLocation());
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileFilterExt("red"));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.CANCEL_OPTION)
            return;
        File file = chooser.getSelectedFile();
        if (!file.getPath().toLowerCase().endsWith(".red")) {
            file = new File(file.getPath() + ".red");
        }

        // Check if we're stepping on anyone's toes...
        if (file.exists()) {
            int answer = JOptionPane.showOptionDialog(this, file.getName()
                    + " exists.  Do you want to overwrite the existing file?",
                    "Overwrite file?", 0, JOptionPane.QUESTION_MESSAGE, null,
                    new String[]{"Overwrite and Save", "Cancel"},
                    "Overwrite and Save");
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
     * Replaces the current set of drawn datastores with a new list
     *
     * @param d The set of dataStores to display
     */
    public void setDrawnDataStores(DataStore[] d) {
        // Remember that we changed something
        changesWereMade();

        drawnDataStores.removeAllElements();
        for (int i = 0; i < d.length; i++) {
            drawnDataStores.add(d[i]);
        }
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
    public void dataSetAdded(DataSet d) {
        changesWereMade();
    }

    @Override
    public void dataSetsRemoved(DataSet[] d) {
        removeFromDrawnDataStores(d);
        changesWereMade();
    }

    @Override
    public void dataGroupAdded(DataGroup g) {
        changesWereMade();
    }

    @Override
    public void dataGroupsRemoved(DataGroup[] g) {
        removeFromDrawnDataStores(g);
        changesWereMade();
    }

    @Override
    public void dataSetRenamed(DataSet d) {
        chromosomeViewer.repaint();
        changesWereMade();
    }

    @Override
    public void dataGroupRenamed(DataGroup g) {
        chromosomeViewer.repaint();
        changesWereMade();
    }

    @Override
    public void dataGroupSamplesChanged(DataGroup g) {
        changesWereMade();
    }

    @Override
    public void probeSetReplaced(ProbeSet p) {
        p.addProbeSetChangeListener(this);
        changesWereMade();
    }

    @Override
    public void replicateSetAdded(ReplicateSet r) {
        changesWereMade();
    }

    @Override
    public void replicateSetsRemoved(ReplicateSet[] r) {
        removeFromDrawnDataStores(r);
        changesWereMade();
    }

    @Override
    public void replicateSetRenamed(ReplicateSet r) {
        chromosomeViewer.repaint();
        changesWereMade();
    }

    @Override
    public void replicateSetStoresChanged(ReplicateSet r) {
        changesWereMade();
    }

    @Override
    public void activeDataStoreChanged(DataStore s) {

    }

    @Override
    public void activeProbeListChanged(ProbeList l) {

    }

    @Override
    public void annotationSetsAdded(AnnotationSet[] annotationSets) {
        // If these annotation sets contains 3 or fewer feature types then add
        // them immediately to the annotation tracks

        changesWereMade();
    }

    @Override
    public void annotationSetRemoved(AnnotationSet annotationSet) {
        // TODO Auto-generated method stub
        // Check the list of drawn feature types to see if they're all still
        // valid

        // This is tricky as the way this works the annotation set hasn't yet
        // been deleted
        // from the annotation collection when we get this signal.

        changesWereMade();
    }

    @Override
    public void annotationSetRenamed(AnnotationSet annotationSet) {
        // TODO Auto-generated method stub
        chromosomeViewer.repaint();
        changesWereMade();
    }

    @Override
    public void annotationFeaturesRenamed(AnnotationSet annotationSet,
                                          String newName) {
        // TODO Auto-generated method stub
        // We have to treat this the same as if a set had been removed in that
        // any
        // of the existing feature tracks could be affected. We assume that they
        // want to
        // put the name

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
        } else if (command.equals("data_written")) {
            // Since we've just saved we can reset the changes flag
            resetChangesWereMade();

            // We might have been called by a previous shutdown
            // operation, in which case we need to send them
            // back to shut down.
            if (shuttingDown) {
                shuttingDown = false;
                dispose();
            }

            // We might have been called by a previous load operation
            // in which case we need to resume this load
            if (fileToLoad != null) {
                loadProject(fileToLoad);
                fileToLoad = null;
            }
        } else if (command.equals("load_annotation")) {
            System.out.println("load_annotation");
        } else {
            throw new IllegalArgumentException(
                    "Don't know how to handle progress command '" + command
                            + "'");
        }
    }

    @Override
    public void probeListAdded(ProbeList l) {
        // TODO Auto-generated method stub
        changesWereMade();
    }

    @Override
    public void probeListRemoved(ProbeList l) {
        // TODO Auto-generated method stub
        changesWereMade();
    }

    @Override
    public void probeListRenamed(ProbeList l) {
        // TODO Auto-generated method stub
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
            if (!application.welcomePanel.cacheDirectoryValid()) {
                JOptionPane.showMessageDialog(application, WELCOME_CONTENT,
                        WELCOME_TITLE, JOptionPane.INFORMATION_MESSAGE);
            }
            if (args.length > 0) {
                File f = new File(args[0]);
                application.loadProject(f);
            }
        } catch (Exception e) {
            new CrashReporter(e);
            e.printStackTrace();
        }

    }

}
