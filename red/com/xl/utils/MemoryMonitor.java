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

package com.xl.utils;

import com.xl.main.REDApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * The Class MemoryMonitor provides a display which summarises the current memory usage and cache state.
 */
public class MemoryMonitor extends JPanel implements Runnable, MouseListener, MouseMotionListener {
    /**
     * The Constant DARK_GREEN.
     */
    private static final Color DARK_GREEN = new Color(0, 180, 0);
    /**
     * The Constant DARK_ORANGE.
     */
    private static final Color DARK_ORANGE = new Color(255, 130, 0);
    /**
     * The Constant DARK_RED.
     */
    private static final Color DARK_RED = new Color(180, 0, 0);
    /**
     * The shown warning.
     */
    public boolean shownWarning = false;
    /**
     * The need to show warning.
     */
    public boolean needToShowWarning = false;
    /**
     * The monitor tool tip.
     */
    private String monitorToolTip;
    /**
     * Horrible hack to work around an initialisation order problem when loading
     */
    private boolean registered = false;

    /**
     * Instantiates a new memory monitor.
     */
    public MemoryMonitor() {
        addMouseListener(this);
        addMouseMotionListener(this);
        Thread t = new Thread(this);
        t.start();
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            while (true) {

                if (!registered) {
                    if (REDApplication.getInstance() != null) {
                        registered = true;
                    }
                }

                Thread.sleep(1000);

                if (needToShowWarning && !shownWarning) showMemoryWarning();
                repaint();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show memory warning.
     */
    synchronized private void showMemoryWarning() {
        if (shownWarning) return;
        shownWarning = true;

        JOptionPane.showMessageDialog(null, "You are running short of available memory.\n Please look at Help > Help Contents > Configuration to see what you" +
                " can do about this.", "Low Memory Warning", JOptionPane.WARNING_MESSAGE);

    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        paintMemoryMonitor(g);

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 0);
    }

    /**
     * Paint memory monitor.
     *
     * @param g the g
     */
    private void paintMemoryMonitor(Graphics g) {

        // The memory monitor is drawn in the right 3/4 of the display

        int xStart = 0;
        int xWidth = (getWidth() * 3) / 4;

        long max = Runtime.getRuntime().maxMemory();
        long allocated = Runtime.getRuntime().totalMemory();
        long used = allocated - Runtime.getRuntime().freeMemory();

        // Base colour is green for total available memory
        g.setColor(DARK_GREEN);
        g.fillRect(xStart, 0, xWidth, getHeight());

        // Orange is for allocated memory
        int allocatedWidth = (int) (xWidth * ((double) allocated / max));
        g.setColor(DARK_ORANGE);
        g.fillRect(xStart, 0, allocatedWidth, getHeight());

        // Red is for used memory
        int usedWidth = (int) (xWidth * ((double) used / max));
        g.setColor(DARK_RED);
        g.fillRect(xStart, 0, usedWidth, getHeight());

        int usedPercentage = (int) (100 * ((double) used / max));
        g.setColor(Color.WHITE);
        g.drawString(usedPercentage + "%", xStart + (xWidth / 2) - 10, getHeight() - 3);


        monitorToolTip = "Memory Usage " + (used / (1024 * 1024)) + "MB " + usedPercentage + "% of " + (max / (1024 * 1024)) + "MB";

        if (usedPercentage > 90 && !shownWarning) {
            needToShowWarning = true;
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (e.getX() < (getWidth() * 3) / 4) {
                Runtime.getRuntime().gc();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getX() < (getWidth() * 3) / 4) {
            setToolTipText(monitorToolTip);
        }
    }
}
