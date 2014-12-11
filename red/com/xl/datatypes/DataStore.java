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

package com.xl.datatypes;

import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteSet;
import com.xl.parsers.dataparsers.DataParser;
import com.xl.utils.Strand;

import java.util.List;

/**
 * The Class DataStore is a generic representation of a set of data and its associated site lists. The two common representations are DataSet for real data and
 * DataGroup for virtual data sets.
 */
public abstract class DataStore implements Comparable<DataStore> {
    /**
     * The collection.
     */
    protected DataCollection collection = null;
    /**
     *
     */
    protected DataParser dataParser = null;
    /**
     * The name.
     */
    private String name;
    private boolean isStandardChromosomeName = true;

    /**
     * The site set.
     */
    private SiteSet siteSet = null;

    /**
     * Instantiates a new data store.
     *
     * @param name the name
     */
    public DataStore(String name) {
        this.name = name;
    }

    /**
     * Sets the site set.
     *
     * @param newSiteSet the new site set
     */
    public void setSiteSet(SiteSet newSiteSet) {
        if (siteSet != null) {
            siteSet.delete();
        }
        siteSet = newSiteSet;
        siteSet.setDataStore(this);
    }

    /**
     * Site set.
     *
     * @return the site set
     */
    public SiteSet siteSet() {
        return siteSet;
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
     * @return the reads for chromosome
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
