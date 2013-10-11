package com.stanfy.enroscar.images;

import java.util.LinkedList;
import java.util.concurrent.Executor;

/** Image request builder. */
public class ImageRequestsBuilder {

  /** Images manager. */
  private final ImagesManager manager;

  /** Requests to process. */
  private final LinkedList<ImageRequest> requests = new LinkedList<ImageRequest>();

  /** Default allowed size. */
  private float defaultAllowedSize = -1;

  /** Executor instance. */
  private Executor executor;

  ImageRequestsBuilder(final ImagesManager manager) {
    this.manager = manager;
  }

  /**
   * Specify an executor to use for images loading. If it's null, loading will take place in the current thread.
   * @param executor executor instance
   * @return this for chaining
   */
  public ImageRequestsBuilder onExecutor(final Executor executor) {
    this.executor = executor;
    return this;
  }

  /**
   * Use default images manager executor to load the images.
   * @return this for chaining
   */
  public ImageRequestsBuilder onDefaultExecutor() {
    this.executor = manager.getImageTaskExecutor();
    return this;
  }

  /**
   * Store images of original size on disk.
   * @return this for chaining
   */
  public ImageRequestsBuilder storeOriginalImages() {
    defaultAllowedSize = -1;
    return this;
  }

  /**
   * Scale images to be maximum of screen size before storing on disk.
   * @return this for chaining
   */
  public ImageRequestsBuilder scaleToScreenSize() {
    defaultAllowedSize = 1;
    return this;
  }

  /**
   * Scale images to be maximum of screen size before storing on disk.
   * @param size relatively to screen size
   * @return this for chaining
   */
  public ImageRequestsBuilder scaleToScreenSize(final float size) {
    if (size <= 0) { throw new IllegalArgumentException("relative size must be bigger than 0"); }
    defaultAllowedSize = size;
    return this;
  }

  /**
   * Plan an image on loading.
   * @param url image URL
   * @return this for chaining
   */
  public ImageRequestsBuilder add(final String url) {
    requests.add(new ImageRequest(manager, url, defaultAllowedSize));
    return this;
  }

  /**
   * Plan an image on loading.
   * @param url image URL
   * @param relativeSize max allowed image size relatively to the screen size
   * @return this for chaining
   */
  public ImageRequestsBuilder add(final String url, final float relativeSize) {
    requests.add(new ImageRequest(manager, url, relativeSize));
    return this;
  }

  /**
   * Start images loading.
   */
  public void startLoading() {
    manager.ensureImages(requests, executor);
  }

}
