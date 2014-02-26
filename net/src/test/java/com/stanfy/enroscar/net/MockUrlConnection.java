package com.stanfy.enroscar.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class MockUrlConnection extends URLConnection {

  /** Properties map. */
  private final Map<String, String> properties = new LinkedHashMap<>();

  private static URL mockUrl() {
    try {
      return new URL("http://example.com");
    } catch (MalformedURLException e) {
      throw new AssertionError(e);
    }
  }

  public MockUrlConnection() {
    this(mockUrl());
  }

  public MockUrlConnection(final URL url) {
    super(url);
  }

  @Override
  public void connect() throws IOException {
    connected = true;
  }

  @Override
  public void addRequestProperty(final String field, final String newValue) {
    super.addRequestProperty(field, newValue);
    properties.put(field, newValue);
  }

  @Override
  public String getRequestProperty(final String field) {
    return properties.get(field);
  }
}
