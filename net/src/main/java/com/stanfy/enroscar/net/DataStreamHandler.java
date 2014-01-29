package com.stanfy.enroscar.net;

import com.stanfy.enroscar.io.IoUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stream handler for {@code data} scheme.
 */
class DataStreamHandler extends URLStreamHandler {

  /** Scheme. */
  public static final String PROTOCOL = "data";

  /** Data URI pattern. */
  private static final Pattern URI_PATTERN = Pattern.compile("^data:([^;]+);(\\w+),(.+)$");

  @Override
  protected URLConnection openConnection(final URL u) throws IOException {
    return new DataUrlConnection(u);
  }

  @Override
  protected void parseURL(final URL url, final String spec, final int start, final int end) {
    Matcher m = URI_PATTERN.matcher(spec);
    if (!m.matches()) {
      throw new RuntimeException("Cannot parse url " + spec);
    }
    String contentType = m.group(1);
    String encoding = m.group(2);
    String data = m.group(3);
    setURL(url, PROTOCOL, PROTOCOL, 0, contentType, encoding, data, null, null);
  }

  private static class DataUrlConnection extends URLConnection {

    /** Data in bytes. */
    private byte[] data;

    public DataUrlConnection(final URL url) {
      super(url);
    }

    @Override
    public void connect() throws IOException {
      data = url.getPath().getBytes(IoUtils.US_ASCII);
      connected = true;
    }

    @Override
    public String getHeaderField(final String key) {
      if ("Content-Type".equals(key)) {
        return url.getAuthority();
      }
      if ("Content-Encoding".equals(key)) {
        return url.getUserInfo();
      }
      return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      if (!connected) {
        connect();
      }
      return new ByteArrayInputStream(data);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      throw new UnsupportedOperationException("Can't write to data scheme URL");
    }
  }

}
