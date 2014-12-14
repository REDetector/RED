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

package com.xl.utils;

import java.awt.*;

/**
 * Created by Xing Li on 2014/7/6.
 * <p/>
 * A font manager to initiate all font used in RED.
 */
public class FontManager {
    public static Font DEFAULT_FONT = new Font("Times New Roman", Font.PLAIN, 10);

    public static Font REPORT_FONT = new Font("Default", Font.BOLD, 12);

    public static Font DIALOG_FONT = new Font(Font.DIALOG, Font.BOLD, 12);

    public static Font COPYRIGHT_FONT = new Font(Font.DIALOG, Font.PLAIN, 14);
}
