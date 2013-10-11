package com.stanfy.enroscar.views.test;

import com.stanfy.enroscar.views.LoadableImageView;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/**
 * Tests for LoadableImageView.
 */
@RunWith(RobolectricTestRunner.class)
public class LoadableImageViewTest {

  /** Image view. */
  private LoadableImageView imageView;

  @Before
  public void create() {
    imageView = new LoadableImageView(Robolectric.application);
  }

}
