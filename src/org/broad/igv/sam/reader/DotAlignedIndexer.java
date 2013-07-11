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

package org.broad.igv.sam.reader;

import org.broad.igv.ui.util.IndexCreatorDialog;

import javax.swing.*;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * @author jrobinso
 * @since Dec 6, 2009
 */
public class DotAlignedIndexer extends AlignmentIndexer {

    int baseOffset = 1;

    public DotAlignedIndexer(File samFile, JProgressBar progressBar, IndexCreatorDialog.SamIndexWorker worker) {
        super(samFile, progressBar, worker);
        if (samFile.getName().endsWith(".bedz") || samFile.getName().endsWith(".bed")) {
            baseOffset = 0;
        }
    }

    int getAlignmentStart(String[] fields) throws NumberFormatException {
        int position = Integer.parseInt(fields[1]) - baseOffset;
        return position;
    }

    int getAlignmentLength(String[] fields) throws NumberFormatException {
        return Integer.parseInt(fields[2]) - Integer.parseInt(fields[1]) + 1;
    }

    String getChromosome(String[] fields) {
        String chr = fields[0];
        return chr;
    }

    @Override
    boolean isMapped(String[] fields) {
        return true;
    }
}
