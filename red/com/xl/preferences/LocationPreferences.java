package com.xl.preferences;

import com.xl.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Administrator on 2014/6/23.
 */
public class LocationPreferences {
    public static final String PROJECT_DIRECTORY = "ProjectDirectory";
    public static final String PROJECT_DATA_DIRECTORY = "ProjectDataDirectory";
    public static final String FASTA_DIRECTORY = "Fasta";
    public static final String GENOME_DIRECTORY = "Genome";
    public static final String RNA_DIRECTORY = "RNA";
    public static final String DNA_DIRECTORY = "DNA";
    public static final String ANNOTATION_DIRECTORY = "Annotation";
    public static final String TEMP_DIRECTORY = "Temp";
    public static final String OTHERS_DIRECTORY = "Others";
    public static final String CACHE_DIRECTORY = "Cache";
    public static final String GENOME_DOWNLOAD_LISTS = "GenomeDownload";
    public static final String RECENTLY_OPENED_FILES = "RecentlyOpenedFiles";
    public static final String RECENTLY_OPENED_FILES_NUMBER = "RecentlyOpenedFilesNumber";
    public static final String DATA_DIRECTORY = "Data";
    public static final String DNA_VCF_FILE = "DNA Vcf File";
    public static final String RNA_VCF_FILE = "RNA Vcf File";
    public static final String REPEAT_FILE = "Repeat File";
    public static final String REF_SEQ_FILE = "RefSeq File";
    public static final String DBSNP_FILE = "dbSNP File";
    public static final String DARNED_FILE = "DARNED File";
    public static final String R_SCRIPT_PATH = "R Script Path";
    public static final String CYTOBAND_FILE = "Cytoband File";

    public static LocationPreferences locationPreferences = new LocationPreferences();

    /**
     * The project root directory
     */
    private final String projectDirectory = new File("").getAbsolutePath();
    private String projectDataDirectory = projectDirectory + File.separator + DATA_DIRECTORY;
    /**
     * The fasta directory, which is associated with the relevent genome.
     */
    private String fastaDirectory = projectDataDirectory + File.separator + FASTA_DIRECTORY;
    /**
     * The genome directory, mainly contains the genome features from 'refGene.txt'
     */
    private String genomeDirectory = projectDataDirectory + File.separator + GENOME_DIRECTORY;
    /**
     * The RNA directory, we save all RNA data or their cache file here.
     */
    private String rnaDirectory = projectDataDirectory + File.separator + RNA_DIRECTORY;
    /**
     * The DNA directory, we save all DNA data or their cache file here.
     */
    private String dnaDirectory = projectDataDirectory + File.separator + DNA_DIRECTORY;
    /**
     * The annotation directory, we save all annotation data or their cache file here.
     */
    private String annotationDirectory = projectDataDirectory + File.separator + ANNOTATION_DIRECTORY;
    /**
     * The directory in which to save temporary cache files
     */
    private String tempDirectory = projectDataDirectory + File.separator + TEMP_DIRECTORY;
    /**
     * The directory in which to save cache files to make all data load faster
     */
    private String cacheDirectory = projectDataDirectory + File.separator + CACHE_DIRECTORY;
    /**
     * The directory in which to save some fragmentary files like 'cytoband.txt'
     */
    private String othersDirectory = projectDataDirectory + File.separator + OTHERS_DIRECTORY;
    /**
     * The default save location.
     */
    private String projectSaveLocation = projectDirectory;
    /**
     * The network address from where we can download new genomes
     */
    private String genomeDownloadLists = "http://igv.broadinstitute.org/genomes/";
    /**
     * It depends on whether the '.genome' file has the cytoband property.
     */
    private String cytobandFile = "";
    private String dnaVcfFile = "";
    private String rnaVcfFile = "";
    private String repeatFile = "";
    private String refSeqFile = "";
    private String dbSNPFile = "";
    private String darnedFile = "";
    private String rScriptPath = "";

    /**
     * The recently opened files list
     */
    private LinkedList<String> recentlyOpenedFiles = new LinkedList<String>();

    private Map<String, String> directories = new HashMap<String, String>();

    private LocationPreferences() {
        initialDirectories();
    }

    public static LocationPreferences getInstance() {
        return locationPreferences;
    }

    public void initialDirectories() {
        directories.clear();
        directories.put(PROJECT_DIRECTORY, projectDirectory);
        directories.put(PROJECT_DATA_DIRECTORY, projectDataDirectory);
        directories.put(GENOME_DIRECTORY, genomeDirectory);
        directories.put(FASTA_DIRECTORY, fastaDirectory);
        directories.put(RNA_DIRECTORY, rnaDirectory);
        directories.put(DNA_DIRECTORY, dnaDirectory);
        directories.put(ANNOTATION_DIRECTORY, annotationDirectory);
        directories.put(TEMP_DIRECTORY, tempDirectory);
        directories.put(OTHERS_DIRECTORY, othersDirectory);
        directories.put(CACHE_DIRECTORY, cacheDirectory);
        Collection<String> collection = directories.values();
        for (String directory : collection) {
            FileUtils.createDirectory(directory);
        }
    }

