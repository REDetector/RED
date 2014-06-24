package com.xl.display.genomeviewer;

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

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.ReplicateSet;
import com.xl.datatypes.genome.Chromosome;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.probes.ProbeList;
import com.xl.datatypes.probes.ProbeSet;
import com.xl.interfaces.DataChangeListener;
import com.xl.interfaces.DisplayPreferencesListener;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * The Class GenomeViewer provides a graphical overview of a whole genome
 */
public class GenomeViewer extends JPanel implements DataChangeListener,
        DisplayPreferencesListener {

    /**
     * The chromosomes.
     */
    private Chromosome[] chromosomes;

    /**
     * The chromosome displays.
     */
    private ChromosomeDisplay[] chromosomeDisplays;

    /**
     * The application.
     */
    private REDApplication application;

    /**
     * Instantiates a new genome viewer.
     *
     * @param genome      the genome
     * @param application the application
     */
    public GenomeViewer(Genome genome, REDApplication application) {
        chromosomes = genome.getAllChromosomes();
        chromosomeDisplays = new ChromosomeDisplay[chromosomes.length];
        this.application = application;

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Chromosomes in " + " "
                + genome.getDisplayName(), JLabel.CENTER);
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
            chromosomePanel.add(new JLabel(chromosomes[i].getName()),
                    gridBagConstraints);
            gridBagConstraints.gridx = 1;
            gridBagConstraints.weightx = 100;
            chromosomeDisplays[i] = new ChromosomeDisplay(genome,
                    chromosomes[i], this);
            chromosomePanel.add(chromosomeDisplays[i], gridBagConstraints);
        }

        // Now add the scale at the bottom
        gridBagConstraints.gridy = chromosomes.length;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.weightx = 100;
        gridBagConstraints.weighty = 0.0001;
        chromosomePanel.add(new ChromosomeScale(genome), gridBagConstraints);

        chromosomePanel.addMouseListener(new MouseListener() {
            public void mouseExited(MouseEvent arg0) {
                setInfo(null);
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseReleased(MouseEvent arg0) {
            }

            public void mousePressed(MouseEvent arg0) {
            }

            public void mouseClicked(MouseEvent arg0) {
            }
        });

        add(new JScrollPane(chromosomePanel), BorderLayout.CENTER);
    }

    /**
     * Application.
     *
     * @return the seq monk application
     */
    public REDApplication application() {
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
            application.setStatusText("Chromosome " + c.getName() + " "
                    + c.getLength() + "bp");
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
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].setView(c, start, end);
        }

    }

    // For all of the listener events we merely forward these to the
    // individual chromosome views

    public void activeDataStoreChanged(DataStore s) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].activeDataStoreChanged(s);
        }
    }

    public void activeProbeListChanged(ProbeList l) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].activeProbeListChanged(l);
        }
    }

    public void dataGroupAdded(DataGroup g) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].dataGroupAdded(g);
        }
    }

    public void dataGroupsRemoved(DataGroup[] g) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].dataGroupsRemoved(g);
        }
    }

    public void dataGroupRenamed(DataGroup g) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].dataGroupRenamed(g);
        }

    }

    public void dataGroupSamplesChanged(DataGroup g) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].dataGroupSamplesChanged(g);
        }
    }

    public void dataSetAdded(DataSet d) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].dataSetAdded(d);
        }
    }

    public void dataSetsRemoved(DataSet[] d) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].dataSetsRemoved(d);
        }
    }

    public void dataSetRenamed(DataSet d) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].dataSetRenamed(d);
        }
    }

    public void probeSetReplaced(ProbeSet p) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].probeSetReplaced(p);
        }
    }

    public void replicateSetAdded(ReplicateSet r) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].replicateSetAdded(r);
        }
    }

    public void replicateSetsRemoved(ReplicateSet[] r) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].replicateSetsRemoved(r);
        }
    }

    public void replicateSetRenamed(ReplicateSet r) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].replicateSetRenamed(r);
        }
    }

    public void replicateSetStoresChanged(ReplicateSet r) {
        for (int i = 0; i < chromosomeDisplays.length; i++) {
            chromosomeDisplays[i].replicateSetStoresChanged(r);
        }
    }

    public void displayPreferencesUpdated(DisplayPreferences prefs) {
        setView(prefs.getCurrentChromosome(), prefs.getCurrentStartLocation(),
                prefs.getCurrentEndLocation());
        repaint();
    }
}
