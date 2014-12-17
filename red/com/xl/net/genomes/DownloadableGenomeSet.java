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

package com.xl.net.genomes;

import com.xl.preferences.LocationPreferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

/**
 * The Class DownloadableGenomeSet downloads the gene lists from IGV server.
 */
public class DownloadableGenomeSet {
    /**
     * The genome lists.
     */
    private static Vector<GenomeList> genomeLists = new Vector<GenomeList>();
    /**
     * The genome count. A simple flag to prevent downloading the gene lists every time opening the import genome dialog.
     */
    private static int genomeCount = 0;

    public DownloadableGenomeSet() throws IOException {
        // If the size of genome lists is equal to the genome count, then we used the genome lists instead of downloading the same thing again.
        if (genomeLists.size() == genomeCount && genomeCount != 0) {
            return;
        }
        genomeCount = 0;
        genomeLists.clear();

        URL genomeIndexURL = new URL(LocationPreferences.getInstance().getGenomeDownloadLists() + "genomes.txt");
        BufferedReader genomeIndexReader = new BufferedReader(new InputStreamReader(genomeIndexURL.openStream()));

        String indexLine;
        while ((indexLine = genomeIndexReader.readLine()) != null) {
            String[] sections = indexLine.split("\\t");
            if (sections[0].startsWith("<")) {
                continue;
            } else if (sections.length < 3) {
                throw new IOException("Genome list file is corrupt. Expected 3 sections on line '" + indexLine + "' but got " + sections.length);
            }
            genomeCount++;
            //A. baumannii str. ATCC	http://igv.broadinstitute.org/genomes/ABaumannii_ATCC_17978.genome	ABaumannii_ATCC_17978
            genomeLists.add(new GenomeList(sections[0], sections[1], sections[2]));
        }
    }

    public static Vector<GenomeList> getGenomeLists() {
        return genomeLists;
    }

    public String toString() {
        return "Downloadable Genomes";
    }

}
