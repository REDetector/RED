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
package com.xl.display.panel;

import com.xl.utils.ParsingUtils;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * A preview panel before opening a RED project file, which will show the samples in the RED project file.
 */
public class REDPreviewPanel extends JPanel implements PropertyChangeListener {
    /**
     * The label.
     */
    private JLabel label;

    public REDPreviewPanel() {

        setPreferredSize(new Dimension(300, 300));

        setLayout(new BorderLayout());

        label = new JLabel("No file selected", JLabel.CENTER);

        add(label, BorderLayout.CENTER);

    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();

        // Make sure we are responding to the right event.
        if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            File selection = (File) e.getNewValue();

            if (selection == null || selection.isDirectory()) {
                clearText();
            } else {
                previewFile(selection);
            }

        }
    }

    /**
     * Reset the label.
     */
    private void clearText() {
        label.setText("No file selected");
    }

    /**
     * Read the genome and samples information by a given RED project file.
     *
     * @param file the RED project file.
     */
    private void previewFile(File file) {

        FileInputStream fis = null;
        BufferedReader br;
        try {
            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(fis)));
        } catch (IOException ioe) {
            try {
                if (fis != null) {
                    fis.close();
                }
                br = new BufferedReader(new FileReader(file));
            } catch (IOException ioe1) {
                label.setText("Failed to read file");
                return;
            }
        }

        try {

            // Read the header into a separate variable in case they've clicked on an empty file.  This way we can check for a null value from reading the
            // first line.
            String header = br.readLine();

            if (header == null || !header.startsWith(ParsingUtils.RED_DATA_VERSION)) {
                label.setText("Not a RED file");
                br.close();
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<html>");

            // The next line should be the genome species and version
            String genome = br.readLine();
            if (!genome.equals(ParsingUtils.GENOME_INFORMATION_START)) {
                label.setText("Not a RED file");
                br.close();
                return;
            }

            genome = genome.replaceAll("\t", " ");

            sb.append(genome);
            sb.append("<br><br>");

            int sampleCount = 0;
            String line;
            // Next we keep going until we hit the samples line, but we'll give up if we haven't found the sample information after 10k lines
            while ((line = br.readLine()) != null) {
                String[] sections = line.split("\\t");
                if (line.startsWith(ParsingUtils.SAMPLES)) {
                    sb.append("Samples:<br>");
                    sampleCount = Integer.parseInt(sections[1]);
                    continue;
                }
                if (sections.length == 8) {
                    sb.append(sections[0]);
                    sb.append("<br>");
                    if (--sampleCount == 0) {
                        break;
                    }
                }
            }
            sb.append("<br>");

            br.close();
            sb.append("</html>");
            label.setText(sb.toString());

        } catch (IOException ex) {
            ex.printStackTrace();
            label.setText("Failed to read file");
        }
    }

}
