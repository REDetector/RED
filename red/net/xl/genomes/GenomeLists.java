package net.xl.genomes;

public class GenomeLists {


    private String id = null;
    private String downloadLocation = null;
    private String displayName = null;

    public GenomeLists(String displayName, String downloadLocation,
                       String id) {
        this.displayName = displayName;
        this.downloadLocation = downloadLocation;
        this.id = id;
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
