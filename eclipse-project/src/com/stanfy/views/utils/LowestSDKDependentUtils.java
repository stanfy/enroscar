package com.stanfy.views.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.view.View;

/**
 * Implementation for old versions.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class LowestSDKDependentUtils implements SDKDependentUtils {

  @Override
  public File getExternalCacheDir(final Context context) {
    return new File(Environment.getExternalStorageDirectory(), "/Android/data/" + context.getPackageName() + "/cache");
  }

  @Override
  public void setOverscrollNever(final View view) { /* not implemented */ }

}
