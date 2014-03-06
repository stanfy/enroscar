package com.stanfy.enroscar.content.async;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.util.SimpleArrayMap;

import com.stanfy.enroscar.content.async.internal.TaskAsync;
import com.stanfy.enroscar.content.async.internal.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class Tools {

  /** Classes cache. */
  private static final SimpleArrayMap<Class<?>, Class<?>> CLASSES_CACHE = new SimpleArrayMap<>();
  /** Constructors cache. */
  private static final SimpleArrayMap<Class<?>, Constructor<?>> CONSTRUCTORS_CACHE = new SimpleArrayMap<>();

  public static <D> Async<D> async(final Callable<D> task) {
    return new TaskAsync<>(task);
  }

  public static <T> T loading(final Class<T> dataLoaderClass, final Fragment fragment) {
    Constructor<T> constructor = getConstructor(getClass(dataLoaderClass));
    try {
      return constructor.newInstance(fragment.getActivity(), fragment.getLoaderManager());
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  public static <T> T loading(final Class<T> dataLoaderClass, final FragmentActivity activity) {
    Constructor<T> constructor = getConstructor(getClass(dataLoaderClass));
    try {
      return constructor.newInstance(activity, activity.getSupportLoaderManager());
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private static synchronized Class<?> getClass(final Class<?> clazz) {
    Class<?> result = CLASSES_CACHE.get(clazz);
    if (result == null) {
      String packageName = clazz.getPackage().getName();
      String fqcn = Utils.getGeneratedClassName(packageName, clazz.getCanonicalName());
      if (packageName.length() > 0) {
        fqcn = packageName + "." + fqcn;
      }
      try {
        result = Class.forName(fqcn);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Generated class " + fqcn + " cannot be loaded. "
            + "You might have forgotten to apply async-compiler or strip this class with Proguard");
      }
      CLASSES_CACHE.put(clazz, result);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private static synchronized <T> Constructor<T> getConstructor(final Class<?> clazz) {
    Constructor<?> constructor = CONSTRUCTORS_CACHE.get(clazz);
    if (constructor == null) {
      try {
        constructor = clazz.getDeclaredConstructor(Context.class, LoaderManager.class);
        constructor.setAccessible(true);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException("Cannot get constructor " + clazz.getSimpleName()
            + "(Context, LoaderManager). Haven't you stripped it with Proguard?");
      }
      CONSTRUCTORS_CACHE.put(clazz, constructor);
    }
    return (Constructor<T>) constructor;
  }

}
