package com.stanfy.enroscar.goro;

import android.os.Looper;

/**
 * Utilities.
 */
final class Util {

  private Util() { }

  public static boolean checkMainThread() {
    return Looper.myLooper() == Looper.getMainLooper();
  }

}
