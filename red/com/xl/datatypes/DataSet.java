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
import com.xl.datatypes.sequence.SequenceRead;
import com.xl.datatypes.sites.Site;
import com.xl.parsers.dataparsers.BAMFileParser;
import com.xl.utils.Strand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A DataSet represents a set of reads coming from a single source (usually a file). It is able to store and retrieve reads in a very efficient manner. If the
 * user has requested that data be cached the DataSet is also responsible for saving and loading this data.
 */
public class DataSet extends DataStore {

    /**
     * We cache the forward read count to save having to reload every chromosome just to get the read count
     */
    protected int forwardReadCount = 0;
    /**
     * We cache the reverse read count to save having to reload every chromosome just to get the read count
     */
    protected int reverseReadCount = 0;
    /**
     * We cache the total read count to save having to reload every chromosome just to get the read count
     */
    protected int totalReadCount = 0;
    /**
     * The total read length.
     */
    protected long totalReadLength = 0;
    private Map<String, List<? extends Location>> readData = new HashMap<String, List<? extends Location>>();
    /**
     * The original file name - can't be changed by the user
     */
    private String fileName;

    private HashMap<Site, List<SequenceRead>> siteMap = new HashMap<Site, List<SequenceRead>>();


    /**
     * Instantiates a new data set.
     *
     * @param name     The initial value for the user changeable name
     * @param fileName The name of the data source - which can't be changed by the user
     */
    public DataSet(String name, String fileName) {
        super(name);
        this.fileName = fileName;

    }

    public void setName(String name) {
        super.setName(name);
        if (collection() != null) {
            collection().dataStoreRenamed(this);
        }
    }

    /**
     * Gets the original data source name for this DataSet - usually the name of the file from which it was parsed.
     *
     * @return the file name
     */
    public String fileName() {
        return fileName;
    }


    @Override
    public List<SequenceRead> getReadsForSite(Site p) {
        if (siteMap.containsKey(p)) {
            return siteMap.get(p);
        } else {
            if (!(dataParser instanceof BAMFileParser)) {
                return new ArrayList<SequenceRead>();
            }
            List<SequenceRead> allReads = ((BAMFileParser) dataParser).getReadsForSite(p);
            siteMap.put(p, allReads);
            return allReads;
        }
    }

    public synchronized List<? extends Location> getReadsForChromosome(String c) {

        if (readData.containsKey(c)) {
            return readData.get(c);
        } else {
            List<? extends Location> sequenceReads = ((BAMFileParser) dataParser).query(c, 0, 0);
            readData.put(c, sequenceReads);
            return sequenceReads;
        }
    }

    public int getReadCountForChromosome(String chr) {

        if (readData.containsKey(chr)) {
            return getReadsForChromosome(chr).size();
        } else {
            return 0;
        }
    }

    public int getTotalReadCount() {
        return totalReadCount;
    }

    public void setTotalReadCount(int totalReadCount) {
        this.totalReadCount = totalReadCount;
    }

    public int getReadCountForStrand(Strand strand) {

        if (strand == Strand.POSITIVE) {
            return forwardReadCount;
        } else {
            return reverseReadCount;
        }
    }

    public long getTotalReadLength() {
        return totalReadLength;
    }

    public void setTotalReadLength(long totalReadLength) {
        this.totalReadLength = totalReadLength;
    }

    public int getForwardReadCount() {
        return forwardReadCount;
    }

    public void setForwardReadCount(int forwardReadCount) {
        this.forwardReadCount = forwardReadCount;
    }

    public int getReverseReadCount() {
        return reverseReadCount;
    }

    public void setReverseReadCount(int reverseReadCount) {
        this.reverseReadCount = reverseReadCount;
    }
}
