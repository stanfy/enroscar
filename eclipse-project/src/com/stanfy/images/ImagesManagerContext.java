package com.stanfy.images;

import android.widget.ImageView;

/**
 * Images manager context.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ImagesManagerContext {

  /** Images DAO. */
  private ImagesDAO imagesDAO;

  /** Downloader. */
  private Downloader downloader;

  /** Images manager. */
  private ImagesManager imagesManager;

  /** @return the imagesDAO */
  public ImagesDAO getImagesDAO() { return imagesDAO; }
  /** @param imagesDAO the imagesDAO to set */
  public void setImagesDAO(final ImagesDAO imagesDAO) { this.imagesDAO = imagesDAO; }
  /** @return the downloader */
  public Downloader getDownloader() { return downloader; }
  /** @param downloader the downloader to set */
  public void setDownloader(final Downloader downloader) { this.downloader = downloader; }
  /** @return the imagesManager */
  public ImagesManager getImagesManager() { return imagesManager; }
  /** @param imagesManager the imagesManager to set */
  public void setImagesManager(final ImagesManager imagesManager) { this.imagesManager = imagesManager; }

  public void populateImage(final ImageView imageView, final String url) {
    final ImagesDAO imagesDAO = this.imagesDAO;
    final Downloader downloader = this.downloader;
    final ImagesManager imagesManager = this.imagesManager;
    if (imagesDAO != null && downloader != null && imagesManager != null) {
      imagesManager.populateImage(imageView, url, imagesDAO, downloader);
    }
  }

}
