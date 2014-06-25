/**
 * Copyright Copyright 2007-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.xl.genomes;

import com.xl.exception.REDException;
import com.xl.interfaces.ProgressListener;
import com.xl.preferences.REDPreferences;
import com.xl.utils.PositionFormat;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The GenomeDownloader actually performs the network interaction required to
 * download a new genome from the main genome database and install it in the
 * local genome cache.
 */
public class GenomeDownloader implements Runnable {

    private Vector<ProgressListener> listeners = new Vector<ProgressListener>();
    private REDPreferences prefs = REDPreferences.getInstance();
    private String id = null;
    private String displayName = null;
    private boolean allowCaching;

    /**
     * Download genome. The values for this should be obtained from the genome
     * index file or the header of an existing SeqMonk file. The size is used
     * merely to provide better feedback during the download of the data and
     * isn't expected to be set correctly from a SeqMonk file where the
     * compressed size isn't recorded.
     *
     * @param id           The latin name of the species
     * @param id           The official assembly name
     * @param allowCaching sets the cache headers to say if a cached copy is OK
     */
    public void downloadGenome(String id, String displayName,
                               boolean allowCaching) {
        this.id = id;
        this.displayName = displayName;
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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {

        // First we need to download the file from the repository
        try {

            // System.out.println("Downloading "+prefs.getGenomeDownloadLocation()+species+"/"+assembly+".zip");
            URL url = new URL(prefs.getGenomeDownloadLocation());
            URLConnection connection = url.openConnection();
            connection.setUseCaches(allowCaching);

            int size = connection.getContentLength();
            if (size == 0) {
                size = 2500000;
            }

            InputStream is = connection.getInputStream();
            DataInputStream d = new DataInputStream(new BufferedInputStream(is));
            File outFile = new File(prefs.getGenomeBase() + File.separator + displayName);
            File dotGenomeFile = new File(outFile.getAbsolutePath()
                    + File.separator + id + ".genome");
            if (outFile.exists()) {
                if (dotGenomeFile.exists() && dotGenomeFile.length() == size) {
                    ProgressListener[] en = listeners.toArray(new ProgressListener[0]);
                    for (int i = en.length - 1; i >= 0; i--) {
                        en[i].progressComplete("genome_downloaded", null);
                    }
                    return;
                    // throw new
                    // REDException("The genome file already exists! You can just load it.");
                }
            } else {
                outFile.mkdirs();
            }
            DataOutputStream o;
            try {
                o = new DataOutputStream(new BufferedOutputStream(
                        new FileOutputStream(dotGenomeFile)));
            } catch (FileNotFoundException fnfe) {
                throw new REDException(
                        "Could't write into your genomes directory.  Please check your file preferences.");
            }
            byte[] b = new byte[8192];
            int totalBytes = 0;
            int i;
            while ((i = d.read(b)) > 0) {
                // System.out.println("Read "+totalBytes+" bytes");
                o.write(b, 0, i);
                totalBytes += i;
                Enumeration<ProgressListener> en = listeners.elements();

                while (en.hasMoreElements()) {
                    en.nextElement().progressUpdated(
                            "Downloaded " + PositionFormat.formatLength(totalBytes, PositionFormat.UNIT_BYTE),
                            totalBytes, size);
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
            return;
        }

        // Tell everyone we're finished

		/*
         * Something odd happens here on my linux system. If I notify the
		 * listeners in the usual order then I'm told that there are two
		 * listeners, but the loop through these listeners (either via
		 * Enumeration or array) only notifies one (SeqMonkApplication) and the
		 * progress dialog is never told.
		 * 
		 * If I notify them in reverse order then it works as expected, but I
		 * can't see why telling the application first should stop further
		 * processing.
		 * 
		 * On my windows system I don't get this problem.
		 */
        ProgressListener[] en = listeners.toArray(new ProgressListener[0]);

        for (int i = en.length - 1; i >= 0; i--) {
            en[i].progressComplete("genome_downloaded", null);
        }

    }

}