    public void updateDirectories() {
        Collection<String> collection = directories.values();
        for (String directory : collection) {
            FileUtils.createDirectory(directory);
        }
        setProjectDataDirectory(directories.get(PROJECT_DATA_DIRECTORY));
        setGenomeDirectory(directories.get(GENOME_DIRECTORY));
        setFastaDirectory(directories.get(FASTA_DIRECTORY));
        setRnaDirectory(directories.get(RNA_DIRECTORY));
        setDnaDirectory(directories.get(DNA_DIRECTORY));
        setAnnotationDirectory(directories.get(ANNOTATION_DIRECTORY));
        setOthersDirectory(directories.get(OTHERS_DIRECTORY));
        setTempDirectory(directories.get(TEMP_DIRECTORY));
        setCacheDirectory(directories.get(CACHE_DIRECTORY));
    }

    /**
     * Gets the default data location. This will initially be the data location
     * saved in the preferneces file, but will be updated during use with the
     * last actual location where data was imported. If you definitely want the
     * location stored in the preferences file then use
     * getDataLocationPreference()
     *
     * @return The default location to look for new data
     */
    public String getProjectDataDirectory() {
        return projectDataDirectory;
    }

    /**
     * Sets the default data location which will be saved in the preferences
     * file.
     *
     * @param projectDataDirectory The new data location
     */
    public void setProjectDataDirectory(String projectDataDirectory) {
        this.projectDataDirectory = projectDataDirectory;
    }


    public String getFastaDirectory() {
        return fastaDirectory;
    }

    public void setFastaDirectory(String fastaDirectory) {
        this.fastaDirectory = fastaDirectory;
    }

    public String getGenomeDirectory() {
        return genomeDirectory;
    }

    public void setGenomeDirectory(String genomeDirectory) {
        this.genomeDirectory = genomeDirectory;
    }

    public String getRnaDirectory() {
        return rnaDirectory;
    }

    public void setRnaDirectory(String rnaDirectory) {
        this.rnaDirectory = rnaDirectory;
    }

    public String getDnaDirectory() {
        return dnaDirectory;
    }

    public void setDnaDirectory(String dnaDirectory) {
        this.dnaDirectory = dnaDirectory;
    }

    public String getAnnotationDirectory() {
        return annotationDirectory;
    }

    public void setAnnotationDirectory(String annotationDirectory) {
        this.annotationDirectory = annotationDirectory;
    }

    /**
     * The location of the directory to use to cache data
     *
     * @return A file representing the temp directory. Null if none is set.
     */
    public String getTempDirectory() {
        return tempDirectory;
    }

    /**
     * Sets the temp directory.
     *
     * @param tempDirectory The new temp directory
     */
    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public String getCytobandFile() {
        return cytobandFile;
    }

    public void setCytobandFile(String cytobandFile) {
        this.cytobandFile = cytobandFile;
    }

    public String getOthersDirectory() {
        return othersDirectory;
    }

    public void setOthersDirectory(String othersDirectory) {
        this.othersDirectory = othersDirectory;
    }

    public String getCacheDirectory() {
        return cacheDirectory;
    }

