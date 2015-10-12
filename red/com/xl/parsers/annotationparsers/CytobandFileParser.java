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

package com.xl.parsers.annotationparsers;

import com.xl.datatypes.annotation.Cytoband;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Class CytobandFileParser is a file parser to parse cytoband file.
 */
public class CytobandFileParser {
    /**
     * Method description
     *
     * @param reader           The buffer reader
     * @param genomeDescriptor The genome descriptor
     * @return A linked hash map of cytoband
     */
    public static LinkedHashMap<String, List<Cytoband>> loadData(BufferedReader reader, GenomeDescriptor genomeDescriptor) {
        LinkedHashMap<String, List<Cytoband>> dataMap = new LinkedHashMap<String, List<Cytoband>>();
        try {
            String cytobandDirectory = LocationPreferences.getInstance().getOthersDirectory() + File.separator + genomeDescriptor.getDisplayName();
            File cytobandFile = new File(cytobandDirectory + File.separator + genomeDescriptor.getCytoBandFileName());
            boolean cytobandHasCached = false;
            FileWriter fw = null;
            if (cytobandFile.exists()) {
                cytobandHasCached = true;
            } else {
                FileUtils.createDirectory(cytobandDirectory);
                fw = new FileWriter(cytobandFile);
            }
            String nextLine;
            while ((nextLine = reader.readLine()) != null && (nextLine.trim().length() > 0)) {
                if (!cytobandHasCached) {
                    fw.write(nextLine + "\r\n");
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
                fw.flush();
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
                if (cytoData.getType() == 'p' && tokens[4].length() > 4) {
                    cytoData.setStain(Short.parseShort(tokens[4].substring(4).trim()));
                }
            }
        }

    }
}
