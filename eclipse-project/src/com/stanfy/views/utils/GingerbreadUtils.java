package com.stanfy.views.utils;

import java.io.File;

import android.content.Context;

/**
 * Gingerbread utilities (API level 9).
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class GingerbreadUtils extends EclairUtils {

  @Override
  public File getExternalCacheDir(final Context context) { return context.getExternalCacheDir(); }

}
