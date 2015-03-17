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

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;

/**
 * Created by Xing Li on 2015/3/16.
 * <p/>
 * The class JFileChooserExt extends JFileChooser and provide
 */
public class JFileChooserExt extends JFileChooser {

    private JTextField tf = null;
    private String regex;

    public JFileChooserExt(String currentDirectoryPath, String regex) {
        super(currentDirectoryPath);
        this.regex = regex;
        init();
    }

    private void init() {
        tf = getTextField(this);

        tf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                modifyFilter();
            }

            public void removeUpdate(DocumentEvent e) {
                modifyFilter();
            }

            public void changedUpdate(DocumentEvent e) {
                modifyFilter();
            }
        });
    }

    private JTextField getTextField(JFileChooser jf) {
        LinkedList<Component> queue = new LinkedList<Component>();
        queue.add(jf);
        while (queue.size() != 0) {
            Component[] c = ((Container) queue.removeFirst()).getComponents();
            for (int i = 0; i < c.length; i++) {
                queue.add(c[i]);
                if (c[i] instanceof JTextField) {
                    return (JTextField) c[i];
                }
            }
        }
        return new JTextField();
    }


    private void modifyFilter() {
        final String text = tf.getText();
        setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (regex != null) {
                    return (f.isDirectory() || f.getName().contains(text) || f.getName().endsWith(regex));
                } else {
                    return (f.isDirectory() || f.getName().contains(text));
                }
            }

            @Override
            public String getDescription() {
                if (regex != null) {
                    return regex;
                } else {
                    return "All files";
                }
            }
        });
    }
}