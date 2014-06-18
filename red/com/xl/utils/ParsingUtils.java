package com.xl.utils;

import net.sf.samtools.seekablestream.SeekableFTPStream;
import net.sf.samtools.seekablestream.SeekableFileStream;
import net.sf.samtools.seekablestream.SeekableHTTPStream;
import net.sf.samtools.seekablestream.SeekableStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ParsingUtils {
    public static final String RED_DATA_VERSION = "RED Data Version";
    public static final String GENOME_INFORMATION_START = "Genome Information Start";
    public static final String GENOME_INFORMATION_END = "Genome Information End";
    public static final String SAMPLES = "Samples";
    public static final String ANNOTATION = "Annotation";
    public static final String DATA_GROUPS = "Data Groups";
    public static final String REPLICATE_SETS = "Replicate Sets";
    public static final String PROBES = "Probes";
    public static final String LISTS = "Lists";
    public static final String VISIBLE_STORES = "Visible Stores";
    public static final String DISPLAY_PREFERENCES = "Display Preferences";

    public static long getContentLength(String path) {
        try {
            long contentLength = -1;
            if (path.startsWith("http:") || path.startsWith("https:")) {
                URL url = new URL(path);
                HttpURLConnection urlConn = (HttpURLConnection) url
                        .openConnection();
                contentLength = urlConn.getContentLengthLong();

            } else if (path.startsWith("ftp:")) {
                // Use JDK url
                URL url = new URL(path);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(10000);
                contentLength = connection.getInputStream().available();
            } else {
                contentLength = (new File(path)).length();
            }
            return contentLength;
        } catch (IOException e) {
            return -1;
        }
    }

    public static SeekableStream getStreamFor(String path) throws IOException {

        SeekableStream is = null;
        if (path.toLowerCase().startsWith("http:")
                || path.toLowerCase().startsWith("https:")) {
            final URL url = new URL(path);

            is = new SeekableHTTPStream(url);

        } else if (path.toLowerCase().startsWith("ftp:")) {
            final URL url = new URL(path);
            is = new SeekableFTPStream(url);
        } else {
            is = new SeekableFileStream(new File(path));
        }
        return is;
    }

    public static URL createURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Open a BufferedReader on the path, which might be a local file or URL,
     * and might be gzipped or not.
     *
     * @param pathOrUrl
     * @return
     * @throws IOException
     */
    public static BufferedReader openBufferedReader(String pathOrUrl)
            throws IOException {
        BufferedReader reader = null;
        if (isHttpPath(pathOrUrl)) {
            URL url = createURL(pathOrUrl);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
        } else if (isFilePath(pathOrUrl)) {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(pathOrUrl))));
        } else if (isFtpPath(pathOrUrl)) {
        }

        return reader;
    }

    public static BufferedReader openBufferedRead(File file) throws IOException {
        return new BufferedReader(new FileReader(file));
    }

    public static boolean isFilePath(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * To check whether the path is start with "http" or "https"
     *
     * @param path
     * @return
     */
    public static boolean isHttpPath(String path) {
        if (path.toLowerCase().startsWith("http")
                || path.toLowerCase().startsWith("https")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * To check whether the path is start with "ftp"
     *
     * @param path
     * @return
     */
    public static boolean isFtpPath(String path) {
        if (path.toLowerCase().startsWith("ftp*")) {
            return true;
        } else {
            return false;
        }
    }

    public static GeneType parseGeneType(String geneTypeName) {
        geneTypeName = geneTypeName.toLowerCase();
        if (geneTypeName.contains("refflat")) {
            return GeneType.REFFLAT;
        } else if (geneTypeName.contains("genepred")
                || geneTypeName.contains("ensgene") || geneTypeName.contains("refgene")) {
            return GeneType.GENEPRED;
        } else if (geneTypeName.contains("ucscgene")) {
            return GeneType.UCSCGENE;
        } else {
            System.err.println("Unable to parse gene type:" + geneTypeName);
            return null;
        }
    }
}
