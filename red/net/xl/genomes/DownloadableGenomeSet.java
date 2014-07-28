package net.xl.genomes;

import com.xl.preferences.LocationPreferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

public class DownloadableGenomeSet {
    private static Vector<GenomeLists> genomeLists = new Vector<GenomeLists>();
    private int genomeCount = 0;

    public DownloadableGenomeSet() throws IOException {
        if (genomeLists.size() == genomeCount && genomeCount != 0) {
            return;
        }
        genomeCount = 0;
        genomeLists.clear();
        //A. baumannii str. ATCC	http://igv.broadinstitute.org/genomes/ABaumannii_ATCC_17978.genome	ABaumannii_ATCC_17978
        URL genomeIndexURL = new URL(LocationPreferences.getInstance()
                .getGenomeDownloadLists() + "genomes.txt");

        BufferedReader genomeIndexReader = new BufferedReader(
                new InputStreamReader(genomeIndexURL.openStream()));

        String indexLine;
        while ((indexLine = genomeIndexReader.readLine()) != null) {
            String[] sections = indexLine.split("\\t");
            if (sections[0].startsWith("<")) {
                continue;
            } else if (sections.length < 3) {
                throw new IOException(
                        "Genome list file is corrupt.  Expected 3 sections on line '"
                                + indexLine + "' but got " + sections.length);
            }
            genomeCount++;
            genomeLists.add(new GenomeLists(sections[0], sections[1], sections[2]));
        }
    }

    public static Vector<GenomeLists> getGenomeLists() {
        return genomeLists;
    }

    public String toString() {
        return "Downloadable Genomes";
    }

}
