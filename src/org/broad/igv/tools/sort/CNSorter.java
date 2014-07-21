/*
 * Copyright (c) 2007-2013 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.tools.sort;

import org.apache.log4j.Logger;
import htsjdk.tribble.readers.AsciiLineReader;

import java.io.*;

/**
 * @author jrobinso
 */
public class CNSorter extends Sorter {

    static private Logger log = Logger.getLogger(Sorter.class);

    public CNSorter(File inputFile, File outputFile) {
        super(inputFile, outputFile);
    }

    String writeHeader(AsciiLineReader reader, PrintWriter writer) throws IOException {
        String nextLine = reader.readLine();
        while (nextLine.startsWith("#") || (nextLine.trim().length() == 0)) {
            writer.println(nextLine);
            nextLine = reader.readLine();
        }
        // column headers
        writer.println(nextLine);

        if(!nextLine.startsWith("SNP")){
            log.warn("Expected header line not found");
        }
        return null;
    }

    Parser getParser() throws IOException {
        String tmp = inputFile.getName();
        String fn = tmp.endsWith(".txt") ? tmp.substring(0, tmp.length() - 4) : tmp;

        if (fn.endsWith(".cn") || fn.endsWith(".xcn") || fn.endsWith(".snp")) {
            return new Parser(1, 2);
        } else if (fn.endsWith(".igv")) {
            return getIGVParser();
        } else {
            throw new RuntimeException("Unrecognized copy number extension: " + fn);
        }
    }

    // THe IGV formats allow user overrides of column numbers.

    Parser getIGVParser() throws IOException {

        int chrColumn = 0;
        int startColumn = 1;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(inputFile));

            String nextLine = "";
            while ((nextLine = reader.readLine()) != null) {
                if (!nextLine.startsWith("#")) {
                    break;
                } else if (nextLine.startsWith("#columns")) {
                    String[] tokens = nextLine.split("\\s+");
                    if (tokens.length > 1) {
                        for (int i = 1; i < tokens.length; i++) {
                            String[] kv = tokens[i].split("=");
                            if (kv.length == 2) {
                                if (kv[0].toLowerCase().equals("chr")) {
                                    int c = Integer.parseInt(kv[1]);
                                    if (c < 1) {
                                        throw new RuntimeException("Error parsing column line: " + nextLine + ". Column numbers must be > 0");
                                    } else {
                                        chrColumn = c - 1;
                                    }
                                } else if (kv[0].toLowerCase().equals("start")) {
                                    int c = Integer.parseInt(kv[1]);
                                    if (c < 1) {
                                        throw new RuntimeException("Error parsing column line: " + nextLine + ". Column numbers must be > 0");
                                    } else {
                                        startColumn = c - 1;
                                    }
                                }
                            }
                        }
                    }
                    break;

                }
            }
            return new Parser(chrColumn, startColumn);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }


    }
}
