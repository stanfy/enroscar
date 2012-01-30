package com.stanfy.utils;

import java.io.File;

import android.content.Context;
import android.view.View;

/**
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public interface SDKDependentUtils {

  /**
   * @param context context instance
   * @return cache directory on the external storage
   */
  File getExternalCacheDir(final Context context);
  
  /**
   * @return standard directory in which to place any audio files
   */
  File getMusicDirectory();

  /**
   * @param view view to set OVER_SCROLL_NEVER
   */
  void setOverscrollNever(final View view);

  /**
   * Enable strict mode.
   */
  void enableStrictMode();

}
