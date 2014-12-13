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

package com.xl.display.dialog.gotodialog;

import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;
import com.xl.utils.NumberKeyListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * The Class GoToDialog provides a quick way to jump to a known position in the genome.
 */
public class GoToDialog extends JDialog implements ActionListener, KeyListener, ListSelectionListener {

    private static RecentLocation[] recentLocations = new RecentLocation[10];
    /**
     * The chromosome.
     */
    private JComboBox chromosome;
    /**
     * The start.
     */
    private JTextField start;
    /**
     * The end.
     */
    private JTextField end;
    /**
     * The ok button.
     */
    private JButton okButton;
    /**
     * A list to store the recent position viewed by user.
     */
    private JList recentList;

    /**
     * Instantiates a new goto dialog.
     *
     * @param application the application
     */
    public GoToDialog(REDApplication application) {
        super(application, "Go To Position...");
        setSize(300, 350);
        setLocationRelativeTo(application);
        setModal(true);

        getContentPane().setLayout(new BorderLayout());

        JPanel choicePanel = new JPanel();

        choicePanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.weighty = 0.5;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        choicePanel.add(new JLabel("Chromosome", JLabel.RIGHT), gbc);

        gbc.gridx++;
        gbc.weightx = 0.6;

        String[] chrNames = application.dataCollection().genome().getAllChromosomeNames();

        chromosome = new JComboBox(chrNames);
        choicePanel.add(chromosome, gbc);

        String currentChromosome = DisplayPreferences.getInstance().getCurrentChromosome().getName();
        chromosome.setSelectedItem(currentChromosome);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.2;

        choicePanel.add(new JLabel("From ", JLabel.RIGHT), gbc);

        gbc.gridx++;
        gbc.weightx = 0.6;

        start = new JTextField("" + DisplayPreferences.getInstance().getCurrentStartLocation(), 5);
        start.addKeyListener(new NumberKeyListener(false, false));
        start.addKeyListener(this);
        choicePanel.add(start, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.2;

        choicePanel.add(new JLabel("To ", JLabel.RIGHT), gbc);

        gbc.gridx++;
        gbc.weightx = 0.6;

        end = new JTextField("" + DisplayPreferences.getInstance().getCurrentEndLocation(), 5);
        end.addKeyListener(new NumberKeyListener(false, false));
        end.addKeyListener(this);
        choicePanel.add(end, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        choicePanel.add(new JLabel("Recent Locations", JLabel.CENTER), gbc);

        recentList = new JList(recentLocations);
        recentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentList.getSelectionModel().addListSelectionListener(this);
        gbc.gridy++;
        gbc.weighty = 0.9;
        choicePanel.add(new JScrollPane(recentList), gbc);

        getContentPane().add(choicePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        okButton = new JButton("OK");
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);
        okButton.setEnabled(true);
        getRootPane().setDefaultButton(okButton);
        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    public static void clearRecentLocations() {
        for (int i = 0; i < recentLocations.length; i++) {
            recentLocations[i] = null;
        }
    }

    public static void addRecentLocation(String c, int start, int end) {
        RecentLocation l = new RecentLocation(c, start, end);

        // We now need to go through the existing set of locations. If we find this location already there we remove it and shuffle everything after it up.

        for (int i = 0; i < recentLocations.length; i++) {
            if (recentLocations[i] == null)
                break;

            if (recentLocations[i].compareTo(l) == 0) {
                System.arraycopy(recentLocations, i + 1, recentLocations, i, recentLocations.length - i - 1);
                break;
            }
        }

        // We now move all of the locations down one, and put the new one at the top
        System.arraycopy(recentLocations, 0, recentLocations, 1, recentLocations.length - 2);
        recentLocations[0] = l;
    }

    /**
     * Do goto.
     */
    private void doGoTo() {

        String chr = (String) chromosome.getSelectedItem();
        int startValue = 1;
        int endValue = 1000;

        if (start.getText().length() > 0) {
            startValue = Integer.parseInt(start.getText());
        }
        if (end.getText().length() > 0) {
            endValue = Integer.parseInt(end.getText());
        }
        if (startValue > endValue) {
            int temp = startValue;
            startValue = endValue;
            endValue = temp;
        }
        DisplayPreferences.getInstance().setLocation(chr, startValue, endValue);

        setVisible(false);
        dispose();

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("cancel")) {
            setVisible(false);
            dispose();
        } else if (ae.getActionCommand().equals("ok")) {
            doGoTo();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent arg0) {
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent ke) {

    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent ke) {
        okButton.setEnabled(true);
    }

    public void valueChanged(ListSelectionEvent lse) {

        RecentLocation l = (RecentLocation) recentList.getSelectedValue();
        if (l != null) {
            chromosome.setSelectedItem(l.chromosome());
            start.setText("" + l.start());
            end.setText("" + l.end());
        }
    }

}
