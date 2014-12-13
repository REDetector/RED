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

import com.xl.datatypes.feature.Feature;
import com.xl.datatypes.genome.Genome;
import com.xl.net.crashreport.CrashReporter;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.FileUtils;
import com.xl.utils.namemanager.SuffixUtils;

import java.io.*;
import java.util.*;

/**
 * AnnotationSet represents a set of genome annotations deriving from a single source (i.e., file). They are combined in an AnnotationCollection to provide the
 * full set of genome annotations used by the program.
 */
public class AnnotationSet {
    /**
     * The genome.
     */
    protected Genome genome;
    /**
     * FeatureSet can be regarded as a HashMap to store features by chromosomes.
     */
    protected FeatureSet featureSet = null;
    /**
     * The annotation set name.
     */
    private String name;
    /**
     * The annotation collection currently used.
     */
    private AnnotationCollection collection = null;
    /**
     * Tell all whether the genome annotation set has been finalised.
     */
    private boolean finalised = false;
    /**
     * All features for this set.
     */
    private List<Feature> allFeatures = null;

    /**
     * Instantiates a new annotation set.
     *
     * @param genome The base genome which this annotation applies to
     * @param name   The name for the set (file name)
     */
    public AnnotationSet(Genome genome, String name) {
        this.genome = genome;
        this.name = name;
        this.featureSet = new FeatureSet();
    }

    /**
     * This method should only be called by an AnnotationCollection to which this annotation set is being added. An annotation set can only be added to one
     * collection. In addition to creating a link between the set and collection this also triggers the caching of data in this set and consequently no more
     * annotation can be added to this set once this has been called.
     *
     * @param collection The AnnotationCollection to which this set has been added
     */
    protected void setCollection(AnnotationCollection collection) {
        if (this.collection != null) {
            throw new IllegalArgumentException("This annotation set is already part of a collection");
        }
        this.collection = collection;

        finalise();
    }

    /**
     * Provide an Set to go through all chromosomes which have data in this set. This is because we don't want to have to return all features in this set at the
     * same time due to memory constraints. Therefore using Set is the recommended way to get hold of all features in the set.
     *
     * @return An enumeration of all chromosome names which have data in this set.
     */
    public Set<String> getChromosomeNames() {
        return featureSet.getChromosomeNames();
    }

    /**
     * Provide a simple way to get features for a given chromosome and return a list which contains features.
     *
     * @param chr The chromosome name
     * @return A list which contains features for this chromosome.
     */
    public List<Feature> getFeaturesForChr(String chr) {
        return featureSet.getFeatureCollection(chr).getFeatures();
    }

    /**
     * Get the feature by a given name, which can be its id or alias name.
     *
     * @param name The name or alias name of feature.
     * @return The feature.
     */
    public List<Feature> getFeaturesForName(String name) {
        List<Feature> features = new ArrayList<Feature>();
        List<Feature> allFeatures = getAllFeatures();
        for (Feature feature : allFeatures) {
            if (feature.getAliasName().toLowerCase().contains(name) || feature.getName().toLowerCase().contains(name)) {
                features.add(feature);
            }
        }
        return features;
    }

    /**
     * Get the feature by a given position.
     *
     * @param position The position.
     * @return The feature.
     */
    public List<Feature> getFeatureForLocation(int position) {
        List<Feature> features = new ArrayList<Feature>();
        List<Feature> allFeatures = getAllFeatures();
        for (Feature feature : allFeatures) {
            if (feature.isInFeature(position)) {
                features.add(feature);
            }
        }
        return features;
    }

    /**
     * Get all features from this annotation set.
     *
     * @return A list which contains all features.
     */
    public List<Feature> getAllFeatures() {
        if (allFeatures == null) {
            allFeatures = new ArrayList<Feature>();
            Set<String> chrNames = featureSet.getChromosomeNames();
            for (String chrName : chrNames) {
                allFeatures.addAll(featureSet.getFeatureCollection(chrName).getFeatures());
            }
        }
        return allFeatures;
    }

    /**
     * This is called when we're added to a collection and lets us optimise storage and cache off unused data. It should only be called once and we prevent the
     * adding of more features once it's been called.
     */
    public synchronized void finalise() {
        if (finalised)
            return;
        finalised = true;
        featureSet.finalise();
    }

    /**
     * This is used to clean up the set when it is being removed from its containing collection. It will sever the links between this set and the annotation
     * collection as well as blanking its internal data structures. This will also trigger the notification of any listeners that this set has been removed.
     */
    public void delete() {
        if (collection != null) {
            collection.removeAnnotationSet(this);
            collection = null;
        }
        if (featureSet != null) {
            featureSet = null;
        }
    }

    /**
     * The name of this annotation set
     *
     * @return The annotation set name
     */
    public String name() {
        return name;
    }

    /**
     * Sets the annotation set name.
     *
     * @param name The new name for this set.
     */
    public void setName(String name) {
        this.name = name;
        // Inform the collection so we can tell any listeners
        if (collection != null) {
            collection.annotationSetRenamed(this);
        }
    }

    public String toString() {
        return name();
    }

