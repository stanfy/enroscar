package com.stanfy.enroscar.images;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.io.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

import com.stanfy.enroscar.images.test.R;

/**
 * Tests for ImageRequest.
 */
public class ImageRequestAndroidTest extends BaseAndroidTest {

  /** Test bitmap. */
  private Bitmap testBitmap;

  /** URL. */
  private String testBitmapUrl;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    @SuppressWarnings("ConstantConditions")
    Resources res = getContext().getResources();
    int id = R.drawable.test_image;
    testBitmap = BitmapFactory.decodeResource(res, id);
    testBitmapUrl = "android.resource://com.stanfy.enroscar.images.test/" + id;
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    BeansManager.get(getContext()).destroy();
  }

  private void checkBitmap(final ImageResult result) {
    assertThat(result.getBitmap()).hasWidth(testBitmap.getWidth());
    assertThat(result.getBitmap()).hasHeight(testBitmap.getHeight());
  }

  public void testDecodeResource() throws Exception {
    ImageRequest request = new ImageRequest(imagesManager, testBitmapUrl, -1);
    ImageResult result = request.readImage();
    checkBitmap(result);
    assertThat(result.getType()).isSameAs(ImageSourceType.DISK);
  }

  private MockWebServer prepareServer() throws IOException{
    MockWebServer server = new MockWebServer();
    server.play();
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    testBitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
    server.enqueue(new MockResponse().setResponseCode(200).setBody(bytes.toByteArray()));
    return server;
  }

  public void testCaching() throws IOException {
    MockWebServer server = prepareServer();
    String url = server.getUrl("/image").toString();

    ImageResult result = new ImageRequest(imagesManager, url, -1).readImage();
    checkBitmap(result);
    assertThat(result.getType()).isSameAs(ImageSourceType.NETWORK);

    ImageResult result2 = new ImageRequest(imagesManager, url, -1).readImage();
    checkBitmap(result2);
    assertThat(result2.getType()).isSameAs(ImageSourceType.DISK);

    server.shutdown();
  }

  public void testStoreToDiskSmallRatio() throws IOException {
    MockWebServer server = prepareServer();
    String url = server.getUrl("/image").toString();

    //noinspection ConstantConditions
    float smallRatio = (float)testBitmap.getWidth()
        / getContext().getResources().getDisplayMetrics().widthPixels;
    ImageRequest request = new ImageRequest(imagesManager, url, smallRatio / 2);

    assertThat(imagesManager.isPresentOnDisk(url)).isFalse();
    request.storeToDisk();
    assertThat(imagesManager.isPresentOnDisk(url)).isTrue();

    server.shutdown();
  }

  public void testStoreToDiskBigRatio() throws IOException {
    MockWebServer server = prepareServer();
    String url = server.getUrl("/image").toString();

    ImageRequest request = new ImageRequest(imagesManager, url, 1);

    assertThat(imagesManager.isPresentOnDisk(url)).isFalse();
    request.storeToDisk();
    assertThat(imagesManager.isPresentOnDisk(url)).isTrue();

    server.shutdown();
  }

  public void testRealRemoteImage() throws IOException {
    MockWebServer server = new MockWebServer();
    server.play();

    final InputStream imageStream = getContext().getResources().getAssets().open("mustard.jpg");
    final byte[] imageBytes = new byte[imageStream.available()];
    assertThat(imageBytes.length).isNotZero();
    imageStream.read(imageBytes);
    IoUtils.closeQuietly(imageStream);

    server.enqueue(new MockResponse().setResponseCode(200).setBody(imageBytes));
    String url = server.getUrl("/image").toString();

    ImageRequest request = new ImageRequest(imagesManager, url, 1);
    ImageResult result = request.readImage();
    assertThat(result.getType()).isSameAs(ImageSourceType.NETWORK);

    final Bitmap resultBitmap = result.getBitmap();
    assertThat(resultBitmap).isNotNull();
  }

  public void testRealBase64Image() throws IOException {
    // 5x5 red dot
    final String base64 = "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GI"
        +"AXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==";
    final String url = "data:image/png;base64," + base64;
    final int size = 5;

    ImageRequest request = new ImageRequest(imagesManager, url, 1);

    ImageResult result = request.readImage();
    assertThat(result.getType()).isSameAs(ImageSourceType.NETWORK);

    final Bitmap resultBitmap = result.getBitmap();
    assertThat(resultBitmap).isNotNull();
    assertThat(resultBitmap).hasWidth(size);
    assertThat(resultBitmap).hasHeight(size);
    assertThat(resultBitmap.getPixel(1, 1)).isEqualTo(Color.RED); // :) red dot
  }
}
