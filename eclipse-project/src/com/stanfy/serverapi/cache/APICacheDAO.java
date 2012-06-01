package com.stanfy.serverapi.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.content.AppContentProvider;
import com.stanfy.images.BuffersPool;
import com.stanfy.images.PoolableBufferedInputStream;
import com.stanfy.utils.AppUtils;
import com.stanfy.utils.Time;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public final class APICacheDAO implements BaseColumns {

  /** Logging tag. */
  private static final String TAG = "APICache";

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_API;

  /** Cache enabled flag. */
  private static boolean cacheEnabled = false;

  /** Cache time rules collection. */
  private static final ArrayList<TimeRule> CACHE_TIME_RULES = new ArrayList<APICacheDAO.TimeRule>();

  /** Table name. */
  public static final String TABLE_NAME = "api_cache";

  /** Prefix for all cache paths. */
  private static final String CACHE_PATH_PREFIX = "apicache/";

  /** Columns. */
  public static final String URL = "url", FILE = "file", TIME = "time";

  /** Columns array to use in queries. */
  private static final String[] COLUMNS = new String[] {_ID, FILE, TIME};

  /** @param timeRule Custom {@link TimeRule} instance. */
  public static void addTimeRule(final TimeRule timeRule) {
    CACHE_TIME_RULES.add(timeRule);
  }
  /**
   * @param p URL pattern
   * @param time how long should the cache live
   */
  public static void addTimeRule(final String pattern, final long time) {
    CACHE_TIME_RULES.add(new TimeRule(Pattern.compile(pattern), time));
  }
  /**
   * @param p URL pattern
   * @param time count of millisecond from 00:00 of the current day to determine an hour when cache expires
   */
  public static void addUntilTimeRule(final String pattern, final long time) {
    CACHE_TIME_RULES.add(new UntilTimeRule(Pattern.compile(pattern), time));
  }

  /** @return the cacheEnabled */
  public static boolean isCacheEnabled() { return cacheEnabled; }
  /** @param cacheEnabled the cacheEnabled to set */
  public static void setCacheEnabled(final boolean cacheEnabled) {
    APICacheDAO.cacheEnabled = cacheEnabled;
    if (!cacheEnabled) { CACHE_TIME_RULES.clear(); }
  }

  /**
   * @param db database instance
   */
  public static void ensureCacheTable(final SQLiteDatabase db) {
    if (!cacheEnabled) { return; }
    final String index = TABLE_NAME + "_url_idx";
    db.execSQL("DROP INDEX IF EXISTS " + index);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    db.execSQL(
        "CREATE TABLE " + TABLE_NAME + " ("
      + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
      + URL + " TEXT,"
      + FILE + " TEXT,"
      + TIME + " INTEGER"
      + ")"
    );
    db.execSQL("CREATE INDEX " + index + " ON " + TABLE_NAME + "(" + URL + ")");
  }

  public static Uri getUri(final Context context, final String authority) {
    return Uri.parse("content://" + authority + "/" + AppContentProvider.PATH_API_CACHE);
  }

  public static File getCachedFile(final Context context, final String authority, final String url) {
    if (!cacheEnabled) { return null; }
    Cursor c = null;
    try {
      c = context.getContentResolver().query(getUri(context, authority), COLUMNS, URL + "=?", new String[] {url}, null);
      if (c == null || !c.moveToFirst()) { return null; }
      final long time = c.getLong(2);
      final int l = CACHE_TIME_RULES.size();
      final String resultPath = c.getString(1);
      if (resultPath == null) { return null; }
      final File resultFile = new File(context.getCacheDir(), resultPath);
      if (!resultFile.exists()) { return null; }
      final long start = System.currentTimeMillis();
      for (int i = 0; i < l; i++) {
        final TimeRule tr = CACHE_TIME_RULES.get(i);
        if (tr.p.matcher(url).matches()) {
          if (!tr.isActual(time)) {
            delete(context, authority, c.getLong(0));
            resultFile.delete();
          }
          if (DEBUG) { Log.d(TAG, "Cache rules time: " + (System.currentTimeMillis() - start) + " ms, cahe rule: " + tr); }
          break;
        }
      }
      if (DEBUG) { Log.d(TAG, "Cache rules time: " + (System.currentTimeMillis() - start) + " ms"); }
      return resultFile.exists() ? resultFile : null;
    } finally {
      if (c != null) { c.close(); }
    }
  }

  private static String getFilePath(final long id) {
    final long divider = 10;
    return CACHE_PATH_PREFIX + AppUtils.buildFilePathById(id / divider, String.valueOf(id));
  }

  /**
   * @param db DB instance
   * @param url API request url
   * @param source source stream
   * @param bsize buffer size
   * @return new input stream
   */
  public static CachedStreamInfo insert(final Context context, final String authority, final String url, final InputStream source, final BuffersPool buffersPool, final int bsize) {
    if (!cacheEnabled) { return new CachedStreamInfo(source, -1); }
    final ContentValues cv = new ContentValues(2);
    cv.put(URL, url);
    cv.put(TIME, System.currentTimeMillis());
    final ContentResolver resolver = context.getContentResolver();
    final Uri baseUri = getUri(context, authority);
    final long id = Long.parseLong(resolver.insert(baseUri, cv).getLastPathSegment());
    final String path = getFilePath(id);
    cv.clear();
    cv.put(FILE, path);

    InputStream result = source;
    final File f = new File(context.getCacheDir(), path);
    try {
      f.getParentFile().mkdirs();
      final FileOutputStream output = new FileOutputStream(f);
      final byte[] buffer = buffersPool.get(bsize);
      int cnt;
      try {
        do {
          cnt = source.read(buffer);
          if (cnt > 0) { output.write(buffer, 0, cnt); }
        } while (cnt >= 0);
      } finally {
        output.close();
        source.close();
        buffersPool.release(buffer);
      }
      result = new PoolableBufferedInputStream(new FileInputStream(f), bsize, buffersPool);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    resolver.update(baseUri, cv, _ID + "=" + id, null);
    return new CachedStreamInfo(result, id);
  }

  /**
   * @param db database instance
   * @param id record identifier
   */
  public static void delete(final Context context, final String authority, final long id) {
    if (!cacheEnabled) { return; }
    final String path = getFilePath(id);
    new File(context.getCacheDir(), path).delete();
    context.getContentResolver().delete(getUri(context, authority), _ID + "=" + id, null);
  }

  private APICacheDAO() { /* hide */ }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static class TimeRule {
    /** Pattern. */
    final Pattern p;
    /** Time to live. */
    final long time;
    public TimeRule(final Pattern p, final long time) {
      this.p = p; this.time = time;
    }
    public boolean isActual(final long createTime) { return time > System.currentTimeMillis() - createTime; }

    @Override
    public String toString() { return getClass().getSimpleName() + ":" + p.pattern() + "/" + (time / Time.MINUTES) + "min"; }
  }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static class UntilTimeRule extends TimeRule {
    /**
     * @param p URL pattern
     * @param time count of millisecond from 00:00 of the current day to determine an hour when cache expires
     */
    public UntilTimeRule(final Pattern p, final long time) {
      super(p, time);
    }

    @Override
    public boolean isActual(final long createTime) {
      final long day = Time.DAYS;
      final long current = System.currentTimeMillis();
      long margin = current / day * day + time;
      if (createTime > margin) { margin += day; }
      return current < margin;
    }
  }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static class CachedStreamInfo {
    /** Stream. */
    private final InputStream stream;
    /** Cache record ID. */
    private final long id;

    public CachedStreamInfo(final InputStream stream, final long id) {
      this.stream = stream;
      this.id = id;
    }

    /** @return the stream */
    public InputStream getStream() { return stream; }
    /** @return the id */
    public long getId() { return id; }
  }

}
