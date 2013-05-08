package org.broad.igv.feature.tribble;




import org.broad.tribble.Feature;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Interface to replace the Tribble equivalent.  Returns plain "Iterator" rather than "CloseableIterator".  A wrapper
 * implementation is provided that handles closing the tribble resource.
 *
 * NOTE:  IGV classes should use this interface, rather than the Tribble FeatureReader.
 *
 * @author jrobinso
 *         Date: 5/8/13
 *         Time: 10:55 AM
 */
public interface IGVFeatureReader {

    public Iterator<Feature> query(final String chr, final int start, final int end) throws IOException;

    public Iterator<Feature> iterator() throws IOException;

    public List<String> getSequenceNames();

    public Object getHeader();
}
