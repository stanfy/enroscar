package com.stanfy.views.utils;

import java.io.File;

import android.content.Context;

/**
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public interface SDKDependentUtils {

  /**
   * @param context context instance
   * @return cache directory on the external storage
   */
  File getExternalCacheDir(final Context context);

}
