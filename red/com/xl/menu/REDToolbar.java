package com.xl.menu;

import com.xl.datatypes.DataCollection;
import com.xl.interfaces.DataChangeListener;

import javax.swing.*;

public abstract class REDToolbar extends JToolBar implements DataChangeListener {

    private DataCollection collection = null;
    private REDMenu menu;
    private boolean shown = false;

    public REDToolbar(REDMenu menu) {
        this.menu = menu;
        setFocusable(false);
        setShown(showByDefault());
    }

    public void setShown(boolean shown) {
        this.shown = shown;
    }

    public boolean shown() {
        return shown;
    }

    abstract public void reset();

    abstract public void genomeLoaded();

    abstract public boolean showByDefault();

    protected REDMenu menu() {
        return menu;
    }


    public void setDataCollection(DataCollection collection) {
        this.collection = collection;
        collection.addDataChangeListener(this);
    }

    protected DataCollection collection() {
        return collection;
    }

    abstract public String name();


}
