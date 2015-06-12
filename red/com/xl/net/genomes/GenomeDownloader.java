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
package com.xl.net.genomes;

import com.xl.interfaces.ProgressListener;
import com.xl.display.dialog.CrashReporter;
import com.xl.preferences.LocationPreferences;
import com.xl.utils.PositionFormat;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The GenomeDownloader actually performs the network interaction required to download a new genome from the main genome database and install it in the local
 * genome cache.
 */
public class GenomeDownloader implements Runnable {
    /**
     * Progress listeners.
     */
    private Vector<ProgressListener> listeners = new Vector<ProgressListener>();
    /**
     * Genome id.
     */
    private String id = null;
    /**
     * A flag to allow the cache mechanism.
     */
    private boolean allowCaching;

    /**
     * Download genome. The values for this should be obtained from the genome index file or the header of an existing RED file.
     *
     * @param id           The latin name of the species
     * @param allowCaching sets the cache headers to say if a cached copy is OK
     */
    public void downloadGenome(String id, boolean allowCaching) {
        this.id = id;
        this.allowCaching = allowCaching;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Adds a progress listener.
     *
     * @param pl The progress listener to add
     */
    public void addProgressListener(ProgressListener pl) {
        if (pl != null && !listeners.contains(pl))
            listeners.add(pl);
    }

    /**
     * Removes a progress listener.
     *
     * @param pl The progress listener to remove
     */
    public void removeProgressListener(ProgressListener pl) {
        if (pl != null && listeners.contains(pl))
            listeners.remove(pl);
    }

    @Override
    public void run() {

        // First we need to download the file from the repository
        try {
            URL url = new URL(LocationPreferences.getInstance().getGenomeDownloadLists() + id + ".genome");
            URLConnection connection = url.openConnection();
            connection.setUseCaches(allowCaching);

            int size = connection.getContentLength();
            if (size == 0) {
                size = 2500000;
            }
            DataInputStream d = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
            File outFile = new File(LocationPreferences.getInstance().getGenomeDirectory());
            File dotGenomeFile = new File(outFile.getAbsolutePath() + File.separator + id + ".genome");
            if (outFile.exists()) {
                if (dotGenomeFile.exists() && dotGenomeFile.length() == size) {
                    ProgressListener[] en = listeners.toArray(new ProgressListener[0]);
                    for (int i = en.length - 1; i >= 0; i--) {
                        en[i].progressComplete("genome_downloaded", null);
                    }
                    return;
                } else if (dotGenomeFile.exists() && dotGenomeFile.length() != size) {
                    if (!dotGenomeFile.delete()) {
                        throw new IOException();
                    }
                }
            } else {
                if (!outFile.mkdirs()) {
                    throw new IOException();
                }
            }
            DataOutputStream o = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dotGenomeFile)));

            byte[] b = new byte[8192];
            int totalBytes = 0;
            int i;
            while ((i = d.read(b)) > 0) {
                o.write(b, 0, i);
                totalBytes += i;
                Enumeration<ProgressListener> en = listeners.elements();

                while (en.hasMoreElements()) {
                    en.nextElement().progressUpdated("Downloaded " + PositionFormat.formatLength(totalBytes, PositionFormat.UNIT_BYTE), totalBytes, size);
                }
            }
            d.close();
            o.close();
        } catch (Exception ex) {

            Enumeration<ProgressListener> en = listeners.elements();

            while (en.hasMoreElements()) {
                en.nextElement().progressExceptionReceived(ex);
            }
            ex.printStackTrace();
            new CrashReporter(ex);
            return;
        }

        // Tell everyone we're finished
        ProgressListener[] en = listeners.toArray(new ProgressListener[0]);

        for (int i = en.length - 1; i >= 0; i--) {
            en[i].progressComplete("genome_downloaded", null);
        }

    }

}
