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

package com.xl.display.chromosomeviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.datatypes.sites.SiteList;
import com.xl.interfaces.ActiveDataChangedListener;
import com.xl.interfaces.DisplayPreferencesListener;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.PositionFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

/**
 * The ChromosomeViewer represents all of the tracks contained in the chromosome view. It is responsible for organising and laying out these tracks and passing
 * information back from them to other parts of the application.
 * <p/>
 * In general the rest of the program shouldn't deal with anything below the chromosome viewer.
 */
public class ChromosomeViewer extends JPanel implements ActiveDataChangedListener, DisplayPreferencesListener, MouseWheelListener {

    /**
     * A list to collect all track, which can be added or removed.
     */
    java.util.List<AbstractTrack> tracks = new ArrayList<AbstractTrack>();
    /**
     * The application.
     */
    private REDApplication application;
    /**
     * Current using chromosome.
     */
    private Chromosome chromosome;
    /**
     * A panel to place all tracks, not including the title label.
     */
    private JPanel featurePanel;
    /**
     * The title label.
     */
    private JLabel titleLabel;
    /**
     * The start position when using mouse to drag in the chromosome view.
     */
    private int selectionStart = 0;
    /**
     * The end position when using mouse to drag in the chromosome view.
     */
    private int selectionEnd = 0;
    /**
     * A status to judge whether the mouse drags or not.
     */
    private boolean makingSelection = false;
    /**
     * Current view start.
     */
    private int currentStart = 0;
    /**
     * Current view end.
     */
    private int currentEnd = 1;
    /**
     * A scroll pane for sequence track.
     */
    private JScrollPane sequenceScrollPane = null;

