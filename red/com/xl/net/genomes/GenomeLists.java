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
