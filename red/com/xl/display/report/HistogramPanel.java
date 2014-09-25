package com.xl.display.report;

import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.probes.Probe;
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

/**
 * The Class HistogramPanel displays an interactive histogram from
 * any linear set of data.
 */
public class HistogramPanel extends JPanel implements Runnable {

    /**
     * The data.
     */
    private Probe[] probes;

    private Genome genome;
    /**
     * The main histogram panel.
     */
    private MainHistogramPanel mainHistogramPanel;

    private HistogramCategory[] histogramCategories;

    /**
     * The status panel.
     */
    private StatusPanel statusPanel;

    /**
     * The calculating categories.
     */
    private boolean calculatingCategories = false;

    /**
     * The stop calculating.
     */
    private boolean stopCalculating = false;

    /**
     * The max data value.
     */
    private int maxCount;

    /**
     * Instantiates a new histogram panel.
     *
     * @param probes the probes
     */
    public HistogramPanel(Genome genome, Probe[] probes) {

//        if (probes.length < 2) {
//            throw new IllegalArgumentException("At least two data points are needed to draw a histogram");
//        }

        this.genome = genome;
        this.probes = probes;


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

        calcuateCategories();

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
     * Calcuate categories.
     */
    private void calcuateCategories() {

        // If we're already calculating then stop and do it
        // again with the new value

        if (calculatingCategories) {
//			System.out.println("Waiting for previous calculation to finish");
            stopCalculating = true;
            while (calculatingCategories) {
//				System.out.println("Still waiting");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Thread t = new Thread(this);
        t.start();

    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {

//		System.out.println("Calculating "+currentCategoryCount+" categories");

        calculatingCategories = true;

        String[] chromosomeNames = genome.getAllChromosomeNames();
        histogramCategories = new HistogramCategory[chromosomeNames.length];
        for (int i = 0, len = chromosomeNames.length; i < len; i++) {
            histogramCategories[i] = new HistogramCategory(chromosomeNames[i]);
        }
        // In this thread we recalculate the categories to display
        for (Probe probe : probes) {
            if (stopCalculating) {
                stopCalculating = false;
                calculatingCategories = false;
                return;
            }
            String chr = probe.getChr();
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
        calculatingCategories = false;
//		System.out.println("Finished counting");
    }

    /**
     * The Class MainHistogramPanel.
     */
    private class MainHistogramPanel extends JPanel implements MouseListener, MouseMotionListener {

        private static final int X_AXIS_SPACE = 50;
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

        /* (non-Javadoc)
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
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
                g.drawString(xAxisScale.format(currentXValue), (int) (X_AXIS_SPACE + xWidth + g
                        .getFontMetrics().getAscent() / 2), getHeight() - Y_AXIS_SPACE + 15);

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
                double interval = (double) (getHeight() - Y_AXIS_SPACE) / categoriesLength;
                for (int i = 0; i < categoriesLength; i++) {
                    g.setColor(Color.BLACK);
                    g.drawString(histogramCategories[i].chr, 2, (int) (interval * i + g.getFontMetrics().getAscent()));
                    if (histogramCategories[i] == selectedCategory) {
                        g.setColor(ColourScheme.HIGHLIGHTED_HISTOGRAM_BAR);
                    } else {
                        g.setColor(ColourScheme.HISTOGRAM_BAR);
                    }
                    g.fillRect(X_AXIS_SPACE, (int) (interval * i), getXWidth(histogramCategories[i].count),
                            (int) interval);
                    // Draw a box around it
                    g.setColor(Color.BLACK);
                    g.drawRect(X_AXIS_SPACE, (int) (interval * i), getXWidth(histogramCategories[i].count),
                            (int) interval);

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
            return histogramCategories[(int) ((double) yPosition / (getHeight() - Y_AXIS_SPACE) * histogramCategories
                    .length)];
        }

        @Override
        public void mouseDragged(MouseEvent e) {

        }

        /* (non-Javadoc)
                 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
                 */
        public void mouseMoved(MouseEvent me) {

            // If we're outside the main plot area we don't need to worry about it
            if (me.getX() < 5 || me.getX() > getWidth() - 5 || me.getY() < 5 || me.getY() > getHeight() - Y_AXIS_SPACE
                    - 5) {
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

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(MouseEvent arg0) {
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        public void mouseEntered(MouseEvent arg0) {
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        public void mouseExited(MouseEvent arg0) {
            selectedCategory = null;
            statusPanel.setSelectedCategory(null);
            repaint();
        }


        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(MouseEvent me) {

            // Zoom out if they pressed the right mouse button
//            if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
//                double interval = currentMaxValue - currentMinValue;
//                double midPoint = currentMinValue + (interval / 2);
//
//                currentMinValue = Math.max(midPoint - interval, minDataValue);
//                currentMaxValue = Math.min(midPoint + interval, maxDataValue);
//
//                calcuateCategories();
//
//            }
//
//            // If we're selecting then make the new selection
//            if (isSelecting) {
//                isSelecting = false;
//                if (Math.abs(selectionStart - selectionEnd) <= 3) {
//                    // Don't make a selection from a really small region
//                    return;
//                }
//
//                currentMinValue = getXValue(Math.min(selectionStart, selectionEnd));
//                currentMaxValue = getXValue(Math.max(selectionStart, selectionEnd));
//
//                calcuateCategories();
//
//            }


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
