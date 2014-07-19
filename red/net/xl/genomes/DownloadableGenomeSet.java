package net.xl.genomes;

import com.xl.preferences.LocationPreferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class DownloadableGenomeSet {

//	private Hashtable<String, GenomeId> seenIds;

    public DownloadableGenomeSet() throws IOException {

        //A. baumannii str. ATCC	http://igv.broadinstitute.org/genomes/ABaumannii_ATCC_17978.genome	ABaumannii_ATCC_17978
        URL genomeIndexURL = new URL(LocationPreferences.getInstance()
                .getGenomeDownloadLists() + "genomes.txt");

        BufferedReader genomeIndexReader = new BufferedReader(
                new InputStreamReader(genomeIndexURL.openStream()));
//		seenIds = new Hashtable<String, GenomeId>();

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
            new GenomeLists(sections[0], sections[1], sections[2]);
        }
    }

    // public GenomeDisplayName [] findUpdateableGenomes () throws IOException {
    //
    // // We need to go through the installed genomes and see if we have an
    // assembly
    // // which is newer than the one which is installed.
    //
    // Vector<GenomeDisplayName>updates = new Vector<GenomeDisplayName>();
    //
    // File [] speciesFolders =
    // REDPreferences.getInstance().getGenomeBase().listFiles();
    //
    // for (int s=0;s<speciesFolders.length;s++) {
    // if (!speciesFolders[s].isDirectory()) continue;
    //
    // File [] assemblyFolders = speciesFolders[s].listFiles();
    //
    // for (int a=0;a<assemblyFolders.length;a++) {
    // if (!assemblyFolders[a].isDirectory()) continue;
    //
    // // Now find the latest modification time on a dat file
    //
    // File [] datFiles = assemblyFolders[a].listFiles();
    //
    // long latestEpoch = 0;
    //
    // for (int d=0;d<datFiles.length;d++) {
    // if (datFiles[d].getDisplayName().toLowerCase().endsWith(".dat")) {
    // if (datFiles[d].lastModified() > latestEpoch) {
    // latestEpoch = datFiles[d].lastModified();
    // }
    // }
    // }
    //
    // Date latestDate = new Date(latestEpoch);
    //
    // // Now see if there is an assembly in the downloadable genomes
    // // which matches this one, and if it's newer than the one we
    // // have installed.
    //
    // if (seenIds.containsKey(speciesFolders[s].getDisplayName())) {
    // GenomeDisplayName [] genomes =
    // seenIds.get(speciesFolders[s].getDisplayName()).displayNames();
    //
    // for (int ga=0;ga<genomes.length;ga++) {
    // if (genomes[ga].displayName().equals(assemblyFolders[a].getDisplayName())){
    // // We have a match, but is it newer
    //
    // if (genomes[ga].date().after(latestDate)) {
    // // We have an update to record.
    // updates.add(genomes[ga]);
    // }
    // // else {
    // //
    // System.out.println("Local date for "+genomes[ga].assembly()+" is "+latestDate.toString()+" but network date is "+genomes[ga].date().toString());
    // //
    // // }
    // }
    // }
    //
    // }
    //
    // }
    // }
    //
    //
    // return updates.toArray(new GenomeDisplayName[0]);
    // }

    public String toString() {
        return "Downloadable Genomes";
    }

    // public static void main(String[] args) {
    // try {
    // DownloadableGenomeSet dgs = new DownloadableGenomeSet();
    //
    // GenomeDisplayName[] updates = dgs.findUpdateableGenomes();
    //
    // System.out.println("There are " + updates.length
    // + " genomes to update");
    //
    // for (int i = 0; i < updates.length; i++) {
    // System.out.println(updates[i].genomeId().name() + "\t"
    // + updates[i].displayName() + " from "
    // + updates[i].date());
    // }
    //
    // } catch (IOException e) {
    // e.printStackTrace();
    // }

    // }

}
