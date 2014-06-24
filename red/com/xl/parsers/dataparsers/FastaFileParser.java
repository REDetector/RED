package com.xl.parsers.dataparsers;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.genome.Genome;
import com.xl.preferences.REDPreferences;
import com.xl.utils.ChromosomeUtils;
import com.xl.utils.ParsingUtils;
import net.sf.jfasta.FASTAElement;
import net.sf.jfasta.FASTAFileReader;
import net.sf.jfasta.impl.FASTAElementIterator;
import net.sf.jfasta.impl.FASTAFileReaderImpl;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FastaFileParser extends DataParser {
    private Genome genome;

    public FastaFileParser(DataCollection collection) {
        super(collection);
        genome = collection.genome();
    }

    @Override
    public void run() {
        try {
            parseFasta();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void parseFasta() throws IOException {

        BufferedReader br = null;
        File cacheBase = new File(REDPreferences.getInstance()
                .getGenomeBase()
                + "/"
                + genome.getDisplayName() + "/cache");
        if (!cacheBase.exists()) {
            if (!cacheBase.mkdir()) {
                throw new IOException(
                        "Can't create cache file for core annotation set");
            }
        }
        File[] fastaFiles = getFiles();
        br = ParsingUtils.openBufferedReader(fastaFiles[0]);
        FileOutputStream fos = null;
        String currentChr = null;
        String nextLine;
        int line = 0;
        int chr = 0;
        while ((nextLine = br.readLine()) != null) {
            if (nextLine.startsWith("#") || nextLine.trim().length() == 0) {

            } else if (nextLine.startsWith(">")) {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                    System.gc();
                }
                currentChr = skipToNextChr(br, nextLine);
                if (currentChr != null) {
                    chr++;
                    line = 0;
                    fos = new FileOutputStream(cacheBase.getAbsolutePath() + File.separator + currentChr + ".fasta", true);
                    System.out.println(FastaFileParser.this.getClass() + ":" + currentChr);
                }
            } else {
                if (fos != null) {
                    fos.write(nextLine.trim().getBytes());
                    line++;
                    if (line % 100000 == 0) {
                        progressUpdated("Read " + line + " lines from " + currentChr + " ", chr, 24);
                    }
                }
            }
        }
        // Add last chr
        if (fos != null) {
            fos.close();
        }
        br.close();
        processingComplete(null);
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

    /**
     * @throws IOException
     */
    public static Map<String, FASTAElement> parseFasta(String path, String name) throws IOException {

        // Read a multi FASTA file element by element.
        Map<String, FASTAElement> sequenceMap = new HashMap<String, FASTAElement>();
        File file = new File(path);

        FASTAFileReader reader = new FASTAFileReaderImpl(file);

        FASTAElementIterator it = reader.getIterator();

        while (it.hasNext()) {
            FASTAElement el = it.next();
            if (ChromosomeUtils.isStandardChromosomeName(el.getHeader())) {
                sequenceMap.put(el.getHeader(), el);
            }
        }
        return sequenceMap;
    }

}
