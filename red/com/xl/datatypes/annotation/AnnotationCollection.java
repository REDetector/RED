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
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.Genome;
import com.xl.exception.RedException;
import com.xl.interfaces.AnnotationCollectionListener;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.namemanager.SuffixUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * The Class AnnotationCollection is the main object through which annotation objects can be accessed
 */
public class AnnotationCollection {
    private static final Logger logger = LoggerFactory.getLogger(AnnotationCollection.class);
    /**
     * The genome.
     */
    private Genome genome;

    /**
     * The annotation sets.
     */
    private Vector<AnnotationSet> annotationSets = new Vector<AnnotationSet>();

    /**
     * The fasta files relative to annotation set.
     */
    private Map<Chromosome, RandomAccessFile> fastaFile = new HashMap<Chromosome, RandomAccessFile>();

    /**
     * The annotation collection listeners.
     */
    private Vector<AnnotationCollectionListener> listeners = new Vector<AnnotationCollectionListener>();

    /**
     * Instantiates a new annotation collection.
     */
    public AnnotationCollection(Genome genome) {
        this.genome = genome;
    }

    /**
     * Annotation sets.
     *
     * @return The array of annotation set.
     */
    public AnnotationSet[] annotationSets() {
        return annotationSets.toArray(new AnnotationSet[0]);
    }

    /**
     * Adds the annotation collection listener.
     *
     * @param l the l
     */
    public void addAnnotationCollectionListener(AnnotationCollectionListener l) {
        if (l != null && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Removes the annotation collection listener.
     *
     * @param l the l
     */
    public void removeAnnotationCollectionListener(
            AnnotationCollectionListener l) {
        if (l != null && listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    /**
     * Adds an annotation set in an efficient manner.
     *
     * @param newSet The annotation set to be added
     */
    public void addAnnotationSet(AnnotationSet newSet) throws RedException {
        if (newSet.getGenome() != genome) {
            throw new RedException("Annotation set genome doesn't match annotation collection");
        }
        annotationSets.add(newSet);
        newSet.setCollection(this);

        Enumeration<AnnotationCollectionListener> l = listeners.elements();
        while (l.hasMoreElements()) {
            l.nextElement().annotationSetAdded(newSet);
        }
    }

    /**
     * Remove the annotation set.
     *
     * @param annotationSet the annotation set
     */
    protected void removeAnnotationSet(AnnotationSet annotationSet) {

        // Notify before removing to not mess up the data tree
        Enumeration<AnnotationCollectionListener> l = listeners.elements();
        while (l.hasMoreElements()) {
            l.nextElement().annotationSetRemoved(annotationSet);
        }
        annotationSets.remove(annotationSet);
    }

    /**
     * Annotation set renamed.
     *
     * @param set the set
     */
    protected void annotationSetRenamed(AnnotationSet set) {
        Enumeration<AnnotationCollectionListener> l = listeners.elements();
        while (l.hasMoreElements()) {
            l.nextElement().annotationSetRenamed(set);
        }
    }

    /**
     * Return the fasta file by a given chromosome. If the fasta file for this chromosome has been loaded into memory, then we just reload the from the HashMap,
     * else we load the cache fasta file from fasta directory.
     *
     * @param chromosome The chromosome
     * @return The fasta file
     */
    public RandomAccessFile getFastaForChr(Chromosome chromosome) throws FileNotFoundException {
        if (fastaFile.containsKey(chromosome)) {
            return fastaFile.get(chromosome);
        } else {
            RandomAccessFile raf = null;
            File f = new File(LocationPreferences.getInstance().getCacheDirectory() + File.separator +
                    genome.getDisplayName() + File.separator + chromosome.getName() + SuffixUtils.CACHE_FASTA);
            if (f.exists()) {
                raf = new RandomAccessFile(f, "r");
                fastaFile.put(chromosome, raf);
            }
            return raf;
        }
    }


    /**
     * Gets the features for type.
     *
     * @param c the c
     * @return the features for type
     */
    public List<Feature> getFeaturesForChr(Chromosome c) {
        List<Feature> features = new ArrayList<Feature>();
        Enumeration<AnnotationSet> sets = annotationSets.elements();
        while (sets.hasMoreElements()) {
            features.addAll(sets.nextElement().getFeaturesForChr(c.getName()));
        }
        Collections.sort(features);
        return features;
    }

    /**
     * Return all features relative to a given name.
     *
     * @param name The name.
     * @return Features relative to a given name.
     */
    public Feature[] getFeaturesForName(String name) {
        List<Feature> features = new ArrayList<Feature>();
        Enumeration<AnnotationSet> sets = annotationSets.elements();
        while (sets.hasMoreElements()) {
            features.addAll(sets.nextElement().getFeaturesForName(name));
        }
        Feature[] allFeatures = features.toArray(new Feature[0]);
        Arrays.sort(allFeatures);
        return allFeatures;
    }

    /**
     * Return all features relative to a given location.
     *
     * @param location The location.
     * @return Features relative to a given location.
     */
    public Feature[] getFeatureForLocation(int location) {
        List<Feature> features = new ArrayList<Feature>();
        Enumeration<AnnotationSet> sets = annotationSets.elements();
        while (sets.hasMoreElements()) {
            features.addAll(sets.nextElement().getFeatureForLocation(location));
        }
        Feature[] allFeatures = features.toArray(new Feature[0]);
        Arrays.sort(allFeatures);
        return allFeatures;
    }

}
