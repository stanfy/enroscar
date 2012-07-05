package com.stanfy.serverapi.request.net.multipart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.util.EncodingUtils;

import android.util.Log;

import com.stanfy.DebugFlags;

/**
 * This class implements a part of a Multipart post object that
 * consists of a file.
 *
 * @author <a href="mailto:mattalbright@yahoo.com">Matthew Albright</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:becke@u.washington.edu">Michael Becke</a>
 * @author <a href="mailto:mdiggory@latte.harvard.edu">Mark Diggory</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 *
 * @since 2.0
 *
 */
public class FilePart extends PartBase {

  /** Logging tag. */
  private static final String TAG = "FilePart";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_IO;

  /** Default content encoding of file attachments. */
  public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

  /** Default charset of file attachments. */
  public static final String DEFAULT_CHARSET = "ISO-8859-1";

  /** Default transfer encoding of file attachments. */
  public static final String DEFAULT_TRANSFER_ENCODING = "binary";

  /** Attachment's file name. */
  protected static final String FILE_NAME = "; filename=";

  /** Attachment's file name as a byte array. */
  private static final byte[] FILE_NAME_BYTES = EncodingUtils.getAsciiBytes(FILE_NAME);

  /** Source of the file part. */
  private PartSource source;

  /**
   * FilePart Constructor.
   *
   * @param name the name for this part
   * @param partSource the source for this part
   * @param contentType the content type for this part, if <code>null</code> the
   * {@link #DEFAULT_CONTENT_TYPE default} is used
   * @param charset the charset encoding for this part, if <code>null</code> the
   * {@link #DEFAULT_CHARSET default} is used
   */
  public FilePart(final String name, final PartSource partSource, final String contentType, final String charset) {

    super(
        name,
        contentType == null ? DEFAULT_CONTENT_TYPE : contentType,
            charset == null ? "ISO-8859-1" : charset,
                DEFAULT_TRANSFER_ENCODING
    );

    if (partSource == null) {
      throw new IllegalArgumentException("Source may not be null");
    }
    this.source = partSource;
  }

  /**
   * FilePart Constructor.
   *
   * @param name the name for this part
   * @param partSource the source for this part
   */
  public FilePart(final String name, final PartSource partSource) {
    this(name, partSource, null, null);
  }

  /**
   * FilePart Constructor.
   *
   * @param name the name of the file part
   * @param file the file to post
   *
   * @throws FileNotFoundException if the <i>file</i> is not a normal
   * file or if it is not readable.
   */
  public FilePart(final String name, final File file)
  throws FileNotFoundException {
    this(name, new FilePartSource(file), null, null);
  }

  /**
   * FilePart Constructor.
   *
   * @param name the name of the file part
   * @param file the file to post
   * @param contentType the content type for this part, if <code>null</code> the
   * {@link #DEFAULT_CONTENT_TYPE default} is used
   * @param charset the charset encoding for this part, if <code>null</code> the
   * {@link #DEFAULT_CHARSET default} is used
   *
   * @throws FileNotFoundException if the <i>file</i> is not a normal
   * file or if it is not readable.
   */
  public FilePart(final String name, final File file, final String contentType, final String charset)
  throws FileNotFoundException {
    this(name, new FilePartSource(file), contentType, charset);
  }

  /**
   * FilePart Constructor.
   *
   * @param name the name of the file part
   * @param fileName the file name
   * @param file the file to post
   *
   * @throws FileNotFoundException if the <i>file</i> is not a normal
   * file or if it is not readable.
   */
  public FilePart(final String name, final String fileName, final File file)
  throws FileNotFoundException {
    this(name, new FilePartSource(fileName, file), null, null);
  }

  /**
   * FilePart Constructor.
   *
   * @param name the name of the file part
   * @param fileName the file name
   * @param file the file to post
   * @param contentType the content type for this part, if <code>null</code> the
   * {@link #DEFAULT_CONTENT_TYPE default} is used
   * @param charset the charset encoding for this part, if <code>null</code> the
   * {@link #DEFAULT_CHARSET default} is used
   *
   * @throws FileNotFoundException if the <i>file</i> is not a normal
   * file or if it is not readable.
   */
  public FilePart(final String name, final String fileName, final File file, final String contentType, final String charset)
  throws FileNotFoundException {
    this(name, new FilePartSource(fileName, file), contentType, charset);
  }

  /**
   * Write the disposition header to the output stream.
   * @param out The output stream
   * @throws IOException If an IO problem occurs
   * @see Part#sendDispositionHeader(OutputStream)
   */
  @Override
  protected void sendDispositionHeader(final OutputStream out) throws IOException {
    if (DEBUG) { Log.v("FilePart", "enter sendDispositionHeader(OutputStream out)"); }
    super.sendDispositionHeader(out);
    final String filename = this.source.getFileName();
    if (filename != null) {
      out.write(FILE_NAME_BYTES);
      out.write(QUOTE_BYTES);
      out.write(EncodingUtils.getAsciiBytes(filename));
      out.write(QUOTE_BYTES);
    }
  }

  /**
   * Write the data in "source" to the specified stream.
   * @param out The output stream.
   * @throws IOException if an IO problem occurs.
   * @see Part#sendData(OutputStream)
   */
  @Override
  protected void sendData(final OutputStream out) throws IOException {
    if (DEBUG) { Log.v(TAG, "enter sendData(OutputStream out)"); }
    if (lengthOfData() == 0) {

      // this file contains no data, so there is nothing to send.
      // we don't want to create a zero length buffer as this will
      // cause an infinite loop when reading.
      if (DEBUG) { Log.d(TAG, "No data to send."); }
      return;
    }

    final int size = 4096;
    final byte[] tmp = new byte[size];
    final InputStream instream = source.createInputStream();
    try {
      int len;
      while ((len = instream.read(tmp)) >= 0) {
        out.write(tmp, 0, len);
      }
    } finally {
      // we're done with the stream, close it
      instream.close();
    }
  }

  /**
   * Returns the source of the file part.
   *
   * @return The source.
   */
  protected PartSource getSource() {
    if (DEBUG) { Log.v(TAG, "enter getSource()"); }
    return this.source;
  }

  /**
   * Return the length of the data.
   * @return The length.
   * @see Part#lengthOfData()
   */
  @Override
  protected long lengthOfData() {
    if (DEBUG) { Log.v(TAG, "enter lengthOfData()"); }
    return source.getLength();
  }

}
