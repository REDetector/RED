package com.xl.panel;

import com.xl.utils.MemoryMonitor;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * The Class StatusPanel shows the interactive bar at the bottom
 * of the main application screen.
 */
public class StatusPanel extends JPanel {

    /**
     * The label.
     */
    private JLabel label = new JLabel("RED---RNA Editing Detector", JLabel.LEFT);

    /**
     * Instantiates a new status panel.
     */
    public StatusPanel() {
        setLayout(new BorderLayout());
        add(label, BorderLayout.WEST);
        add(new MemoryMonitor(), BorderLayout.EAST);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    }

    /**
     * Sets the text.
     *
     * @param text the new text
     */
    public void setText(String text) {
        label.setText(text);
    }
}
