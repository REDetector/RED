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

package com.xl.menu;

import com.xl.datatypes.DataCollection;
import com.xl.interfaces.ActiveDataChangedListener;

import javax.swing.*;

public abstract class REDToolbar extends JToolBar implements ActiveDataChangedListener {

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
        collection.addActiveDataListener(this);
    }

    protected DataCollection collection() {
        return collection;
    }

    abstract public String name();


}
