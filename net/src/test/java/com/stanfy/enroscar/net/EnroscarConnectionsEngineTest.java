package com.stanfy.enroscar.net;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.net.ResponseCache;
import java.net.URL;
import java.net.URLConnection;

import android.os.Build.VERSION_CODES;

import com.stanfy.enroscar.io.IoUtils;

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
  public void shouldBeAbleToSetupDataSchemeHandling() throws Exception {
    // very custom data
    URLConnection connection = new URL("data:text/html;plain,hello").openConnection();
    assertThat(connection).isNotNull();
    assertThat(IoUtils.streamToString(connection.getInputStream(), null)).isEqualTo("hello");

    // and something from RFC
    connection = new URL("data:image/gif;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAAAMAAw"
        + "AAAC8IyPqcvt3wCcDkiLc7C0qwyGHhSWpjQu5yqmCYsapyuvUUlvONmOZtfzgFz"
        + "ByTB10QgxOR0TqBQejhRNzOfkVJ+5YiUqrXF5Y5lKh/DeuNcP5yLWGsEbtLiOSp"
        + "a/TPg7JpJHxyendzWTBfX0cxOnKPjgBzi4diinWGdkF8kjdfnycQZXZeYGejmJl"
        + "ZeGl9i2icVqaNVailT6F5iJ90m6mvuTS4OK05M0vDk0Q4XUtwvKOzrcd3iq9uis"
        + "F81M1OIcR7lEewwcLp7tuNNkM3uNna3F2JQFo97Vriy/Xl4/f1cf5VWzXyym7PH"
        + "hhx4dbgYKAAA7").openConnection();
    assertThat(connection).isNotNull();
    assertThat(IoUtils.streamToString(connection.getInputStream(), null)).isNotEmpty();
  }

}
