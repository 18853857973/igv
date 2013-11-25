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

package org.broad.igv.feature.tribble;

import org.broad.igv.AbstractHeadlessTest;
import org.broad.igv.track.TribbleFeatureSource;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.util.TestUtils;
import org.broad.igv.variant.vcf.VCFVariant;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author jacob
 * @date 2013-Jun-21
 */
public class VCFWrapperCodecTest extends AbstractHeadlessTest {

    /**
     * It is apparently a matter of some contention whether having a missing
     * field within a comma-separated list of fields should be legal VCF.
     * We've decided that IGV will accept these files, but since we use the picard VCF codec (and they don't want to)
     * we need to work around it.
     * @throws Exception
     */
    @Test
    public void testMissingFieldInCommaSeparated() throws Exception{

        String filePath = TestUtils.DATA_DIR + "vcf/missingFields.vcf";
        TestUtils.createIndex(filePath);

        TribbleFeatureSource src = TribbleFeatureSource.getFeatureSource(new ResourceLocator(filePath), genome);

        Iterator iter = src.getFeatures("chr2", 3321000, 13346000);

        int count = 0;
        boolean found = false;

        while(iter.hasNext()){
            VCFVariant vcfVariant = (VCFVariant) iter.next();
            assertNotNull(vcfVariant);

            if(vcfVariant.getStart() == 3796932){
                assertEquals(9, vcfVariant.getSampleNames().size());
                found = true;
            }

            count++;
        }

        assertEquals(26, count);
        assertTrue("Feature of interest not found", found);


    }
}
