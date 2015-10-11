/*
 * RED: RNA Editing Detector Copyright (C) <2014> <Xing Li>
 * 
 * RED is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * RED is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.xl.display.dialog;

import com.xl.interfaces.ProgressListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xing Li on 2014/10/20.
 * <p/>
 * A progress manager for progress dialog.
 */
public class RedProgressBar {
    /**
     * The instance of RedProgressBar.
     */
    private static RedProgressBar progressBar = new RedProgressBar();
    /**
     * Listeners.
     */
    protected List<ProgressListener> listeners;

    private RedProgressBar() {
        listeners = new ArrayList<ProgressListener>();
    }

    public static RedProgressBar getInstance() {
        return progressBar;
    }

    public void addProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    public boolean removeProgressListener(ProgressListener listener) {
        return listeners.remove(listener);
    }

    public void progressExceptionReceived(Exception e) {
        for (ProgressListener listener : listeners) {
            listener.progressExceptionReceived(e);
        }
    }

    public void progressWarningReceived(Exception e) {
        for (ProgressListener listener : listeners) {
            listener.progressWarningReceived(e);
        }
    }

    public void progressUpdated(String message, int current, int max) {
        for (ProgressListener listener : listeners) {
            listener.progressUpdated(message, current, max);
        }
    }

    public void progressCancelled() {
        for (ProgressListener listener : listeners) {
            listener.progressCancelled();
        }
    }

    public void progressComplete(String command, Object result) {
        for (ProgressListener listener : listeners) {
            listener.progressComplete(command, result);
        }
    }
}
