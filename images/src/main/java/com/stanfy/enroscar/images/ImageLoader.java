package com.stanfy.enroscar.images;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static com.stanfy.enroscar.images.ImagesManager.*;

/**
 * Image loader task.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
class ImageLoader implements Callable<Void> {

  /** Image URL. */
  final ImageRequest request;

  /** Images manager. */
  private final ImagesManager imagesManager;

  /** Future task instance. */
  final FutureTask<Void> future;

  /** Targets. */
  private final ArrayList<ImageConsumer> targets = new ArrayList<ImageConsumer>();

  /** Result drawable. */
  private ImageResult result;

  /** Result error. */
  private Throwable error;

  /**
   * @param request image loading request
   * @param imagesManager manager instance
   */
  public ImageLoader(final ImageRequest request, final ImagesManager imagesManager) {
    this.request = request;
    this.imagesManager = imagesManager;
    this.future = new FutureTask<Void>(this);
  }


  // main thread
  public boolean addTarget(final ImageConsumer imageHolder) {
    if (future.isCancelled()) { return false; } // we should start a new task

    imageHolder.onStart(this, request.url);

    synchronized (targets) {
      if (result != null) {
        imagesManager.setResultImage(imageHolder, result, false);
        imageHolder.onFinish(request.url, result);
      } else if (error != null) {
        imageHolder.onError(request.url, error);
      } else {
        imageHolder.currentLoader = this;
        targets.add(imageHolder);
      }
    }

    return true;
  }

  // main thread
  public void removeTarget(final ImageConsumer consumer) {
    if (DEBUG) { Log.d(TAG, "Cancel request: " + request.getKey() + "\nLoader: " + this); }

    consumer.onCancel(request.url);

    synchronized (targets) {

      targets.remove(consumer);

      if (targets.isEmpty()) {
        if (!future.cancel(true)) {
          if (DEBUG) { Log.d(TAG, "Can't cancel task so let's try to remove loader manually"); }
          imagesManager.currentLoads.remove(request.getKey(), this);
        }
      }

    }
  }

  // worker thread
  private void safeImageSet(final ImageResult result) {
    if (DEBUG) { Log.v(ImagesManager.TAG, "Post setting drawable for " + request.getKey()); }

    synchronized (targets) {
      if (this.result != null) { throw new IllegalStateException("Result is already set"); }
      memCacheImage(result);
      this.result = result;
    }

    post(new Runnable() {
      @Override
      public void run() {
        if (imagesManager.debug) {
          Log.v(TAG, "Set drawable for " + request.getKey());
        }

        final ArrayList<ImageConsumer> targets = ImageLoader.this.targets;
        final int count = targets.size();
        if (count > 0) {
          //noinspection ForLoopReplaceableByForEach
          for (int i = 0; i < count; i++) {
            final ImageConsumer imageHolder = targets.get(i);
            if (DEBUG) {
              Log.d(TAG, "Try to set " + imageHolder + " - " + request.getKey());
            }
            setToConsumer(imageHolder, result);
          }
        } else if (DEBUG) {
          Log.w(TAG, "set drawable: have no targets in list");
        }

      }
    });
  }

  // main thread
  private void setToConsumer(final ImageConsumer consumer, final ImageResult result) {
    if (consumer.currentLoader == this) {
      imagesManager.setResultImage(consumer, result, true);
    } else {
      if (imagesManager.debug) { Log.d(TAG, "Skip set for " + consumer); }
    }
  }

  private Bitmap prepare(final Bitmap map) {
    int dstW = request.getRequiredWidth(), dstH = request.getRequiredHeight();
    if (dstW <= 0 || dstH <= 0 || request.isSkipScaleBeforeMemCache()) {
      if (imagesManager.debug) {
        Log.d(TAG, "Skip scaling for " + request.getKey() + " skip flag: " + request.isSkipScaleBeforeMemCache());
      }
      return map;
    }

    final int w = map.getWidth(), h = map.getHeight();

    if (w <= dstW && h <= dstH) { return map; }

    final double ratio = (double)w / h;
    if (w > h) {
      dstH = (int)(dstW / ratio);
    } else {
      dstW = (int)(dstH * ratio);
    }

    if (dstW <= 0 || dstH <= 0) { return map; }

    final Bitmap scaled = Bitmap.createScaledBitmap(map, dstW, dstH, true);
    scaled.setDensity(imagesManager.getResources().getDisplayMetrics().densityDpi);
    return scaled;
  }

  private void memCacheImage(final ImageResult result) {
    if (result.getType() != ImageResult.ResultType.MEMORY) {
      Bitmap input = result.getBitmap();
      Bitmap resultBitmap = prepare(input);
      if (resultBitmap != input) {
        result.setBitmap(resultBitmap);
        input.recycle();
      }
      imagesManager.memCacheImage(request.url, resultBitmap);
    }
  }

  private void finish() {
    post(new Runnable() {
      @Override
      public void run() {
        final ArrayList<ImageConsumer> targets = ImageLoader.this.targets;
        final int count = targets.size();
        final ImageResult res = result;
        final String url = request.url;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < count; i++) {
          targets.get(i).onFinish(url, res);
        }
      }
    });
  }

  private void cancel() {
    post(new Runnable() {
      @Override
      public void run() {
        final ArrayList<ImageConsumer> targets = ImageLoader.this.targets;
        final int count = targets.size();
        final String url = request.url;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < count; i++) {
          targets.get(i).onCancel(url);
        }
      }
    });
  }

  private void error(final Throwable e) {
    post(new Runnable() {
      @Override
      public void run() {
        final ArrayList<ImageConsumer> targets = ImageLoader.this.targets;
        final int count = targets.size();
        final String url = request.url;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < count; i++) {
          targets.get(i).onError(url, e);
        }
      }
    });
  }

  private ImageConsumer findMainTarget() {
    ImageConsumer consumer = null;
    synchronized (targets) {
      if (!targets.isEmpty()) {
        consumer = targets.get(0);
      }
    }
    return consumer;
  }

  private void post(final Runnable action) {
    ImageConsumer consumer = findMainTarget();
    if (consumer != null) {
      consumer.post(action);
    }
  }

  @Override
  public Void call() {
    if (DEBUG) { Log.d(TAG, "Start image task"); }
    try {

      if (!imagesManager.waitForPause()) {
        // interrupted
        cancel();
        return null;
      }

      final ImageResult result = request.readImage();
      if (result == null) {
        throw new IllegalStateException("Image is not returned!");
      }
      safeImageSet(result);

      finish();

    } catch (final MalformedURLException e) {

      Log.e(TAG, "Bad URL: " + request.url + ". Loading canceled.", e);
      error(e);

    } catch (final IOException e) {

      if (DEBUG_IO) { Log.e(TAG, "IO error for " + request.url + ": " + e.getMessage()); }
      error(e);

    } catch (final Exception e) {

      Log.e(TAG, "Cannot load image " + request.url, e);
      error(e);

    } finally {

      final boolean removed = imagesManager.currentLoads.remove(request.getKey(), this);
      if (DEBUG) {
        Log.d(TAG, "Current loaders count: " + imagesManager.currentLoads.size());
        if (!removed) { Log.w(TAG, "Incorrect loader in currents for " + request.getKey()); }
      }

    }
    return null;
  }

}
