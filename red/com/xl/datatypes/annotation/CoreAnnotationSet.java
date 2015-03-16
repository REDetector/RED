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

package com.xl.datatypes.annotation;

import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.display.dialog.CrashReporter;
import com.xl.main.Global;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.FileUtils;
import com.xl.utils.namemanager.GenomeUtils;
import com.xl.utils.namemanager.SuffixUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The Class CoreAnnotationSet is an extension of AnnotationSet reserved for use by the core genome annotation files which are not stored with the user data but
 * are assumed to be available from the online repositories.
 */
public class CoreAnnotationSet extends AnnotationSet {
    /*
     * This class is used merely to signify that an annotation set comes from the core annotation for that genome and not a user supplied set.
	 */

    /**
     * Instantiates a new core annotation set.
     *
     * @param genome the genome
     */
    public CoreAnnotationSet(Genome genome) {
        super(genome, GenomeDescriptor.getInstance().getGeneTrackName());
    }

    /**
     * As a mechanism to speed loading of existing genomes we can do an object dump of a parsed set of features which we can reload directly rather than having
     * to re-parse them each time.  This can only work for the core annotation classes, and shouldn't be attempted elsewhere.
     *
     * @param chromosome The chromosome.
     * @param file       The temp file for this chromosome.
     */
    public void addPreCachedFile(String chromosome, File file) {
        featureSet.addPreCacheFeatureTypeCollection(chromosome, file);
    }

    public synchronized void finalise() {
        String currentCacheDirectory = LocationPreferences.getInstance().getCacheDirectory()
                + File.separator + genome.getDisplayName();
        FileUtils.createDirectory(currentCacheDirectory);
        // If this annotation set has already been finalised (ie loaded entirely from cache), then we don't need to do anything here
        File cacheCompleteCheckFile;
        cacheCompleteCheckFile = new File(currentCacheDirectory + File.separator + SuffixUtils.CACHE_GENOME_COMPLETE);
        if (cacheCompleteCheckFile.exists()) {
            return;
        }

        super.finalise();

        // In addition to the feature cache files we need to write out a file with the details of the chromosomes in it so we know how long they are for next
        // time.

        try {
            // If for some reason someone has constructed a genome with no features in it it's possible that the cache folder won't yet have been
            // constructed, so we might need to create it here

            File chrListFile = new File(currentCacheDirectory + File.separator + "chr_list.txt");
            PrintWriter pr = new PrintWriter(chrListFile);

            Chromosome[] chrs = genome.getAllChromosomes();
            for (Chromosome chr : chrs) {
                pr.println(chr.getName() + "\t" + chr.getLength());

            }
            pr.close();
        } catch (IOException ioe) {
            new CrashReporter(ioe);
        }

        // Once we have successfully completed finalization we write out a marker file so the parsers next time can tell that there is a complete cache set
        // they can use.

        try {
            File cacheCompleteFile = new File(currentCacheDirectory + File.separator + SuffixUtils.CACHE_GENOME_COMPLETE);
            PrintWriter pr = new PrintWriter(cacheCompleteFile);
            pr.println(GenomeUtils.KEY_VERSION_NAME + "=" + Global.VERSION + "\n");
            pr.println(GenomeDescriptor.getInstance().toString());
            pr.close();
        } catch (IOException ioe) {
            new CrashReporter(ioe);
        }
    }

}
