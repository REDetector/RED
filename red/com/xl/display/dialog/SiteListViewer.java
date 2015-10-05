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

import com.sun.java.TableSorter;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.FontManager;
import com.xl.utils.namemanager.MenuUtils;
import com.xl.utils.ui.OptionDialogUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;

/**
 * The Class SiteListViewer shows a simple view of a site list and its description
 */
public class SiteListViewer extends JDialog implements MouseListener, ActionListener {

    /**
     * The table.
     */
    private JTable table;

    /**
     * Instantiates a new site list viewer.
     *
     * @param list        the list
     * @param application the application
     */
    public SiteListViewer(SiteList list, REDApplication application) {
        super(application, list.getListName() + " (" + list.getAllSites().length + " sites)");

        Site[] sites = list.getAllSites();

        getContentPane().setLayout(new BorderLayout());

        JTextArea description = new JTextArea("Description:\n\n" + list.description() + "\n\nComments:\n\n" + list.comments(), 5, 0);
        description.setEditable(false);
        description.setFont(FontManager.DEFAULT_FONT);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        getContentPane().add(new JScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.NORTH);

        String[] headers = new String[]{"Chr", "Position", "Reference Base", "Alternative Base"};
        Class[] classes = new Class[]{String.class, Integer.class, Character.class, Character.class};

        Object[][] rowData = new Object[sites.length][headers.length];

        for (int i = 0; i < sites.length; i++) {
            rowData[i][0] = sites[i].getChr();
            rowData[i][1] = sites[i].getStart();
            rowData[i][2] = sites[i].getRefBase();
            rowData[i][3] = sites[i].getAltBase();
        }

        TableSorter sorter = new TableSorter(new SiteTableModel(rowData, headers, classes));
        table = new JTable(sorter);
        //		table.setDefaultRenderer(Double.class, new SmallDoubleCellRenderer());
        table.addMouseListener(this);
        sorter.setTableHeader(table.getTableHeader());

        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton cancelButton = new JButton(MenuUtils.CLOSE_BUTTON);
        cancelButton.setActionCommand(MenuUtils.CLOSE_BUTTON);
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton(MenuUtils.SAVE_BUTTON);
        saveButton.setActionCommand(MenuUtils.SAVE_BUTTON);
        saveButton.addActionListener(this);
        buttonPanel.add(saveButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(500, 350);
        setLocationRelativeTo(application);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);

    }

    public SiteListViewer(Site[] sites, String siteName, String descriptions, REDApplication application) {
        super(application, siteName + " (" + sites.length + " sites)");

        getContentPane().setLayout(new BorderLayout());

        JTextArea description = new JTextArea("Description:\n\n" + descriptions, 5, 0);
        description.setEditable(false);
        description.setFont(FontManager.DEFAULT_FONT);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        getContentPane().add(new JScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.NORTH);

        String[] headers = new String[]{"Chr", "Position", "Reference Base", "Alternative Base"};
        Class[] classes = new Class[]{String.class, Integer.class, Character.class, Character.class};

        Object[][] rowData = new Object[sites.length][headers.length];

        for (int i = 0; i < sites.length; i++) {
            rowData[i][0] = sites[i].getChr();
            rowData[i][1] = sites[i].getStart();
            rowData[i][2] = sites[i].getRefBase();
            rowData[i][3] = sites[i].getAltBase();
        }

        TableSorter sorter = new TableSorter(new SiteTableModel(rowData, headers, classes));
        table = new JTable(sorter);
        //		table.setDefaultRenderer(Double.class, new SmallDoubleCellRenderer());
        table.addMouseListener(this);
        sorter.setTableHeader(table.getTableHeader());

        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton cancelButton = new JButton(MenuUtils.CLOSE_BUTTON);
        cancelButton.setActionCommand(MenuUtils.CLOSE_BUTTON);
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton(MenuUtils.SAVE_BUTTON);
        saveButton.setActionCommand(MenuUtils.SAVE_BUTTON);
        saveButton.addActionListener(this);
        buttonPanel.add(saveButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(500, 350);
        setLocationRelativeTo(application);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);

    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {
        //We're only interested in double clicks
        if (me.getClickCount() != 2) return;
        // This is only linked from the report JTable
        JTable t = (JTable) me.getSource();
        int r = t.getSelectedRow();
        int position = (Integer) t.getValueAt(r, 1);
        if (position < 5) {
            DisplayPreferences.getInstance().setLocation(0, position + 5);
        } else if (position > DisplayPreferences.getInstance().getCurrentChromosome().getLength() - 5) {
            DisplayPreferences.getInstance().setLocation(position - 5, DisplayPreferences.getInstance()
                    .getCurrentChromosome()
                    .getLength());
        } else {
            DisplayPreferences.getInstance().setLocation(position - 5, position + 5);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent arg0) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent arg0) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent arg0) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent arg0) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {

        if (ae.getActionCommand().equals(MenuUtils.CLOSE_BUTTON)) {
            setVisible(false);
            dispose();
        } else if (ae.getActionCommand().equals(MenuUtils.SAVE_BUTTON)) {
            JFileChooser chooser = new JFileChooserExt(LocationPreferences.getInstance().getProjectSaveLocation(), "txt");
            chooser.setMultiSelectionEnabled(false);

            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.CANCEL_OPTION) return;

            File file = chooser.getSelectedFile();
            LocationPreferences.getInstance().setProjectSaveLocation(file.getParent());

            if (!file.getPath().toLowerCase().endsWith(".txt")) {
                file = new File(file.getPath() + ".txt");
            }


            // Check if we're stepping on anyone's toes...
            if (file.exists()) {
                int answer = OptionDialogUtils.showFileExistDialog(this, file.getName());

                if (answer > 0) {
                    return;
                }
            }


            try {
                PrintWriter p = new PrintWriter(new FileWriter(file));

                TableModel model = table.getModel();

                int rowCount = model.getRowCount();
                int colCount = model.getColumnCount();

                // Do the headers first
                StringBuffer b = new StringBuffer();
                for (int c = 0; c < colCount; c++) {
                    b.append(model.getColumnName(c));
                    if (c + 1 != colCount) {
                        b.append("\t");
                    }
                }

                p.println(b);

                for (int r = 0; r < rowCount; r++) {
                    b = new StringBuffer();
                    for (int c = 0; c < colCount; c++) {
                        b.append(model.getValueAt(r, c));
                        if (c + 1 != colCount) {
                            b.append("\t");
                        }
                    }
                    p.println(b);

                }
                p.close();

            } catch (FileNotFoundException e) {
                new CrashReporter(e);
            } catch (IOException e) {
                new CrashReporter(e);
            }

        }

    }

    /**
     * The Class SiteTableModel.
     */
    private class SiteTableModel extends AbstractTableModel {

        /**
         * The data.
         */
        private Object[][] data;

        /**
         * The headers.
         */
        private String[] headers;

        /**
         * The classes.
         */
        private Class[] classes;

        /**
         * Instantiates a new site table model.
         *
         * @param data    the data
         * @param headers the headers
         * @param classes the classes
         */
        public SiteTableModel(Object[][] data, String[] headers, Class[] classes) {
            super();
            this.data = data;
            this.headers = headers;
            this.classes = classes;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#calRowCount()
         */
        public int getRowCount() {
            return data.length;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            if (data.length > 0) {
                return data[0].length;
            }
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int r, int c) {
            return data[r][c];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        public String getColumnName(int c) {
            return headers[c];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        public Class getColumnClass(int c) {
            return classes[c];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
         */
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    }
}
