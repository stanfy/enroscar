package com.stanfy.images.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.stanfy.views.R;

/**
 * Cached image.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class CachedImage implements BaseColumns {

  /** Cached image contract. */
  public static class Contract {
    /** Table name. */
    public static final String TABLE_NAME = "cached_image";

    /** Columns. */
    public static final String URL = "url",
                               PATH = "path",
                               LOADED = "loaded",
                               REMOVED = "removed";

    /** Column indexes. */
    public static final int INDEX_ID = 0, INDEX_URL = 1, INDEX_PATH = 2, INDEX_LOADED = 3;

    /** Column names. */
    public static final String[] COLUMNS = new String[] {
      _ID,
      URL,
      PATH,
      LOADED
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
      return cv;
    }

  }


  /** Identifier. */
  private long id;

  /** Image URL. */
  private String url;

  /** Local image path. */
  private String path;

  /** News record ID. */
  private long newsRecordId;

  /** Loaded flag. */
  private boolean loaded = false;

  public CachedImage(final long id) {
    this.id = id;
  }

  /**
   * @return the url
   */
  public String getUrl() { return url; }

  /**
   * @param url the url to set
   */
  public void setUrl(final String url) { this.url = url; }

  /**
   * @return the path
   */
  public String getPath() { return path; }

  /**
   * @param path the path to set
   */
  public void setPath(final String path) { this.path = path; }

  /**
   * @return the newsRecordId
   */
  public long getNewsRecordId() { return newsRecordId; }

  /**
   * @param newsRecordId the newsRecordId to set
   */
  public void setNewsRecordId(final long newsRecordId) { this.newsRecordId = newsRecordId; }

  /**
   * @return the id
   */
  public long getId() { return id; }
  /**
   * @param id the id to set
   */
  public void setId(final long id) { this.id = id; }

  /**
   * @return the loaded
   */
  public boolean isLoaded() { return loaded; }

  /**
   * @param loaded the loaded to set
   */
  public void setLoaded(final boolean loaded) { this.loaded = loaded; }

  public void set(final CachedImage image) {
    setLoaded(image.isLoaded());
    setNewsRecordId(image.getNewsRecordId());
    setPath(image.getPath());
    setUrl(image.getUrl());
  }

}
