package com.stanfy.content;

import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.util.Log;

/**
 * Very simple content provider that supports cache access only.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class DefaultContentProvider extends AppContentProvider {

  /** Logging tag. */
  protected static final String TAG = "DefContentProvider";

  @Override
  protected void onUriMatcherCreate(final UriMatcher uriMatcher) {
    final Context context = getContext();
    final String packageName = context.getPackageName();
    final PackageManager pm = context.getPackageManager();
    try {
      final PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_PROVIDERS);

      String name = getClass().getName();
      if (name.startsWith(packageName)) {
        name = name.substring(packageName.length());
      }

      for (final ProviderInfo pi : info.providers) {
        if (pi.name.endsWith(name)) {
          configureCacheDAO(pi.authority, uriMatcher);
        }
      }
    } catch (final NameNotFoundException e) {
      Log.w(TAG, "Cannot configure application content provider", e);
    }

  }

}
