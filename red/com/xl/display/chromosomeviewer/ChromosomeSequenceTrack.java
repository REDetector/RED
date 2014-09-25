package com.xl.display.chromosomeviewer;

import com.xl.preferences.DisplayPreferences;
import com.xl.utils.AsciiUtils;
import com.xl.utils.ColourScheme;
import com.xl.utils.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The ChromosomeFeatureTrack is a display which shows one feature type in the
 * chromosome view. It is usually only created and managed by a surrounding
 * instance of ChromsomeViewer.
 */
public class ChromosomeSequenceTrack extends JPanel {

    /**
     * The chromosome viewer which contains this track *
     */
    private ChromosomeViewer viewer;

    /**
     * The current width of this window
     */
    private int displayWidth;

    /**
     * The height of this track
     */
    private int displayHeight;

    private String sequenceName;

    private RandomAccessFile fastaFile;

    /**
     * Instantiates a new chromosome feature track. We have to send the name of
     * the feature type explicitly in case there aren't any features of a given
     * type on a chromosome and we couldn't then work out the name of the track
     * from the features themselves.
     *
     * @param viewer       The chromosome viewer which holds this track
     * @param sequenceName The name of the type of features we're going to show
     */
    public ChromosomeSequenceTrack(ChromosomeViewer viewer, String sequenceName, RandomAccessFile fastaFile) {
        this.viewer = viewer;
        this.fastaFile = fastaFile;
        this.sequenceName = sequenceName;
        addMouseListener(new SequenceListener());
    }

    public void updateSequence(RandomAccessFile fastaFile) {
        this.fastaFile = fastaFile;
        repaint();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }
        displayWidth = getWidth();
        displayHeight = getHeight();
        g.setColor(ColourScheme.REFERENCE_TRACK_BACKGROUND);
        g.fillRect(0, 0, displayWidth, displayHeight);

        if (viewer.makingSelection()) {
            int selStart = viewer.selectionStart();
            int selEnd = viewer.selectionEnd();
            int useStart = (selEnd > selStart) ? selStart : selEnd;
            int selWidth = selEnd - selStart;
            if (selWidth < 0)
                selWidth = 0 - selWidth;
            g.setColor(ColourScheme.DRAGGED_SELECTION);
            g.fillRect(useStart, 0, selWidth, displayHeight);
        }

        // Now go through all the features figuring out whether they
        // need to be displayed

        int startBp = viewer.currentStart();
        int endBp = viewer.currentEnd();


        if (fastaFile != null) {
            g.drawRoundRect(0, 0, displayWidth, 10, 3, 3);
            g.setFont(FontManager.defaultFont);
            drawSequence(getSequenceForChr(fastaFile, startBp, endBp), g);
        }

        // Draw a box into which we'll put the track name so it's not obscured
        // by the data
        int nameWidth = g.getFontMetrics().stringWidth(sequenceName);
        int nameHeight = g.getFontMetrics().getAscent();

        g.setColor(Color.ORANGE);
        g.fillRect(0, 1, nameWidth + 3, nameHeight + 3);

        // Lastly draw the name of the track
        g.setColor(ColourScheme.TRACK_NAME);
        g.drawString(sequenceName, 2, nameHeight + 2);

    }

    public byte[] getSequenceForChr(RandomAccessFile raf, int start, int end) {
        byte[] sequence = new byte[end - start];
        try {
            raf.seek(start - 1);
            int len = raf.read(sequence);
            if (len == end - start) {
                return sequence;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // There's no sense in letting the annotation tracks get too tall. We're
    // better off using that space for data tracks.
    /*
     * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
    public Dimension getMinimumSize() {
        return new Dimension(20, 20);
    }

    private void drawSequence(byte[] sequence, Graphics g) {
        char[] cChar = AsciiUtils.getChars(sequence);
        double pixelForEachBase = (double) displayWidth / (sequence.length);
        for (int i = 0, len = cChar.length; i < len; i++) {
            char c = cChar[i];
            g.setColor(ColourScheme.getBaseColor(c));
            g.drawString(String.valueOf(c), (int) (pixelForEachBase * i + pixelForEachBase / 2), displayHeight);
        }
    }

    /**
     * Pixel to bp.
     *
     * @param x the x
     * @return the int
     */
    private int pixelToBp(int x) {
        int pos = viewer.currentStart()
                + (int) (((double) x / displayWidth) * (viewer.currentEnd() - viewer
                .currentStart()));
        if (pos < 1)
            pos = 1;
        if (pos > viewer.chromosome().getLength())
            pos = viewer.chromosome().getLength();
        return pos;
    }

    /**
     * The listener interface for receiving feature events. The class that is
     * interested in processing a feature event implements this interface, and
     * the object created with that class is registered with a component using
     * the component's <code>addFeatureListener<code> method. When
     * the feature event occurs, that object's appropriate
     * method is invoked.
     */
    private class SequenceListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent me) {
            if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK && me.getClickCount() == 1) {
                viewer.zoomOut();
            } else if ((me.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK && me.getClickCount() ==
                    1) {
                viewer.zoomIn();
            }
        }

        @Override
        public void mousePressed(MouseEvent me) {
            viewer.setMakingSelection(true);
            viewer.setSelectionStart(me.getX());
            viewer.setSelectionEnd(me.getX());
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            viewer.setMakingSelection(false);

            int width = viewer.selectionEnd() - viewer.selectionStart();
            if (width < 0) {
                width = 0 - width;
            }

            // Stop people from accidentally making really short selections
            if (width < 5)
                return;

            DisplayPreferences.getInstance().setLocation(
                    pixelToBp(viewer.selectionStart()),
                    pixelToBp(viewer.selectionEnd()));
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
        }

        @Override
        public void mouseExited(MouseEvent arg0) {
            repaint();
        }

    }

}
