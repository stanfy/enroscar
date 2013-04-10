package com.stanfy.enroscar.rest;

import android.content.Context;

import com.stanfy.enroscar.beans.BeansManager;

/**
 * Internal utilities.
 */
public final class Utils {

  /** Debug networks-related classes. */
  public static final boolean DEBUG_IO = false; // @debug.io@

  /** Debug loaders. */
  public static final boolean DEBUG_LOADERS = false; // @debug.loaders@

  private Utils() { }
  
  public static RemoteServerApiConfiguration getConfig(final Context context) {
    return BeansManager.get(context).getContainer().getBean(RemoteServerApiConfiguration.BEAN_NAME, RemoteServerApiConfiguration.class);
  }
  
  public static boolean isDebugRest(final Context context) {
    RemoteServerApiConfiguration config = getConfig(context);
    return config != null && config.isDebugRest();
  }
  
  public static boolean isDebugRestResponse(final Context context) {
    RemoteServerApiConfiguration config = getConfig(context);
    return config != null && config.isDebugRestResponse();
  }

  public static int getTrafficStatsTag(final String stringTag) {
    return Math.abs(stringTag.hashCode());
  }
  
}
