package com.stanfy.serverapi.request;

/** Available operation. */
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
