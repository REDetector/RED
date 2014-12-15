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
package com.xl.display.dialog;

import com.xl.interfaces.Cancellable;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;
import com.xl.net.crashreport.CrashReporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * The Class ProgressDialog is a generic progress dialog showing a progress bar and a changing label.  It can also display a cancel button for progress
 * listeners which allow it.
 */
public class ProgressDialog extends JDialog implements Runnable, ProgressListener, ActionListener {
    /**
     * The label.
     */
    private JLabel label;
    /**
     * The cancellable.
     */
    private Cancellable cancellable;
    /**
     * The current.
     */
    private int current = 0;
    /**
     * The total.
     */
    private int total = 1;
    /**
     * The progress bar.
     */
    private ProgressBar progressBar = new ProgressBar();
    /**
     * The warning count.
     */
    private int warningCount = 0;
    /**
     * The warnings.
     */
    private Vector<Exception> warnings = new Vector<Exception>();

    /**
     * A record of any exception we've received
     */
    private Exception reportedException = null;

    /**
     * Instantiates a new progress dialog.
     *
     * @param title the title
     */
    public ProgressDialog(String title) {
        this(REDApplication.getInstance(), title, null);
    }

    /**
     * Instantiates a new progress dialog.
     *
     * @param title       the title
     * @param cancellable a cancellable object to end this process
     */
    public ProgressDialog(String title, Cancellable cancellable) {
        this(REDApplication.getInstance(), title, cancellable);
    }

    /**
     * Instantiates a new progress dialog.
     *
     * @param parent the parent
     * @param title  the title
     */
    public ProgressDialog(JFrame parent, String title) {
        this(parent, title, null);
    }

    /**
     * Instantiates a new progress dialog.
     *
     * @param parent      the parent
     * @param title       the title
     * @param cancellable the cancellable
     */
    public ProgressDialog(JFrame parent, String title, Cancellable cancellable) {
        super(parent, title);
        setup(parent, cancellable);
    }

    /**
     * Instantiates a new progress dialog.
     *
     * @param parent the parent
     * @param title  the title
     */
    public ProgressDialog(JDialog parent, String title) {
        this(parent, title, null);
    }

    /**
     * Instantiates a new progress dialog.
     *
     * @param parent      the parent
     * @param title       the title
     * @param cancellable the cancellable
     */
    public ProgressDialog(JDialog parent, String title, Cancellable cancellable) {
        super(parent, title);
        setup(parent, cancellable);
    }

    /**
     * Setup.
     *
     * @param parent      the parent
     * @param cancellable the cancellable
     */
    private void setup(Component parent, Cancellable cancellable) {
        setSize(600, 100);
        setLocationRelativeTo(parent);

        this.cancellable = cancellable;
        label = new JLabel("", JLabel.CENTER);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(label, BorderLayout.CENTER);

        if (cancellable != null) {
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(this);
            cancelButton.setActionCommand("cancel");
            getContentPane().add(cancelButton, BorderLayout.EAST);
        }

        getContentPane().add(progressBar, BorderLayout.SOUTH);
        Thread t = new Thread(this);
        t.start();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setVisible(true);

    }

    public void run() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void progressUpdated(String message, int currentPos, int totalPos) {
        label.setText(message);
        current = currentPos;
        total = totalPos;
        progressBar.repaint();

    }

    public void progressExceptionReceived(Exception e) {

        if (reportedException != null && reportedException == e) return;

        reportedException = e;

        setVisible(false);
        dispose();
        new CrashReporter(e);
    }


    public void progressCancelled() {
        setVisible(false);
        dispose();
    }

    public void progressComplete(String command, Object result) {
        setVisible(false);

        if (warningCount > 0) {
            // We need to display a list of the warnings
            new WarningDisplayDialog(this, warningCount, warnings.toArray(new Exception[0]));
        }
        dispose();
    }

    public void progressWarningReceived(Exception e) {
        warningCount++;
        // We just store this warning so we can display all of them at the end.  We only keep the first 5000 so that things don't get too out of hand
        if (warningCount <= 5000) {
            warnings.add(e);
        }
    }

    public void actionPerformed(ActionEvent e) {
        // This can only come from the cancel button
        cancellable.cancel();
    }

    /**
     * The Class ProgressBar.
     */
    private class ProgressBar extends JPanel {

        /* (non-Javadoc)
         * @see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(Color.RED);
            g.fillRect(0, 0, (int) (getWidth() * ((float) current / total)), getHeight());
        }

    }

}
