package com.stanfy.enroscar.beans;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;

/**
 * Beans manager.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class BeansManager {

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_BEANS_CREATE;

  /** Logging tag. */
  private static final String TAG = "Beans";

  /** Old SDK flag. */
  private static final boolean OLD_SDK = VERSION.SDK_INT < VERSION_CODES.ICE_CREAM_SANDWICH;

  /** Singleton instance. */
  private static BeansManager instance;

  /** Beans manager factory. */
  private static Factory factory;
  
  /** Application instance. */
  private final Application application;

  /** Container. */
  private final BeansContainer container;

  /** Register callbacks flag. */
  private boolean callbacksRegistered = false;

  /** Whether commit is currently performed. */
  private boolean commitInProgress = false;

  /** Postponed edit actions. */
  private LinkedHashMap<String, PutBean> postponedActions;

  /**
   * Main constructor. Stores reference to an application object and created a {@link BeansContainer}.
   * @param application Android application instance
   */
  protected BeansManager(final Application application) {
    this.application = application;
    this.container = createContainer(application);
  }

  public static synchronized BeansManager get(final Context context) {
    if (instance == null) {
      if (context == null) {
        throw new IllegalArgumentException("Cannot create a beans manager without Android context. "
            + "'context' parameter in BeansManager.get(context) is null.");
      }
      final Context appContext = context.getApplicationContext();
      
      if (appContext != null && appContext instanceof Application) {
        Application app = (Application) appContext;
        instance = factory != null ? factory.createBeansManager(app) : new BeansManager(app);
      } else {
        throw new IllegalArgumentException("Context " + context + " does not provide reference to the application");
      }
      
    }
    return instance;
  }

  public static synchronized void setFactory(final Factory factory) {
    if (factory == null) {
      throw new IllegalArgumentException("Factory cannot be null");
    }
    if (BeansManager.factory != null) {
      throw new IllegalStateException("Beans manager factory is already set");
    }
    if (BeansManager.instance != null) {
      throw new IllegalStateException("Beans manager has already bean created");
    }
    BeansManager.factory = factory;
  }
  
  /**
   * Call this method from {@link Application#onLowMemory()} to integrate enroscar on pre-ICS versions.
   * @param context application context
   */
  public static void onLowMemory(final Context context) {
    if (OLD_SDK) {
      get(context).container.onLowMemory();
    }
  }
  /**
   * Call this method from {@link Application#onConfigurationChanged(Configuration)} to integrate enroscar on pre-ICS versions.
   * @param context application context
   * @param newConfig new configuration
   */
  public static void onConfigurationChanged(final Context context, final Configuration newConfig) {
    if (OLD_SDK) {
      get(context).container.onConfigurationChanged(newConfig);
    }
  }

  /**
   * Integrate with component callbacks. It calls {@link Application#registerComponentCallbacks(android.content.ComponentCallbacks)},
   * so that it's useless on prior ICS SDKs.
   */
  void registerComponentCallbacks() {
    if (callbacksRegistered) { return; } // do it once only
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      application.registerComponentCallbacks(this.container);
    }
    callbacksRegistered = true;
  }

  /**
   * @param application application instance
   * @return beans container implementation
   */
  protected BeansContainer createContainer(final Application application) {
    return new DefaultBeansContainer();
  }

  public Application getApplication() { return application; }
  public BeansContainer getContainer() { return container; }

  /** Destroy the beans manager. */
  public final void destroy() {
    instance = null;
  }
  
  /** @return beans editor instance */
  public Editor edit() { return new Editor(); }

  /**
   * Beans editor.
   */
  public class Editor {
    /** Container instance. */
    final BeansContainer container = getContainer();

    /** Editor actions. */
    private final LinkedHashMap<String, PutBean> editorActions = new LinkedHashMap<String, PutBean>();

    /**
     * @return actions map to commit
     */
    protected final LinkedHashMap<String, PutBean> getEditorActions() {
      return editorActions;
    }
    
    private void checkIntrfacesOnCreate(final Object bean) {
      if (bean instanceof ManagerAwareBean) {
        ((ManagerAwareBean) bean).setBeansManager(BeansManager.this);
      }
    }
    private void checkIntrfacesOnInit(final Object bean) {
      if (bean instanceof InitializingBean) {
        ((InitializingBean) bean).onInitializationFinished(container);
      }
    }

    public boolean hasBean(final String name) {
      return container.containsBean(name) || editorActions.containsKey(name);
    }

    public <T> Editor put(final String name, final T bean) {
      editorActions.put(name, new PutBean() {
        @Override
        public Object put() {
          container.putEntityInstance(name, bean);
          return bean;
        }
      });
      return this;
    }
    public <T> Editor put(final Class<T> beanClass) {
      final EnroscarBean info = BeanUtils.getBeanInfo(beanClass);
      editorActions.put(info.value(), new PutBean() {
        @Override
        public Object put() {
          return container.putEntityInstance(beanClass, application);
        }
      });
      return this;
    }
    public <T> Editor put(final T bean) {
      final EnroscarBean info = BeanUtils.getBeanInfo(bean.getClass());
      editorActions.put(info.value(), new PutBean() {
        @Override
        public Object put() {
          container.putEntityInstance(bean);
          return bean;
        }
      });
      return this;
    }

    private void performActions(final Map<String, PutBean> editorActions) {
      final long start = System.currentTimeMillis();
      ArrayList<Object> editedBeans = new ArrayList<Object>(editorActions.size());

      for (final Entry<String, PutBean> entry : editorActions.entrySet()) {
        final long startAction = System.currentTimeMillis();
        Object bean = entry.getValue().put();
        if (bean != null) {
          checkIntrfacesOnCreate(bean);
          editedBeans.add(bean);
        }
        if (DEBUG) { Log.d(TAG, "One action time: " + (System.currentTimeMillis() - startAction)); }
      }
      if (DEBUG) { Log.d(TAG, "Run actions time: " + (System.currentTimeMillis() - start)); }

      if (DEBUG) { Log.d(TAG, "Before init time: " + (System.currentTimeMillis() - start)); }
      for (int i = 0; i < editedBeans.size(); i++) {
        checkIntrfacesOnInit(editedBeans.get(i));
      }
      if (DEBUG) { Log.d(TAG, "After init time: " + (System.currentTimeMillis() - start)); }
    }

    /**
     * Commit all bean changes.
     * This method is supposed to be called from the main thread.
     */
    public void commit() {
      if (commitInProgress) {
        // postpone commit
        if (postponedActions == null) {
          postponedActions = new LinkedHashMap<String, PutBean>();
        }
        postponedActions.putAll(editorActions);
        return;
      }

      final long start = System.currentTimeMillis();
      commitInProgress = true;

      try {

        performActions(editorActions);

        registerComponentCallbacks();

        if (postponedActions != null && !postponedActions.isEmpty()) {
          performActions(postponedActions);
          postponedActions.clear();
        }

      } finally {
        commitInProgress = false;
        if (DEBUG) { Log.d(TAG, "All commit time: " + (System.currentTimeMillis() - start)); }
      }
    }

  }

  /** Put bean operation. */
  protected interface PutBean {
    /**
     * Perform bean editor operation: put bean into container.
     * @return bean instance
     */
    Object put();
  }

  /**
   * Factory that can create a {@link BeansManager}.
   */
  public interface Factory {
    BeansManager createBeansManager(Application app);
  }
  
}
