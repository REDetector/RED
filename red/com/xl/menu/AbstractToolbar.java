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

import com.xl.interfaces.ActiveDataChangedListener;

import javax.swing.*;

/**
 * A abstract toolbar which provides the basic shortcuts with the most wanted functions.
 */
public abstract class AbstractToolbar extends JToolBar implements ActiveDataChangedListener {
    /**
     * The RED menu.
     */
    private REDMenu menu;
    /**
     * Show this toolbar or not.
     */
    private boolean shown = false;

    /**
     * Initiate a new toolbar.
     *
     * @param menu the menu associated with this toolbar.
     */
    public AbstractToolbar(REDMenu menu) {
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

    public abstract void reset();

    public abstract void genomeLoaded();

    public abstract boolean showByDefault();

    protected REDMenu menu() {
        return menu;
    }

    public abstract String name();


}