    /**
     * Genome
     *
     * @return The genome which underlies this annotation set
     */
    public Genome getGenome() {
        return genome;
    }

    /**
     * Adds a feature. Note that this operation can fail if the data in this set has been cached to disk. This happens when the set is first queried or when it
     * is added to an annotation collection. In these cases an IllegalArgumentException will be thrown.
     *
     * @param f The feature to add.
     */
    public void addFeature(Feature f) {
        featureSet.addFeature(f);
    }

    /**
     * FeatureSet represents the different sets of chromosomes.
     */
    protected class FeatureSet {

        private Map<String, FeatureCollection> chrFeatures = new HashMap<String, FeatureCollection>();

        /**
         * Adds a feature.
         *
         * @param f The feature to add
         */
        public void addFeature(Feature f) {
            String chr = f.getChr();
            if (chrFeatures.containsKey(chr)) {
                chrFeatures.get(chr).addFeature(f);
            } else {
                FeatureCollection t = new FeatureCollection(chr);
                t.addFeature(f);
                chrFeatures.put(chr, t);
            }
        }

        /**
         * Chromosome names.
         *
         * @return An set of all chromosome names for which we hold data
         */
        public Set<String> getChromosomeNames() {
            return chrFeatures.keySet();
        }

        /**
         * Get the feature collection by a given chromosome.
         *
         * @param chr The chromosome name
         * @return The FeatureCollection which contains all features for this chromosome.
         */
        protected FeatureCollection getFeatureCollection(String chr) {
            if (!chrFeatures.containsKey(chr)) {
                chrFeatures.put(chr, new FeatureCollection(chr));
            }
            return chrFeatures.get(chr);
        }

        /**
         * Retrieve features from the cache file.
         *
         * @param chr  The chromosome name
         * @param file The cache file
         */
        protected void addPreCacheFeatureTypeCollection(String chr, File file) {
            chrFeatures.put(chr, new FeatureCollection(chr, file));
        }

        /**
         * Optimises and caches all held data.
         */
        public void finalise() {
            // We're not optimising anything at this level, but we do need to tell the FeatureCollection about this.
            for (Map.Entry<String, FeatureCollection> entry : chrFeatures.entrySet()) {
                entry.getValue().finalise();
            }
        }

    }

    /**
     * FeatureCollection represents a list of features on the same chromosome. It also provides the caching mechanism for feature data.
     */
    protected class FeatureCollection implements Runnable {

        /**
         * A list that contains features for a chromosome.
         */
        private List<Feature> buildFeatures = null;
        /**
         * The cache file for the list feature.
         */
        private File cacheFile = null;
        /**
         * Current chromosome.
         */
        private String chr = null;

        public FeatureCollection(String chr) {
            this.chr = chr;
            buildFeatures = new ArrayList<Feature>();
        }

        /**
         * This constructor is a shortcut where there is a pre-cached file which we can reuse. Since this is somewhat fragile it should be used with caution.
         *
         * @param chr       The chromosome name
         * @param cacheFile The cache file which has cached the features for this chromosome.
         */
        public FeatureCollection(String chr, File cacheFile) {
            this.chr = chr;
            this.cacheFile = cacheFile;
            buildFeatures = null;
        }

        /**
         * Add a feature.
         *
         * @param f The feature to add
         */
        public void addFeature(Feature f) {
            if (buildFeatures == null) {
                throw new IllegalArgumentException("Can't add data to a finalised feature collection");
            }
            buildFeatures.add(f);
        }

        /**
         * Optimises and caches feature data.
         */
        public void finalise() {
            if (buildFeatures == null || cacheFile != null) {
                System.err.println("This already appears to be finalised");
                return;
            }

            // If this is a core annotation set then we want to maintain a set of cache files for future use so we write them where we can get back to them
            // in future. We also don't optimise for size since we need to keep them all.
            if (AnnotationSet.this instanceof CoreAnnotationSet) {
                try {
                    String cacheBase = LocationPreferences.getInstance().getCacheDirectory() + File.separator + genome.getDisplayName();
                    FileUtils.createDirectory(cacheBase);
                    cacheFile = new File(cacheBase + File.separator + chr + SuffixUtils.CACHE_GENOME);
                    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cacheFile)));
                    oos.writeObject(buildFeatures);
                    oos.close();
                    buildFeatures = null;

                } catch (IOException ioe) {
                    new CrashReporter(ioe);
                }
            }

        }

        /**
         * Clean up any temp files.
         */
        public void run() {
            if (cacheFile != null) {
                if (!cacheFile.delete()) {
                    System.err.println("Cache file " + cacheFile.getName() + " can not be deleted.");
                }
            }
        }

        /**
         * Gets a list of features.
         *
         * @return The list of stored features.
         */
        public List<Feature> getFeatures() {
            if (buildFeatures != null) {
                finalise();
            }
            List<Feature> returnedFeatures = null;
            if (cacheFile != null) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cacheFile)));
                    returnedFeatures = (List<Feature>) ois.readObject();
                    ois.close();
                    return returnedFeatures;
                } catch (Exception e) {
                    new CrashReporter(e);
                }
            }
            return returnedFeatures;
        }
    }
}
