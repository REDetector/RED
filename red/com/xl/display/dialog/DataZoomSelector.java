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

package com.xl.display.dialog;

import com.xl.datatypes.genome.Chromosome;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Hashtable;

/**
 * Provides a modal stick on toolbar which allows the user to select a suitable data zoom level. Applies the selected level to currently visible data tracks
 */
public class DataZoomSelector extends JDialog implements ChangeListener {
    /**
     * The application.
     */
    private REDApplication application;
    /**
     * Current chromosome
     */
    private Chromosome currentChromosome;
    /**
     * Current chromosome length.
     */
    private int currentChromosomeLength;
    /**
     * Current view length.
     */
    private int currentViewLength;
    /**
     * The slider.
     */
    private JSlider slider;

    /**
     * Instantiates a new data zoom selector.
     *
     * @param application the application
     */
    public DataZoomSelector(REDApplication application) {
        super(application, "Set Data Zoom");
        this.application = application;
        setJSlider();
    }

    public void setJSlider() {
        currentChromosome = DisplayPreferences.getInstance().getCurrentChromosome();
        currentChromosomeLength = DisplayPreferences.getInstance().getCurrentChromosome().getLength();
        currentViewLength = DisplayPreferences.getInstance().getCurrentLength();
        getContentPane().setLayout(new BorderLayout());
        Hashtable<Integer, Component> labelTable = new Hashtable<Integer, Component>();
        for (int i = 0; i <= 20; i++) {
            if (i % 2 == 0) {
                labelTable.put(currentChromosomeLength / 20 * i, new JLabel("" + i));
            } else {
                labelTable.put(currentChromosomeLength / 20 * i, new JLabel(""));
            }
        }
        if (slider == null) {
            slider = new JSlider(0, currentChromosomeLength, currentViewLength);
            getContentPane().add(slider, BorderLayout.CENTER);
        } else {
            getContentPane().remove(slider);
            slider = new JSlider(0, currentChromosomeLength, currentViewLength);
            getContentPane().add(slider, BorderLayout.CENTER);
        }
        slider.setPaintTicks(true);
        slider.setSnapToTicks(false);
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        slider.setOrientation(JSlider.HORIZONTAL);
        slider.addChangeListener(this);
        slider.setMajorTickSpacing(currentChromosomeLength / 20);
    }

    /**
     * Provide a simple way for data zooming.
     *
     * @param ce the change event
     */
    public void stateChanged(ChangeEvent ce) {
        if (currentChromosome != DisplayPreferences.getInstance().getCurrentChromosome()) {
            setJSlider();
            return;
        }
        int currentMidPoint = DisplayPreferences.getInstance().getCurrentMidPoint();
        currentViewLength = slider.getValue();
        int start = DisplayPreferences.getInstance().getCurrentStartLocation();
        int end = DisplayPreferences.getInstance().getCurrentEndLocation();
        if (start < 20) {
            DisplayPreferences.getInstance().setLocation(0, currentMidPoint + currentViewLength / 2);
        } else if (end > currentChromosomeLength - 20) {
            DisplayPreferences.getInstance().setLocation(currentMidPoint - currentViewLength / 2, currentChromosomeLength);
        } else if (start == 0 && end == currentChromosomeLength) {
            DisplayPreferences.getInstance().setLocation(0, currentChromosomeLength);
            slider.setValue(currentChromosomeLength);
        } else {
            DisplayPreferences.getInstance().setLocation(currentMidPoint - currentViewLength / 2, currentMidPoint + currentViewLength / 2);
        }
        application.genomeViewer().repaint();
    }
}
