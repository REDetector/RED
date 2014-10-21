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

package com.xl.dialog;

import com.xl.interfaces.ProgressListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2014/10/20.
 */
public class REDProgressBar implements ProgressListener {
    private static REDProgressBar progressBar = new REDProgressBar();

    protected List<ProgressListener> listeners;

    private REDProgressBar() {
        listeners = new ArrayList<ProgressListener>();
    }

    public static REDProgressBar getInstance() {
        return progressBar;
    }

    public void addProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    @Override
    public void progressExceptionReceived(Exception e) {
        for (ProgressListener listener : listeners) {
            listener.progressExceptionReceived(e);
        }
    }

    @Override
    public void progressWarningReceived(Exception e) {
        for (ProgressListener listener : listeners) {
            listener.progressWarningReceived(e);
        }
    }

    @Override
    public void progressUpdated(String message, int current, int max) {
        for (ProgressListener listener : listeners) {
            listener.progressUpdated(message, current, max);
        }
    }

    @Override
    public void progressCancelled() {
        for (ProgressListener listener : listeners) {
            listener.progressCancelled();
        }
    }

    @Override
    public void progressComplete(String command, Object result) {
        for (ProgressListener listener : listeners) {
            listener.progressComplete(command, result);
        }
    }
}