    public void setCacheDirectory(String cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    public String getProjectSaveLocation() {
        return projectSaveLocation;
    }

    /**
     * Sets the save location to record in the preferences file
     *
     * @param projectSaveLocation The new save location
     */
    public void setProjectSaveLocation(String projectSaveLocation) {
        this.projectSaveLocation = projectSaveLocation;
    }

    public Map<String, String> getDirectories() {
        return directories;
    }


    /**
     * Gets the list of recently opened files.
     *
     * @return the recently opened files
     */
    public LinkedList<String> getRecentlyOpenedFiles() {
        return recentlyOpenedFiles;
    }

    /**
     * Adds a path to the recently opened files list. We store up to 5 recently
     * used files on a rotating basis. Adding a new one pushes out the oldest
     * one.
     *
     * @param filePath The new file location to add
     */
    public void addRecentlyOpenedFile(String filePath) {
        // I know this is inefficient in a linked list but
        // it's only going to contain 5 elements so who cares
        if (recentlyOpenedFiles.contains(filePath)) {
            recentlyOpenedFiles.remove(filePath);
        }
        recentlyOpenedFiles.add(0, filePath);

        // Only keep 9 items
        while (recentlyOpenedFiles.size() > 5) {
            recentlyOpenedFiles.remove(5);
        }
        try {
            REDPreferences.getInstance().savePreferences();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the genome download location.
     *
     * @return The URL under which new genomes can be downloaded
     */
    public String getGenomeDownloadLists() {
        return genomeDownloadLists;
    }

    public void setGenomeDownloadLists(String genomeDownloadLists) {
        this.genomeDownloadLists = genomeDownloadLists;
    }

    public String getDarnedFile() {
        return darnedFile;
    }

    public void setDarnedFile(String darnedFile) {
        this.darnedFile = darnedFile;
    }

    public String getRnaVcfFile() {
        return rnaVcfFile;
    }

    public void setRnaVcfFile(String rnaVcfFile) {
        this.rnaVcfFile = rnaVcfFile;
    }

    public String getRepeatFile() {
        return repeatFile;
    }

    public void setRepeatFile(String repeatFile) {
        this.repeatFile = repeatFile;
    }

    public String getRefSeqFile() {
        return refSeqFile;
    }

    public void setRefSeqFile(String refSeqFile) {
        this.refSeqFile = refSeqFile;
    }

    public String getDbSNPFile() {
        return dbSNPFile;
    }

    public void setDbSNPFile(String dbSNPFile) {
        this.dbSNPFile = dbSNPFile;
    }

    public String getDnaVcfFile() {
        return dnaVcfFile;
    }

    public void setDnaVcfFile(String dnaVcfFile) {
        this.dnaVcfFile = dnaVcfFile;
    }

    public String getRScriptPath() {
        return rScriptPath;
    }

    public void setRScriptPath(String rScriptPath) {
        this.rScriptPath = rScriptPath;
    }

    /**
     * Save preferences.
     *
     * @throws IOException
     */
    public void savePreferences(Properties properties) throws IOException {
        properties.setProperty(PROJECT_DIRECTORY, projectDirectory);
        properties.setProperty(PROJECT_DATA_DIRECTORY, projectDataDirectory);
        properties.setProperty(FASTA_DIRECTORY, fastaDirectory);
        properties.setProperty(GENOME_DIRECTORY, genomeDirectory);
        properties.setProperty(RNA_DIRECTORY, rnaDirectory);
        properties.setProperty(DNA_DIRECTORY, dnaDirectory);
        properties.setProperty(ANNOTATION_DIRECTORY, annotationDirectory);
        properties.setProperty(TEMP_DIRECTORY, tempDirectory);
        properties.setProperty(OTHERS_DIRECTORY, othersDirectory);
        properties.setProperty(GENOME_DOWNLOAD_LISTS, genomeDownloadLists);
        properties.setProperty(CACHE_DIRECTORY, cacheDirectory);
        properties.setProperty(RNA_VCF_FILE, rnaVcfFile);
        properties.setProperty(DNA_VCF_FILE, dnaVcfFile);
        properties.setProperty(REPEAT_FILE, repeatFile);
        properties.setProperty(REF_SEQ_FILE, refSeqFile);
        properties.setProperty(DBSNP_FILE, dbSNPFile);
        properties.setProperty(DARNED_FILE, darnedFile);
        properties.setProperty(R_SCRIPT_PATH, rScriptPath);
        properties.setProperty(CYTOBAND_FILE, cytobandFile);
        // Save the recently opened file list
        properties.setProperty(RECENTLY_OPENED_FILES_NUMBER, recentlyOpenedFiles.size() + "");
        for (int i = 0, len = recentlyOpenedFiles.size(); i < len; i++) {
            properties.setProperty(RECENTLY_OPENED_FILES + "_" + i, recentlyOpenedFiles.get(i));
        }
    }

    public void loadPreferences(Properties properties) throws IOException {
        setProjectDataDirectory(properties.getProperty(PROJECT_DATA_DIRECTORY));
        setFastaDirectory(properties.getProperty(FASTA_DIRECTORY));
        setGenomeDirectory(properties.getProperty(GENOME_DIRECTORY));
        setRnaDirectory(properties.getProperty(RNA_DIRECTORY));
        setDnaDirectory(properties.getProperty(DNA_DIRECTORY));
        setAnnotationDirectory(properties.getProperty(ANNOTATION_DIRECTORY));
        setTempDirectory(properties.getProperty(TEMP_DIRECTORY));
        setOthersDirectory(properties.getProperty(OTHERS_DIRECTORY));
        setGenomeDownloadLists(properties.getProperty(GENOME_DOWNLOAD_LISTS));
        setCacheDirectory(properties.getProperty(CACHE_DIRECTORY));
        setRnaVcfFile(properties.getProperty(RNA_VCF_FILE));
        setDnaVcfFile(properties.getProperty(DNA_VCF_FILE));
        setRepeatFile(properties.getProperty(REPEAT_FILE));
        setRefSeqFile(properties.getProperty(REF_SEQ_FILE));
        setDbSNPFile(properties.getProperty(DBSNP_FILE));
        setDarnedFile(properties.getProperty(DARNED_FILE));
        setRScriptPath(properties.getProperty(R_SCRIPT_PATH));
        setCytobandFile(properties.getProperty(CYTOBAND_FILE));
        int recentlyOpenedFilesNumber = Integer.parseInt(properties.getProperty(RECENTLY_OPENED_FILES_NUMBER));
        recentlyOpenedFiles.clear();
        for (int i = 0; i < recentlyOpenedFilesNumber; i++) {
            recentlyOpenedFiles.add(properties.getProperty(RECENTLY_OPENED_FILES + "_" + i));
        }
    }

}
