package com.stanfy.enroscar.beans;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * A class that contains instances of different named application entities.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 * @deprecated Beans subsystem is deprecated now. Use some dependency injection framework (e.g. Dagger) instead.
 */
@Deprecated
public class DefaultBeansContainer implements BeansContainer {

  /** Entities map. */
  private final HashMap<String, Object> beansMap = new HashMap<String, Object>();

  /** Identifiers counter. */
  private int idCounter = 0;

  @Override
  public <T> T putEntityInstance(final Class<T> clazz, final Context context) {
    final EnroscarBean beanAnnotation = BeanUtils.getBeanInfo(clazz);
    T instance;
    String name;
    try {
      if (beanAnnotation != null) {
        name = beanAnnotation.value();
        if (beanAnnotation.contextDependent()) {
          if (context == null) { throw new IllegalArgumentException("Bean is context dependent but context is not supplied"); }
          final Constructor<T> constr = clazz.getConstructor(Context.class);
          instance = constr.newInstance(context);
        } else {
          instance = clazz.newInstance();
        }
      } else {
        instance = clazz.newInstance();
        name = clazz.getName();
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to instantiate bean " + clazz + " with name " + beanAnnotation.value(), e);
    }
    putEntityInstance(name, instance);
    return instance;
  }

  @Override
  public void putEntityInstance(final String name, final Object instance) {
    if (DEBUG) { Log.d(TAG, "New bean: " + name + " - " + instance.getClass()); }
    beansMap.put(name, instance);
  }

  @Override
  public <T> T getBean(final Class<T> clazz) {
    final EnroscarBean beanAnnotation = clazz.getAnnotation(EnroscarBean.class);
    String name = beanAnnotation != null ? beanAnnotation.value() : clazz.getName();
    return getBean(name, clazz);
  }

  @Override
  public <T> T getBean(final String name, final Class<T> clazz) {
    final Object instance = beansMap.get(name);
    return clazz.cast(instance);
  }

  @Override
  public void putEntityInstance(final Object instance) {
    final EnroscarBean beanAnnotation = BeanUtils.getBeanInfo(instance.getClass());
    putEntityInstance(beanAnnotation != null ? beanAnnotation.value() : instance.getClass().getName(), instance);
  }

  @Override
  public void onConfigurationChanged(final Configuration config) {
    for (final Entry<String, Object> entry : beansMap.entrySet()) {
      final Object instance = entry.getValue();
      if (instance instanceof ConfigurationDependentBean) {
        ((ConfigurationDependentBean) instance).triggerConfigurationChange(config);
      }
    }
  }

  @Override
  public void onLowMemory() {
    for (final Entry<String, Object> entry : beansMap.entrySet()) {
      final Object instance = entry.getValue();
      if (instance instanceof FlushableBean) {
        ((FlushableBean) instance).flushResources(this);
      }
    }
  }

  @Override
  public void destroy() {
    for (final Entry<String, Object> entry : beansMap.entrySet()) {
      final Object instance = entry.getValue();
      if (instance instanceof DestroyingBean) {
        ((DestroyingBean) instance).onDestroy(this);
      }
    }
    beansMap.clear();
  }

  @Override
  public boolean containsBean(final String name) {
    return beansMap.containsKey(name);
  }

  @Override
  public void removeEntityInstance(final String name) {
    final Object instance = beansMap.remove(name);
    if (instance == null) {
      throw new IllegalArgumentException("Bean " + name + " is not found in beans container");
    }
    if (DEBUG) { Log.d(TAG, "Remove bean: " + name + " - " + instance.getClass()); }
  }

  @Override
  public void removeEntityInstance(final Object instance) {
    String name = null;
    for (Entry<String, Object> pair : beansMap.entrySet()) {
      if (pair.getValue() == instance) {
        name = pair.getKey();
        break;
      }
    }
    removeEntityInstance(name);
  }

  @Override
  public String putTemporaryInstance(final Object instance) {
    String beanName = instance.getClass() + "-" + System.currentTimeMillis() + "-" + (++idCounter);
    putEntityInstance(beanName, instance);
    return beanName;
  }

}
