package com.stanfy.enroscar.sample.model;

import java.io.Serializable;

import android.net.Uri;

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

  /** User. */
  private User user;

  /** Image URI, */
  private transient Uri profileImageUri;

  /** @return the text */
  public String getText() { return text; }
  public Uri getProfileImageUri() {
    if (profileImageUri == null && user.profileImageUrl != null) {
      profileImageUri = Uri.parse(user.profileImageUrl);
    }
    return profileImageUri;
  }

  @Override
  public long getId() { return id; }

}
