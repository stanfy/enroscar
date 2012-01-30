package com.stanfy.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

/**
 * Gingerbread utilities (API level 9).
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class GingerbreadUtils extends EclairUtils {

  @Override
  public File getExternalCacheDir(final Context context) { return context.getExternalCacheDir(); }

  @Override
  public File getMusicDirectory() { return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC); }
  
  @Override
  public void setOverscrollNever(final View view) { view.setOverScrollMode(View.OVER_SCROLL_NEVER); }

  @Override
  public void enableStrictMode() {
    Log.w("Utils", "Enable strict mode");
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
      .detectAll()
      .penaltyLog()
      .build()
    );
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
      .detectAll()
      .penaltyLog()
      .build()
    );
  }

}
