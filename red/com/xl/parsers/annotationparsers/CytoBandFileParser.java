package com.xl.parsers.annotationparsers;

import com.xl.datatypes.annotation.Cytoband;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Class description
 *
 * @author Enter your name here...
 * @version Enter version here..., 08/10/16
 */
public class CytoBandFileParser {


    /**
     * Method description
     *
     * @param reader           The buffer reader
     * @param genomeDescriptor The genome descriptor
     * @return A linked hash map of cytoband
     */
    public static LinkedHashMap<String, List<Cytoband>> loadData(BufferedReader reader,
                                                                 GenomeDescriptor genomeDescriptor) {
        LinkedHashMap<String, List<Cytoband>> dataMap = new LinkedHashMap<String, List<Cytoband>>();
        try {
            String cytobandDirectory = LocationPreferences.getInstance().getOthersDirectory() + File.separator +
                    genomeDescriptor.getDisplayName();
            File cytobandFile = new File(cytobandDirectory + File.separator + genomeDescriptor.getCytoBandFileName());
            boolean cytobandHasCached = false;
            FileWriter fw = null;
            BufferedWriter bw = null;
            if (cytobandFile.exists()) {
                cytobandHasCached = true;
            } else {
                FileUtils.createDirectory(cytobandDirectory);
                fw = new FileWriter(cytobandFile);
                bw = new BufferedWriter(fw);
            }
            String nextLine;
            while ((nextLine = reader.readLine()) != null && (nextLine.trim().length() > 0)) {
                if (!cytobandHasCached) {
                    bw.write(nextLine + "\r\n");
                }
                String[] data = nextLine.split("\t");
                String chr = data[0].trim();
                List<Cytoband> cytobands = dataMap.get(chr);
                if (cytobands == null) {
                    cytobands = new ArrayList<Cytoband>();
                    dataMap.put(chr, cytobands);
                }
                Cytoband cytoData = new Cytoband(chr);
                parseData(data, cytoData);
                cytobands.add(cytoData);
            }
            if (!cytobandHasCached) {
                bw.flush();
                bw.close();
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataMap;

    }

    private static void parseData(String[] tokens, Cytoband cytoData) {

        cytoData.setStart(Integer.parseInt(tokens[1].trim()));
        cytoData.setEnd(Integer.parseInt(tokens[2].trim()));
        if (tokens.length > 3) {
            cytoData.setName(tokens[3]);
        }
        if (tokens.length > 4) {
            if (tokens[4].equals("acen")) {
                cytoData.setType('c');
            } else {
                cytoData.setType(tokens[4].charAt(1));
                if (cytoData.getType() == 'p') {
                    cytoData.setStain(Short.parseShort(tokens[4].substring(4).trim()));
                }
            }
        }

    }
}
