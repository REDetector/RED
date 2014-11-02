package com.xl.filter;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataStore;
import com.xl.dialog.TypeColourRenderer;
import com.xl.utils.ListDefaultSelector;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * Created by Administrator on 2014/10/11.
 */
public abstract class AbstractOptionPanel extends JPanel implements ListSelectionListener {
    protected JList dataList;

    public AbstractOptionPanel(DataCollection collection) {
        setLayout(new BorderLayout());
        JPanel dataPanel = new JPanel();
        dataPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        dataPanel.setLayout(new BorderLayout());
        dataPanel.add(new JLabel("Data Sets/Groups", JLabel.CENTER), BorderLayout.NORTH);

        DefaultListModel dataModel = new DefaultListModel();

        DataStore[] stores = collection.getAllDataStores();

        for (DataStore store : stores) {
            dataModel.addElement(store);
        }

        dataList = new JList(dataModel);
        ListDefaultSelector.selectDefaultStores(dataList);
        dataList.setCellRenderer(new TypeColourRenderer());
        dataList.addListSelectionListener(this);
        dataPanel.add(new JScrollPane(dataList), BorderLayout.CENTER);
        add(dataPanel, BorderLayout.WEST);

        add(new JScrollPane(getOptionPanel()), BorderLayout.CENTER);
        valueChanged(null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 300);
    }

    protected JPanel getOptionPanel() {
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JTextArea textField = new JTextArea();
        textField.setText(getPanelDescription());
        textField.setEditable(false);
        textField.setLineWrap(true);
        totalPanel.add(textField, BorderLayout.NORTH);

        if (hasChoicePanel()) {
            totalPanel.add(getChoicePanel(), BorderLayout.CENTER);
        }
        return totalPanel;
    }

    protected abstract boolean hasChoicePanel();

    protected abstract JPanel getChoicePanel();

    protected abstract String getPanelDescription();
}
