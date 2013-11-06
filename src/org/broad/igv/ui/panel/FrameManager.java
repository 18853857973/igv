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

package org.broad.igv.ui.panel;

import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.Locus;
import org.broad.igv.feature.exome.ExomeReferenceFrame;
import org.broad.igv.lists.GeneList;
import org.broad.igv.track.FeatureTrack;
import org.broad.igv.track.RegionScoreType;
import org.broad.igv.track.Track;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.action.SearchCommand;
import org.broad.igv.ui.util.MessageUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author jrobinso
 * @date Sep 10, 2010
 */
public class FrameManager {

    private static List<ReferenceFrame> frames = new ArrayList();
    private static ReferenceFrame defaultFrame;
    private static boolean exomeMode = false;

    public static final String DEFAULT_FRAME_NAME = "genome";

    static {
        frames.add(getDefaultFrame());
    }

    public synchronized static ReferenceFrame getDefaultFrame() {
        if (defaultFrame == null) {
            defaultFrame = new ReferenceFrame(DEFAULT_FRAME_NAME);
        }
        return defaultFrame;
    }

    /**
     * Set exome mode.
     *
     * @param b
     * @param showTrackMenu
     * @return true if a change was made,
     *         false if not.
     */
    public static boolean setExomeMode(boolean b, boolean showTrackMenu) {
        if (b == exomeMode) return false;  // No change
        if (b) {
            return switchToExomeMode(showTrackMenu);
        } else {
            return switchToGenomeMode();
        }
    }


    public static boolean isExomeMode() {
        return exomeMode;
    }


    static FeatureTrack exomeTrack = null;

    private static boolean switchToExomeMode(boolean showTrackMenu) {

        Frame parent = IGV.hasInstance() ? IGV.getMainFrame() : null;
        List<FeatureTrack> featureTracks = IGV.getInstance().getFeatureTracks();
        if (featureTracks.size() == 1) {
            exomeTrack = featureTracks.get(0);
        } else {
            if (exomeTrack == null || showTrackMenu) {
                FeatureTrackSelectionDialog dlg = new FeatureTrackSelectionDialog(parent);
                dlg.setVisible(true);
                if (dlg.getIsCancelled()) return false;
                exomeTrack = dlg.getSelectedTrack();
            }
        }

        if (exomeTrack == null) return false;

        ExomeReferenceFrame exomeFrame = new ExomeReferenceFrame(defaultFrame, exomeTrack);

        Locus locus = new Locus(defaultFrame.getChrName(), (int) defaultFrame.getOrigin(), (int) defaultFrame.getEnd());
        exomeFrame.jumpTo(locus);
        defaultFrame = exomeFrame;
        frames.clear();
        frames.add(defaultFrame);
        exomeMode = true;
        return true;
    }

    private static boolean switchToGenomeMode() {
        ReferenceFrame refFrame = new ReferenceFrame(defaultFrame);

        Locus locus = new Locus(defaultFrame.getChrName(), (int) defaultFrame.getOrigin(), (int) defaultFrame.getEnd());
        refFrame.jumpTo(locus);
        defaultFrame = refFrame;
        frames.clear();
        frames.add(defaultFrame);
        exomeMode = false;
        return true;
    }


    public static List<ReferenceFrame> getFrames() {
        return frames;
    }

    public static void setFrames(List<ReferenceFrame> f) {
        frames = f;
    }

    public static boolean isGeneListMode() {
        return frames.size() > 1;
    }


    public static void setToDefaultFrame(String searchString) {
        frames.clear();
        if (searchString != null) {
            Locus locus = getLocus(searchString, 0);
            if (locus != null) {
                getDefaultFrame().jumpTo(locus);
            }
        }
        frames.add(getDefaultFrame());
        getDefaultFrame().recordHistory();
    }


