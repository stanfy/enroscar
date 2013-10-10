package com.stanfy.enroscar.images;

import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.images.cache.SupportLruImageMemoryCache;
import com.stanfy.enroscar.net.UrlConnectionBuilder;
import com.stanfy.enroscar.net.UrlConnectionBuilderFactory;
import com.stanfy.enroscar.net.cache.EnhancedResponseCache;

import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowBitmap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CacheRequest;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.fest.assertions.api.ANDROID.assertThat;

/**
 * Tests for ImageRequest.
 */
public class ImageRequestTest extends AbstractImagesTest {

  private static final int DEFAULT_SIZE = 100;

  private static final String URL = "http://example.com/image";

  @Override
  protected void configureBeansManager(final BeansManager.Editor editor) {
    super.configureBeansManager(editor);
    setupFakeStream(editor);
  }

  @Test
  public void testConstructor() {
    Resources res = Robolectric.application.getResources();

    ImageRequest request = new ImageRequest(manager, "any", -1);
    assertThat(request.maxAllowedWidth).isEqualTo(-1);
    assertThat(request.maxAllowedHeight).isEqualTo(-1);
    assertThat(request.hasAllowedSize()).isFalse();

    request = new ImageRequest(manager, "any", 2);
    assertThat(request.maxAllowedWidth).isEqualTo(res.getDisplayMetrics().widthPixels * 2);
    assertThat(request.maxAllowedHeight).isEqualTo(res.getDisplayMetrics().heightPixels * 2);
    assertThat(request.hasAllowedSize()).isTrue();
  }

  @Test
  public void shouldThrowIfUndefinedRequiredSize() throws IOException {
    try {
      ImageRequest request = new ImageRequest(manager, URL, -1);
      request.readImage();
      fail("Exception expected");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Absolutely undefined size");
    }
  }

  @Test
  public void shouldNotThrowIfAllowedSizedSet() throws IOException {
    ImageRequest request = new ImageRequest(manager, URL, 1);
    ImageResult result = request.readImage();
    assertThat(result).isNotNull();
  }

  @Test
  public void shouldDecodeImage() throws IOException {
    ImageRequest request = new ImageRequest(manager, URL, -1);
    request.setRequiredHeight(100);
    request.setRequiredWidth(100);
    ImageResult result = request.readImage();
    assertThat(result).isNotNull();
    assertThat(result.getBitmap()).isNotNull();
    assertThat(result.getType()).isSameAs(ImageResult.ResultType.NETWORK);
    assertThat(result.getBitmap()).hasWidth(100);
    assertThat(result.getBitmap()).hasHeight(100);
  }

  @Test
  public void shouldIndicateWhenImageIsLoadedFromCache() throws Exception {
    CacheRequest cacheRequest = manager.getImagesResponseCache().put(new URI(URL), fakeConnection(new URL(URL)));
    OutputStream out = cacheRequest.getBody();
    out.write(new byte[] {1});
    out.close();

    ImageRequest request = new ImageRequest(manager, URL, 1);
    assertThat(request.readImage().getType()).isSameAs(ImageResult.ResultType.CACHE);
  }

  @Test
  public void shouldScaleBitmaps() throws IOException {
    ImageRequest request = new ImageRequest(manager, URL, 1);
    request.setRequiredHeight(DEFAULT_SIZE / 2);
    request.setRequiredWidth(DEFAULT_SIZE / 2);
    ImageResult result = request.readImage();
    assertThat(result.getBitmap()).hasWidth(request.getRequiredWidth());
    assertThat(result.getBitmap()).hasHeight(request.getRequiredHeight());
  }

  private InputStream imageStream() {
    //return getClass().getResourceAsStream("/logo.png");
    return new ByteArrayInputStream(new byte[100000]);
  }

  private URLConnection fakeConnection(final URL url) {
    return new HttpURLConnection(url) {
      @Override
      public void connect() throws IOException {

      }

      @Override
      public String getRequestMethod() {
        return "GET";
      }

      @Override
      public InputStream getInputStream() throws IOException {
        return imageStream();
      }

      @Override
      public void disconnect() {

      }

      @Override
      public boolean usingProxy() {
        return false;
      }
    };
  }

  private void setupFakeStream(final BeansManager.Editor editor) {
    editor.put(ImagesManager.CONNECTION_BUILDER_FACTORY_NAME, new UrlConnectionBuilderFactory() {
      @Override
      public UrlConnectionBuilder newUrlConnectionBuilder() {
        return new UrlConnectionBuilder() {
          @Override
          protected URLConnection openConnection(URL url) throws IOException {
            return fakeConnection(url);
          }
        };
      }
    });
  }

}
