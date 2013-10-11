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

package org.broad.igv.tools.motiffinder;

import org.broad.igv.batch.CommandExecutor;
import org.broad.igv.feature.Locus;
import org.broad.igv.track.FeatureTrack;
import org.broad.igv.track.Track;
import org.broad.igv.ui.AbstractHeadedTest;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.IGVMenuBar;
import org.broad.igv.ui.panel.FrameManager;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.util.TestUtils;
import org.broad.tribble.Feature;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author jacob
 * @date 2013-Oct-09
 */
public class MotifFinderPluginHeadedTest extends AbstractHeadedTest {

    private static final String EGFR_begseq ="GCCCCCCGCACGGTGTGAGCGCCCGACGCGGCCGAGGCGGCCGGAGTCCCGAG";

    @Test
    public void testBasicSearch() throws Exception{
        List<Track> tracks = MotifFinderPlugin.addTracksForPatterns(new String[]{EGFR_begseq}, new String[]{"pos"}, new String[]{"neg"});
        tstSearchEGFR(tracks);
    }

    @Test
    public void testBatchCommand_Error() throws Exception{
        CommandExecutor cmdexec = new CommandExecutor();
        String cmd = "org.broad.igv.tools.motiffinder.MotifFinderPlugin blarg " + EGFR_begseq;
        String resp = cmdexec.execute(cmd);
        assertTrue("Expected ERROR, was " + resp, resp.contains("ERROR"));

        assertEquals(2, IGV.getInstance().getVisibleTrackCount());
    }

    @Test
    public void testBatchCommand() throws Exception{
        CommandExecutor cmdexec = new CommandExecutor();
        String cmd = "org.broad.igv.tools.motiffinder.MotifFinderPlugin find " + EGFR_begseq;
        String resp = cmdexec.execute(cmd);
        assertTrue("Expected OK, was " + resp, resp.contains("OK"));

        List<Track> finderTracks = new ArrayList<Track>(2);
        //Slight weakness here, we require that the tracks be in order positive/negative,
        //but don't check that
        String seqStub = EGFR_begseq.substring(0, 20);
        for(FeatureTrack ft: IGV.getInstance().getFeatureTracks()){
            if(ft.getName().contains(seqStub)){
                finderTracks.add(ft);
            }
        }
        tstSearchEGFR(finderTracks);
    }

    /**
     * Test searching EGFR.
     * @param PosNegFeatureTracks length-2 list of tracks, positive and negative strand motif finders
     */
    private void tstSearchEGFR(List<Track> PosNegFeatureTracks){
        assertEquals(2, PosNegFeatureTracks.size());


        IGV.getInstance().goToLocus("EGFR");
        FeatureTrack posTrack = (FeatureTrack) PosNegFeatureTracks.get(0);
        FeatureTrack negTrack = (FeatureTrack) PosNegFeatureTracks.get(1);
        Feature expFeature = new Locus("chr7", 55054248, 55054301);

        ReferenceFrame frame = FrameManager.getDefaultFrame();
        List<Feature> posFeats = posTrack.getFeatures(frame.getChrName(), (int) frame.getOrigin(), (int) frame.getEnd());

        assertEquals(1, posFeats.size());
        TestUtils.assertFeaturesEqual(expFeature, posFeats.get(0));

        List<Feature> negFeats = negTrack.getFeatures(frame.getChrName(), (int) frame.getOrigin(), (int) frame.getEnd());

        assertEquals(0, negFeats.size());
    }

    @Test
    public void testToolsMenuEntryExists() throws Exception{
        IGVMenuBar bar = IGVMenuBar.getInstance();
        boolean foundFinder = false;
        for(Component c: bar.getComponents()){
            String name = c.getName();
            if("Tools".equals(name)){
                JMenu menu = (JMenu) c;
                for(Component toolItem: menu.getMenuComponents()){
                    if(!(toolItem instanceof JMenuItem)){
                        continue;
                    }
                    String toolName = ((JMenuItem) toolItem).getText();
                    if(toolName == null) continue;
                    toolName = toolName.toLowerCase();
                    if(toolName.contains("motif")){
                        foundFinder = true;
                    }
                }
            }
        }
        assertTrue("Find Motif tool entry not found", foundFinder);
    }
}
