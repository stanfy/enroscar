package com.stanfy.images;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

/**
 * Images memory cache.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ImageMemoryCache {

  /** Logging tag. */
  private static final String TAG = "ImageMemoryCache";

  /** Default image size. */
  private static final int DEFAULT_IMG_SIZE = 100 * 100;

  /** Working set size. */
  private static final int WORKING_SET_SIZE = 40;

  /** Maximum size. */
  private int maxSize, currentSize;

  /** Working set. */
  private WorkingSet workingSet = new WorkingSet();
  /** Cache map. */
  private Map<String, BitmapDrawable> cacheMap;

  /**
   * @param image image instance
   * @return image size
   */
  private static int imageSize(final BitmapDrawable image) {
    final Bitmap bmp = image.getBitmap();
    return bmp.getWidth() * bmp.getHeight();
  }

  /**
   * Constructor.
   * @param maxSize maximum amount of memory
   */
  public ImageMemoryCache(final int maxSize) {
    this.maxSize = maxSize;
    final int s = (maxSize >> 2) / DEFAULT_IMG_SIZE;
    this.cacheMap = new ConcurrentHashMap<String, BitmapDrawable>(s > 0 ? s : WORKING_SET_SIZE);
    currentSize = 0;
  }

  private void clean() {
    int cs = currentSize;
    final int ms = maxSize;
    final WorkingSet ws = workingSet;
    final Map<String, BitmapDrawable> cache = cacheMap;
    BitmapDrawable candidate = null;

    while (cs >= ms && !cache.isEmpty()) {
      final Iterator<Entry<String, BitmapDrawable>> i = cache.entrySet().iterator();
      if (cache.size() > ws.size()) {
        while (i.hasNext()) {
          candidate = i.next().getValue();
          if (!ws.contains(candidate)) {
            cs -= imageSize(candidate);
            i.remove();
            break;
          }
        }
      } else {
        if (i.hasNext()) {
          candidate = i.next().getValue(); i.remove();
          ws.remove(candidate);
          cs -= imageSize(candidate);
        } else {
          Log.w(TAG, "Wrong behavior: currentSize=" + cs + ", count=" + cache.size()
              + ", workingSet=" + workingSet.size());
          cs = 0;
        }
      }
    }
    currentSize = cs;
  }

  /**
   * @param url URL
   * @param image image instance
   */
  public synchronized void putElement(final String url, final BitmapDrawable image) {
    final int s = imageSize(image);
    currentSize += s;
    clean();
    if (currentSize > maxSize) {
      currentSize -= s;
      return;
    }
    workingSet.add(image);
    cacheMap.put(url, image);
  }


  /**
   * @param url URL
   * @return image
   */
  public BitmapDrawable getElement(final String url) {
    return cacheMap.get(url);
  }

  public boolean contains(final String url) {
    return cacheMap.containsKey(url);
  }

  public void remove(final String url) {
    final BitmapDrawable image = cacheMap.remove(url);
    if (image == null) { return; }
    workingSet.remove(image);
    currentSize += imageSize(image);
  }

  public void clear() {
    cacheMap.clear();
    workingSet.clear();
    currentSize = 0;
  }

  /**
   * Working set.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  private class WorkingSet extends LinkedList<BitmapDrawable> {
    /** serialVersionUID. */
    private static final long serialVersionUID = 8962320743200790724L;
    @Override
    public boolean add(final BitmapDrawable object) {
      while (size() >= WORKING_SET_SIZE) { removeFirst(); }
      return super.add(object);
    }
    @Override
    public boolean contains(final Object object) {
      for (final BitmapDrawable image : this) {
        if (object == image) { return true; }
      }
      return false;
    }
  }

}
