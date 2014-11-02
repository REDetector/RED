package com.xl.display.genomeviewer;

import com.xl.datatypes.genome.Genome;
import com.xl.utils.AxisScale;

import javax.swing.*;
import java.awt.*;

/**
 * The Class ChromosomeDisplay shows a single chromosome within the genome view.
 */
public class ChromosomeScale extends JPanel {

    /**
     * The max len.
     */
    private int maxLen;

    /**
     * The scale used on the axis *
     */
    private AxisScale axisScale;

    /**
     * The suffix used on the scales *
     */
    private String scaleSuffix;

    /**
     * The division on the scales *
     */
    private int division;

    // Values cached from the last update and used when relating pixels to positions
    public ChromosomeScale(Genome genome) {
        this(genome.getLongestChromosomeLength());
    }

    /**
     * Instantiates a new chromosome scale.
     *
     * @param longestChr the genome
     */
    public ChromosomeScale(int longestChr) {
        maxLen = longestChr;
        axisScale = new AxisScale(0, maxLen);

        if (maxLen >= 1000000) {
            scaleSuffix = "Mbp";
            division = 1000000;
        } else if (maxLen >= 10000) {
            scaleSuffix = "kbp";
            division = 1000;
        } else {
            scaleSuffix = "bp";
            division = 1;
        }

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int xOffset = getWidth() / 80;

        if (xOffset > 10) xOffset = 10;
        if (xOffset < 1) xOffset = 1;

        int width = getWidth() - (2 * xOffset);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.DARK_GRAY);
        g.drawLine(xOffset, 2, xOffset + width, 2);

        double currentY = axisScale.getStartingValue();
        while (currentY < maxLen) {

            int thisX = xOffset + scaleX(width, currentY, maxLen);
            String label = "" + (currentY / division);
            label = label.replaceAll("\\.0+$", "");

            label = label + " " + scaleSuffix;

            g.drawLine(thisX, 2, thisX, 4);

            thisX -= (g.getFontMetrics().stringWidth(label) / 2);

            if (thisX < 2) {
                thisX = 2;
            }

            g.drawString(label, thisX, getHeight() - 2);

            currentY += axisScale.getInterval();
        }

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 20);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(100, 20);
    }


    /**
     * Scale x.
     *
     * @param width   the width
     * @param measure the measure
     * @param max     the max
     * @return the int
     */
    private int scaleX(int width, double measure, double max) {
        return (int) (width * (measure / max));
    }

}
