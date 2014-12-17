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

package com.xl.utils.ui;

import javax.swing.*;

/**
 * The icon loader.
 */
public class IconLoader {
    /**
     * The information icon.
     */
    public static Icon ICON_INFO = new ImageIcon(ClassLoader.getSystemResource("resources/information.png"));
    /**
     * The error icon.
     */
    public static Icon ICON_ERROR = new ImageIcon(ClassLoader.getSystemResource("resources/error.png"));
    /**
     * The warning icon.
     */
    public static Icon ICON_WARNING = new ImageIcon(ClassLoader.getSystemResource("resources/warning.png"));
    /**
     * The tick icon.
     */
    public static Icon ICON_TICK = new ImageIcon(ClassLoader.getSystemResource("resources/tick.png"));
    /**
     * Logo of BUPT.
     */
    public static ImageIcon LOGO_1 = new ImageIcon(ClassLoader.getSystemResource("resources/logo_bupt.png"));
    /**
     * Logo of CQMU.
     */
    public static ImageIcon LOGO_2 = new ImageIcon(ClassLoader.getSystemResource("resources/logo_cqmu.png"));

}
