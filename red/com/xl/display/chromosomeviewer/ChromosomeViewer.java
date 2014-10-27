package com.xl.display.chromosomeviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.interfaces.DataChangeListener;
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
 * The ChromosomeViewer represents all of the tracks contained in the chromosome
 * view. It is responsible for organising and laying out these tracks and
 * passing information back from them to other parts of the application.
 * <p/>
 * In general the rest of the program shouldn't deal with anything below the
 * chromosome viewer.
 */
public class ChromosomeViewer extends JPanel implements DataChangeListener, DisplayPreferencesListener, MouseWheelListener {

	/*
     * DO NOT CHANGE THESE CONSTANTS.
	 * 
	 * Although they are arbitrary numbers they are used in the SeqMonk file
	 * format and if they are changed the settings will not be restored
	 * correctly and we'll probably get errors.
	 */

    java.util.List<AbstractTrack> tracks = new ArrayList<AbstractTrack>();
    private REDApplication application;
    private Chromosome chromosome;
    private JPanel featurePanel;
    private JLabel titleLabel; // Tried using a TextField to get Copy/Paste, but this broke SVG export
    private int selectionStart = 0;
    private int selectionEnd = 0;
    private boolean makingSelection = false;
    private int currentStart = 0;
    private int currentEnd = 1;

    private JScrollPane sequenceScrollPane = null;

