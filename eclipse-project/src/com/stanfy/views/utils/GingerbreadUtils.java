package com.stanfy.views.utils;

import java.io.File;

import android.content.Context;
import android.view.View;

/**
 * Gingerbread utilities (API level 9).
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class GingerbreadUtils extends EclairUtils {

  @Override
  public File getExternalCacheDir(final Context context) { return context.getExternalCacheDir(); }

  @Override
  public void setOverscrollNever(final View view) { view.setOverScrollMode(View.OVER_SCROLL_NEVER); }
}
