package com.stanfy.views.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;

/**
 * Implementation for old versions.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
class LowestSDKDependentUtils implements SDKDependentUtils {

  @Override
  public File getExternalCacheDir(final Context context) {
    return new File(Environment.getExternalStorageDirectory(), "/Android/data/" + context.getPackageName() + "/cache");
  }

}
