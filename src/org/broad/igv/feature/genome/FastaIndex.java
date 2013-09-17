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

package org.broad.igv.feature.genome;

import org.apache.log4j.Logger;
import org.broad.igv.Globals;
import org.broad.igv.util.ParsingUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Representation of a fasta (.fai) index.  This is a modified version of a similar class in Picard, but extended
 * to handle loading by URLs.
 *
 * @auther jrobinso
 * @since 2011 Aug 7
 */
public class FastaIndex {

    static Logger log = Logger.getLogger(FastaIndex.class);

    /**
     * Store the entries.  Use a LinkedHashMap for consistent iteration in insertion order.
     */
    private final LinkedHashMap<String, FastaSequenceIndexEntry> sequenceEntries = new LinkedHashMap<String, FastaSequenceIndexEntry>();

    public FastaIndex(String indexPath) throws IOException {
        parseIndexFile(indexPath);
    }

    public Set<String> getSequenceNames() {
        return sequenceEntries.keySet();
    }

    public FastaSequenceIndexEntry getIndexEntry(String name) {
        return sequenceEntries.get(name);
    }


    public int getSequenceSize(String name) {
        FastaSequenceIndexEntry entry = sequenceEntries.get(name);
        return entry == null ? -1 : (int) entry.getSize();
    }

    /**
     * Parse the contents of an index file
     * <p/>
     * Example index file
     * <p/>
     * sequenceName size locationInFile basesPerLine bytesPerLine
     * chr01p	6220112	8	50	51
     * chr02q	8059593	6344531	50	51
     * chr03q	5803340	14565324	50	51
     *
     * @param indexFile File to parse.
     * @throws java.io.FileNotFoundException Thrown if file could not be opened.
     */
    private void parseIndexFile(String indexFile) throws IOException {

        BufferedReader reader = null;
        try {
            reader = ParsingUtils.openBufferedReader(indexFile);

            String nextLine;
            while ((nextLine = reader.readLine()) != null) {

                // Tokenize and validate the index line.
                String[] tokens =  Globals.singleTabMultiSpacePattern.split(nextLine);
                int nTokens =  tokens.length;
                if (nTokens != 5) {
                    throw new RuntimeException("Error.  Unexpected number of tokens parsing: " + indexFile);
                }
                // Parse the index line.
                String contig = tokens[0];
                contig = GenomeImporter.SEQUENCE_NAME_SPLITTER.split(contig, 2)[0];
                long size = Long.parseLong(tokens[1]);
                long location = Long.parseLong(tokens[2]);
                int basesPerLine = Integer.parseInt(tokens[3]);
                int bytesPerLine = Integer.parseInt(tokens[4]);

                // Build sequence structure
                add(new FastaSequenceIndexEntry(contig, location, size, basesPerLine, bytesPerLine));
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void add(FastaSequenceIndexEntry indexEntry) {
        final FastaSequenceIndexEntry ret = sequenceEntries.put(indexEntry.getContig(), indexEntry);
        if (ret != null) {
            throw new RuntimeException("Contig '" + indexEntry.getContig() + "' already exists in fasta index.");
        }
    }


    /**
     * Hold an individual entry in a fasta sequence index file.
     */
    static class FastaSequenceIndexEntry {
        private String contig;
        private long position;
        private long size;
        private int basesPerLine;
        private int bytesPerLine;


        /**
         * Create a new entry with the given parameters.
         *
         * @param contig       Contig this entry represents.
         * @param position     Location (byte coordinate) in the fasta file.
         * @param size         The number of bases in the contig.
         * @param basesPerLine How many bases are on each line.
         * @param bytesPerLine How many bytes are on each line (includes newline characters).
         */
        public FastaSequenceIndexEntry(String contig,
                                       long position,
                                       long size,
                                       int basesPerLine,
                                       int bytesPerLine) {
            this.contig = contig;
            this.position = position;
            this.size = size;
            this.basesPerLine = basesPerLine;
            this.bytesPerLine = bytesPerLine;
        }

        /**
         * @return The contig.
         */
        public String getContig() {
            return contig;
        }

        /**
         * @return seek position within the fasta, in bytes
         */
        public long getPosition() {
            return position;
        }

        /**
         * @return size of the contig bases, in bytes.
         */
        public long getSize() {
            return size;
        }

        /**
         * @return Number of bases in a given fasta line
         */
        public int getBasesPerLine() {
            return basesPerLine;
        }

        /**
         * @return Number of bytes (bases + whitespace) in a line.
         */
        public int getBytesPerLine() {
            return bytesPerLine;
        }


        /**
         * @return A string representation of the contig line.
         */
        public String toString() {
            return String.format("contig %s; position %d; size %d; basesPerLine %d; bytesPerLine %d", contig,
                    position,
                    size,
                    basesPerLine,
                    bytesPerLine);
        }

    }
}