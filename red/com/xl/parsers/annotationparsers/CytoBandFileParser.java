package com.xl.parsers.annotationparsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.xl.datatypes.annotation.Cytoband;

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
     * @param reader
     * @return
     */
    public static LinkedHashMap<String, List<Cytoband>> loadData(BufferedReader reader) {

        LinkedHashMap<String, List<Cytoband>> dataMap = new LinkedHashMap<String, List<Cytoband>>();
        try {

            String nextLine;
            while ((nextLine = reader.readLine()) != null && (nextLine.trim().length() > 0)) {
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
