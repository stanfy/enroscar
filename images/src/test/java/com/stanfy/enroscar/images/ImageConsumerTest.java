package com.stanfy.enroscar.images;

import android.content.Context;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Tests for ImageConsumer.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ImageConsumerTest {

  @Test
  public void consumerShouldReturnRequiredSizeRespectingScreenDimensions() {
    ImageConsumer consumer = new Consumer(0, 0);
    Context context = Robolectric.application;
    assertThat(consumer.getRequiredWidth()).isEqualTo(context.getResources().getDisplayMetrics().widthPixels);
    assertThat(consumer.getRequiredHeight()).isEqualTo(context.getResources().getDisplayMetrics().heightPixels);
  }

}

