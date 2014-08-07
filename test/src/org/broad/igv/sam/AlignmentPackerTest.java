/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
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

package org.broad.igv.sam;

import htsjdk.samtools.util.CloseableIterator;
import org.broad.igv.AbstractHeadlessTest;
import org.broad.igv.sam.reader.AlignmentReader;
import org.broad.igv.sam.reader.AlignmentReaderFactory;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.util.TestUtils;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author jrobinso
 */
public class AlignmentPackerTest extends AbstractHeadlessTest {

    String path = TestUtils.LARGE_DATA_DIR + "HG00171.hg18.bam";
    String chr = "chr1";
    int start = 151666494;
    int end = start + 1000;
    boolean contained = false;


    private AlignmentInterval getAlignmentInterval() throws Exception {
        ResourceLocator rl = new ResourceLocator(path);
        AlignmentReader samReader = AlignmentReaderFactory.getReader(rl);
        CloseableIterator<Alignment> iter = samReader.query(chr, start, end, contained);
        List<Alignment> list = new ArrayList<Alignment>();
        while(iter.hasNext()){
            list.add(iter.next());
        }
        AlignmentInterval interval = new AlignmentInterval(chr, start, end, list, null, null, null);
        return interval;
    }

    /**
     * Test of packAlignments method, of class AlignmentPacker.
     */
    @Test
    public void testPackAlignments() throws Exception {

        ///////////////////////////
        /*
        boolean showDuplicates = false;
        int qualityThreshold = 0;
        int maxLevels = 1000;
        */
        AlignmentInterval interval = getAlignmentInterval();

        Map<String, List<Row>> result = (new AlignmentPacker()).packAlignments(interval, new AlignmentTrack.RenderOptions());
        assertEquals(1, result.size());
        for (List<Row> alignmentrows : result.values()) {
            for (Row alignmentrow : alignmentrows) {
                List<Alignment> alignments = alignmentrow.alignments;
                for (int ii = 1; ii < alignments.size(); ii++) {
                    assertTrue(alignments.get(ii).getAlignmentStart() > alignments.get(ii - 1).getAlignmentStart());
                    assertTrue(alignments.get(ii).getAlignmentStart() - alignments.get(ii - 1).getAlignmentEnd() >= AlignmentPacker.MIN_ALIGNMENT_SPACING);
                }
            }
        }


    }

    @Test
    public void testGroupAlignmentsPairOrientation() throws Exception {
        int expSize = 3; //AlignmentTrack.OrientationType.values().length;
        Map<String, List<Row>> result = tstGroupAlignments(AlignmentTrack.GroupOption.PAIR_ORIENTATION, expSize);
    }

    public Map<String, List<Row>> tstGroupAlignments(AlignmentTrack.GroupOption groupOption, int expSize) throws Exception {

        AlignmentTrack.RenderOptions renderOptions = new AlignmentTrack.RenderOptions();
        renderOptions.groupByOption = groupOption;

        AlignmentInterval interval = getAlignmentInterval();
        Map<String, List<Row>> result = (new AlignmentPacker()).packAlignments(interval, renderOptions);
        Set<String> names = result.keySet();
        //names.removeAll(Arrays.asList("", null));

        assertEquals(expSize, names.size());
        return result;

    }


}