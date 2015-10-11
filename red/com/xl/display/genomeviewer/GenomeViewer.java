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

package com.xl.display.genomeviewer;

import com.xl.datatypes.DataStore;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.sites.SiteList;
import com.xl.interfaces.ActiveDataChangedListener;
import com.xl.interfaces.DisplayPreferencesListener;
import com.xl.main.RedApplication;
import com.xl.preferences.DisplayPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * The Class GenomeViewer provides a graphical overview of a whole genome
 */
public class GenomeViewer extends JPanel implements ActiveDataChangedListener, DisplayPreferencesListener {

    /**
     * The chromosome displays.
     */
    private ChromosomeDisplay[] chromosomeDisplays;

    /**
     * The application.
     */
    private RedApplication application;
    /**
     * Since that we want to export the genome view without any highlighted place, so we temporarily cancel painting the highlight when exporting image and
     * restore it after export has been finished.
     */
    private boolean isExportImage = false;

    /**
     * Instantiates a new genome viewer.
     *
     * @param genome      the genome
     * @param application the application
     */
    public GenomeViewer(Genome genome, RedApplication application) {

        Chromosome[] chromosomes = genome.getAllChromosomes();
        chromosomeDisplays = new ChromosomeDisplay[chromosomes.length];
        this.application = application;

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Chromosomes in " + genome.getDisplayName(), JLabel.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel chromosomePanel = new JPanel();
        chromosomePanel.setBackground(Color.WHITE);
        chromosomePanel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;

        for (int i = 0; i < chromosomes.length; i++) {
            gridBagConstraints.gridy = i;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.weightx = 0.1;
            chromosomePanel.add(new JLabel(chromosomes[i].getName()), gridBagConstraints);
            gridBagConstraints.gridx = 1;
            gridBagConstraints.weightx = 100;
            chromosomeDisplays[i] = new ChromosomeDisplay(genome, chromosomes[i], this);
            chromosomePanel.add(chromosomeDisplays[i], gridBagConstraints);
        }

        // Now add the scale at the bottom
        gridBagConstraints.gridy = chromosomes.length;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 100;
        gridBagConstraints.weighty = 0.0001;
        chromosomePanel.add(new ChromosomeScale(genome), gridBagConstraints);

        chromosomePanel.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent arg0) {
            }

            public void mousePressed(MouseEvent arg0) {
            }

            public void mouseReleased(MouseEvent arg0) {
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseExited(MouseEvent arg0) {
                setInfo(null);
            }
        });

        add(new JScrollPane(chromosomePanel), BorderLayout.CENTER);
    }

    public void setExportImage(boolean isExportImage) {
        this.isExportImage = isExportImage;
        repaint();
    }

    /**
     * Application.
     *
     * @return the RED application
     */
    public RedApplication application() {
        return application;
    }

    /**
     * Sets the info.
     *
     * @param c the new info
     */
    protected void setInfo(Chromosome c) {
        if (c == null) {
            application.setStatusText("RED");
        } else {
            application.setStatusText("Chromosome " + c.getName() + " " + c.getLength() + "bp");
        }
    }

    /**
     * Sets the view.
     *
     * @param c     the c
     * @param start the start
     * @param end   the end
     */
    private void setView(Chromosome c, int start, int end) {
        boolean drawSites;
        switch (DisplayPreferences.getInstance().getDisplayMode()) {
            case DisplayPreferences.DISPLAY_MODE_PROBES_ONLY:
                drawSites = true;
                break;
            case DisplayPreferences.DISPLAY_MODE_READS_ONLY:
                drawSites = false;
                break;
            case DisplayPreferences.DISPLAY_MODE_READS_AND_PROBES:
                drawSites = true;
                break;
            default:
                drawSites = false;
        }
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.showSites(drawSites);
            if (!isExportImage) {
                display.setView(c, start, end);
            } else {
                display.setView(null, start, end);
            }
        }
    }

    @Override
    public void displayPreferencesUpdated(DisplayPreferences prefs) {
        setView(prefs.getCurrentChromosome(), prefs.getCurrentStartLocation(), prefs.getCurrentEndLocation());
        repaint();
    }

    @Override
    public void activeDataChanged(DataStore dataStore, SiteList siteList) {
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.activeDataChanged(dataStore, siteList);
        }
    }
}
