package com.xl.utils;

import com.xl.exception.DataLoadException;
import org.broad.tribble.readers.AsciiLineReader;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2014/7/4.
 */
public class FastaUtils {
    static Pattern WHITE_SPACE = Pattern.compile("\\s+");

    /**
     * Creates an index for the provided fasta file
     * inputPath can be a URL, outputPath must point to a file.
     *
     * @param inputPath
     * @param outputPath
     * @return
     * @throws DataLoadException If the fasta file cannot be indexed, for instance
     *                           because the lines are of an uneven length
     */
    public static void createIndexFile(String inputPath, String outputPath) throws DataLoadException, IOException {

        AsciiLineReader reader = null;
        BufferedWriter writer = null;

        try {
            reader = new AsciiLineReader(ParsingUtils.openInputStream(inputPath));
            writer = new BufferedWriter(new FileWriter(outputPath));
            String line;
            String curContig = null;
            Set<String> allContigs = new HashSet<String>();
            int basesPerLine = -1, bytesPerLine = -1;
            long location = 0, size = 0, lastPosition = 0;

            int basesThisLine, bytesThisLine;
            int numInconsistentLines = -1;
            boolean haveTasks = true;
            //Number of blank lines in the current contig.
            //-1 for not set
            int numBlanks = -1;
            int lastBlankLineNum = -1;
            int curLineNum = 0;


            //We loop through, generating a new FastaSequenceIndexEntry
            //every time we see a new header line, or when the file ends.
            while (haveTasks) {
                line = reader.readLine();
                curLineNum++;

                if (line == null || line.startsWith(">")) {
                    //The last line can have a different number of bases/bytes
                    if (numInconsistentLines >= 2) {
                        throw new DataLoadException("Fasta file has uneven line lengths in contig " + curContig, inputPath);
                    }

                    //Done with old contig
                    if (curContig != null) {
                        writeLine(writer, curContig, size, location, basesPerLine, bytesPerLine);
                    }

                    if (line == null) {
                        haveTasks = false;
                        break;
                    }

                    //Header line
                    curContig = WHITE_SPACE.split(line)[0];
                    curContig = curContig.substring(1);
                    if (allContigs.contains(curContig)) {
                        throw new DataLoadException("Contig '" + curContig + "' found multiple times in file.", inputPath);
                    } else {
                        allContigs.add(curContig);
                    }

                    //Should be starting position of next line
                    location = reader.getPosition();
                    size = 0;
                    basesPerLine = -1;
                    bytesPerLine = -1;
                    numInconsistentLines = -1;
                } else {
                    basesThisLine = line.length();
                    bytesThisLine = (int) (reader.getPosition() - lastPosition);

                    //Calculate stats per line if first line, otherwise
                    //check for consistency
                    if (numInconsistentLines < 0) {
                        basesPerLine = basesThisLine;
                        bytesPerLine = bytesThisLine;
                        numInconsistentLines = 0;
                        numBlanks = 0;
                    } else {
                        if ((basesPerLine != basesThisLine || bytesPerLine != bytesThisLine) && basesThisLine > 0) {
                            numInconsistentLines++;
                        }
                    }

                    //Empty line. This is allowed if it's at the end of the contig);
                    if (basesThisLine == 0) {
                        numBlanks++;
                        lastBlankLineNum = curLineNum;
                    } else if (numBlanks >= 1) {
                        throw new DataLoadException(String.format("Blank line at line number %d, followed by data line at %d, in contig %s\nBlank lines are only allowed at the end of a contig", lastBlankLineNum, curLineNum, curContig), inputPath);
                    }

                    size += basesThisLine;
                }
                lastPosition = reader.getPosition();
            }
        } finally {
            if (reader != null) reader.close();
            if (writer != null) writer.close();

        }

    }

    static void writeLine(Writer writer, String contig, long size, long location, int basesPerLine, int bytesPerLine) throws IOException {
        String delim = "\t";
        String line = contig + delim + size + delim + location + delim + basesPerLine + delim + bytesPerLine;
        writer.write(line);
        //We infer the newline character based on bytesPerLine - basesPerLine
        //Fasta file may not have been created on this platform, want to keep the index and fasta file consistent
        String newline = "\n";
        if (bytesPerLine - basesPerLine == 2) {
            newline = "\r\n";
        }
        writer.write(newline);
    }

    public static void regularizeFastaFile(File inputFile, File outputFile) throws IOException {

        int basesPerLine = 80;

        BufferedReader br = null;
        PrintWriter pw = null;

        try {
            br = new BufferedReader(new FileReader(inputFile));
            pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
            String nextLine;
            int count = 0;
            while ((nextLine = br.readLine()) != null) {
                if (nextLine.startsWith(">")) {
                    if (count != 0) {
                        pw.println();
                    }
                    pw.println(nextLine);
                    count = 0;
                } else {
                    char[] characters = nextLine.toCharArray();
                    for (int i = 0; i < characters.length; i++) {
                        pw.print(characters[i]);
                        count++;
                        if (count == basesPerLine) {
                            pw.println();
                            count = 0;
                        }
                    }
                }
            }
        } finally {
            if (br != null) br.close();
            if (pw != null) pw.close();
        }
    }

    public static boolean isFastaPath(String absolutePath) {
        String pth = absolutePath.toLowerCase();
        return pth.endsWith(".fa") || pth.endsWith(".fasta") || pth.endsWith(".fna");
    }
}
