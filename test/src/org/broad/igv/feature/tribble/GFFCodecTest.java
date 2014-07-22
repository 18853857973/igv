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

package org.broad.igv.feature.tribble;

import org.broad.igv.AbstractHeadlessTest;
import org.broad.igv.feature.BasicFeature;
import org.broad.igv.track.FeatureSource;
import org.broad.igv.track.GFFFeatureSource;
import org.broad.igv.track.TribbleFeatureSource;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.util.TestUtils;
import htsjdk.tribble.Feature;
import org.junit.Test;

import java.util.Iterator;

import static junit.framework.Assert.assertEquals;

/**
 * User: jacob
 * Date: 2013-Mar-21
 */
public class GFFCodecTest extends AbstractHeadlessTest {

    /**
     * Make sure we parse the attributes to get the name of this feature
     * GTF has a bunch of different ones
     *
     * @throws Exception
     */
    @Test
    public void testGetNameGTF() throws Exception {
        String path = TestUtils.DATA_DIR + "gtf/transcript_id.gtf";
        String expName = "YAL069W";

        GFFFeatureSource src = new GFFFeatureSource(TribbleFeatureSource.getFeatureSource(new ResourceLocator(path), null));

        Iterator<Feature> iter = src.getFeatures("I", 0, Integer.MAX_VALUE);
        while (iter.hasNext()) {
            BasicFeature bf = (BasicFeature) iter.next();
            assertEquals(expName, bf.getName());
        }

    }

    /**
     * Insure we can parse a GFF file that includes a fasta section.
     *
     * @throws Exception
     */
    @Test
    public void testGFFWithFasta() throws Exception {

        String path = TestUtils.DATA_DIR + "gff/gffWithFasta.gff";
        final ResourceLocator locator = new ResourceLocator(path);

        TribbleFeatureSource tribbleFeatureSource = TribbleFeatureSource.getFeatureSource(locator, genome);
        FeatureSource source = new GFFFeatureSource(tribbleFeatureSource);

        int featureCount = 0;
        Iterator<Feature> iter = source.getFeatures("chr7", 0, Integer.MAX_VALUE);
        while (iter.hasNext()) {
            BasicFeature bf = (BasicFeature) iter.next();
            featureCount++;
        }
        assertEquals(2, featureCount);

    }
}
