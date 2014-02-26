package com.stanfy.enroscar.net;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.FileInputStream;
import java.net.URL;

import static org.mockito.Mockito.*;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for ContentUriConnection.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ContentUriConnectionTest {

  /** Test content type. */
  private final String contentType = "test/type";
  /** Test URI. */
  private final Uri uri = Uri.parse("content://authority/path");

  /** Test connection. */
  private ContentUriConnection connection;
  /** Mock content resolver. */
  private ContentResolver mockResolver;

  @Before
  public void init() throws Exception {
    EnroscarConnectionsEngine.config().treatFileScheme(false).setup(Robolectric.application);

    mockResolver = mock(ContentResolver.class);
    connection = new ContentUriConnection(new URL(uri.toString()), mockResolver);

    doReturn(contentType).when(mockResolver).getType(any(Uri.class));
    FileInputStream mockStream = mock(FileInputStream.class);
    AssetFileDescriptor mockD = mock(AssetFileDescriptor.class);
    doReturn(mockStream).when(mockD).createInputStream();
    doReturn(mockD).when(mockResolver).openAssetFileDescriptor(any(Uri.class), any(String.class));
  }

  @Test
  public void shouldOpenAssetFileDescriptor() throws Exception {
    assertThat(connection.getInputStream()).isNotNull();
    verify(mockResolver).openAssetFileDescriptor(uri, "r");
  }

  @Test
  public void shouldOpenInReadWriteModeForDoOutput() throws Exception {
    connection.setDoOutput(true);
    connection.connect();
    verify(mockResolver).openAssetFileDescriptor(uri, "rw");
  }

  @Test
  public void shouldBeAbleToOpenInWriteOnlyMode() throws Exception {
    connection.setDoOutput(true);
    connection.setDoInput(false);
    connection.connect();
    verify(mockResolver).openAssetFileDescriptor(uri, "w");
  }

  @Test
  public void shouldProvideContentType() throws Exception {
    connection.connect();
    assertThat(connection.getContentType()).isEqualTo(contentType);
    assertThat(connection.getHeaderField("Content-Type")).isEqualTo(contentType);
    verify(mockResolver).getType(uri);
  }

}
