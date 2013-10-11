package com.stanfy.enroscar.images;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.stanfy.enroscar.beans.BeansManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for ImageRequest.
 */
public class ImageRequestRealTest extends BaseTest {

  /** Test bitmap. */
  private Bitmap testBitmap;

  /** URL. */
  private String testBitmapUrl;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    @SuppressWarnings("ConstantConditions")
    Resources res = getContext().getResources();
    int id = android.R.drawable.ic_delete;
    testBitmap = BitmapFactory.decodeResource(res, id);
    testBitmapUrl = "android.resource://android/" + id;
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
    ImageRequest request = new ImageRequest(imagesManager, testBitmapUrl, 1);
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

    ImageResult result = new ImageRequest(imagesManager, url, 1).readImage();
    checkBitmap(result);
    assertThat(result.getType()).isSameAs(ImageSourceType.NETWORK);

    ImageResult result2 = new ImageRequest(imagesManager, url, 1).readImage();
    checkBitmap(result2);
    assertThat(result2.getType()).isSameAs(ImageSourceType.DISK);

    server.shutdown();
  }

  public void testStoreToDiskSmallRatio() throws IOException {
    MockWebServer server = prepareServer();
    String url = server.getUrl("/image").toString();

    float smallRatio = (float)testBitmap.getWidth() / getContext().getResources().getDisplayMetrics().widthPixels;
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
}
