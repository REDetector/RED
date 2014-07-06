package com.xl.parsers.dataparsers;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;
import com.xl.utils.ChromosomeUtils;
import com.xl.utils.FileUtils;
import com.xl.utils.ParsingUtils;

import javax.swing.*;
import java.io.*;
import java.util.Vector;

public class FastaFileParser extends DataParser {
    private final String CACHE_COMPLETE = "fasta.complete";
    private Genome genome;
    private File fastaBase;


    public FastaFileParser(DataCollection collection) {
        super(collection);
        genome = collection.genome();
    }

    private static String skipToNextChr(BufferedReader br, String nextLine) throws IOException {
        nextLine = nextLine.substring(1).split("\\s+")[0];
        if (ChromosomeUtils.isStandardChromosomeName(nextLine)) {
            return nextLine;
        } else {
            while ((nextLine = br.readLine()) != null) if (nextLine.startsWith(">")) {
                nextLine = nextLine.substring(1).split("\\s+")[0];
                if (ChromosomeUtils.isStandardChromosomeName(nextLine)) {
                    return nextLine;
                }
            }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            fastaBase = new File(REDPreferences.getInstance().getGenomeBase() + File.separator + genome.getDisplayName() + File.separator + "fasta");
            if (fastaBase.exists()) {
                File cacheFile = new File(fastaBase.getCanonicalPath() + File.separator + CACHE_COMPLETE);
                FileReader fr = new FileReader(cacheFile);
                BufferedReader br = new BufferedReader(fr);
                String version = br.readLine();
                if (version.equals(REDApplication.VERSION)) {
                    processingComplete(null);
                } else {
                    FileUtils.deleteDirectory(fastaBase.getCanonicalPath());
                    reparseFasta();
                }
            } else {
                if (!fastaBase.mkdirs()) {
                    System.err.println("Fasta file path " + fastaBase.getAbsolutePath() + " can be accessed.");
                    progressCancelled();
                } else {
                    reparseFasta();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GenomeDescriptor.getInstance().setFasta(true);
        GenomeDescriptor.getInstance().setFastaDirectory(true);
        GenomeDescriptor.getInstance().setSequenceLocation(fastaBase.getAbsolutePath());
    }

    @Override
    public JPanel getOptionsPanel() {
        return null;
    }

    @Override
    public boolean hasOptionsPanel() {
        return false;
    }

    @Override
    public boolean readyToParse() {
        return true;
    }

    @Override
    public String parserName() {
        return "Fasta File Importer";
    }

    @Override
    public String getDescription() {
        return "Import the fasta data including original reference sequence";
    }

    /**
     * Read an entire fasta file, which might be local or remote and might be gzipped.
     *
     * @param
     */
    public void reparseFasta() throws IOException {
        if (!fastaBase.exists()) {
            if (!fastaBase.mkdirs()) {
                throw new IOException("The path '" + fastaBase.getCanonicalPath() + "' can not be accessed. ");
            }
        }
        BufferedReader br;
        FileOutputStream fos = null;
        String currentChr = null;
        File[] fastaFiles = getFiles();
        String nextLine;
        Vector<String> fastaFilesPath = new Vector<String>(24);
        for (File fastaFile : fastaFiles) {
            br = ParsingUtils.openBufferedReader(fastaFile);
            int line = 0;
            int chr = 0;
            while ((nextLine = br.readLine()) != null) {
                if (nextLine.startsWith(">")) {
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                        System.gc();
                    }
                    currentChr = skipToNextChr(br, nextLine);
                    if (currentChr != null) {
                        chr++;
                        line = 0;
                        String currentChrPath = fastaBase.getAbsolutePath() + File.separator + currentChr + "" +
                                ".fasta.cache";
                        fos = new FileOutputStream(currentChrPath, true);
                        fastaFilesPath.add(currentChrPath);
                    }
                } else {
                    if (fos != null) {
                        fos.write(nextLine.trim().getBytes());
                        if (line++ % 100000 == 0) {
                            progressUpdated("Read " + line + " lines from " + currentChr + ", " + genome.getDisplayName(), chr, 24);
                        }
                    }
                }
            }
            if (fos != null) {
                fos.flush();
                fos.close();
                System.gc();
            }
            br.close();
        }
        FileWriter fw = new FileWriter(fastaBase.getAbsolutePath() + File.separator + CACHE_COMPLETE);
        fw.write(REDApplication.VERSION);
        fw.close();
        processingComplete(null);
    }

}
