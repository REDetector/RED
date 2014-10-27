package com.xl.display.genomeviewer;

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
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
     * The chromosome displays.
     */
    private ChromosomeDisplay[] chromosomeDisplays;

    /**
     * The application.
     */
    private REDApplication application;
    private boolean isExportImage = false;

    /**
     * Instantiates a new genome viewer.
     *
     * @param genome      the genome
     * @param application the application
     */
    public GenomeViewer(Genome genome, REDApplication application) {

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
            chromosomePanel.add(new JLabel(chromosomes[i].getName()),
                    gridBagConstraints);
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

    public void setExportImage(boolean isExportImage) {
        this.isExportImage = isExportImage;
        repaint();
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
        boolean drawProbes;
        switch (DisplayPreferences.getInstance().getDisplayMode()) {
            case DisplayPreferences.DISPLAY_MODE_PROBES_ONLY:
                drawProbes = true;
                break;
            case DisplayPreferences.DISPLAY_MODE_READS_ONLY:
                drawProbes = false;
                break;
            case DisplayPreferences.DISPLAY_MODE_READS_AND_PROBES:
                drawProbes = true;
                break;
            default:
                drawProbes = false;
        }
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.setShowProbes(drawProbes);
            if (!isExportImage) {
                display.setView(c, start, end);
            } else {
                display.setView(null, start, end);
            }
        }
    }

    // For all of the listener events we merely forward these to the
    // individual chromosome views

    public void activeDataStoreChanged(DataStore s) {
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.activeDataStoreChanged(s);
        }
    }

    public void activeProbeListChanged(ProbeList l) {
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.activeProbeListChanged(l);
        }
    }

    public void dataGroupAdded(DataGroup g) {
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.dataGroupAdded(g);
        }
    }

    public void dataGroupsRemoved(DataGroup[] g) {
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.dataGroupsRemoved(g);
        }
    }

    public void dataGroupRenamed(DataGroup g) {
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.dataGroupRenamed(g);
        }

    }

    public void dataGroupSamplesChanged(DataGroup g) {
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.dataGroupSamplesChanged(g);
        }
    }

    public void dataSetAdded(DataSet d) {
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.dataSetAdded(d);
        }
    }

    public void dataSetsRemoved(DataSet[] d) {
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.dataSetsRemoved(d);
        }
    }

    public void dataSetRenamed(DataSet d) {
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.dataSetRenamed(d);
        }
    }

    public void probeSetReplaced(ProbeSet p) {
        for (ChromosomeDisplay display : chromosomeDisplays) {
            display.probeSetReplaced(p);
        }
    }

    public void displayPreferencesUpdated(DisplayPreferences prefs) {
        setView(prefs.getCurrentChromosome(), prefs.getCurrentStartLocation(), prefs.getCurrentEndLocation());
        repaint();
    }
}
