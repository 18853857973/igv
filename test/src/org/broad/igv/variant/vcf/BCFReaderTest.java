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

package org.broad.igv.variant.vcf;

import org.broad.igv.AbstractHeadlessTest;
import org.broad.igv.track.FeatureSource;
import org.broad.igv.track.TribbleFeatureSource;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.util.TestUtils;
import org.broad.tribble.Feature;
import org.broad.tribble.readers.PositionalBufferedStream;
import org.broadinstitute.variant.bcf2.BCF2Codec;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author jacob
 * @date 2013-Jun-14
 */
public class BCFReaderTest extends AbstractHeadlessTest {


    /**
     * Just test that we load a BCF file without crashing
     *
     * @throws Exception
     */
    @Test
    public void loadBCF() throws Exception {
        String path = TestUtils.DATA_DIR + "bcf/ex2.bcf";
        FeatureSource source = TribbleFeatureSource.getFeatureSource(new ResourceLocator(path), genome);
        Iterator<Feature> features = source.getFeatures("chr20", 14000, 1300000);
        int count = 0;

        while (features.hasNext()) {
            features.next();
            count++;
        }

        assertTrue("No features read", count > 0);
    }

    /**
     * Compare a BCF and VCF file
     *
     * @throws Exception
     */
    @Test
    public void compareBCFtoVCF() throws Exception {
        String BCF2path = TestUtils.DATA_DIR + "bcf/ex2.bcf";
        FeatureSource BCF2source = TribbleFeatureSource.getFeatureSource(new ResourceLocator(BCF2path), genome);
        Iterator<Feature> BCF2features = BCF2source.getFeatures("chr20", 14000, 1300000);
        List<VCFVariant> BCF2List = new ArrayList<VCFVariant>();

        String VCFpath = TestUtils.DATA_DIR + "vcf/ex2.vcf";
        TestUtils.createIndex(VCFpath);
        FeatureSource VCFsource = TribbleFeatureSource.getFeatureSource(new ResourceLocator(VCFpath), genome);
        Iterator<Feature> VCFfeatures = VCFsource.getFeatures("chr20", 14000, 1300000);
        List<VCFVariant> VCFList = new ArrayList<VCFVariant>();

        while (BCF2features.hasNext()) {
            VCFVariant bcfV = (VCFVariant) BCF2features.next();
            VCFVariant vcfV = (VCFVariant) VCFfeatures.next();

            assertEquals(vcfV.getAlleleFraction(), bcfV.getAlleleFraction(), 1e-4f);
            assertEquals(vcfV.getType(), bcfV.getType());

            BCF2List.add(bcfV);
            VCFList.add(vcfV);
        }

        TestUtils.assertFeatureListsEqual(VCFList.iterator(), BCF2List.iterator());
    }

    //Quick method for checking if a bcf file has the magic header
    @Ignore
    //@Test
    public void rawTestFile() throws Exception {
        String path = "/path/to/myfile.bcf";
        PositionalBufferedStream ps = new PositionalBufferedStream(new FileInputStream(path));

        BCF2Codec codec = new BCF2Codec();
        codec.readHeader(ps);

    }


}
