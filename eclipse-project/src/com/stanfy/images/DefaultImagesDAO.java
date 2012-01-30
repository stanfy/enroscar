package com.stanfy.images;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.stanfy.content.AppContentProvider;
import com.stanfy.images.model.CachedImage;
import com.stanfy.utils.Time;

/**
 * Default headless images DAO.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class DefaultImagesDAO implements ImagesDAO<CachedImage> {

  /** Threshold to update usage timestamp. */
  private static final float WRITE_USAGE_TS_THRESHOLD = 0.02f;

  /** Default TTL. */
  public static final long DEFAULT_IMAGES_TTL = 5 * Time.DAYS;

  /** Context instance. */
  private final Context context;

  /** Content URI. */
  private final Uri contentUri;

  /** TTL. */
  private long ttl = DEFAULT_IMAGES_TTL;

  public DefaultImagesDAO(final Context context, final String authority) {
    this(context, Uri.parse("content://" + authority + "/" + AppContentProvider.PATH_IMAGES_CACHE));
  }
  public DefaultImagesDAO(final Context context, final Uri contentUri) {
    this.context = context;
    this.contentUri = contentUri;
  }

  /** @param ttl the TTL to set */
  public void setTTL(final long ttl) { this.ttl = ttl; }
  /** @return the TTL */
  public long getTTL() { return ttl; }

  @Override
  public void updateImage(final CachedImage image) {
    final Context context = this.context;
    if (context == null) { return; }
    ContentValues cv = new ContentValues(CachedImage.Contract.COLUMNS.length);
    cv = CachedImage.Contract.toContentValues(image, cv);
    cv.remove(CachedImage.Contract._ID);
    context.getContentResolver().update(contentUri, cv, CachedImage.Contract._ID + "=" + image.getId(), null);
  }
  @Override
  public CachedImage getCachedImage(final String url) {
    final Context context = this.context;
    if (context == null) { return null; }
    final Cursor c = context.getContentResolver().query(contentUri, null, CachedImage.Contract.URL + "=?", new String[] {url}, null);
    try {
      if (c.moveToFirst()) {
        return CachedImage.Contract.fromCursor(c, new CachedImage(0));
      }
    } finally {
      if (c != null) { c.close(); }
    }
    return null;
  }
  @Override
  public CachedImage createCachedImage(final String url) {
    if (context == null) { return null; }
    final CachedImage image = new CachedImage(0);
    image.setUrl(url);
    ContentValues cv = new ContentValues(CachedImage.Contract.COLUMNS.length);
    cv = CachedImage.Contract.toContentValues(image, cv);
    cv.remove(CachedImage.Contract._ID);
    final Uri uri = context.getContentResolver().insert(contentUri, cv);
    if (uri == null) { return null; }
    image.setId(Long.parseLong(uri.getLastPathSegment()));
    return image;
  }

  @Override
  public Cursor getOldImages(final long time) {
    final Context context = this.context;
    if (context == null) { return null; }
    final long margin = time - ttl;
    return context.getContentResolver().query(contentUri, null, CachedImage.Contract.TIMESTAMP + "<" + margin, null, null);
  }

  @Override
  public int deleteOldImages(final long time) {
    final Context context = this.context;
    if (context == null) { return 0; }
    final long margin = time - ttl;
    return context.getContentResolver().delete(contentUri, CachedImage.Contract.TIMESTAMP + "<" + margin, null);
  }

  @Override
  public Cursor getLessUsedImages() {
    if (context == null) { return null; }
    return context.getContentResolver().query(contentUri, null, null, null, CachedImage.Contract.USAGE_TIMESTAMP + " ASC");
  }

  @Override
  public void deleteImage(final long id) {
    if (context == null) { return; }
    context.getContentResolver().delete(contentUri, CachedImage.Contract._ID + "=" + id, null);
  }

  @Override
  public void updateUsageTimestamp(final CachedImage image) {
    final Context context = this.context;
    if (context == null) { return; }
    final long time = System.currentTimeMillis();
    final float d = (float)(time - image.getUsageTimestamp()) / ttl;
    if (d > WRITE_USAGE_TS_THRESHOLD) {
      image.setUsageTimestamp(time);
      final ContentValues cv = new ContentValues(1);
      cv.put(CachedImage.Contract.USAGE_TIMESTAMP, time);
      context.getContentResolver().update(contentUri, cv, CachedImage.Contract._ID + "=" + image.getId(), null);
    }
  }

}
