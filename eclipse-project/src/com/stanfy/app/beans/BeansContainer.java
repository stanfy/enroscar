package com.stanfy.app.beans;

import static com.stanfy.DebugFlags.DEBUG_BEANS;
import android.content.res.Configuration;

/**
 * Contains instances of different named application entities.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface BeansContainer {

  /** Logging tag. */
  String TAG = "BeansContainer";
  /** Debug flag. */
  boolean DEBUG = DEBUG_BEANS;

  /**
   * @param clazz bean class (it must be annotated with {@link EnroscarBean})
   * @return bean instance
   */
  <T> T getBean(final Class<T> clazz);

  /**
   * @param name bean name
   * @param clazz bean class
   * @return bean instance
   */
  <T> T getBean(final String name, final Class<T> clazz);

  /**
   * Register the entity.
   * @param clazz entity class
   */
  void putEntityInstance(final Class<?> clazz);

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
   * Called from {@link con.stanfy.app.Application#onLowMemory()}.
   */
  void onLowMemory();

  /**
   * Called from {@link con.stanfy.app.Application#onConfigurationChanged(Configuration)}.
   * @param config configuration instance
   */
  void onConfigurationChange(final Configuration config);

}
