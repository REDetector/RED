package com.xl.panel;

import com.xl.utils.NumberKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class DataParserOptionsPanel extends JPanel implements KeyListener, ActionListener {

    private JCheckBox removeDuplicates;
    private JTextField minMappingQuality;

    public DataParserOptionsPanel() {
        setLayout(new BorderLayout());

        JPanel commonOptions = new JPanel();
        commonOptions.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        commonOptions.add(new JLabel("Remove duplicate reads"), gbc);
        removeDuplicates = new JCheckBox();
        removeDuplicates.setSelected(true);
        gbc.gridx = 2;
        commonOptions.add(removeDuplicates, gbc);

        minMappingQuality = new JTextField("0");

        gbc.gridx = 1;
        gbc.gridy++;
        commonOptions.add(new JLabel("Min mapping quality"), gbc);
        gbc.gridx = 2;
        minMappingQuality.addKeyListener(new NumberKeyListener(false, false, 255));
        commonOptions.add(minMappingQuality, gbc);

        add(commonOptions, BorderLayout.NORTH);

    }

    public int minMappingQuality() {
        if (minMappingQuality.getText().length() == 0) return 0;
        return Integer.parseInt(minMappingQuality.getText());
    }

    public boolean removeDuplicates() {
        return removeDuplicates.isSelected();
    }

    public Dimension getPreferredSize() {
        return new Dimension(150, 50);
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {

        JTextField source = (JTextField) e.getSource();
        try {
            int i = Integer.parseInt(source.getText());
            if (i < 0) {
                throw new Exception("Negative value");
            }
        } catch (Exception nfe) {
            if (source.getText().length() > 0) {
                source.setText(source.getText().substring(0, source.getText().length() - 1));
                // Do it again in case this doesn't fix the problem
                keyReleased(e);
            }
        }
    }


    public void keyTyped(KeyEvent e) {
    }

    public void actionPerformed(ActionEvent ae) {
    }
}
