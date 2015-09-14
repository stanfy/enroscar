package com.stanfy.enroscar.beans;

import android.content.ComponentCallbacks;
import android.content.Context;

import static com.stanfy.enroscar.beans.DebugFlags.DEBUG_BEANS;

/**
 * Contains instances of different named application entities.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 * @deprecated Beans subsystem is deprecated now. Use some dependency injection framework (e.g. Dagger) instead.
 */
@Deprecated
public interface BeansContainer extends ComponentCallbacks {

  /** Logging tag. */
  String TAG = "BeansContainer";
  /** Debug flag. */
  boolean DEBUG = DEBUG_BEANS;

  /**
   * @param <T> bean type
   * @param clazz bean class (it must be annotated with {@link EnroscarBean})
   * @return bean instance
   */
  <T> T getBean(final Class<T> clazz);

  /**
   * @param <T> bean type
   * @param name bean name
   * @param clazz bean class
   * @return bean instance
   */
  <T> T getBean(final String name, final Class<T> clazz);

  /**
   * Register the entity.
   * @param <T> bean type
   * @param clazz entity class
   * @param context context instance that can be used for bean creation
   * @return entity instance
   */
  <T> T putEntityInstance(final Class<T> clazz, final Context context);

  /**
   * Register the entity.
   * @param instance bean instance (its class must be annotated with {@link EnroscarBean}).
   */
  void putEntityInstance(final Object instance);

  /**
   * @param name entity name
   * @param instance entity instance
   */
  void putEntityInstance(final String name, final Object instance);

  /**
   * @param name entity name
   */
  void removeEntityInstance(final String name);

  /**
   * @param instance instance to remove
   */
  void removeEntityInstance(final Object instance);

  /**
   * Put a short-living bean to the container.
   * @param instance instance to put
   * @return bean name
   */
  String putTemporaryInstance(final Object instance);

  /**
   * Destroy all beans.
   */
  void destroy();

  /**
   * @param name bean name
   * @return true if bean with the given name exists
   */
  boolean containsBean(final String name);

}
