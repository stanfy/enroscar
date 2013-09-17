package com.stanfy.enroscar.views.test;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import com.stanfy.enroscar.views.LoadableImageView;

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

  @Test
  public void isUseSamplisgShouldBeTrueByDefault() {
    assertThat(imageView.isUseSampling()).isTrue();
  }

}
