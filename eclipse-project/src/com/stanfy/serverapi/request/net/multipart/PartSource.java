package com.stanfy.serverapi.request.net.multipart;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface for providing access to data when posting MultiPart messages.
 *
 * @see FilePart
 *
 * @author <a href="mailto:becke@u.washington.edu">Michael Becke</a>
 *
 * @since 2.0
 */
public interface PartSource {

    /**
     * Gets the number of bytes contained in this source.
     *
     * @return a value >= 0
     */
    long getLength();

    /**
     * Gets the name of the file this source represents.
     *
     * @return the fileName used for posting a MultiPart file part
     */
    String getFileName();

    /**
     * Gets a new InputStream for reading this source.  This method can be
     * called more than once and should therefore return a new stream every
     * time.
     *
     * @return a new InputStream
     *
     * @throws IOException if an error occurs when creating the InputStream
     */
    InputStream createInputStream() throws IOException;

}
