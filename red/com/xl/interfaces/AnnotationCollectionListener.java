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
package com.xl.interfaces;

import com.xl.datatypes.annotation.AnnotationSet;

/**
 * The listener interface for receiving AnnotationCollection events. The class that is interested in processing a AnnotationCollection event implements this
 * interface, and the object created with that class is registered with a component using the component's <code>addAnnotationCollectionListener<code> method.
 * When the AnnotationCollection event occurs, that object's appropriate method is invoked.
 */
public interface AnnotationCollectionListener {
    /**
     * Annotation sets added.
     *
     * @param annotationSets the newly added annotation sets
     */
    public void annotationSetAdded(AnnotationSet annotationSets);

    /**
     * Annotation set removed.
     *
     * @param annotationSet the annotation set
     */
    public void annotationSetRemoved(AnnotationSet annotationSet);

    /**
     * Annotation set renamed.
     *
     * @param annotationSet the annotation set
     */
    public void annotationSetRenamed(AnnotationSet annotationSet);
}
