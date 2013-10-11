package com.stanfy.enroscar.images;

import android.graphics.Bitmap;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.images.cache.SupportLruImageMemoryCache;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.robolectric.Robolectric;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.fest.assertions.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link ImagesManager}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ImagesManagerTest extends AbstractImagesTest {

  @Test
  public void getFromMemCacheShouldCheckConsumerSize() {
    final int size = 50;

    ImagesManager imagesManager = BeansManager.get(Robolectric.application).getContainer().getBean(ImagesManager.class);
    SupportLruImageMemoryCache memCache = BeansManager.get(Robolectric.application).getContainer().getBean(SupportLruImageMemoryCache.class);

    memCache.putElement("http://123.com", Bitmap.createBitmap(size - 1, size + 1, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(size, size))).isNotNull();

    memCache.putElement("http://123.com", Bitmap.createBitmap(size / 2, size + 1, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(size, size))).isNull();

    memCache.putElement("http://123.com", Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(size, size))).isNull();

    memCache.putElement("http://123.com", Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(0, 0))).isNotNull();

    memCache.putElement("http://123.com", Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(0, size))).isNotNull();

    memCache.putElement("http://123.com", Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888));
    assertThat(imagesManager.getMemCached("http://123.com", new Consumer(0, size))).isNull();
  }

  @Test
  public void calculateSampleFactorShouldRespectDynamicSize() {
    //CHECKSTYLE:OFF
    assertThat(ImagesManager.calculateSampleFactor(100, 100, 50, 0)).isEqualTo(2);
    assertThat(ImagesManager.calculateSampleFactor(100, 100, 0, 25)).isEqualTo(4);
    //CHECKSTYLE:ON
  }

  @Test
  public void calculateSampleFactorShouldRespectSmallInput() {
    //CHECKSTYLE:OFF
    assertThat(ImagesManager.calculateSampleFactor(50, 50, 100, 100)).isEqualTo(1);
    assertThat(ImagesManager.calculateSampleFactor(50, 50, 50, 100)).isEqualTo(1);
    assertThat(ImagesManager.calculateSampleFactor(50, 50, 100, 50)).isEqualTo(1);
    assertThat(ImagesManager.calculateSampleFactor(50, 50, 50, 50)).isEqualTo(1);
    assertThat(ImagesManager.calculateSampleFactor(50, 50, 20, 20)).isEqualTo(2);
    //CHECKSTYLE:ON
  }

  @Test
  public void isPresentInFileCacheShouldCheckUriScheme() {
    assertThat(manager.isPresentOnDisk("http://bla/bla")).isFalse();
    assertThat(manager.isPresentOnDisk("file://ahahaha")).isTrue();
    assertThat(manager.isPresentOnDisk("android.resource://ahahaha")).isTrue();
    assertThat(manager.isPresentOnDisk("content://ahahaha")).isTrue();
  }

  @Test
  public void requestsBuilderShouldCallEnsureImages() {
    manager = spy(manager);
    doNothing().when(manager).ensureImages(anyListOf(ImageRequest.class), any(Executor.class));

    manager.load().add("1").add("2", 1).scaleToScreenSize().add("3").scaleToScreenSize(2).add("4").startLoading();

    verify(manager).ensureImages(argThat(new BaseMatcher<List<ImageRequest>>() {
      @Override
      public boolean matches(final Object o) {
        @SuppressWarnings("unchecked") List<ImageRequest> requests = (List<ImageRequest>) o;
        assertThat(requests).hasSize(4);
        assertThat(requests.get(0).hasAllowedSize()).isFalse();
        assertThat(requests.get(1).hasAllowedSize()).isTrue();
        assertThat(requests.get(2).getRequiredWidth()).isEqualTo(requests.get(1).getRequiredWidth());
        assertThat(requests.get(3).getRequiredWidth()).isEqualTo(requests.get(1).getRequiredWidth() * 2);
        return true;
      }

      @Override
      public void describeTo(final Description description) {
        description.appendText("bad requests built");
      }
    }), isNull(Executor.class));
  }

  @Test
  public void ensureImagesShouldDelegateToRequestAndIgnoreErrors() throws IOException {
    ImageRequest request = spy(new ImageRequest(manager, "1", 1));
    doThrow(IOException.class).when(request).storeToDisk();
    manager.ensureImages(Collections.singletonList(request), null);
    verify(request).storeToDisk();
  }

}
