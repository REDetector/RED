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
import com.xl.exception.REDException;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;
import com.xl.utils.*;
import com.xl.utils.filefilters.FileFilterImpl;
import com.xl.utils.namemanager.GenomeUtils;

import java.io.*;
import java.util.*;
import java.util.zip.*;

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
    private File baseLocation = null;
    private File genomeFile = null;
    private ZipFile zipFile = null;
    private FileInputStream fileInputStream = null;
    private ZipInputStream zipInputStream = null;
    private Map<String, ZipEntry> zipEntries = null;
    private boolean cacheFailed = true;

    private static Collection<Collection<String>> loadChrAliases(
            BufferedReader br) throws IOException {
        String nextLine;
        Collection<Collection<String>> synonymList = new ArrayList<Collection<String>>();
        while ((nextLine = br.readLine()) != null) {
            String[] tokens = nextLine.split("\t");
            if (tokens.length > 1) {
                Collection<String> synonyms = new ArrayList<String>();
                for (String t : tokens) {
                    String syn = t.trim();
                    if (t.length() > 0)
                        synonyms.add(syn.trim());
                }
                synonymList.add(synonyms);
            }
        }
        return synonymList;
    }

    /**
     * Parses the genome.
     *
     * @param baseLocation the base location
     */
    public void parseGenome(File baseLocation) {
        if (baseLocation.isDirectory()) {
            File[] files = baseLocation.listFiles(new FileFilterImpl("genome"));
            this.genomeFile = files[0];
            this.baseLocation = baseLocation;
        } else {
            this.genomeFile = baseLocation;
            this.baseLocation = genomeFile.getParentFile();
        }
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        File cacheCompleteFile = new File(baseLocation.getAbsolutePath() + "/cache/cache.complete");
        Properties properties = null;
        if (cacheCompleteFile.exists()) {
            cacheFailed = false;
            try {
                properties = new Properties();
                InputStream is = new FileInputStream(cacheCompleteFile);
                properties.load(is);
                String version = properties.getProperty(GenomeUtils.KEY_VERSION_NAME);
                if (!REDApplication.VERSION.equals(version)) {
                    System.err.println("Version mismatch between cache ('" + version + "') and current version ('" + REDApplication.VERSION + "') - reparsing");
                    cacheFailed = true;
                    if (!cacheCompleteFile.delete()) {
                        System.err.println("Can not delete 'cache.complete' file. Please delete it individually...");
                    }
                }
                // We re-parse if the cache was made by a different version
            } catch (Exception ioe) {
                cacheFailed = true;
            }
        }
        if (cacheFailed) {
            MessageUtils.showInfo(IGVGenomeParser.class, "parseNewGenome();");
            try {
                FileUtils.deleteDirectory(baseLocation.getCanonicalPath() + "/cache");
            } catch (IOException e) {
                e.printStackTrace();
            }
            parseNewGenome();
        } else {
            if (properties != null) {
                setGenomeDescriptor(properties);
            }
            MessageUtils.showInfo(this.getClass().getName() + ":reloadCacheGenome();");
            try {
                reloadCacheGenome();
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
            if (sequenceLocation == null) {
                sequence = null;
            } else if (!isFasta) {
                System.out.println(this.getClass().getName() + ":!isFasta");
                IGVSequence igvSequence = new IGVSequence(sequenceLocation);
                sequence = new SequenceWrapper(igvSequence);
            } else if (fastaFileNames != null) {
                System.out.println(this.getClass().getName()
                        + ":fastaFileNames != null");
                FastaDirectorySequence fastaDirectorySequence = new FastaDirectorySequence(
                        sequenceLocation, fastaFileNames);
                sequence = new SequenceWrapper(fastaDirectorySequence);
            } else {
                System.out.println(this.getClass().getName() + ":else");
                FastaIndexedSequence fastaSequence = new FastaIndexedSequence(
                        sequenceLocation);
                sequence = new SequenceWrapper(fastaSequence);
                chromosOrdered = false;
            }
            genome = new Genome(id, displayName, sequence, chromosOrdered);
        }

        CoreAnnotationSet coreAnnotation = new CoreAnnotationSet(genome);

        File cacheDir = new File(baseLocation.getAbsoluteFile() + "/cache/");
        // First we need to get the list of chromosomes and set those
        // up before we go on to add the actual feature sets.
        File chrListFile = new File(baseLocation.getAbsoluteFile() + "/cache/chr_list");
        try {
            BufferedReader br = ParsingUtils.openBufferedReader(chrListFile);

            String line;
            while ((line = br.readLine()) != null) {
                String[] chrLen = line.split("\\t");
                if (ChromosomeUtils.isStandardChromosomeName(chrLen[0])) {
                    Chromosome c = new Chromosome(chrLen[0], Integer.parseInt(chrLen[1]));
                    genome.addChromosome(c);
                }
            }
        } catch (Exception e) {
//            new CrashReporter(e);
            e.printStackTrace();
        }

        File[] cacheFiles = cacheDir.listFiles(new FileFilterImpl("cache"));
        for (int i = 0; i < cacheFiles.length; i++) {
            // Update the listeners
            String name = cacheFiles[i].getName();
            name = name.replaceAll("\\.cache$", "");
            if (ChromosomeUtils.isStandardChromosomeName(name)) {
                coreAnnotation.addPreCachedFile(name, cacheFiles[i]);
            }
        }
        genome.getAnnotationCollection().addAnnotationSets(new AnnotationSet[]{coreAnnotation});
        System.out.println(this.getClass().getName() + ":AllFeatures().length:" + coreAnnotation.getAllFeatures().length);
        progressComplete("load_genome", genome);
    }

    private void parseNewGenome() {
        try {
            GenomeDescriptor genomeDescriptor = parseGenomeArchiveFile(genomeFile);
            final String id = genomeDescriptor.getGenomeId();
            final String displayName = genomeDescriptor.getDisplayName();
            boolean isFasta = genomeDescriptor.isFasta();
            String[] fastaFileNames = genomeDescriptor.getFastaFileNames();
            LinkedHashMap<String, List<Cytoband>> cytobandMap = null;
            if (genomeDescriptor.hasCytobands()) {
                cytobandMap = loadCytoBandFile();
            }
            String sequenceLocation = genomeDescriptor.getSequenceLocation();
            Sequence sequence = null;
            boolean chromosOrdered = false;
            if (sequenceLocation == null) {
                sequence = null;
            } else if (!isFasta) {
                System.out.println(this.getClass().getName() + ":!isFasta");
                IGVSequence igvSequence = new IGVSequence(sequenceLocation);
                if (cytobandMap != null) {
                    chromosOrdered = genomeDescriptor.isChromosomesAreOrdered();
                    igvSequence
                            .generateChromosomes(cytobandMap, chromosOrdered);
                }
                sequence = new SequenceWrapper(igvSequence);
            } else if (fastaFileNames != null) {
                System.out.println(this.getClass().getName()
                        + ":fastaFileNames != null");
                FastaDirectorySequence fastaDirectorySequence = new FastaDirectorySequence(
                        sequenceLocation, fastaFileNames);
                sequence = new SequenceWrapper(fastaDirectorySequence);
            } else {
                System.out.println(this.getClass().getName() + ":else");
                FastaIndexedSequence fastaSequence = new FastaIndexedSequence(
                        sequenceLocation);
                sequence = new SequenceWrapper(fastaSequence);
                chromosOrdered = false;
            }
            genome = new Genome(id, displayName, sequence, chromosOrdered);

            if (cytobandMap != null) {
                genome.setCytobands(cytobandMap);
            }

            InputStream geneStream = null;
            String geneFileName = genomeDescriptor.getGeneFileName();
            if (geneFileName != null) {
                try {
                    if (geneFileName.endsWith(".gz")) {
                        geneStream = new GZIPInputStream(
                                zipFile.getInputStream(zipEntries
                                        .get(geneFileName)));
                    } else {
                        geneStream = zipFile.getInputStream(zipEntries
                                .get(geneFileName));
                    }
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(geneStream));
                    UCSCRefGeneParser parser = new UCSCRefGeneParser(genome);
                    GeneType geneType = ParsingUtils
                            .parseGeneType(geneFileName);
                    AnnotationSet[] sets = parser.parseAnnotation(geneType, reader);

                    // Here we have to add the new sets to the annotation
                    // collection before we say that we're finished otherwise
                    // this object can get destroyed before the program gets
                    // chance to execute the operation which adds the sets to
                    // the annotation collection.
                    genome.getAnnotationCollection().addAnnotationSets(sets);

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    progressExceptionReceived(e);
                } finally {
                    if (geneStream != null)
                        geneStream.close();
                }
            }

            progressComplete("load_genome", genome);

        } catch (Exception e) {
            e.printStackTrace();
            progressCancelled();
        } finally {
            try {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setGenomeDescriptor(Properties properties) {
        String cytobandFileName = properties
                .getProperty("cytobandFile");
        String geneFileName = properties.getProperty("geneFile");
        String chrAliasFileName = properties
                .getProperty("chrAliasFile");
        String sequenceLocation = properties
                .getProperty("sequenceLocation");
        boolean chrNamesAltered = Boolean.parseBoolean(properties
                .getProperty("filenamesAltered"));
        boolean fasta = Boolean.parseBoolean(properties
                .getProperty("fasta"));
        boolean fastaDirectory = Boolean.parseBoolean(properties
                .getProperty("fastaDirectory"));
        boolean chromosomesAreOrdered = Boolean
                .parseBoolean(properties.getProperty("ordered"));
        boolean hasCustomSequenceLocation = Boolean
                .parseBoolean(properties
                        .getProperty("customSequenceLocation"));
        String fastaFileNameString = properties
                .getProperty("fastaFiles");
        String url = properties.getProperty("url");
        String name = properties.getProperty("name");
        String id = properties.getProperty("id");
        String geneTrackName = properties
                .getProperty("geneTrackName");
        GenomeDescriptor.getInstance().setAttributes(name,
                chrNamesAltered, id, cytobandFileName,
                geneFileName, chrAliasFileName, geneTrackName, url,
                sequenceLocation, hasCustomSequenceLocation,
                chromosomesAreOrdered, fasta, fastaDirectory,
                fastaFileNameString);
    }

    public GenomeDescriptor parseGenomeArchiveFile(File dotGenomeFile)
            throws ZipException, IOException {
        zipFile = new ZipFile(dotGenomeFile);
        fileInputStream = new FileInputStream(dotGenomeFile);
        zipInputStream = new ZipInputStream(fileInputStream);
        zipEntries = new HashMap<String, ZipEntry>();
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String zipEntryName = zipEntry.getName();
            zipEntries.put(zipEntryName, zipEntry);
            if (zipEntryName.equalsIgnoreCase("property.txt")) {
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                Properties properties = new Properties();
                properties.load(inputStream);
                setGenomeDescriptor(properties);
            }
        }
        return GenomeDescriptor.getInstance();
    }

    public LinkedHashMap<String, List<Cytoband>> loadCytoBandFile() {
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = zipFile.getInputStream(zipEntries.get(GenomeDescriptor
                    .getInstance().getCytoBandFileName()));

            if (GenomeDescriptor.getInstance().getCytoBandFileName()
                    .toLowerCase().endsWith(".gz")) {
                is = new GZIPInputStream(is);
            }
            reader = new BufferedReader(new InputStreamReader(is));
            return CytoBandFileParser.loadData(reader);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Load the chromosome alias file, if any, specified in the genome
     * descriptor.
     *
     * @param genomeDescriptor
     * @return The chromosome alias table, or null if none is defined.
     * @throws REDException
     */
    private Collection<Collection<String>> loadChrAliases(
            GenomeDescriptor genomeDescriptor) throws REDException {
        InputStream aliasStream = null;
        try {
            String fileName = genomeDescriptor.getChrAliasFileName();
            if (fileName == null || !zipEntries.containsKey(fileName)) {
                return null;
            }
            aliasStream = zipFile.getInputStream(zipEntries.get(fileName));
            if (aliasStream != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(aliasStream));
                return loadChrAliases(reader);
            } else {
                return null;
            }
        } catch (IOException e) {
            // We don't want to bomb if the alias load fails. Just log it and
            // proceed.
            throw new REDException("Error loading chromosome alias table");
        } finally {
            try {
                if (aliasStream != null) {
                    aliasStream.close();
                }
            } catch (IOException ex) {
                throw new REDException("Error closing zip stream!");
            }
        }
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
