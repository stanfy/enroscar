package com.stanfy.serverapi.request;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 *
 */
public class OperationDescriptor implements Operation {

  /** Operation code. */
  private final int code;
  /** Operation type. */
  private final int type;
  /** Part of the URL. */
  private final String urlPart;

  /**
   * @param code operation code (unique for application)
   * @param type operation type (associations with GET, POST)
   * @param urlPart part of the URL
   */
  public OperationDescriptor(final int code, final int type, final String urlPart) {
    super();
    this.code = code;
    this.type = type;
    this.urlPart = urlPart;
  }

  @Override
  public int getCode() { return code; }
  @Override
  public int getType() { return type; }
  @Override
  public String getUrlPart() { return urlPart; }

}
