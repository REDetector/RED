package com.xl.datatypes.annotation;

/**
 * Copyright 2010-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.dialog.CrashReporter;
import com.xl.main.REDApplication;
import com.xl.preferences.REDPreferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The Class CoreAnnotationSet is an extension of AnnotationSet reserved for use
 * by the core genome annotation files which are not stored with the user data
 * but are assumed to be available from the online repositories.
 */
public class CoreAnnotationSet extends AnnotationSet {

	/*
     * This class is used merely to signify that an annotation set comes from
	 * the core annotation for that genome and not a user supplied set.
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
     * As a mechanism to speed loading of existing genomes we can do an object
     * dump of a parsed set of features which we can reload directly rather than
     * having to re-parse them each time.  This can only work for the core
     * annotation classes, and shouldn't be attempted elsewhere.
     *
     * @param chromosome
     * @param file
     */
    public void addPreCachedFile(String chromosome, File file) {
        featureSet.addPreCacheFeatureTypeCollection(chromosome, file);
    }

    public synchronized void finalise() {

        // If this dataset has already been finalised (ie loaded entirely from
        // cache), then we don't need to do anything here
        File cacheCompleteCheckFile;
        try {
            cacheCompleteCheckFile = new File(REDPreferences.getInstance()
                    .getGenomeBase()
                    + "/"
                    + genome.getDisplayName()
                    + "/cache/cache.complete");
            if (cacheCompleteCheckFile.exists()) {
                // System.out.println("Skipping finalisation for core annotation set");
                return;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        super.finalise();

        // In addition to the feature cache files we need to write out
        // a file with the details of the chromosomes in it so we know
        // how long they are for next time.

        try {

            // If for some reason someone has constructed a genome with no
            // features in it it's possible that the cache folder won't yet have
            // been constructed, so we might need to create it here

            File cacheBase = new File(REDPreferences.getInstance()
                    .getGenomeBase()
                    + "/"
                    + genome.getDisplayName() + "/cache");
            if (!cacheBase.exists()) {
                if (!cacheBase.mkdir()) {
                    throw new IOException(
                            "Can't create cache file for core annotation set");
                }
            }

            File chrListFile = new File(REDPreferences.getInstance()
                    .getGenomeBase()
                    + "/"
                    + genome.getDisplayName() + "/cache/chr_list");
            PrintWriter pr = new PrintWriter(chrListFile);

            Chromosome[] chrs = genome.getAllChromosomes();
            for (int c = 0; c < chrs.length; c++) {
                pr.println(chrs[c].getName() + "\t" + chrs[c].getLength());
            }
            pr.close();
        } catch (IOException ioe) {
            new CrashReporter(ioe);
        }

        // Once we have successfully completed finalization we write out
        // a marker file so the parsers next time can tell that there is
        // a complete cache set they can use.

        try {
            File cacheCompleteFile = new File(REDPreferences.getInstance()
                    .getGenomeBase()
                    + "/"
                    + genome.getDisplayName() + "/cache/cache.complete");
            PrintWriter pr = new PrintWriter(cacheCompleteFile);
            pr.println(REDApplication.VERSION + "\n");
            pr.println(GenomeDescriptor.getInstance().toString());
            pr.close();
        } catch (IOException ioe) {
            new CrashReporter(ioe);
        }
    }

}
