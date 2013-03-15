package com.stanfy.enroscar.rest.request.net.multipart;


/**
 * Provides setters and getters for the basic Part properties.
 *
 * @author Michael Becke
 */
public abstract class PartBase extends Part {

  /** Name of the file part. */
  private String name;

  /** Content type of the file part. */
  private String contentType;

  /** Content encoding of the file part. */
  private String charSet;

  /** The transfer encoding. */
  private String transferEncoding;

  /**
   * Constructor.
   *
   * @param name The name of the part
   * @param contentType The content type, or <code>null</code>
   * @param charSet The character encoding, or <code>null</code>
   * @param transferEncoding The transfer encoding, or <code>null</code>
   */
  public PartBase(final String name, final String contentType, final String charSet, final String transferEncoding) {

    if (name == null) {
      throw new IllegalArgumentException("Name must not be null");
    }
    this.name = name;
    this.contentType = contentType;
    this.charSet = charSet;
    this.transferEncoding = transferEncoding;
  }

  /**
   * Returns the name.
   * @return The name.
   * @see Part#getName()
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Returns the content type of this part.
   * @return String The name.
   */
  @Override
  public String getContentType() {
    return this.contentType;
  }

  /**
   * Return the character encoding of this part.
   * @return String The name.
   */
  @Override
  public String getCharSet() {
    return this.charSet;
  }

  /**
   * Returns the transfer encoding of this part.
   * @return String The name.
   */
  @Override
  public String getTransferEncoding() {
    return transferEncoding;
  }

  /**
   * Sets the character encoding.
   *
   * @param charSet the character encoding, or <code>null</code> to exclude the character
   * encoding header
   */
  public void setCharSet(final String charSet) {
    this.charSet = charSet;
  }

  /**
   * Sets the content type.
   *
   * @param contentType the content type, or <code>null</code> to exclude the content type header
   */
  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }

  /**
   * Sets the part name.
   *
   * @param name
   */
  public void setName(final String name) {
    if (name == null) {
      throw new IllegalArgumentException("Name must not be null");
    }
    this.name = name;
  }

  /**
   * Sets the transfer encoding.
   *
   * @param transferEncoding the transfer encoding, or <code>null</code> to exclude the
   * transfer encoding header
   */
  public void setTransferEncoding(final String transferEncoding) {
    this.transferEncoding = transferEncoding;
  }

}
