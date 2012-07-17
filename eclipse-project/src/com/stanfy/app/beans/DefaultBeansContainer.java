package com.stanfy.app.beans;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map.Entry;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.stanfy.utils.AppUtils;

/**
 * A class that contains instances of different named application entities.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class DefaultBeansContainer implements BeansContainer {

  /** Entities map. */
  private final HashMap<String, Object> entitiesMap = new HashMap<String, Object>();

  @Override
  public <T> T putEntityInstance(final Class<T> clazz, final Context context) {
    final EnroscarBean beanAnnotation = AppUtils.getBeanInfo(clazz);
    T instance;
    try {
      if (beanAnnotation.contextDependent()) {
        if (context == null) { throw new IllegalArgumentException("Bean is context dependent but context is not supplied"); }
        final Constructor<T> constr = clazz.getConstructor(Context.class);
        instance = constr.newInstance(context);
      } else {
        instance = clazz.newInstance();
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to instantiate bean " + clazz + " with name " + beanAnnotation.value(), e);
    }
    putEntityInstance(beanAnnotation.value(), instance);
    return instance;
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
  public <T> T getBean(final Class<T> clazz) {
    final EnroscarBean beanAnnotation = clazz.getAnnotation(EnroscarBean.class);
    if (beanAnnotation == null) { throw new IllegalArgumentException("Bean must be annotated as @" + EnroscarBean.class.getSimpleName()); }
    return getBean(beanAnnotation.value(), clazz);
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
    putEntityInstance(beanAnnotation.value(), instance);
  }

  @Override
  public void onConfigurationChanged(final Configuration config) {
    for (final Entry<String, Object> entry : entitiesMap.entrySet()) {
      final Object instance = entry.getValue();
      if (instance instanceof ConfigurationDependentBean) {
        ((ConfigurationDependentBean) instance).triggerConfigurationChange(config);
      }
    }
  }

  @Override
  public void onLowMemory() {
    for (final Entry<String, Object> entry : entitiesMap.entrySet()) {
      final Object instance = entry.getValue();
      if (instance instanceof FlushableBean) {
        ((FlushableBean) instance).flushResources(this);
      }
    }
  }

  @Override
  public void triggerInitFinished() {
    for (final Entry<String, Object> entry : entitiesMap.entrySet()) {
      final Object instance = entry.getValue();
      if (instance instanceof InitializingBean) {
        ((InitializingBean) instance).onInitializationFinished(this);
      }
    }
  }

  @Override
  public void destroy() {
    for (final Entry<String, Object> entry : entitiesMap.entrySet()) {
      final Object instance = entry.getValue();
      if (instance instanceof DestroyingBean) {
        ((DestroyingBean) instance).onDestroy(this);
      }
    }
    entitiesMap.clear();
  }

  @Override
  public boolean containsBean(final String name) {
    return entitiesMap.containsKey(name);
  }

}
