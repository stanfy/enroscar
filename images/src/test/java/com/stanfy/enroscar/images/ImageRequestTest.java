package com.stanfy.enroscar.images;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.stanfy.enroscar.io.IoUtils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.InputStream;

import static com.stanfy.enroscar.images.TestUtils.TEST_BITMAP_SIZE;
import static com.stanfy.enroscar.images.TestUtils.putCachedContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.android.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Tests for ImageRequest.
 */
@Config(emulateSdk = 18)
public class ImageRequestTest extends AbstractImagesTest {

  private String defaultUrl;

  @Override
  public void startServer() throws IOException {
    super.startServer();
    server.enqueue(new MockResponse().setResponseCode(200).setBody(""));
    defaultUrl = server.getUrl("/").toString();
  }

  @Test
  public void testConstructor() {
    Resources res = Robolectric.application.getResources();

    ImageRequest request = new ImageRequest(manager, "any", -1);
    assertThat(request.getRequiredWidth()).isEqualTo(0);
    assertThat(request.getRequiredHeight()).isEqualTo(0);
    assertThat(request.hasAllowedSize()).isFalse();

    request = new ImageRequest(manager, "any", 2);
    assertThat(request.getRequiredWidth()).isEqualTo(res.getDisplayMetrics().widthPixels * 2);
    assertThat(request.getRequiredHeight()).isEqualTo(res.getDisplayMetrics().heightPixels * 2);
    assertThat(request.hasAllowedSize()).isTrue();
  }

  @Test
  public void shouldNotThrowIfUndefinedRequiredSize() throws IOException {
    ImageRequest request = new ImageRequest(manager, defaultUrl, -1);
    ImageResult result = request.readImage();
    assertThat(result).isNotNull();
    assertThat(result.getBitmap()).isNotNull();
  }

  @Test
  public void shouldNotThrowIfAllowedSizedSet() throws IOException {
    ImageRequest request = new ImageRequest(manager, defaultUrl, 1);
    ImageResult result = request.readImage();
    assertThat(result).isNotNull();
  }

  @Test
  public void shouldDecodeImage() throws IOException {
    ImageRequest request = new ImageRequest(manager, defaultUrl, -1);
    request.setRequiredHeight(TEST_BITMAP_SIZE);
    request.setRequiredWidth(TEST_BITMAP_SIZE);
    ImageResult result = request.readImage();
    assertThat(result).isNotNull();
    assertThat(result.getBitmap()).isNotNull();
    assertThat(result.getType()).isSameAs(ImageSourceType.NETWORK);
    assertThat(result.getBitmap()).hasWidth(TEST_BITMAP_SIZE);
    assertThat(result.getBitmap()).hasHeight(TEST_BITMAP_SIZE);
  }

  @Test
  public void shouldDecodeBase64Image() throws IOException {
    ImageRequest request = new ImageRequest(manager, "data:image/gif;base64,"
        + Base64.encodeToString("fake image".getBytes(IoUtils.US_ASCII_NAME), Base64.DEFAULT), -1);
    request.setRequiredHeight(TEST_BITMAP_SIZE);
    request.setRequiredWidth(TEST_BITMAP_SIZE);
    ImageResult result = request.readImage();
    assertThat(result).isNotNull();
    assertThat(result.getBitmap()).isNotNull();
    assertThat(result.getType()).isSameAs(ImageSourceType.NETWORK);
    assertThat(result.getBitmap()).hasWidth(TEST_BITMAP_SIZE);
    assertThat(result.getBitmap()).hasHeight(TEST_BITMAP_SIZE);
  }

  @Test
  public void shouldIndicateWhenImageIsLoadedFromCache() throws Exception {
    putCachedContent(manager, defaultUrl);
    ImageRequest request = new ImageRequest(manager, defaultUrl, 1);
    assertThat(request.readImage().getType()).isSameAs(ImageSourceType.DISK);
  }

  @Test
  public void shouldScaleBitmaps() throws IOException {
    ImageRequest request = new ImageRequest(manager, defaultUrl, 1);
    request.setRequiredHeight(TEST_BITMAP_SIZE / 3);
    request.setRequiredWidth(TEST_BITMAP_SIZE / 3);
    ImageResult result = request.readImage();
    assertThat(result.getBitmap()).hasWidth(request.getRequiredWidth());
    assertThat(result.getBitmap()).hasHeight(request.getRequiredHeight());
  }

