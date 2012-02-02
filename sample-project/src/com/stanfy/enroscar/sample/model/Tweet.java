package com.stanfy.enroscar.sample.model;

import java.io.Serializable;

import com.stanfy.content.UniqueObject;

/**
 * Represent a post in twitter timeline.
 */
public class Tweet implements Serializable, UniqueObject {

  /** serialVersionUID. */
  private static final long serialVersionUID = 3655414093691267892L;

  /** Identifier. */
  private long id;

  /** Message text. */
  private String text;

  /** @return the text */
  public String getText() { return text; }

  @Override
  public long getId() { return id; }

}
