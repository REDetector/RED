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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.Genome;
import com.xl.display.featureviewer.Feature;
import com.xl.interfaces.AnnotationCollectionListener;
import com.xl.utils.MessageUtils;

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
    public void addAnnotationSets(AnnotationSet[] newSets) {
        MessageUtils.showInfo(AnnotationCollection.class, "addAnnotationSets(AnnotationSet[] newSets)\t" + newSets.length);
        for (int s = 0; s < newSets.length; s++) {

            if (newSets[s].genome() != genome) {
                throw new IllegalArgumentException(
                        "Annotation set genome doesn't match annotation collection");
            }
            annotationSets.add(newSets[s]);
            newSets[s].setCollection(this);
        }

        Enumeration<AnnotationCollectionListener> l = listeners.elements();
        while (l.hasMoreElements()) {
            l.nextElement().annotationSetsAdded(newSets);
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

    /**
     * Gets the features for type.
     *
     * @param c the c
     * @return the features for type
     */
    public Feature[] getFeaturesForChr(Chromosome c) {
        Vector<Feature> features = new Vector<Feature>();
        Enumeration<AnnotationSet> sets = annotationSets.elements();
        while (sets.hasMoreElements()) {
            Feature[] f = sets.nextElement().getFeaturesForChr(c.getName());
            for (int i = 0; i < f.length; i++) {
                features.add(f[i]);
            }
        }

        Feature[] allFeatures = features.toArray(new Feature[0]);
        Arrays.sort(allFeatures);
        return allFeatures;
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