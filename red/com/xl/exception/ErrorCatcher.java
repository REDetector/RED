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

package com.xl.exception;

import com.xl.display.dialog.CrashReporter;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * The error catcher can be attached to the main JVM and is triggered any time a throwable exception makes it back all the way through the stack without being
 * caught so we don't miss any errors.
 */
public class ErrorCatcher implements UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread arg0, Throwable arg1) {
        new CrashReporter(arg1);
    }

}
