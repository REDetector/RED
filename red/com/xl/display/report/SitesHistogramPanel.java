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

package com.xl.display.report;

import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.Site;
import com.xl.display.dialog.SiteListViewer;
import com.xl.main.REDApplication;
import com.xl.utils.AxisScale;
import com.xl.utils.ColourScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * The Class SitesHistogramPanel displays an interactive histogram from any linear set of data.
 */
public class SitesHistogramPanel extends JPanel implements Runnable {
    /**
     * The data store.
     */
    private DataStore dataStore;
    /**
     * The site data.
     */
    private Site[] sites;
    /**
     * The main histogram panel.
     */
    private MainHistogramPanel mainHistogramPanel;
    /**
     * The histogram categories.
     */
    private HistogramCategory[] histogramCategories;
    /**
     * The status panel.
     */
    private StatusPanel statusPanel;
    /**
     * The stop calculating.
     */
    private boolean stopCalculating = false;
    /**
     * The max data value.
     */
    private int maxCount;
    /**
     * The interval.
     */
    private double interval = 0;

    /**
     * Instantiates a new site histogram panel.
     *
     * @param dataStore the data store.
     */
    public SitesHistogramPanel(DataStore dataStore) {

        this.dataStore = dataStore;
        sites = dataStore.siteSet().getActiveList().getAllSites();

        setLayout(new BorderLayout());
        JPanel textPanel = new JPanel();
        JTextArea textField = new JTextArea("RNA Editing Sites Distribution Histogram");
        textField.setEditable(false);
        textPanel.add(textField, BorderLayout.CENTER);
        add(textPanel, BorderLayout.NORTH);

        mainHistogramPanel = new MainHistogramPanel();
        add(mainHistogramPanel, BorderLayout.CENTER);

        statusPanel = new StatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        calculateCategories();

    }

    /**
     * Main histogram panel.
     *
     * @return the j panel
     */
    public JPanel mainHistogramPanel() {
        return mainHistogramPanel;
    }

    public void exportData(File file) throws IOException {
        mainHistogramPanel.exportData(file);
    }

    /**
     * Calculate categories.
     */
    private void calculateCategories() {
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        String[] chromosomeNames = dataStore.collection().genome().getAllChromosomeNames();
        histogramCategories = new HistogramCategory[chromosomeNames.length];
        for (int i = 0, len = chromosomeNames.length; i < len; i++) {
            histogramCategories[i] = new HistogramCategory(chromosomeNames[i]);
        }
        // In this thread we recalculate the categories to display
        for (Site site : sites) {
            if (stopCalculating) {
                stopCalculating = false;
                return;
            }
            String chr = site.getChr();
            for (HistogramCategory histogramCategory : histogramCategories) {
                if (chr.equals(histogramCategory.chr)) {
                    histogramCategory.count++;
                }
            }
        }

        for (HistogramCategory histogramCategory : histogramCategories) {
            if (histogramCategory.count > maxCount) {
                maxCount = histogramCategory.count;
            }
        }
    }

    /**
     * The Class MainHistogramPanel.
     */
    private class MainHistogramPanel extends JPanel implements MouseListener, MouseMotionListener {

        /**
         * Space of x axis.
         */
        private static final int X_AXIS_SPACE = 50;
        /**
         * Space of y axis.
         */
        private static final int Y_AXIS_SPACE = 30;
        /**
         * The selected category.
         */
        private HistogramCategory selectedCategory = null;

