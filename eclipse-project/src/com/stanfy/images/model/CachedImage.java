package com.stanfy.images.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.stanfy.views.R;

/**
 * Cached image.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class CachedImage {

  /** Cached image contract. */
  public static class Contract implements BaseColumns {
    protected Contract() { }

    /** Table name. */
    public static final String TABLE_NAME = "cached_image";

    /** Columns. */
    public static final String URL = "url",
                               PATH = "path",
                               LOADED = "loaded",
                               REMOVED = "removed",
                               TIMESTAMP = "ts",
                               USAGE_TIMESTAMP = "usage_ts";

    /** Column indexes. */
    public static final int INDEX_ID = 0, INDEX_URL = 1, INDEX_PATH = 2, INDEX_LOADED = 3, INDEX_TIMESTAMP = 4, INDEX_USAGE_TIMESTAMP = 5;

    /** Column names. */
    public static final String[] COLUMNS = new String[] {
      _ID,
      URL,
      PATH,
      LOADED,
      TIMESTAMP,
      USAGE_TIMESTAMP
    };

    /** DDL script identifier. */
    public static final int DDL_SCRIPT = R.string.sql_ddl_cached_image;

    /**
     * @param cursor cursor
     * @param instance instance
     * @return instance with values
     */
    public static CachedImage fromCursor(final Cursor cursor, final CachedImage instance) {
      instance.setId(cursor.getLong(INDEX_ID));
      instance.setUrl(cursor.getString(INDEX_URL));
      instance.setPath(cursor.getString(INDEX_PATH));
      instance.setLoaded(cursor.getInt(INDEX_LOADED) == 1);
      instance.setTimestamp(cursor.getLong(INDEX_TIMESTAMP));
      instance.setUsageTimestamp(cursor.getLong(INDEX_USAGE_TIMESTAMP));
      return instance;
    }

    /**
     * @param image image instance
     * @param cv content values instance
     * @return filled content values
     */
    public static ContentValues toContentValues(final CachedImage image, final ContentValues cv) {
      cv.put(_ID, image.getId());
      cv.put(URL, image.getUrl());
      cv.put(PATH, image.getPath());
      cv.put(LOADED, image.isLoaded() ? 1 : 0);
      cv.put(TIMESTAMP, image.getTimestamp());
      cv.put(USAGE_TIMESTAMP, image.getUsageTimestamp());
      return cv;
    }

  }

  /** Identifier. */
  private long id;
  /** Image URL. */
  private String url;
  /** Local image path. */
  private String path;
  /** Loaded flag. */
  private boolean loaded = false;
  /** Timestamp. */
  private long timestamp;
  /** Usage timestamp. */
  private long usageTimestamp;

  public CachedImage() { /* nothing */ }

  public CachedImage(final long id) {
    this.id = id;
  }

  /** @return the url */
  public String getUrl() { return url; }
  /** @param url the url to set */
  public void setUrl(final String url) { this.url = url; }

  /** @return the path */
  public String getPath() { return path; }
  /** @param path the path to the cache file */
  public void setPath(final String path) { this.path = path; }

  /** @return the id */
  public long getId() { return id; }
  /** @param id the id to set */
  public void setId(final long id) { this.id = id; }

  /** @return the loaded */
  public boolean isLoaded() { return loaded; }
  /** @param loaded the loaded to set */
  public void setLoaded(final boolean loaded) { this.loaded = loaded; }

  /** @param timestamp the timestamp to set */
  public void setTimestamp(final long timestamp) { this.timestamp = timestamp; }
  /** @return the timestamp */
  public long getTimestamp() { return timestamp; }

  /** @param usageTimestamp the usageTimestamp to set */
  public void setUsageTimestamp(final long usageTimestamp) { this.usageTimestamp = usageTimestamp; }
  /** @return the usageTimestamp */
  public long getUsageTimestamp() { return usageTimestamp; }

  public void set(final CachedImage image) {
    setLoaded(image.isLoaded());
    setPath(image.getPath());
    setUrl(image.getUrl());
    setTimestamp(image.getTimestamp());
    setUsageTimestamp(image.getUsageTimestamp());
  }

}
