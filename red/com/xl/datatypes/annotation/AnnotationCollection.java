package com.xl.datatypes.annotation;

import com.xl.datatypes.feature.Feature;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.Genome;
import com.xl.exception.REDException;
import com.xl.interfaces.AnnotationCollectionListener;
import com.xl.net.crashreport.CrashReporter;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.namemanager.SuffixUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * The Class AnnotationCollection is the main object through which annotation
 * objects can be accessed
 */
public class AnnotationCollection {

    /**
     * The genome.
     */
    private Genome genome;

    /**
     * The annotation sets.
     */
    private Vector<AnnotationSet> annotationSets = new Vector<AnnotationSet>();

    private Map<Chromosome, RandomAccessFile> fastaFile = new HashMap<Chromosome, RandomAccessFile>();

    /**
     * The listeners.
     */
    private Vector<AnnotationCollectionListener> listeners = new Vector<AnnotationCollectionListener>();

    /**
     * Instantiates a new annotation collection.
     *
     * @param genome the genome
     */
    public AnnotationCollection(Genome genome) {
        this.genome = genome;
    }

    /**
     * Anotation sets.
     *
     * @return the annotation set[]
     */
    public AnnotationSet[] anotationSets() {
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
     * Adds multiple annotation sets in an efficient manner.
     *
     * @param newSets the annotation sets to add
     */
    public void addAnnotationSet(AnnotationSet newSets) {
        System.out.println(AnnotationCollection.class.getName() + ":addAnnotationSet(AnnotationSet[] newSets)\t" + newSets.name());
//        for (int s = 0; s < newSets.length; s++) {

        if (newSets.getGenome() != genome) {
            throw new IllegalArgumentException("Annotation set genome doesn't match annotation collection");
        }
        annotationSets.add(newSets);
        newSets.setCollection(this);
//        }

        Enumeration<AnnotationCollectionListener> l = listeners.elements();
        while (l.hasMoreElements()) {
            l.nextElement().annotationSetAdded(newSets);
        }
    }

    /**
     * Removes the annotation set.
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
     * Annotation features renamed.
     *
     * @param set the set
     */
    protected void annotationFeaturesRenamed(AnnotationSet set, String name) {
        Enumeration<AnnotationCollectionListener> l = listeners.elements();
        while (l.hasMoreElements()) {
            l.nextElement().annotationFeaturesRenamed(set, name);
        }
    }

    public RandomAccessFile getFastaForChr(Chromosome chromosome) {
        System.out.println(this.getClass().getName() + ":getFastaForChr():" + chromosome.getName());
        if (fastaFile.containsKey(chromosome)) {
            return fastaFile.get(chromosome);
        } else {
            RandomAccessFile raf;
            try {
                File f = new File(LocationPreferences.getInstance().getCacheDirectory() + File.separator +
                        genome.getDisplayName() + File.separator + chromosome.getName() + SuffixUtils.CACHE_FASTA);
                if (!f.exists()) {
                    return null;
                } else {
                    raf = new RandomAccessFile(f, "r");
                    fastaFile.put(chromosome, raf);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                new CrashReporter(new REDException("The fasta file has not been loaded or this file could not be " +
                        "cache correctly..."));
                return null;
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

    public Feature[] getFeaturesForName(String name) {
        Vector<Feature> features = new Vector<Feature>();
        Enumeration<AnnotationSet> sets = annotationSets.elements();
        while (sets.hasMoreElements()) {
            Feature f = sets.nextElement().getFeaturesForName(name);
            if (f != null) {
                features.add(f);
            }
        }
        Feature[] allFeatures = features.toArray(new Feature[0]);
        Arrays.sort(allFeatures);
        return allFeatures;
    }

    public Feature[] getFeatureForLocation(int location) {
        Vector<Feature> features = new Vector<Feature>();
        Enumeration<AnnotationSet> sets = annotationSets.elements();
        while (sets.hasMoreElements()) {
            Feature f = sets.nextElement().getFeatrueForLocation(location);
            if (f != null) {
                features.add(f);
            }
        }
        Feature[] allFeatures = features.toArray(new Feature[0]);
        Arrays.sort(allFeatures);
        return allFeatures;
    }

}
