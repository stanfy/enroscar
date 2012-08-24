package com.stanfy.app.loader;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import android.content.Context;

import com.stanfy.utils.ApiMethodsSupport;
import com.stanfy.utils.ApplicationServiceSupport;
import com.xtremelabs.robolectric.Robolectric;

/**
 * Provides access to package fields of request builder loader.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public final class RbLoaderAccess {

  private RbLoaderAccess() { /* hidden */ }

  public static <T extends RequestBuilderLoader<?>> T initLoader(final T loader) throws Exception {
    final ApiMethodsSupport support = loader.apiSupport;
    final Field contextRefField = ApplicationServiceSupport.class.getDeclaredField("contextRef");
    contextRefField.setAccessible(true);
    contextRefField.set(support, new WeakReference<Context>(Robolectric.application));
    return loader;
  }

}