    /**
     * Instantiates a new chromosome viewer.
     *
     * @param application the application
     * @param chromosome  the chromosome
     */
    public ChromosomeViewer(REDApplication application, Chromosome chromosome) {
        this.application = application;
        this.chromosome = chromosome;

        setLayout(new BorderLayout());
        // Although this is a TextField we alter it to look like a normal label just one you can copy from.
        titleLabel = new JLabel(application.dataCollection().genome().getDisplayName() + " " + chromosome.getName() + ":", JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        featurePanel = new JPanel();
        featurePanel.setLayout(new GridBagLayout());
        add(featurePanel, BorderLayout.CENTER);

        addMouseWheelListener(this);

        tracksUpdated();
    }

    /**
     * This allows you to change the chromosome the viewer is looking at without altering the list of tracks which are displayed.
     *
     * @param chromosome The new chromosome to display.
     */
    private void setChromosome(Chromosome chromosome) {
        if (chromosome == null)
            throw new IllegalArgumentException("Chromosome can't be null");
        if (chromosome != this.chromosome) {
            this.chromosome = chromosome;
        }
        for (AbstractTrack track : tracks) {
            track.updateTrack(chromosome);
        }
    }

    /**
     * This is quite a heavyweight call to make. It forces the recalculation of the layout of all tracks. In many cases it is sufficient to call repaint on the
     * chromosome viewer which will update existing information (name changes, selection changes etc). Only use this when the actual data has been changed.
     */
    public synchronized void tracksUpdated() {
        System.out.println(this.getClass().getName() + "\ttracksUpdated()");

        if (featurePanel == null)
            return;
        // Clear all tracks in chromosome view.
        tracks.clear();
        DataCollection collection = application.dataCollection();
        // Feature track
        AbstractTrack featureTrack = new ChromosomeFeatureTrack(this, collection, GenomeDescriptor.getInstance().getGeneTrackName());
        tracks.add(featureTrack);
        //Sequence track
        AbstractTrack sequenceTrack = new ChromosomeSequenceTrack(this, collection, application.dataCollection().genome().getDisplayName());
        tracks.add(sequenceTrack);
        //Data track
        DataStore[] dataStores = application.drawnDataStores();
        for (DataStore dataStore : dataStores) {
            tracks.add(new ChromosomeDataTrack(this, dataStore));
        }
        for (AbstractTrack track : tracks) {
            track.updateTrack(chromosome);
        }
        featurePanel.removeAll();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        for (AbstractTrack abstractTrack : tracks) {
            if (gridBagConstraints.gridy == 0) {
                gridBagConstraints.weighty = 0.1;
                featurePanel.add(abstractTrack, gridBagConstraints);
            } else if (gridBagConstraints.gridy == 1) {
                gridBagConstraints.weighty = 0.1;
                sequenceScrollPane = new JScrollPane(abstractTrack);
                sequenceScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                sequenceScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                featurePanel.add(sequenceScrollPane, gridBagConstraints);
                sequenceScrollPane.setVisible(false);
            } else {
                gridBagConstraints.weighty = 1;
                JScrollPane scrollCDT = new JScrollPane(abstractTrack);
                scrollCDT.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                scrollCDT.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scrollCDT.getVerticalScrollBar().setUnitIncrement(20);
                featurePanel.add(scrollCDT, gridBagConstraints);
            }
            gridBagConstraints.gridy++;
        }

        // Finally add a scale track, which we weigh very lightly
        gridBagConstraints.weighty = 0.001;
        featurePanel.add(new ChromosomeScaleTrack(this), gridBagConstraints);
        gridBagConstraints.gridy++;
        featurePanel.validate();
        featurePanel.repaint();
    }

    /**
     * Sets the view to a particular location
     *
     * @param start The start position
     * @param end   The end position
     */
    private void setView(int start, int end) {

        // If the view is a reversed section we need to swap start and end
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        currentStart = start;
        currentEnd = end;

        // If the view length is shorter than screen pixel, we show the sequence track.
        if (currentEnd - currentStart < getWidth() && DisplayPreferences.getInstance().isFastaEnable()) {
            sequenceScrollPane.setVisible(true);
        } else {
            sequenceScrollPane.setVisible(false);
        }
        repaint();
    }

    /**
     * Doubles the area of the current view keeping the same midpoint if possible.
     */
    public synchronized void zoomOut() {
        int midBase = currentStart + ((currentEnd - currentStart) / 2);
        int currentWidth = (currentEnd - currentStart) + 1;

        int newStart = midBase - currentWidth;
        if (newStart < 1)
            newStart = 1;

        int newEnd = newStart + (currentWidth * 2);
        if (newEnd > chromosome.getLength())
            newEnd = chromosome.getLength();

        currentStart = newStart;
        currentEnd = newEnd;
        DisplayPreferences.getInstance().setLocation(newStart, newEnd);
    }

    /**
     * Halves the area of the current view keeping the same mid point.
     */
    public synchronized void zoomIn() {
        int midBase = currentStart + ((currentEnd - currentStart) / 2);
        int currentWidth = (currentEnd - currentStart) + 1;

        int newStart = midBase - (currentWidth / 4);

        int newEnd = newStart + (currentWidth / 2);

        DisplayPreferences.getInstance().setLocation(newStart, newEnd);
        currentStart = newStart;
        currentEnd = newEnd;
    }

    /**
     * Moves the view a small amount left
     */
    public void moveLeft() {
        int currentWidth = (currentEnd - currentStart) + 1;
        int interval = currentWidth / 10;
        if (currentStart < interval + 1)
            interval = currentStart - 1;
        DisplayPreferences.getInstance().setLocation(currentStart - interval, currentEnd - interval);
        currentStart -= interval;
        currentEnd -= interval;
    }

    /**
     * Moves the view a small amount right.
     */
    public void moveRight() {
        int currentWidth = (currentEnd - currentStart) + 1;
        int interval = currentWidth / 10;
        if (currentEnd + interval > chromosome.getLength())
            interval = chromosome.getLength() - currentEnd;
        DisplayPreferences.getInstance().setLocation(currentStart + interval, currentEnd + interval);
        currentStart += interval;
        currentEnd += interval;
    }

    /**
     * Says that we're starting to make a selection.
     *
     * @param b true if making a selection
     */
    public void setMakingSelection(boolean b) {
        makingSelection = b;
        repaint();
    }

    /**
     * Sets the selection start.
     *
     * @param x The new selection start
     */
    public void setSelectionStart(int x) {
        int min = findMin(new int[]{selectionStart, selectionEnd, x});
        int max = findMax(new int[]{selectionStart, selectionEnd, x});
        Rectangle r = new Rectangle(min, 0, (max - min) + 1, getHeight());
        selectionStart = x;
        for (AbstractTrack track : tracks) {
            track.repaint(r);
        }
    }

    /**
     * Sets the selection end.
     *
     * @param x The new selection end
     */
    public void setSelectionEnd(int x) {
        int min = findMin(new int[]{selectionStart, selectionEnd, x});
        int max = findMax(new int[]{selectionStart, selectionEnd, x});
        Rectangle r = new Rectangle(min, 0, (max - min) + 1, getHeight());
        selectionEnd = x;
        for (AbstractTrack track : tracks) {
            track.repaint(r);
        }
    }

    /**
     * Says if we're currently making a selection.
     *
     * @return true, if we're making a selection
     */
    public boolean makingSelection() {
        return makingSelection;
    }

    /**
     * Selection start.
     *
     * @return The point where the last complete selection was started.
     */
    public int selectionStart() {
        return selectionStart;
    }

    /**
     * Selection end.
     *
     * @return The point where the last complete selection was ended
     */
    public int selectionEnd() {
        return selectionEnd;
    }

    /**
     * Current view start.
     *
     * @return view start
     */
    public int currentStart() {
        return currentStart;
    }

    /**
     * Current view end.
     *
     * @return view end
     */
    public int currentEnd() {
        return currentEnd;
    }

    /**
     * Application.
     *
     * @return The RED application
     */
    public REDApplication application() {
        return application;
    }

    /**
     * Get feature track.
     *
     * @return the feature track
     */
    public ChromosomeFeatureTrack getFeatureTrack() {
        for (AbstractTrack track : tracks) {
            if (track instanceof ChromosomeFeatureTrack) {
                return (ChromosomeFeatureTrack) track;
            }
        }
        return null;
    }

    /**
     * Gets the positional index of a data track
     *
     * @param t The track to query
     * @return The position of this track in the current set of displayed data tracks.
     */
    public int getIndex(AbstractTrack t) {
        int index = 0;
        for (AbstractTrack track : tracks) {
            if (t == track) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Chromosome.
     *
     * @return The chromosome
     */
    public Chromosome chromosome() {
        return chromosome;
    }

    @Override
    public void activeDataChanged(DataStore dataStore, SiteList siteList) {
        for (AbstractTrack abstractTrack : tracks) {
            if (abstractTrack instanceof ChromosomeDataTrack)
                ((ChromosomeDataTrack) abstractTrack).activeDataChanged(dataStore, siteList);
        }
    }

    /**
     * Finds the max value from a set of integers.
     *
     * @param integers the integers
     * @return the max value
     */
    private int findMax(int[] integers) {
        int max = integers[0];
        for (int i = 1; i < integers.length; i++) {
            if (integers[i] > max)
                max = integers[i];
        }
        return max;
    }

    /**
     * Finds the min value from a set of integers
     *
     * @param integers the integers
     * @return the int
     */
    private int findMin(int[] integers) {
        int min = integers[0];
        for (int i = 1; i < integers.length; i++) {
            if (integers[i] < min)
                min = integers[i];
        }
        return min;
    }

    @Override
    public void displayPreferencesUpdated(DisplayPreferences displayPrefs) {
        if (displayPrefs.getCurrentChromosome() != null && !chromosome.equals(displayPrefs.getCurrentChromosome())) {
            setChromosome(displayPrefs.getCurrentChromosome());
        }
        if (featurePanel != null) {
            setView(displayPrefs.getCurrentStartLocation(), displayPrefs.getCurrentEndLocation());
            int currentLength = (currentEnd - currentStart) + 1;
            String currentLengthString = PositionFormat.formatLength(currentLength, PositionFormat.UNIT_BASEPAIR);

            titleLabel.setText(application.dataCollection().genome().getDisplayName() + " " + chromosome.getName() + ":" + currentStart + "-" + currentEnd +
                    " (" + currentLengthString + ")");
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {

        if (mwe.getWheelRotation() > 0) {
            moveRight();
        } else if (mwe.getWheelRotation() < 0) {
            moveLeft();
        }
    }

}
