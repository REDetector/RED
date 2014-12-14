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
import com.xl.interfaces.ProgressListener;
import com.xl.main.Global;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.ChromosomeUtils;
import com.xl.utils.FileUtils;
import com.xl.utils.ParsingUtils;
import com.xl.utils.namemanager.SuffixUtils;

import javax.swing.*;
import java.io.*;
import java.util.Iterator;
import java.util.List;

public class FastaFileParser extends DataParser {
    private Genome genome;
    private String fastaCacheDirectory;


    public FastaFileParser(DataCollection collection) {
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
            fastaCacheDirectory = LocationPreferences.getInstance().getCacheDirectory() + File.separator + genome
                    .getDisplayName();
            if (FileUtils.createDirectory(fastaCacheDirectory)) {
                File cacheCompleteFile = new File(fastaCacheDirectory + File.separator + SuffixUtils.CACHE_FASTA_COMPLETE);
                if (cacheCompleteFile.exists()) {
                    BufferedReader br = new BufferedReader(new FileReader(cacheCompleteFile));
                    String version = br.readLine();
                    if (version.equals(Global.VERSION)) {
                        processingComplete(null);
                    } else {
                        br.close();
                        cacheCompleteFile.delete();
                        FileUtils.deleteAllFilesWithSuffix(fastaCacheDirectory, SuffixUtils.CACHE_FASTA);
                        reparseFasta();
                    }
                } else {
                    reparseFasta();
                }
            } else {
                System.err.println("Fasta cache file path " + fastaCacheDirectory + " can not be accessed.");
                progressCancelled();
            }
            GenomeDescriptor.getInstance().setFasta(true);
            GenomeDescriptor.getInstance().setFastaDirectory(true);
            GenomeDescriptor.getInstance().setSequenceLocation(fastaCacheDirectory);
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

    @Override
    public List<? extends Location> query(String chr, int start, int end) {
        throw new UnsupportedOperationException();
    }

    /**
     * Read an entire fasta file, which might be local or remote and might be gzipped.
     */
    public void reparseFasta() throws IOException {
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
                if (fos != null) {
                    fos.flush();
                    fos.close();
                    System.gc();
                }
                currentChr = skipToNextChr(br, nextLine);
                if (currentChr != null) {
                    chr++;
                    line = 0;
                    String currentChrPath = fastaCacheDirectory + File.separator + currentChr + SuffixUtils.CACHE_FASTA;
                    fos = new FileOutputStream(currentChrPath, true);
                }
            } else {
                if (fos != null) {
                    fos.write(nextLine.trim().getBytes());
                    if (++line % 100000 == 0) {
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
        FileWriter fw = new FileWriter(fastaCacheDirectory + File.separator + SuffixUtils.CACHE_FASTA_COMPLETE);
        fw.write(Global.VERSION);
        fw.close();
        processingComplete(null);
    }

    @Override
    protected void processingComplete(DataSet[] dataSets) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressComplete("fasta_loaded", null);
        }
    }
}
