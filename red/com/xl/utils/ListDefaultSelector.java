package com.xl.utils;

import com.xl.datatypes.DataStore;
import com.xl.main.REDApplication;

import javax.swing.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by Administrator on 2014/7/24.
 */
public class ListDefaultSelector {

    public static void selectDefaultStores(JList list) {
        DataStore[] stores = REDApplication.getInstance().drawnDataStores();
        Vector<Integer> selected = new Vector<Integer>();

        for (int index = 0; index < list.getModel().getSize(); index++) {
            for (int store = 0; store < stores.length; store++) {
                if (stores[store] == list.getModel().getElementAt(index)) {
                    selected.add(index);
                }
            }
        }
        int[] selectedIndices = new int[selected.size()];

        Enumeration<Integer> e = selected.elements();
        int i = 0;
        while (e.hasMoreElements()) {
            selectedIndices[i] = e.nextElement();
            i++;
        }

        list.setSelectedIndices(selectedIndices);


    }
}
