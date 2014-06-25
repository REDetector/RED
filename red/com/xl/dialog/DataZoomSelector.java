/**
 * Copyright Copyright 2007-13 Simon Andrews
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

package com.xl.dialog;

import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * Provides a small dialog which allows the user to select a suitable data zoom
 * level. Applies the selected level to currently visible data tracks
 */
public class DataZoomSelector extends JDialog implements ActionListener,
        ChangeListener {

    private REDApplication application;
    private double currentMaxZoom;
    private JSlider slider;
    private Hashtable<Integer, JLabel> labels = null;

    /**
     * Instantiates a new data zoom selector.
     *
     * @param application
     */
    public DataZoomSelector(REDApplication application) {
        super(application, "Set Data Zoom");
        this.application = application;

        // We use custom labels since we want 200 positions on the
        // slider but the labels to run from 2-20 in increments of 2
        if (labels == null) {
            labels = new Hashtable<Integer, JLabel>();

            for (int i = 0; i <= 20; i++) {
                if (i % 2 == 0) {
                    labels.put(new Integer(i), new JLabel("" + i));
                } else {
                    labels.put(new Integer(i), new JLabel(""));
                }
            }
        }

        currentMaxZoom = (int) DisplayPreferences.getInstance()
                .getMaxDataValue();

        // Set some limits in case we end up with an impossible range to
        // consider
        if (currentMaxZoom > Math.pow(2, 20))
            currentMaxZoom = Math.pow(2, 20);
        if (currentMaxZoom < 1)
            currentMaxZoom = 1;

        getContentPane().setLayout(new BorderLayout());

        // The slider actually ends up as an exponential scale (to the power of
        // 2).
        // We allow 200 increments on the slider but only go up to 2**20 hence
        // dividing
        // by 10 to get the actual power to raise to.
        slider = new JSlider(0, 200,
                (int) (10 * (Math.log(currentMaxZoom) / Math.log(2))));
        slider.setOrientation(JSlider.VERTICAL);
        slider.addChangeListener(this);
        slider.setLabelTable(labels);
        slider.setMajorTickSpacing(10);

        // This looks a bit pants, but we need it in to work around a bug in
        // the windows 7 LAF where the slider is tiny if labels are not drawn.
        slider.setPaintTicks(true);

        slider.setSnapToTicks(false);
        slider.setPaintTrack(true);
        Hashtable<Integer, Component> labelTable = new Hashtable<Integer, Component>();

        for (int i = 0; i <= 200; i += 20) {
            labelTable.put(new Integer(i), new JLabel("" + (i / 10)));
        }
        slider.setLabelTable(labelTable);

        slider.setPaintLabels(true);
        getContentPane().add(slider, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        getRootPane().setDefaultButton(closeButton);
        closeButton.addActionListener(this);

        getContentPane().add(closeButton, BorderLayout.SOUTH);

        setSize(100, 250);
        setLocationRelativeTo(application);
        setVisible(true);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        setVisible(false);
        dispose();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
     * )
     */
    public void stateChanged(ChangeEvent ce) {
        currentMaxZoom = Math.pow(2, slider.getValue() / 10d);
        DisplayPreferences.getInstance().setMaxDataValue(currentMaxZoom);
        application.genomeViewer().repaint();
    }
}
