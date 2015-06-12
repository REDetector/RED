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
import java.awt.*;

/**
 * Created by Xing Li on 2014/11/14.
 * <p/>
 * Option dialog utils.
 */
public class OptionDialogUtils {
    public static int showSaveBeforeExitDialog(Component c) {
        return JOptionPane.showOptionDialog(c, "You have made changes which were not saved.  Do you want to save before exiting?",
                "Save before exit?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{
                        "Save and Exit", "Exit without Saving", "Cancel"
                }, "Save and Exit");
    }

    public static int showTableExistDialog(Component c, String tableName) {
        return JOptionPane.showOptionDialog(c, "The table '" + tableName + "' has been existed in database. Do you want to override it? If the answer is yes, " +
                        "then the old table would be deleted.", "Override the table?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Override", "Cancel"}, "Override");
    }

    public static int showFileExistDialog(Component c, String fileName) {
        return JOptionPane.showOptionDialog(c, fileName + " exists.  Do you want to overwrite " +
                        "the existing file?", "Overwrite file?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Overwrite and Save", "Cancel"}, "Overwrite and Save");
    }


    public static void showMessageDialog(Component c, String message) {
        JOptionPane.showMessageDialog(c, message);
    }

    public static void showMessageDialog(Component c, String message, String title) {
        JOptionPane.showMessageDialog(c, message, title, JOptionPane.INFORMATION_MESSAGE, IconLoader.ICON_INFO);
    }

    public static void showWarningDialog(Component c, String message, String title) {
        JOptionPane.showMessageDialog(c, message, title, JOptionPane.WARNING_MESSAGE, IconLoader.ICON_WARNING);
    }

    public static void showErrorDialog(Component c, String message) {
        JOptionPane.showMessageDialog(c, message, "Oops, RED has encountered a problem...", JOptionPane.ERROR_MESSAGE, IconLoader.ICON_ERROR);
    }


}
