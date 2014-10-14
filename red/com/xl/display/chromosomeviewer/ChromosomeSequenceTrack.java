package com.xl.display.chromosomeviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.genome.Chromosome;
import com.xl.utils.AsciiUtils;
import com.xl.utils.FontManager;

import java.awt.*;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The ChromosomeFeatureTrack is a display which shows one feature type in the
 * chromosome view. It is usually only created and managed by a surrounding
 * instance of ChromsomeViewer.
 */
public class ChromosomeSequenceTrack extends AbstractTrack {

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
    public ChromosomeSequenceTrack(ChromosomeViewer viewer, DataCollection collection, String sequenceName) {
        super(viewer, collection, sequenceName);
    }

    public void paint(Graphics g) {
        super.paint(g);
        if (fastaFile != null) {
            g.drawRoundRect(0, displayHeight - 10, displayWidth, 10, 3, 3);
            g.setFont(FontManager.defaultFont);
            drawSequence(getSequenceForChr(fastaFile, currentViewerStart, currentViewerEnd), g);
        }
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

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(30, 30);
    }

    private void drawSequence(byte[] sequence, Graphics g) {
        char[] cChar = AsciiUtils.getChars(sequence);
        for (int i = 0, len = cChar.length; i < len; i++) {
            drawBase(g, cChar[i], currentViewerStart + i, displayHeight);
        }
    }

    @Override
    protected void updateTrack(Chromosome chromosome) {
        fastaFile = dataCollection.genome().getAnnotationCollection().getFastaForChr(chromosome);
        repaint();
    }

}
