package com.stanfy.enroscar.net;

import android.os.Build.VERSION_CODES;

import com.stanfy.enroscar.io.IoUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.ResponseCache;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for EnroscarConnectionsEngine.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = VERSION_CODES.JELLY_BEAN_MR2)
public class EnroscarConnectionsEngineTest {

  @Before
  public void init() {
    EnroscarConnectionsEngine.config().setup(Robolectric.application);
  }

  @Test
  public void shouldBeAbleToSetupCacheSwitcher() throws Exception {
    assertThat(ResponseCache.getDefault()).isNotNull();
  }

  @Test
  public void shouldBeAbleToSetupContentUrlStreamHandler() throws Exception {
    assertThat(new URL("content://authority/data").openConnection()).isNotNull();
  }

  @Test
  public void dataSchemeShouldBeTreated() throws Exception {
    URLConnection connection = new URL("data:text/html;enc,hello").openConnection();
    assertThat(connection).isNotNull();
    assertThat(connection.getContentType()).isEqualTo("text/html");
    assertThat(connection.getContentEncoding()).isEqualTo("enc");
    assertThat(IoUtils.streamToString(connection.getInputStream(), null)).isEqualTo("hello");
  }

  @Test
  public void dataSchemeShouldAllowOptionalEncoding() throws Exception {
    URLConnection connection = new URL("data:text/xml,<hello/>").openConnection();
    assertThat(connection).isNotNull();
    assertThat(connection.getContentType()).isEqualTo("text/xml");
    assertThat(connection.getContentEncoding()).isNull();
    assertThat(IoUtils.streamToString(connection.getInputStream(), null)).isEqualTo("<hello/>");
  }

  @Test
  public void dataSchemeShouldAllowContentTypeParameters() throws Exception {
    URLConnection connection = new URL("data:text/xml;encoding=utf-8;base64,aGVsbG8gd29ybGQ=")
        .openConnection();
    assertThat(connection).isNotNull();
    assertThat(connection.getContentType()).isEqualTo("text/xml;encoding=utf-8");
    assertThat(connection.getContentEncoding()).isNull();
    assertThat(IoUtils.streamToString(connection.getInputStream(), null))
        .isEqualTo("hello world");
  }

  @Test
  public void dataSchemeShouldAllowGenericText() throws Exception {
    URLConnection connection = new URL("data:A%20brief%20note").openConnection();
    assertThat(connection).isNotNull();
    assertThat(IoUtils.streamToString(connection.getInputStream(), null)).isEqualTo("A brief note");
    assertThat(connection.getContentType()).isNull();
    assertThat(connection.getContentEncoding()).isNull();

    connection = new URL("data:" + URLEncoder.encode("Вася Пупкін", "UTF-8")).openConnection();
    assertThat(IoUtils.streamToString(connection.getInputStream(), null)).isEqualTo("Вася Пупкін");
  }

  @Test
  public void dataSchemeShouldAllowImageData() throws Exception {
    URLConnection connection = new URL("data:image/gif;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAAAMAAw"
        + "AAAC8IyPqcvt3wCcDkiLc7C0qwyGHhSWpjQu5yqmCYsapyuvUUlvONmOZtfzgFz"
        + "ByTB10QgxOR0TqBQejhRNzOfkVJ+5YiUqrXF5Y5lKh/DeuNcP5yLWGsEbtLiOSp"
        + "a/TPg7JpJHxyendzWTBfX0cxOnKPjgBzi4diinWGdkF8kjdfnycQZXZeYGejmJl"
        + "ZeGl9i2icVqaNVailT6F5iJ90m6mvuTS4OK05M0vDk0Q4XUtwvKOzrcd3iq9uis"
        + "F81M1OIcR7lEewwcLp7tuNNkM3uNna3F2JQFo97Vriy/Xl4/f1cf5VWzXyym7PH"
        + "hhx4dbgYKAAA7").openConnection();
    assertThat(connection).isNotNull();
    assertThat(IoUtils.streamToString(connection.getInputStream(), null)).isNotEmpty();
    assertThat(connection.getContentType()).isEqualTo("image/gif");
  }

  @Test
  public void dataSchemeShouldDecodeBase64() throws Exception {
    URLConnection connection = new URL("data:text/plain;base64,aGVsbG8gd29ybGQ=").openConnection();
    assertThat(connection.getContentEncoding()).isNull();
    assertThat(IoUtils.streamToString(connection.getInputStream(), null)).isEqualTo("hello world");
  }

}
