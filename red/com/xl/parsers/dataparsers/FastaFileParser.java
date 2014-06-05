package com.xl.parsers.dataparsers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import com.xl.datatypes.DataCollection;
import com.xl.utils.ParsingUtils;

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
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(10000);
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
}
