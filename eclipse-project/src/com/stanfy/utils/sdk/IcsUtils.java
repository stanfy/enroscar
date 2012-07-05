/**
 *
 */
package com.stanfy.utils.sdk;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.os.Build;

import com.stanfy.net.cache.IcsHttpResponseCacheInstaller;


/**
 * ICS utitlities (API level 14).
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class IcsUtils extends HoneycombMr1Utils {

  @SuppressWarnings("unchecked")
  @Override
  public IcsHttpResponseCacheInstaller getSystemResponseCacheInstaller() {
    return IcsHttpResponseCacheInstaller.getInstance();
  }

  @Override
  public void registerComponentCallbacks(final Application application, final ComponentCallbacks callbacks) {
    application.registerComponentCallbacks(callbacks);
  }

}
