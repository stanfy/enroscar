package com.stanfy.serverapi.request;

/** List of available operations. */
public interface Operation {

  /** No operation code. */
  int NOP = -1;

  /** @return operation code */
  int getCode();
  /** @return operation type */
  int getType();
  /** @return URL part */
  String getUrlPart();

}
