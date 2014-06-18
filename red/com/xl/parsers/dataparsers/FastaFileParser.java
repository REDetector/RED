package com.xl.parsers.dataparsers;

import com.xl.datatypes.DataCollection;
import com.xl.utils.ParsingUtils;
import net.sf.jfasta.FASTAElement;
import net.sf.jfasta.FASTAFileReader;
import net.sf.jfasta.impl.FASTAElementIterator;
import net.sf.jfasta.impl.FASTAFileReaderImpl;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class FastaFileParser extends DataParser {

    public FastaFileParser(DataCollection collection) {
        super(collection);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run() {

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
     * @param path
     */
    public static Map<String, byte[]> parseFasta(String path) throws IOException {

        Map<String, byte[]> sequenceMap = new HashMap<String, byte[]>();
        BufferedReader br = null;

        try {
            br = ParsingUtils.openBufferedReader(path);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            String currentChr = null;
            String nextLine;
            while ((nextLine = br.readLine()) != null) {
                if (nextLine.startsWith("#") || nextLine.trim().length() == 0) {
                    continue;
                } else if (nextLine.startsWith(">")) {
                    if (currentChr != null) {
                        byte[] seq = buffer.toByteArray();
                        sequenceMap.put(currentChr, seq);
                        buffer.reset();   // Resets the count field of this byte array output stream to zero
                    }
                    currentChr = nextLine.substring(1).split("\\s+")[0];
                } else {
                    System.out.println(nextLine);
                    buffer.write(nextLine.trim().getBytes());
                }
            }
            // Add last chr
            if (currentChr != null) {
                byte[] seq = buffer.toByteArray();
                sequenceMap.put(currentChr, seq);
            }
        } finally {
            if (br != null) br.close();
        }

        return sequenceMap;
    }

    /**
     * @throws IOException
     */
    public static void parseFasta(String path, String name) throws IOException {

        // Read a multi FASTA file element by element.

        File file = new File(path);

        FASTAFileReader reader = new FASTAFileReaderImpl(file);

        FASTAElementIterator it = reader.getIterator();
        int i = 0;
        while (it.hasNext()) {
            FASTAElement el = it.next();
            if (i++ < 1000) {
                System.out.println(el.getSequence());
            } else {
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Map<String, byte[]> map = parseFasta("E:\\Master\\ChongQing\\Data\\hg19.fa.align");
        Set<String> sets = map.keySet();
        Collection<byte[]> coll = map.values();
        int i = 0;
        Iterator<byte[]> iter = coll.iterator();
        while (iter.hasNext()) {
            if (i++ < 1000) {
                System.out.println(new String(iter.next()));
            } else {
                break;
            }
        }
    }
}
