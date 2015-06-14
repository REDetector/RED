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

package com.xl.parsers.dataparsers;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.datatypes.sequence.Location;
import com.xl.display.dialog.CrashReporter;
import com.xl.interfaces.ProgressListener;
import com.xl.main.Global;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.FileUtils;
import com.xl.utils.NameRetriever;
import com.xl.utils.ParsingUtils;
import com.xl.utils.namemanager.SuffixUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * The Class FastaFileParser is a parser to parse FASTA format file. We have a cached mechanism to divide the whole fasta file by chromosomes in the fasta file
 * because the  whole file is too large to be loaded with small memory.
 */
public class FastaFileParser extends DataParser {
    private final Logger logger = LoggerFactory.getLogger(FastaFileParser.class);
    /**
     * The genome.
     */
    private Genome genome;

    /**
     * Initiate a new fasta file parser.
     *
     * @param collection the data collection.
     */
    public FastaFileParser(DataCollection collection) {
        genome = collection.genome();
    }

    public FastaFileParser(Genome genome) {
        this.genome = genome;
    }

    /**
     * Skip to the next standard chromosome and deprecate the others.
     *
     * @param br       the buffered reader.
     * @param nextLine next line.
     * @return the next chromosome.
     * @throws IOException If the reader can't read the next line then throw this exception.
     */
    private String skipToNextChr(BufferedReader br, String nextLine) throws IOException {
        nextLine = nextLine.substring(1).split("\\s+")[0];
        if (NameRetriever.isStandardChromosomeName(nextLine)) {
            return nextLine;
        } else {
            while ((nextLine = br.readLine()) != null)
                if (nextLine.startsWith(">")) {
                    nextLine = nextLine.substring(1).split("\\s+")[0];
                    if (NameRetriever.isStandardChromosomeName(nextLine)) {
                        return nextLine;
                    }
                }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            String fastaCacheDirectory = LocationPreferences.getInstance().getCacheDirectory() + File.separator + genome.getDisplayName();
            if (FileUtils.createDirectory(fastaCacheDirectory)) {
                File cacheCompleteFile = new File(fastaCacheDirectory + File.separator + SuffixUtils.CACHE_FASTA_COMPLETE);
                if (cacheCompleteFile.exists()) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(cacheCompleteFile)));
                    String version = br.readLine();
                    if (version.equals(Global.VERSION)) {
                        processingComplete(null);
                    } else {
                        br.close();
                        if (!cacheCompleteFile.delete()) {
                            throw new IOException();
                        }
                        FileUtils.deleteAllFilesWithSuffix(fastaCacheDirectory, SuffixUtils.CACHE_FASTA);
                        parseNewFasta(fastaCacheDirectory);
                    }
                } else {
                    parseNewFasta(fastaCacheDirectory);
                }
            } else {
                throw new IOException("Fasta cache file path " + fastaCacheDirectory + " can not be accessed, please have a check for permission.");
            }
            GenomeDescriptor.getInstance().setFasta(true);
            GenomeDescriptor.getInstance().setFastaDirectory(true);
            GenomeDescriptor.getInstance().setSequenceLocation(fastaCacheDirectory);
        } catch (IOException e) {
            new CrashReporter(e);
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
        return "Import the fasta data which includes the original reference sequences";
    }

    @Override
    public List<? extends Location> query(String chr, int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void processingComplete(DataSet[] dataSets) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressComplete("fasta_loaded", null);
        }
    }

    /**
     * Read an entire fasta file, which might be local or remote and might be gzipped.
     */
    public void parseNewFasta(String fastaCacheDirectory) throws IOException {
        BufferedReader br;
        FileOutputStream fos = null;
        String currentChr = null;
        File fastaFile = getFile();
        String nextLine;
        br = ParsingUtils.openBufferedReader(fastaFile);
        int line = 0;
        int chr = 0;
        while ((nextLine = br.readLine()) != null) {
            if (nextLine.startsWith(">")) {
                // A new chromosome starts with '>', so we flush the previous chromosome and find the next.
                if (fos != null) {
                    fos.flush();
                    fos.close();
                    System.gc();
                }
                // Find the next standard chromosome.
                currentChr = skipToNextChr(br, nextLine);
                // Open and cache it.
                if (currentChr != null) {
                    chr++;
                    line = 0;
                    String currentChrPath = fastaCacheDirectory + File.separator + currentChr + SuffixUtils.CACHE_FASTA;
                    fos = new FileOutputStream(currentChrPath, true);
                }
            } else {
                // Copy the data.
                if (fos != null) {
                    fos.write(nextLine.trim().getBytes());
                    if (++line % 100000 == 0) {
                        progressUpdated("Read " + line + " lines from " + currentChr + ", " + genome.getDisplayName(), chr, 24);
                    }
                }
            }
        }
        // Flush the last chromosome.
        if (fos != null) {
            fos.flush();
            fos.close();
            System.gc();
        }
        br.close();
        // Set up a complete flag.
        FileWriter fw = new FileWriter(fastaCacheDirectory + File.separator + SuffixUtils.CACHE_FASTA_COMPLETE);
        fw.write(Global.VERSION);
        fw.close();
        processingComplete(null);
    }

    /**
     * Read an entire fasta file, which might be local or remote and might be gzipped.
     */
    public void parseNewFasta(String fastaFile, String fastaCacheDirectory) throws IOException {
        logger.info("Fasta file path: " + fastaFile);
        logger.info("Fasta cache directory: " + fastaCacheDirectory);
        BufferedReader br;
        FileOutputStream fos = null;
        String currentChr = null;
        String nextLine;
        br = ParsingUtils.openBufferedReader(fastaFile);
        int line = 0;
        int chr = 0;
        while ((nextLine = br.readLine()) != null) {
            if (nextLine.startsWith(">")) {
                // A new chromosome starts with '>', so we flush the previous chromosome and find the next.
                if (fos != null) {
                    fos.flush();
                    fos.close();
                    System.gc();
                }
                // Find the next standard chromosome.
                currentChr = skipToNextChr(br, nextLine);
                // Open and cache it.
                if (currentChr != null) {
                    chr++;
                    line = 0;
                    String currentChrPath = fastaCacheDirectory + File.separator + currentChr + SuffixUtils.CACHE_FASTA;
                    fos = new FileOutputStream(currentChrPath, true);
                }
            } else {
                // Copy the data.
                if (fos != null) {
                    fos.write(nextLine.trim().getBytes());
                    if (++line % 100000 == 0) {
                        progressUpdated("Read " + line + " lines from " + currentChr + ", " + genome.getDisplayName(), chr, 24);
                    }
                }
            }
        }
        // Flush the last chromosome.
        if (fos != null) {
            fos.flush();
            fos.close();
            System.gc();
        }
        br.close();
        // Set up a complete flag.
        FileWriter fw = new FileWriter(fastaCacheDirectory + File.separator + SuffixUtils.CACHE_FASTA_COMPLETE);
        fw.write(Global.VERSION);
        fw.close();
        processingComplete(null);
    }

    @Override
    public FileFilter getFileFilter() {
        return new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".fasta")|| f.getName().toLowerCase().endsWith(".fa");
            }

            public String getDescription() {
                return "Fasta Files";
            }
        };
    }
}
