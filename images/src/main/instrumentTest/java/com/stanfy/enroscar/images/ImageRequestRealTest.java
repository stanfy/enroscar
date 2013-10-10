package com.stanfy.enroscar.images;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.test.AndroidTestCase;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine;

import java.io.IOException;
import java.net.URL;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.ANDROID.assertThat;

/**
 * Tests for ImageRequest.
 */
public class ImageRequestRealTest extends AndroidTestCase {

  /** Image URL. */
  private URL imageUrl;

  /** Images manager. */
  private ImagesManager imagesManager;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    EnroscarConnectionsEngine.config().install(getContext());
    BeansManager.Editor editor = BeansManager.get(getContext()).edit();
    BeanSetup.setup(editor);
    editor.commit();

    imageUrl = getClass().getResource("/logo.png");
    assertThat(imageUrl).isNotNull();

    imagesManager = BeansManager.get(getContext()).getContainer().getBean(ImagesManager.class);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    BeansManager.get(getContext()).destroy();
  }

  public void testSimpleDecodeImage() throws IOException {
    ImageRequest request = new ImageRequest(imagesManager, imageUrl.toString(), -1);
    Drawable d = request.readImage();
    assertThat(d).isInstanceOf(BitmapDrawable.class);
  }

}
