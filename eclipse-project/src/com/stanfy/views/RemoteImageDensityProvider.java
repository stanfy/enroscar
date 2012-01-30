package com.stanfy.views;

/**
 * Interface for views that can provide information about remote image density.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface RemoteImageDensityProvider {

  /** @return remote image density */
  int getSourceDensity();

}
