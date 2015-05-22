/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */
package org.broad.igv.repeats;

import org.broad.igv.Globals;
import org.broad.igv.util.ParsingUtils;
import htsjdk.tribble.readers.AsciiLineReader;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Splits a repeat mask file downloaded from UCSC into multiple files,  one per repeat class.
 * Assumes downloaded columns as follows (use table browser, and "select columns" option
 * <p/>
 * genoName  genoStart  genoEnd  strand  repName repClass repFamily
 * <p/>
 * Assumes file is sorted by chromosome
 *
 * @author jrobinso
 */
public class RepeatMaskSplitter {

    public static void main(String[] args) {
        File file = new File(args[0]);
        split(file);
    }

    public static void split(File inputFile) {

        int binCol = 0;
        int millDivCol = 2;
        int millDelCol = 3;
        int millInsCol = 4;
        int chrCol = 5;
        int startCol = 6;
        int endCol = 7;
        int strandCol = 9;
        int nameCol = 10;
        int classCol = 11;
        int famCol = 12;

        Map<String, LinkedHashMap<String, String>> fileMappings = new HashMap();

        AsciiLineReader reader = null;
        HashMap<String, PrintWriter> writers = new HashMap();
        PrintWriter allWriter = null;
        try {
            String lastChr = "";
            reader = new AsciiLineReader(new FileInputStream(inputFile));
            // Skip header
            reader.readLine();
            String nextLine;
            File dir = inputFile.getParentFile();

            allWriter = new PrintWriter(new BufferedWriter(new FileWriter("rmsk.bed")));
            allWriter.println("#gffTags");
            allWriter.println("track name=\"Repeat Masker\"");

            while ((nextLine = reader.readLine()) != null) {
                String[] tokens = Globals.tabPattern.split(nextLine, -1);
                String chr = tokens[chrCol];
                if (!chr.equals(lastChr)) {
                    closeWriters(writers);
                }
                lastChr = chr;

                String repClass = tokens[classCol];
                if (repClass.contains("?")) {
                    continue;
                }
                String filename = repClass + ".bed";

                // Get or create file writer for the class
                PrintWriter pw = writers.get(filename);
                if (pw == null) {

                    File outputFile = new File(dir, filename);
                    pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
                    writers.put(filename, pw);
                }

                String nm = tokens[nameCol];
                String fam = tokens[famCol];

                String name = "Name=" + nm + ";Class=" + repClass + ";Family=" + fam;

                pw.print(chr);
                pw.print("\t");
                pw.print(Integer.parseInt(tokens[startCol]));
                pw.print("\t");
                pw.print(Integer.parseInt(tokens[endCol]));
                pw.print("\t");
                pw.print(name);
                pw.print("\t");
                pw.print(tokens[strandCol]);
                pw.println();

                allWriter.print(chr);
                allWriter.print("\t");
                allWriter.print(Integer.parseInt(tokens[startCol]));
                allWriter.print("\t");
                allWriter.print(Integer.parseInt(tokens[endCol]));
                allWriter.print("\t");
                allWriter.print(name);
                allWriter.print("\t");
                allWriter.print(tokens[strandCol]);
                allWriter.println();

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.close();
            allWriter.close();
            closeWriters(writers);
        }

    }

    private static void closeWriters(HashMap<String, PrintWriter> writers) {
        for (PrintWriter pw : writers.values()) {
            pw.close();
        }
        writers.clear();
    }
}

