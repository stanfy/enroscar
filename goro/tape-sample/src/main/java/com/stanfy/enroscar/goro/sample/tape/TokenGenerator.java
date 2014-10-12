package com.stanfy.enroscar.goro.sample.tape;

import java.util.UUID;

public class TokenGenerator {

  public String nextToken() {
    return UUID.randomUUID().toString();
  }

}