  @Test
  public void shouldRecoverFromOom() throws IOException {
    ImageRequest request = new ImageRequest(manager, defaultUrl, 1);
    request = spy(request);
    doThrow(OutOfMemoryError.class).when(request).doStreamDecode(any(InputStream.class), any(BitmapFactory.Options.class));
    try {
      request.readImage();
      fail("Wrapped OOM expected");
    } catch (IOException e) {
      assertThat(e).hasMessage("out of memory for " + request.getKey());
    }
  }

  @Test
  public void shoutDecodeWithSpecifiedFormat() throws IOException {
    ImageRequest request = new ImageRequest(manager, defaultUrl, 1);
    request.setFormat(Bitmap.Config.ALPHA_8);
    request = spy(request);
    request.readImage();
    verify(request, times(2)).doStreamDecode(any(InputStream.class), argThat(new BaseMatcher<BitmapFactory.Options>() {

      @Override
      public boolean matches(final Object o) {
        if (!(o instanceof BitmapFactory.Options)) {
          return false;
        }
        BitmapFactory.Options opts = (BitmapFactory.Options) o;
        return opts.inPreferredConfig == Bitmap.Config.ALPHA_8;
      }

      @Override
      public void describeTo(final Description description) {
        description.appendText("Bitmap format is not respected");
      }
    }));
  }

  @Test
  public void storeToDiskShouldNotDecodeImageIfAllowedSizeIsNotSpecified() throws IOException {
    ImageRequest request = spy(new ImageRequest(manager, defaultUrl, -1));
    request.storeToDisk();
    verify(request).newUrlConnection();
    verify(request, times(0)).doStreamDecode(any(InputStream.class), any(BitmapFactory.Options.class));
  }

  @Test
  public void storeToDiskShouldDoNothingIfImageIsOnTheDisk() throws Exception {
    putCachedContent(manager, defaultUrl);
    ImageRequest request = spy(new ImageRequest(manager, defaultUrl, -1));
    request.storeToDisk();
    verify(request, times(0)).newUrlConnection();
  }

  @Test
  public void storeToDiskShouldDecodeImageIfAllowedSizeIsSet() throws Exception {
    final float small = 0.05f;
    final ImageRequest request = spy(new ImageRequest(manager, defaultUrl, small));
    request.storeToDisk();
    verify(request).newUrlConnection();
    verify(request, times(2)).doStreamDecode(any(InputStream.class), any(BitmapFactory.Options.class));

    int factor = ImagesManager.calculateSampleFactor(TEST_BITMAP_SIZE, TEST_BITMAP_SIZE,
        request.getRequiredWidth(), request.getRequiredHeight());
    final int expectedSize = TEST_BITMAP_SIZE / factor;
    verify(request).writeBitmapToDisk(argThat(new BaseMatcher<Bitmap>() {
      private Bitmap bitmap;

      @Override
      public boolean matches(Object o) {
        this.bitmap = (Bitmap) o;
        return bitmap.getWidth() == expectedSize && bitmap.getHeight() == expectedSize;
      }

      @Override
      public void describeTo(final Description description) {
        description.appendText("Bitmap has bad size: " + bitmap.getWidth() + "x" + bitmap.getHeight() + "; expected: "
            + expectedSize + "x" + expectedSize);
      }
    }));
  }

  @Test
  public void storeToDiskShouldNotFullyDecodeIfScaleFactorIsOne() throws Exception {
    final ImageRequest request = spy(new ImageRequest(manager, defaultUrl, 1));
    request.storeToDisk();
    verify(request).newUrlConnection();
    verify(request, times(1)).doStreamDecode(any(InputStream.class), any(BitmapFactory.Options.class));
    verify(request, times(0)).writeBitmapToDisk(any(Bitmap.class));
  }

  @Test
  @Ignore("Caching does not work")
  public void shouldCacheImages() throws IOException {
    final ImageRequest request = spy(new ImageRequest(manager, defaultUrl, 1));
    doAnswer(new Answer() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        InputStream input = (InputStream) invocation.getArguments()[0];
        BitmapFactory.Options options = (BitmapFactory.Options) invocation.getArguments()[1];
        if (!options.inJustDecodeBounds) {
          IoUtils.consumeStream(input, manager.getBuffersPool());
        }
        return invocation.callRealMethod();
      }
    }).when(request).doStreamDecode(any(InputStream.class), any(BitmapFactory.Options.class));

    ImageResult netResult = request.readImage();
    assertThat(netResult.getType()).isSameAs(ImageSourceType.NETWORK);
    ImageResult diskResult = request.readImage();
    assertThat(diskResult.getType()).isSameAs(ImageSourceType.DISK);
  }

}
