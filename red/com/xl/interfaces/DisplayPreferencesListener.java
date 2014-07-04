package com.xl.interfaces;

import com.xl.preferences.DisplayPreferences;

/**
 * Copyright 2012-13 Simon Andrews
 * <p/>
 * This file is part of SeqMonk.
 * <p/>
 * SeqMonk is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * SeqMonk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with SeqMonk; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
public interface DisplayPreferencesListener {

    public void displayPreferencesUpdated(DisplayPreferences prefs);

}