    public static void resetFrames(GeneList gl) {

        frames.clear();

        if (gl == null) {
            frames.add(getDefaultFrame());
        } else {
            int flankingRegion = PreferenceManager.getInstance().getAsInt(PreferenceManager.FLANKING_REGION);
            List<String> lociNotFound = new ArrayList();
            List<String> loci = gl.getLoci();
            if (loci.size() == 1) {
                Locus locus = getLocus(loci.get(0), flankingRegion);
                if (locus == null) {
                    lociNotFound.add(loci.get(0));
                } else {
                    IGV.getInstance().getSession().setCurrentGeneList(null);
                    getDefaultFrame().jumpTo(locus.getChr(), locus.getStart(), locus.getEnd());
                }
            } else {
                for (String searchString : gl.getLoci()) {
                    Locus locus = getLocus(searchString, flankingRegion);
                    if (locus == null) {
                        lociNotFound.add(searchString);
                    } else {
                        ReferenceFrame referenceFrame = new ReferenceFrame(searchString);
                        referenceFrame.jumpTo(locus);
                        frames.add(referenceFrame);
                    }
                }
            }

            if (lociNotFound.size() > 1) {
                StringBuffer message = new StringBuffer();
                message.append("<html>The following loci could not be found in the currently loaded annotation sets: <br>");
                for (String s : lociNotFound) {
                    message.append(s + " ");
                }
                MessageUtils.showMessage(message.toString());

            }
        }
    }

    /**
     * @return The minimum scale among all active frames
     *         TODO -- track this with "rescale" events, rather than compute on the fly
     */
    public static double getMinimumScale() {
        double minScale = Double.MAX_VALUE;
        for (ReferenceFrame frame : frames) {
            minScale = Math.min(minScale, frame.getScale());
        }
        return minScale;
    }


    public static Locus getLocus(String name) {
        int flankingRegion = PreferenceManager.getInstance().getAsInt(PreferenceManager.FLANKING_REGION);
        return getLocus(name, flankingRegion);
    }

    /**
     * Runs a search for the specified string, and returns a locus
     * of the given region with additional space on each side
     *
     * @param searchString
     * @param flankingRegion
     * @return The found locus, null if not found
     */
    public static Locus getLocus(String searchString, int flankingRegion) {
        SearchCommand cmd = new SearchCommand(getDefaultFrame(), searchString);
        List<SearchCommand.SearchResult> results = cmd.runSearch(searchString);
        Locus locus = null;
        for (SearchCommand.SearchResult result : results) {
            if (result.getType() != SearchCommand.ResultType.ERROR) {

                int delta;
                if (flankingRegion < 0) {
                    delta = (-flankingRegion * (result.getEnd() - result.getStart())) / 100;

                } else {
                    delta = flankingRegion;
                }

                int start = result.getStart() - delta;
                //Don't allow flanking region to extend past origin
                //There are some circumstances in which we render before origin (e.g. soft-clips)
                //so we are conservative
                if (start < 0 && result.getStart() >= -1) {
                    start = 0;
                }
                locus = new Locus(
                        result.getChr(),
                        start,
                        result.getEnd() + delta);
                //We just take the first result
                break;
            }
        }
        return locus;
    }

    public static void removeFrame(ReferenceFrame frame) {
        frames.remove(frame);
    }


    public static void sortFrames(final Track t) {

        Collections.sort(frames, new Comparator<ReferenceFrame>() {
            @Override
            public int compare(ReferenceFrame o1, ReferenceFrame o2) {
                float s1 = t.getRegionScore(o1.getChromosome().getName(), (int) o1.getOrigin(), (int) o1.getEnd(),
                        o1.getZoom(), RegionScoreType.SCORE, o1.getName());
                float s2  = t.getRegionScore(o2.getChromosome().getName(), (int) o2.getOrigin(), (int) o2.getEnd(),
                        o2.getZoom(), RegionScoreType.SCORE, o2.getName());
                return (s1 == s2 ? 0 : (s1 > s2) ? -1 : 1);
            }
        });

    }

}

