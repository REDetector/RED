package com.xl.datatypes;

import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteSet;
import com.xl.parsers.dataparsers.DataParser;
import com.xl.utils.Strand;

import java.util.List;

/**
 * The Class DataStore is a generic representation of a set
 * of data and its associated quantitation values.  The two
 * common representations are DataSet for real data and
 * DataGroup for virtual datasets.
 */
public abstract class DataStore implements Comparable<DataStore> {

    protected DataParser dataParser = null;
    /**
     * The name.
     */
    private String name;
    /**
     * The collection.
     */
    private DataCollection collection = null;
    private boolean isStandardChromosomeName = true;

    /**
     * Instantiates a new data store.
     *
     * @param name the name
     */
    public DataStore(String name) {
        this.name = name;
    }

    /**
     * Sets the collection.
     *
     * @param collection the new collection
     */
    public void setCollection(DataCollection collection) {
        this.collection = collection;
    }

    /**
     * Collection.
     *
     * @return the data collection
     */
    public DataCollection collection() {
        return collection;
    }

    public boolean isStandardChromosomeName() {
        return isStandardChromosomeName;
    }

    public void setStandardChromosomeName(boolean isStandardChromosomeName) {
        this.isStandardChromosomeName = isStandardChromosomeName;
    }

    public DataParser getDataParser() {
        return dataParser;
    }

    public void setDataParser(DataParser dataParser) {
        this.dataParser = dataParser;
    }

    /**
     * Gets the reads for site.
     *
     * @param p the p
     * @return the reads for site
     */
    public abstract List<? extends Location> getReadsForSite(Site p);

    /**
     * Gets the reads for chromosome.
     *
     * @param chr the chromosome
     * @return the reads for chromsome
     */
    public abstract List<? extends Location> getReadsForChromosome(String chr);

    /**
     * Gets the read count for chromosome.
     *
     * @param chr the c
     * @return the read count for chromosome
     */
    public abstract int getReadCountForChromosome(String chr);

    /**
     * Gets the total read count.
     *
     * @return the total read count
     */
    public abstract int getTotalReadCount();

    /**
     * Gets the total read count for a particular strand.
     *
     * @param strand the strand requested (FORWARD,REVERSE or UNKNOWN)
     * @return the total read count for the specified strand
     */
    public abstract int getReadCountForStrand(Strand strand);

    /**
     * Gets the total read length.
     *
     * @return the total read length
     */
    public abstract long getTotalReadLength();

    /**
     * Name.
     *
     * @return the string
     */
    public String name() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Site set replaced.
     *
     * @param sites the sites
     */
    public void siteSetReplaced(SiteSet sites) {

    }

    public String toString() {
        return name();
    }

    public int compareTo(DataStore s) {
        // Sort alphabetically
        return name().toLowerCase().compareTo(s.name().toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataStore)) return false;

        DataStore dataStore = (DataStore) o;

        if (!collection.equals(dataStore.collection)) return false;
        if (!name.equals(dataStore.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + collection.hashCode();
        return result;
    }
}
