package com.stanfy.serverapi.request.net.multipart;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A PartSource that reads from a File.
 *
 * @author <a href="mailto:becke@u.washington.edu">Michael Becke</a>
 * @author <a href="mailto:mdiggory@latte.harvard.edu">Mark Diggory</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 *
 * @since 2.0
 */
public class FilePartSource implements PartSource {

  /** File part file. */
  private File file = null;

  /** File part file name. */
  private String fileName = null;

  /**
   * Constructor for FilePartSource.
   *
   * @param file the FilePart source File.
   *
   * @throws FileNotFoundException if the file does not exist or
   * cannot be read
   */
  public FilePartSource(final File file) throws FileNotFoundException {
    this.file = file;
    if (file != null) {
      if (!file.isFile()) {
        throw new FileNotFoundException("File is not a normal file.");
      }
      if (!file.canRead()) {
        throw new FileNotFoundException("File is not readable.");
      }
      this.fileName = file.getName();
    }
  }

  /**
   * Constructor for FilePartSource.
   *
   * @param fileName the file name of the FilePart
   * @param file the source File for the FilePart
   *
   * @throws FileNotFoundException if the file does not exist or
   * cannot be read
   */
  public FilePartSource(final String fileName, final File file)
  throws FileNotFoundException {
    this(file);
    if (fileName != null) {
      this.fileName = fileName;
    }
  }

  /**
   * Return the length of the file.
   * @return the length of the file
   * @see PartSource#getLength()
   */
  @Override
  public long getLength() {
    if (this.file != null) {
      return this.file.length();
    } else {
      return 0;
    }
  }

  /**
   * Return the current filename.
   * @return the filename.
   * @see PartSource#getFileName()
   */
  @Override
  public String getFileName() {
    return (fileName == null) ? "noname" : fileName;
  }

  /**
   * Return a new {@link FileInputStream} for the current filename.
   * @return the new input stream.
   * @throws IOException If an IO problem occurs.
   * @see PartSource#createInputStream()
   */
  @Override
  public InputStream createInputStream() throws IOException {
    if (this.file != null) {
      return new FileInputStream(this.file);
    } else {
      return new ByteArrayInputStream(new byte[] {});
    }
  }

}
