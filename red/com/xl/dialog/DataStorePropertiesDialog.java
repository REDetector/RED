package com.xl.dialog;

import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.sequence.Location;
import com.xl.main.REDApplication;
import com.xl.utils.Strand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The Class DataStorePropertiesDialog shows some basic stats about a data store
 */
public class DataStorePropertiesDialog extends JDialog implements
        ActionListener, Runnable {

    /**
     * The forward count.
     */
    private JLabel forwardCount;

    /**
     * The revese count.
     */
    private JLabel reveseCount;

    /**
     * The total count.
     */
    private JLabel totalCount;

    /**
     * The unknown count.
     */
    private JLabel unknownCount;

    /**
     * The average length.
     */
    private JLabel averageLength;

    /**
     * The data store.
     */
    private DataStore dataStore;

    /**
     * Instantiates a new data store properties dialog.
     *
     * @param dataStore the data store
     */
    public DataStorePropertiesDialog(DataStore dataStore) {

        super(REDApplication.getInstance(), "DataStore Properties");
        this.dataStore = dataStore;
        getContentPane().setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        infoPanel.add(new JLabel("Name"), gbc);
        gbc.gridx = 2;
        infoPanel.add(new JLabel(dataStore.name()), gbc);

        gbc.gridx = 1;
        gbc.gridy++;

        if (dataStore instanceof DataSet) {
            infoPanel.add(new JLabel("File Name"), gbc);
            gbc.gridx = 2;
            infoPanel.add(new JLabel(((DataSet) dataStore).fileName()), gbc);
        } else if (dataStore instanceof DataGroup) {
            infoPanel.add(new JLabel("Data Sets"), gbc);
            gbc.gridx = 2;
            infoPanel.add(new JLabel(((DataGroup) dataStore).dataSets().length + ""), gbc);

        }

        gbc.gridx = 1;
        gbc.gridy++;

        infoPanel.add(new JLabel("Total Reads"), gbc);
        gbc.gridx = 2;
        totalCount = new JLabel("" + dataStore.getTotalReadCount());
        infoPanel.add(totalCount, gbc);

        gbc.gridx = 1;
        gbc.gridy++;

        infoPanel.add(new JLabel("Forward Count"), gbc);
        gbc.gridx = 2;
        forwardCount = new JLabel("" + dataStore.getReadCountForStrand(Strand.POSITIVE));
        infoPanel.add(forwardCount, gbc);

        gbc.gridx = 1;
        gbc.gridy++;

        infoPanel.add(new JLabel("Reverse Count"), gbc);
        gbc.gridx = 2;
        reveseCount = new JLabel("" + dataStore.getReadCountForStrand(Strand.NEGATIVE));
        infoPanel.add(reveseCount, gbc);

        gbc.gridx = 1;
        gbc.gridy++;

        infoPanel.add(new JLabel("Unknown Count"), gbc);
        gbc.gridx = 2;
        unknownCount = new JLabel("" + dataStore.getReadCountForStrand(Strand.NONE));
        infoPanel.add(unknownCount, gbc);

        gbc.gridx = 1;
        gbc.gridy++;

        infoPanel.add(new JLabel("Average Read Length"), gbc);
        gbc.gridx = 2;
        averageLength = new JLabel("Calculating...");
        infoPanel.add(averageLength, gbc);

        getContentPane().add(new JScrollPane(infoPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(this);
        buttonPanel.add(closeButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(300, 250);
        setLocationRelativeTo(REDApplication.getInstance());
        setVisible(true);

        Thread t = new Thread(this);
        t.start();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        // The only action is to close
        setVisible(false);
        dispose();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {

        String[] chrs = dataStore.collection().genome().getAllChromosomeNames();
        double averageLength = 0;
        int totalCount = 0;
        int shortestLength = 0;
        int longestLength = 0;


        for (int c = 0; c < chrs.length; c++) {
            java.util.List<? extends Location> reads = dataStore.getReadsForChromosome(chrs[c]);

            for (int i = 0; i < reads.size(); i++) {
                totalCount++;
                Location location = reads.get(i);
                int readLength = location.getEnd() - location.getStart();

                if (i == 0) {
                    shortestLength = readLength;
                    longestLength = readLength;
                }

                if (readLength < shortestLength)
                    shortestLength = readLength;
                if (readLength > longestLength)
                    longestLength = readLength;

                averageLength += readLength;
            }
        }
        averageLength /= totalCount;

        this.averageLength.setText("" + (int) averageLength + "bp (" + shortestLength + "-" + longestLength + ")");

    }

}
