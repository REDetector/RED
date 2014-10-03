package com.xl.parsers.annotationparsers;

import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.annotation.CoreAnnotationSet;
import com.xl.datatypes.annotation.Cytoband;
import com.xl.datatypes.fasta.FastaDirectorySequence;
import com.xl.datatypes.fasta.FastaIndexedSequence;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.datatypes.sequence.IGVSequence;
import com.xl.datatypes.sequence.Sequence;
import com.xl.datatypes.sequence.SequenceWrapper;
import com.xl.dialog.ProgressDialog;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.*;
import com.xl.utils.filefilters.FileFilterImpl;
import com.xl.utils.namemanager.GenomeUtils;
import com.xl.utils.namemanager.SuffixUtils;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class IGVGenomeParser implements Runnable {
    /**
     * The listeners.
     */
    private Vector<ProgressListener> listeners = new Vector<ProgressListener>();
    /**
     * The genome.
     */
    private Genome genome = null;
    /**
     * The base location.
     */
    private File genomeFile = null;

    private String displayNameInCacheFile = null;

    private boolean cacheFailed = false;
    private BufferedReader cytobandReader = null;
    private BufferedReader geneReader = null;
    private GeneType geneType = null;

    /**
     * Parses the genome.
     *
     * @param baseLocation the base location
     */
    public void parseGenome(File baseLocation) {
        if (baseLocation.isDirectory()) {
            File[] files = baseLocation.listFiles(new FileFilterImpl("genome"));
            this.genomeFile = files[0];
        } else {
            this.genomeFile = baseLocation;
        }
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        File cacheDirectory = new File(LocationPreferences.getInstance().getCacheDirectory());
        Properties properties = null;
        List<File> genomeCacheCompleteFiles = FileUtils.searchfile(SuffixUtils.CACHE_GENOME_COMPLETE, cacheDirectory);
        String id = genomeFile.getAbsolutePath();
        boolean foundCacheFile = false;
        if (genomeCacheCompleteFiles.size() != 0) {
            for (File cacheCompleteFile : genomeCacheCompleteFiles) {
                try {
                    properties = new Properties();
                    properties.load(new FileReader(cacheCompleteFile));
                    String idInCacheFile = properties.getProperty(GenomeUtils.KEY_GENOME_ID);
                    if (id.contains(idInCacheFile)) {
                        foundCacheFile = true;
                        displayNameInCacheFile = properties.getProperty(GenomeUtils.KEY_DISPLAY_NAME);
                        String version = properties.getProperty(GenomeUtils.KEY_VERSION_NAME);
                        if (!REDApplication.VERSION.equals(version)) {
                            System.err.println("Version mismatch between cache version ('" + version + "') " +
                                    "and current version ('" + REDApplication.VERSION + "') - reparsing");
                            cacheFailed = true;
                            if (!cacheCompleteFile.delete()) {
                                System.err.println("Can not delete 'cache.complete' file. Please delete it individually...");
                                return;
                            }
                        } else {
                            cacheFailed = false;
                            break;
                        }
                    }
                } catch (IOException e) {
                    cacheFailed = true;
                    e.printStackTrace();
                }
            }
            if (!foundCacheFile) {
                cacheFailed = true;
            }
        } else {
            cacheFailed = true;
        }
        try {
            if (cacheFailed) {
                MessageUtils.showInfo(this.getClass().getName() + ":parseNewGenome();");
                if (displayNameInCacheFile != null)
                    FileUtils.deleteAllFilesWithSuffix(LocationPreferences.getInstance().getCacheDirectory() + File.separator
                            + displayNameInCacheFile, SuffixUtils.CACHE_GENOME);
                parseNewGenome();
            } else {
                if (properties != null) {
                    setGenomeDescriptor(properties);
                }
                MessageUtils.showInfo(this.getClass().getName() + ":reloadCacheGenome();");
                reloadCacheGenome();

            }
        } catch (IOException e) {
            progressCancelled();
            e.printStackTrace();
        }

        List<File> fastaCacheCompleteFiles = FileUtils.searchfile(SuffixUtils.CACHE_FASTA_COMPLETE, cacheDirectory);
        for (File fastaCacheCompleteFile : fastaCacheCompleteFiles) {
            properties = new Properties();
            try {
                properties.load(new FileReader(fastaCacheCompleteFile));
                String version = properties.getProperty(GenomeUtils.KEY_VERSION_NAME);
                if (REDApplication.VERSION.equals(version)) {
                    DisplayPreferences.getInstance().setFastaEnable(true);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void reloadCacheGenome() throws IOException {
        Enumeration<ProgressListener> el = listeners.elements();
        while (el.hasMoreElements()) {
            el.nextElement().progressUpdated("Reloading cache files", 0, 1);
        }
        if (genome == null) {
            GenomeDescriptor genomeDescriptor = GenomeDescriptor.getInstance();
            String id = genomeDescriptor.getGenomeId();
            String displayName = genomeDescriptor.getDisplayName();
            boolean isFasta = genomeDescriptor.isFasta();
            String[] fastaFileNames = genomeDescriptor.getFastaFileNames();
            String sequenceLocation = genomeDescriptor.getSequenceLocation();
            Sequence sequence;
            boolean chromosOrdered = false;
            LinkedHashMap<String, List<Cytoband>> cytobandMap = null;
            if (genomeDescriptor.hasCytobands()) {
                File cytobandFile = new File(LocationPreferences.getInstance().getOthersDirectory() + File
                        .separator + displayName + File.separator + genomeDescriptor.getCytoBandFileName());
                BufferedReader br = new BufferedReader(new FileReader(cytobandFile));
                cytobandMap = CytoBandFileParser.loadData(br, genomeDescriptor);
            }
            if (sequenceLocation == null) {
                sequence = null;
            } else if (!isFasta) {
                System.out.println(this.getClass().getName() + ":!isFasta");
                IGVSequence igvSequence = new IGVSequence(sequenceLocation);
                if (cytobandMap != null) {
                    chromosOrdered = genomeDescriptor.isChromosomesAreOrdered();
                    igvSequence.generateChromosomes(cytobandMap, chromosOrdered);
                }
                sequence = new SequenceWrapper(igvSequence);
            } else if (fastaFileNames != null && fastaFileNames.length != 0) {
                System.out.println(this.getClass().getName()
                        + ":fastaFileNames != null");
                FastaDirectorySequence fastaDirectorySequence = new FastaDirectorySequence(
                        sequenceLocation, fastaFileNames);
                sequence = new SequenceWrapper(fastaDirectorySequence);
            } else {
                System.out.println(this.getClass().getName() + ":else");
                FastaIndexedSequence fastaSequence = new FastaIndexedSequence(
                        sequenceLocation);
                chromosOrdered = true;
                sequence = new SequenceWrapper(fastaSequence);
            }
            genome = new Genome(id, displayName, sequence, chromosOrdered);
        }

        File cacheDir = new File(LocationPreferences.getInstance().getCacheDirectory() + File.separator + genome.getDisplayName());
        // First we need to get the list of chromosomes and set those
        // up before we go on to add the actual feature sets.
        File chrListFile = new File(cacheDir.getAbsoluteFile() + File.separator + "chr_list.txt");
        BufferedReader br = ParsingUtils.openBufferedReader(chrListFile);
        String line;
        while ((line = br.readLine()) != null) {
            String[] chrLen = line.split("\\t");
            if (ChromosomeUtils.isStandardChromosomeName(chrLen[0])) {
                Chromosome c = new Chromosome(chrLen[0], Integer.parseInt(chrLen[1]));
                genome.addChromosome(c);
            }
        }
        CoreAnnotationSet coreAnnotation = new CoreAnnotationSet(genome);
        File[] cacheFiles = cacheDir.listFiles(new FileFilterImpl(SuffixUtils.SUFFIX_CACHE_GENOME));
        for (int i = 0; i < cacheFiles.length; i++) {
            // Update the listeners
            String name = cacheFiles[i].getName();
            name = name.replaceAll("\\.genome.cache$", "");
            if (ChromosomeUtils.isStandardChromosomeName(name)) {
                Enumeration<ProgressListener> en = listeners.elements();
                while (en.hasMoreElements()) {
                    en.nextElement().progressUpdated("Adding cache file " + cacheFiles[i].getName(), i, cacheFiles.length);
                }
                coreAnnotation.addPreCachedFile(name, cacheFiles[i]);
            }
        }
        genome.getAnnotationCollection().addAnnotationSets(new AnnotationSet[]{coreAnnotation});
        progressComplete("load_genome", genome);
    }

    private void parseNewGenome() throws IOException {
        GenomeDescriptor genomeDescriptor = parseGenomeArchiveFile(genomeFile);
        final String id = genomeDescriptor.getGenomeId();
        final String displayName = genomeDescriptor.getDisplayName();
        boolean isFasta = genomeDescriptor.isFasta();
        String[] fastaFileNames = genomeDescriptor.getFastaFileNames();
        LinkedHashMap<String, List<Cytoband>> cytobandMap = null;
        if (cytobandReader != null && genomeDescriptor.hasCytobands()) {
            cytobandMap = CytoBandFileParser.loadData(cytobandReader, genomeDescriptor);
        }
        String sequenceLocation = genomeDescriptor.getSequenceLocation();
        Sequence sequence;
        boolean chromosOrdered = false;
        if (sequenceLocation == null) {
            sequence = null;
        } else if (!isFasta) {
            System.out.println(this.getClass().getName() + ":!isFasta");
            IGVSequence igvSequence = new IGVSequence(sequenceLocation);
            if (cytobandMap != null) {
                chromosOrdered = genomeDescriptor.isChromosomesAreOrdered();
                igvSequence.generateChromosomes(cytobandMap, chromosOrdered);
            }
            sequence = new SequenceWrapper(igvSequence);
        } else if (fastaFileNames != null && fastaFileNames.length != 0) {
            System.out.println(this.getClass().getName()
                    + ":fastaFileNames != null");
            FastaDirectorySequence fastaDirectorySequence = new FastaDirectorySequence(
                    sequenceLocation, fastaFileNames);
            sequence = new SequenceWrapper(fastaDirectorySequence);
        } else {
            System.out.println(this.getClass().getName() + ":else");
            FastaIndexedSequence fastaSequence = new FastaIndexedSequence(
                    sequenceLocation);
            chromosOrdered = true;
            sequence = new SequenceWrapper(fastaSequence);
        }
        genome = new Genome(id, displayName, sequence, chromosOrdered);

        if (cytobandMap != null) {
            genome.setCytobands(cytobandMap);
        }
        if (geneReader != null && geneType != null) {
            UCSCRefGeneParser parser = new UCSCRefGeneParser(genome);
            parser.addProgressListener(new ProgressDialog(REDApplication.getInstance(), parser.name(), parser));
            AnnotationSet[] sets = parser.parseAnnotation(geneType, geneReader, genome);

            // Here we have to add the new sets to the annotation
            // collection before we say that we're finished otherwise
            // this object can get destroyed before the program gets
            // chance to execute the operation which adds the sets to
            // the annotation collection.
            genome.getAnnotationCollection().addAnnotationSets(sets);

        }
        progressComplete("load_genome", genome);
    }

    public void setGenomeDescriptor(Properties properties) {
        String cytobandFileName = properties
                .getProperty(GenomeUtils.KEY_CYTOBAND_FILE_NAME);
        String geneFileName = properties.getProperty(GenomeUtils.KEY_GENE_FILE_NAME);
        String chrAliasFileName = properties
                .getProperty(GenomeUtils.KEY_CHR_ALIAS_FILE_NAME);
        String sequenceLocation = properties
                .getProperty(GenomeUtils.KEY_SEQUENCE_LOCATION);
        boolean chrNamesAltered = Boolean.parseBoolean(properties
                .getProperty(GenomeUtils.KEY_CHR_NAMES_ALTERED));
        boolean fasta = Boolean.parseBoolean(properties
                .getProperty(GenomeUtils.KEY_FASTA));
        boolean fastaDirectory = Boolean.parseBoolean(properties
                .getProperty(GenomeUtils.KEY_FASTA_DIRECTORY));
        boolean chromosomesAreOrdered = Boolean
                .parseBoolean(properties.getProperty(GenomeUtils.KEY_CHROMOSOMES_ARE_ORDERED));
        boolean hasCustomSequenceLocation = Boolean
                .parseBoolean(properties
                        .getProperty(GenomeUtils.KEY_HAS_CUSTOM_SEQUENCE_LOCATION));
        String fastaFileNameString = properties
                .getProperty(GenomeUtils.KEY_FASTA_FILE_NAME_STRING);
        String url = properties.getProperty(GenomeUtils.KEY_URL);
        String name = properties.getProperty(GenomeUtils.KEY_DISPLAY_NAME);
        String id = properties.getProperty(GenomeUtils.KEY_GENOME_ID);
        String geneTrackName = properties
                .getProperty(GenomeUtils.KEY_GENE_TRACK_NAME);
        GenomeDescriptor.getInstance().setAttributes(name,
                chrNamesAltered, id, cytobandFileName,
                geneFileName, chrAliasFileName, geneTrackName, url,
                sequenceLocation, hasCustomSequenceLocation,
                chromosomesAreOrdered, fasta, fastaDirectory,
                fastaFileNameString);
    }

    public GenomeDescriptor parseGenomeArchiveFile(File dotGenomeFile)
            throws IOException {
        ZipFile zipFile = new ZipFile(dotGenomeFile);
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(dotGenomeFile));
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String zipEntryName = zipEntry.getName();
            InputStream inputStream = zipFile.getInputStream(zipEntry);
            if (zipEntryName.toLowerCase().contains("property")) {
                Properties properties = new Properties();
                properties.load(inputStream);
                setGenomeDescriptor(properties);
            } else if (zipEntryName.toLowerCase().contains("cytoband")) {
                cytobandReader = new BufferedReader(new InputStreamReader(inputStream));
            } else if (zipEntryName.toLowerCase().contains("gene")) {
                geneReader = new BufferedReader(new InputStreamReader(inputStream));
                geneType = ParsingUtils.parseGeneType(zipEntryName);
            }
        }
        return GenomeDescriptor.getInstance();
    }

    /**
     * Adds the progress listener.
     *
     * @param pl the pl
     */
    public void addProgressListener(ProgressListener pl) {
        if (pl != null && !listeners.contains(pl))
            listeners.add(pl);
    }

    /**
     * Removes the progress listener.
     *
     * @param pl the pl
     */
    public void removeProgressListener(ProgressListener pl) {
        if (pl != null && listeners.contains(pl))
            listeners.remove(pl);
    }

    /**
     * Progress exception received.
     *
     * @param e the e
     */
    private void progressExceptionReceived(Exception e) {
        Enumeration<ProgressListener> en = listeners.elements();
        while (en.hasMoreElements()) {
            en.nextElement().progressExceptionReceived(e);
        }
    }

    private void progressCancelled() {
        Enumeration<ProgressListener> en = listeners.elements();
        while (en.hasMoreElements()) {
            en.nextElement().progressCancelled();
        }
    }

    /**
     * Progress complete.
     *
     * @param command the command
     * @param result  the result
     */
    private void progressComplete(String command, Object result) {
        Enumeration<ProgressListener> en = listeners.elements();
        while (en.hasMoreElements()) {
            en.nextElement().progressComplete(command, result);
        }

    }
}
