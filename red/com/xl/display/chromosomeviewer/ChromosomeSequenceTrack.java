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

package com.xl.display.chromosomeviewer;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.genome.Chromosome;
import com.xl.utils.AsciiUtils;
import com.xl.utils.FontManager;

import java.awt.*;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The ChromosomeSequenceTrack is a display which shows the reference genome from a FASTA file in the chromosome view. It is usually only created and managed by
 * a surrounding instance of ChromosomeViewer.
 */
public class ChromosomeSequenceTrack extends AbstractTrack {

    /**
     * Current fasta file used.
     */
    private RandomAccessFile fastaFile;
    /**
     * The collection.
     */
    private DataCollection collection;

    /**
     * Instantiates a new chromosome sequence track.
     *
     * @param viewer       The chromosome viewer which holds this track
     * @param collection   The collection for the fasta file.
     * @param sequenceName The name of this reference genome.
     */
    public ChromosomeSequenceTrack(ChromosomeViewer viewer, DataCollection collection, String sequenceName) {
        super(viewer, sequenceName);
        this.collection = collection;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (fastaFile != null) {
            g.drawRoundRect(0, displayHeight - 10, displayWidth, 10, 3, 3);
            g.setFont(FontManager.DEFAULT_FONT);
            drawSequence(getSequenceForChr(fastaFile, currentViewerStart, currentViewerEnd), g);
        }
    }

    /**
     * Get a small continuous piece of sequence from a huge cached fasta file.
     *
     * @param raf   the cached fasta file
     * @param start the start position
     * @param end   the end position
     * @return the byte array which contains the bases for this small piece.
     */
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
        return new Dimension(30, 25);
    }

    /**
     * Draw the sequence obtained from the cached fasta file.
     *
     * @param sequence the sequence
     * @param g        the g
     */
    private void drawSequence(byte[] sequence, Graphics g) {
        char[] cChar = AsciiUtils.getChars(sequence);
        for (int i = 0, len = cChar.length; i < len; i++) {
            drawBase(g, cChar[i], currentViewerStart + i, displayHeight);
        }
    }

    /**
     * Update the cached fasta file if chromosome is changed.
     *
     * @param chromosome the chromosome
     */
    @Override
    protected void updateTrack(Chromosome chromosome) {
        fastaFile = collection.genome().getAnnotationCollection().getFastaForChr(chromosome);
        repaint();
    }

}