    /**
     * Instantiates a new chromosome viewer.
     *
     * @param application
     * @param chromosome
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
     * This allows you to change the chromsome the viewer is looking at without
     * altering the list of tracks which are displayed.
     *
     * @param chromosome The new chromosome to display.
     */
    private void setChromosome(Chromosome chromosome) {
        System.out.println(this.getClass().getName() + ":setChr(Chromosome chromosome)");
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
     * This is quite a heavyweight call to make. It forces the recalculation of
     * the layout of all tracks. In many cases it is sufficient to call repaint
     * on the chromosome viewer which will update existing information (name
     * changes, selection changes etc). Only use this when the actual data has
     * changed.
     */
    public synchronized void tracksUpdated() {
        System.out.println(this.getClass().getName() + "\ttracksUpdated()");

        if (featurePanel == null)
            return;

        String currentFeatureTrackName = GenomeDescriptor.getInstance().getGeneTrackName();
        if (currentFeatureTrackName == null) {
            currentFeatureTrackName = "Feature Track";
        }
        tracks.clear();
        DataCollection collection = application.dataCollection();
        AbstractTrack featureTrack = new ChromosomeFeatureTrack(this, collection, currentFeatureTrackName);
        tracks.add(featureTrack);
        AbstractTrack sequenceTrack = new ChromosomeSequenceTrack(this, collection, application.dataCollection().genome().getDisplayName());
        tracks.add(sequenceTrack);

        DataStore[] dataStores = application.drawnDataStores();
        for (DataStore dataStore : dataStores) {
            tracks.add(new ChromosomeDataTrack(this, application.dataCollection(), dataStore));
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
                DisplayPreferences.getInstance().addListener((ChromosomeDataTrack) abstractTrack);
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

        // If the view is a reversed section we need to swap
        // start and end
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        currentStart = start;
        currentEnd = end;

        if (currentEnd - currentStart < getWidth() && DisplayPreferences.getInstance().isFastaEnable()) {
            sequenceScrollPane.setVisible(true);
        } else {
            sequenceScrollPane.setVisible(false);
        }
        repaint();
    }

    /**
     * Doubles the area of the current view keeping the same midpoint if
     * possible.
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
        // System.out.println(this.getClass().getDisplayName()+":moveLeft()");
        int currentWidth = (currentEnd - currentStart) + 1;
        int interval = currentWidth / 10;
        if (currentStart < interval + 1)
            interval = currentStart - 1;
        DisplayPreferences.getInstance().setLocation(currentStart - interval,
                currentEnd - interval);
        currentStart -= interval;
        currentEnd -= interval;
    }

    /**
     * Moves the view a small amount right.
     */
    public void moveRight() {
        // System.out.println(this.getClass().getDisplayName()+":moveRight()");
        int currentWidth = (currentEnd - currentStart) + 1;
        int interval = currentWidth / 10;
        if (currentEnd + interval > chromosome.getLength())
            interval = chromosome.getLength() - currentEnd;
        DisplayPreferences.getInstance().setLocation(currentStart + interval,
                currentEnd + interval);
        currentStart += interval;
        currentEnd += interval;
    }

    /**
     * Says that we're starting to make a selction
     *
     * @param b
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

    public int currentStart() {
        return currentStart;
    }

    public int currentEnd() {
        return currentEnd;
    }

    /**
     * Application.
     *
     * @return The seq monk application
     */
    public REDApplication application() {
        // TODO: Remove this dependency so messages pass in a nicer way
        return application;
    }

    public ChromosomeFeatureTrack getFeatureTrack() {
        for (AbstractTrack track : tracks) {
            if (track instanceof ChromosomeFeatureTrack) {
                return (ChromosomeFeatureTrack) track;
            }
        }
        return null;
    }

    public java.util.List<AbstractTrack> getTrackSets() {
        return tracks;
    }

    /**
     * Gets the positional index of a data track
     *
     * @param t The track to query
     * @return The position of this track in the current set of displayed data
     * tracks.
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

    public void activeDataStoreChanged(DataStore s) {
        repaint();
    }

    public void activeProbeListChanged(ProbeList l) {
        for (AbstractTrack abstractTrack : tracks) {
            if (abstractTrack instanceof ChromosomeDataTrack)
                ((ChromosomeDataTrack) abstractTrack).activeProbeListChanged(l);
        }
    }

    public void dataGroupAdded(DataGroup g) {
    }

    public void dataGroupsRemoved(DataGroup[] g) {
    }

    public void dataGroupRenamed(DataGroup g) {
        repaint();
    }

    public void dataGroupSamplesChanged(DataGroup g) {
    }

    public void dataSetAdded(DataSet d) {
    }

    public void dataSetsRemoved(DataSet[] d) {
    }

    public void dataSetRenamed(DataSet d) {
        repaint();
    }

    public void probeSetReplaced(ProbeSet p) {
        // TODO: Do we need to do anything here? Probably not as active probe
        // list replaced will be called
        for (AbstractTrack abstractTrack : tracks) {
            if (abstractTrack instanceof ChromosomeDataTrack)
                ((ChromosomeDataTrack) abstractTrack).probeSetReplaced(p);
        }
    }

    /**
     * Finds the max value from a set of ints.
     *
     * @param ints the ints
     * @return the max value
     */
    private int findMax(int[] ints) {
        int max = ints[0];
        for (int i = 1; i < ints.length; i++) {
            if (ints[i] > max)
                max = ints[i];
        }
        return max;
    }

    /**
     * Finds the min value from a set of ints
     *
     * @param ints the ints
     * @return the int
     */
    private int findMin(int[] ints) {
        int min = ints[0];
        for (int i = 1; i < ints.length; i++) {
            if (ints[i] < min)
                min = ints[i];
        }
        return min;
    }

    public void displayPreferencesUpdated(DisplayPreferences displayPrefs) {
        if (displayPrefs.getCurrentChromosome() != null && !chromosome.equals(displayPrefs.getCurrentChromosome())) {
            setChromosome(displayPrefs.getCurrentChromosome());
        }
        if (featurePanel != null) {
            setView(displayPrefs.getCurrentStartLocation(), displayPrefs.getCurrentEndLocation());
            int currentLength = (currentEnd - currentStart) + 1;
            String currentLengthString = PositionFormat.formatLength(currentLength, PositionFormat.UNIT_BASEPAIR);

            titleLabel.setText(application.dataCollection().genome().getDisplayName() + "  " + chromosome.getName() + ":" + currentStart + "-" + currentEnd +
                    " (" + currentLengthString + ")");
        }
    }

    public void mouseWheelMoved(MouseWheelEvent mwe) {

        if (mwe.getWheelRotation() > 0) {
            moveRight();
        } else if (mwe.getWheelRotation() < 0) {
            moveLeft();
        }
    }

}
