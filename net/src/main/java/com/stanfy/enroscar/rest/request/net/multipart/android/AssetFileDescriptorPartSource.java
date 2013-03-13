package com.stanfy.enroscar.rest.request.net.multipart.android;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetFileDescriptor;

import com.stanfy.enroscar.rest.request.net.multipart.PartSource;

/**
 * {@link PartSource} based on {@link AssetFileDescriptor}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class AssetFileDescriptorPartSource implements PartSource {

  /** File descriptor. */
  private final AssetFileDescriptor fd;
  /** Name. */
  private final String name;

  public AssetFileDescriptorPartSource(final String name, final AssetFileDescriptor fd) {
    this.fd = fd;
    this.name = name;
  }

  @Override
  public long getLength() { return fd.getLength(); }
  @Override
  public String getFileName() { return name; }
  @Override
  public InputStream createInputStream() throws IOException { return fd.createInputStream(); }

}