        /**
         * Instantiates a new main histogram panel.
         */
        public MainHistogramPanel() {
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public void exportData(File file) throws IOException {

            PrintWriter pr = new PrintWriter(file);

            pr.println("Chromosome\tCount");
            for (HistogramCategory hc : histogramCategories) {
                pr.println(hc.chr + "\t" + hc.count);
            }
            pr.close();

        }

        /**
         * Sets the data.
         *
         * @param categories the new data
         */
        public void setData(HistogramCategory[] categories) {
            histogramCategories = categories;
            maxCount = 0;
            for (HistogramCategory hc : histogramCategories) {
                if (hc.count > maxCount) {
                    maxCount = hc.count;
                }
            }
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // We want a white background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw the graph axes first.  We leave a border on all sides
            g.setColor(Color.BLACK);

            g.drawLine(X_AXIS_SPACE, 5, X_AXIS_SPACE, getHeight() - Y_AXIS_SPACE);
            g.drawLine(X_AXIS_SPACE, getHeight() - Y_AXIS_SPACE, getWidth() - 5, getHeight() - Y_AXIS_SPACE);

            // If we don't have any data we can stop here
            if (histogramCategories == null) return;

            // We need the scaling factor for the y-axis
            double xScale;

            xScale = (double) (getWidth() - (5 + X_AXIS_SPACE)) / maxCount;

            // Now draw the scale on the y axis
            AxisScale xAxisScale = new AxisScale(0, maxCount);

            double currentXValue = xAxisScale.getStartingValue();

            while (currentXValue < maxCount) {

                double xWidth = currentXValue * xScale;
                String currentXString = xAxisScale.format(currentXValue);
                g.drawString(currentXString, (int) (X_AXIS_SPACE + xWidth + xScale / 2 - g.getFontMetrics().stringWidth(currentXString) / 2),
                        getHeight() - Y_AXIS_SPACE + 15);

                // Put a line across the plot
                if (currentXValue != 0) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawLine((int) (X_AXIS_SPACE + xWidth), 0, (int) (X_AXIS_SPACE + xWidth),
                            getHeight() - Y_AXIS_SPACE);
                    g.setColor(Color.BLACK);
                }
                currentXValue += xAxisScale.getInterval();
            }

            // Now draw the scale on the x axis
            int categoriesLength = histogramCategories.length;
            if (categoriesLength > 0) {
                interval = (double) (getHeight() - Y_AXIS_SPACE) / (categoriesLength + 1);
                for (int i = 0; i < categoriesLength; i++) {
                    g.setColor(Color.BLACK);
                    g.drawString(histogramCategories[i].chr, 2, (int) (interval * (i + 1.5) - g.getFontMetrics().getAscent() / 2));
                    if (histogramCategories[i] == selectedCategory) {
                        g.setColor(ColourScheme.HIGHLIGHTED_HISTOGRAM_BAR);
                    } else {
                        g.setColor(ColourScheme.HISTOGRAM_BAR);
                    }
                    g.fillRect(X_AXIS_SPACE, (int) (interval * (i + 0.5)), getXWidth(histogramCategories[i].count), (int) interval);
                    // Draw a box around it
                    g.setColor(Color.BLACK);
                    g.drawRect(X_AXIS_SPACE, (int) (interval * (i + 0.5)), getXWidth(histogramCategories[i].count), (int) interval);

                }
            }
        }

        private int getXWidth(int count) {
            int x = (int) ((double) (count) / maxCount * (getWidth() - X_AXIS_SPACE));
            if (x <= 5) {
                return x;
            } else {
                return x - 5;
            }
        }

        private HistogramCategory getHistogram(int yPosition) {
            return histogramCategories[(int) ((yPosition - interval / 2) / (getHeight() - Y_AXIS_SPACE - interval) * histogramCategories.length)];
        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent me) {

            // If we're outside the main plot area we don't need to worry about it
            if (me.getX() < 5 || me.getX() > getWidth() - 5 || me.getY() < interval / 2 || me.getY() > getHeight() - Y_AXIS_SPACE - interval / 2) {
                if (selectedCategory != null) {
                    selectedCategory = null;
                    statusPanel.setSelectedCategory(null);
                    repaint();
                }
                return;
            }
            selectedCategory = getHistogram(me.getY());
            statusPanel.setSelectedCategory(selectedCategory);
            repaint();
        }

        @Override
        public void mouseClicked(MouseEvent arg0) {
            if (arg0.getClickCount() == 2 && selectedCategory != null) {
                String chr = selectedCategory.chr;
                java.util.List<Site> siteList = new ArrayList<Site>();
                for (Site site : sites) {
                    if (site.getChr().equals(chr)) {
                        siteList.add(site);
                    }
                }
                new SiteListViewer(siteList.toArray(new Site[0]), "RNA editing sites in " + chr, "RNA editing sites in " + chr,
                        REDApplication.getInstance());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent me) {
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
        }

        @Override
        public void mouseExited(MouseEvent arg0) {
            selectedCategory = null;
            statusPanel.setSelectedCategory(null);
            repaint();
        }
    }

    /**
     * The Class HistogramCategory.
     */
    private class HistogramCategory {
        /**
         * The chromosome.
         */
        public String chr;
        /**
         * The count.
         */
        public int count;
        /**
         * Instantiates a new histogram category.
         */
        public HistogramCategory(String chr) {
            this.chr = chr;
            count = 0;
        }

        @Override
        public String toString() {
            return chr + "\t" + count;
        }
    }

    /**
     * The Class StatusPanel.
     */
    private class StatusPanel extends JPanel {
        /**
         * The label.
         */
        private JLabel label;

        /**
         * Instantiates a new status panel.
         */
        public StatusPanel() {
            setBackground(Color.WHITE);
            setOpaque(true);
            label = new JLabel("No selected category", JLabel.LEFT);
            setLayout(new BorderLayout());
            add(label, BorderLayout.WEST);
            setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
        }

        /**
         * Sets the selected category.
         *
         * @param category the new selected category
         */
        public void setSelectedCategory(HistogramCategory category) {
            if (category == null) {
                label.setText("No selected Category");
            } else {
                label.setText("Chromosome: " + category.chr + ", RNA Editing Sites: " + category.count);
            }
        }

    }
}
