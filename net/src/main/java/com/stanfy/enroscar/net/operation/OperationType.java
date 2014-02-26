package com.stanfy.enroscar.net.operation;

/**
 * Possible operation types.
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
public final class OperationType {

  /** Simple post (parameters as an HTML form data). */
  public static final int SIMPLE_POST = 0;

  /** Upload post. Multi-part should be used for HTTP. */
  public static final int UPLOAD_POST = 1;

  /** Simple get. */
  public static final int SIMPLE_GET = 2;

  /** Post with payload (uses binary content as a payload). */
  public static final int PAYLOAD_POST = 3;

  private OperationType() { }

}
