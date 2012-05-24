package com.stanfy.app.beans;

import java.util.HashMap;
import java.util.Map.Entry;

import android.content.res.Configuration;
import android.util.Log;

/**
 * A class that contains instances of different named application entities.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class DefaultBeansContainer implements BeansContainer {

  /** Entities map. */
  private final HashMap<String, Object> entitiesMap = new HashMap<String, Object>();

  @Override
  public void putEntityInstance(final Class<?> clazz) {
    final EnroscarBean beanAnnotation = clazz.getAnnotation(EnroscarBean.class);
    if (beanAnnotation == null) { throw new IllegalArgumentException("Bean must be annotated as @" + EnroscarBean.class.getSimpleName()); }

    Object instance;
    try {
      instance = clazz.newInstance();
    } catch (final Exception e) {
      throw new RuntimeException("Unable to instantiate bean " + clazz + " with name " + beanAnnotation.name(), e);
    }
    putEntityInstance(beanAnnotation.name(), instance);
  }

  @Override
  public void putEntityInstance(final String name, final Object instance) {
    if (DEBUG) { Log.d(TAG, "New bean: " + name + " - " + instance.getClass()); }
    entitiesMap.put(name, instance);
  }

  @Override
  public void removeEntityInstance(final String name) {
    final Object instance = entitiesMap.remove(name);
    if (DEBUG) { Log.d(TAG, "Remove bean: " + name + " - " + instance.getClass()); }
  }

  @Override
  public void onLowMemory() {
    for (final Entry<String, Object> entry : entitiesMap.entrySet()) {
      final Object instance = entry.getValue();
      if (instance instanceof FlushableBean) {
        ((FlushableBean) instance).flushResources();
      }
    }
  }

  @Override
  public void onConfigurationChange(final Configuration config) {
    for (final Entry<String, Object> entry : entitiesMap.entrySet()) {
      final Object instance = entry.getValue();
      if (instance instanceof ConfigurationDependentBean) {
        ((ConfigurationDependentBean) instance).triggerConfigurationChange(config);
      }
    }
  }

  @Override
  public <T> T getBean(final Class<T> clazz) {
    final EnroscarBean beanAnnotation = clazz.getAnnotation(EnroscarBean.class);
    if (beanAnnotation == null) { throw new IllegalArgumentException("Bean must be annotated as @" + EnroscarBean.class.getSimpleName()); }
    return getBean(beanAnnotation.name(), clazz);
  }

  @Override
  public <T> T getBean(final String name, final Class<T> clazz) {
    final Object instance = entitiesMap.get(name);
    return clazz.cast(instance);
  }

  @Override
  public void putEntityInstance(final Object instance) {
    final EnroscarBean beanAnnotation = instance.getClass().getAnnotation(EnroscarBean.class);
    if (beanAnnotation == null) { throw new IllegalArgumentException("Bean must be annotated as @" + EnroscarBean.class.getSimpleName()); }
    putEntityInstance(beanAnnotation.name(), instance);
  }

}
