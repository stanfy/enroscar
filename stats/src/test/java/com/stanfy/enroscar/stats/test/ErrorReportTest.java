package com.stanfy.enroscar.stats.test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;

import com.stanfy.enroscar.stats.StatsManager;

/**
 * Tests for error reports messages.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
@RunWith(Runner.class)
public class ErrorReportTest {

  /** Example stack trace. */
  private static final String EXAMPLE_ST =
        "android.database.sqlite.SQLiteConstraintException: error code 19: constraint failed\n"
        + "at android.database.sqlite.SQLiteStatement.native_execute(Native Method)\n"
        + "at android.database.sqlite.SQLiteStatement.execute(SQLiteStatement.java:61)\n"
        + "at android.database.sqlite.SQLiteDatabase.insertWithOnConflict(SQLiteDatabase.java:1582)\n"
        + "at android.database.sqlite.SQLiteDatabase.insert(SQLiteDatabase.java:1426)\n"
        + "at com.stanfy.content.AppContentProvider.insert(AppContentProvider.java:109)\n"
        + "at android.content.ContentProvider$Transport.insert(ContentProvider.java:198)\n"
        + "at android.content.ContentResolver.insert(ContentResolver.java:604)\n"
        + "at com.stanfy.images.DefaultImagesDAO.createCachedImage(DefaultImagesDAO.java:77)\n"
        + "at com.stanfy.images.ImagesManager$ImageLoader.call(ImagesManager.java:736)\n"
        + "at com.stanfy.images.ImagesManager$ImageLoader.call(ImagesManager.java:1)\n"
        + "at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:306)\n"
        + "at java.util.concurrent.FutureTask.run(FutureTask.java:138)\n"
        + "at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1088)\n"
        + "at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:581)\n"
        + "at java.lang.Thread.run(Thread.java:1019)";


  @Test
  public void testStackTraceTrim() {
    final String trimmed = StatsManager.trimStackTrace(EXAMPLE_ST);
    assertThat("Stack trace is not trimmed", trimmed.length(), lessThan(EXAMPLE_ST.length()));
    assertThat("Stack trace has \\n", -1, equalTo(trimmed.indexOf('\n')));
    assertThat("Stack trace has \\r", -1, equalTo(trimmed.indexOf('\r')));
    assertThat("Stack trace has \\t", -1, equalTo(trimmed.indexOf('\t')));
    assertThat("Exception class has not been cut", 0, equalTo(trimmed.indexOf("SQLiteConstraintException")));
  }

  @Test
  public void testReadError() {
    final int maxLen = 255;
    final String msg = new StatsManager() {
      @Override
      public void onStartScreen(final Activity activity) {
      }
      @Override
      public void onLeaveScreen(final Activity activity) {
      }
      @Override
      public void onComeToScreen(final Activity activity) {
      }
      @Override
      public void event(final String tag, final Map<String, String> params) {
      }
      @Override
      public void error(final String tag, final Throwable e) {
      }
      @Override
      public void onStartSession(final Activity activity) {
      }
      @Override
      public void onEndSession(final Activity activity) {
      }
    }
    .readException(new Throwable(), maxLen);
    assertThat(msg.length(), lessThanOrEqualTo(maxLen));
    assertThat(msg, startsWith("Throwable"));
  }

}
