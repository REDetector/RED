package net.xl.genomes;

import java.util.Vector;

public class GenomeLists {

    private static Vector<GenomeLists> genomeLists = new Vector<GenomeLists>();
    private String id = null;
    private String downloadLocation = null;
    private String displayName = null;

    public GenomeLists(String displayName, String downloadLocation,
                       String id) {
        this.displayName = displayName;
        this.downloadLocation = downloadLocation;
        this.id = id;
        genomeLists.add(this);
    }

    public static GenomeLists[] getGenomeList() {
        return genomeLists.toArray(new GenomeLists[0]);
    }

    public String getId() {
        return id;
    }

    public String getGenomeDownloadLocation() {
        return downloadLocation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String toString() {
        return displayName;
    }

}
