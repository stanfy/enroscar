package com.stanfy.enroscar.images;

import android.content.Context;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ImageConsumer.
 */
@Config(emulateSdk = 18)
public class ImageConsumerTest extends AbstractImagesTest {

  @Test
  public void consumerShouldReturnRequiredSizeRespectingScreenDimensions() {
    ImageConsumer consumer = new Consumer(0, 0);
    Context context = Robolectric.application;
    assertThat(consumer.getRequiredWidth()).isEqualTo(context.getResources().getDisplayMetrics().widthPixels);
    assertThat(consumer.getRequiredHeight()).isEqualTo(context.getResources().getDisplayMetrics().heightPixels);
  }

}

