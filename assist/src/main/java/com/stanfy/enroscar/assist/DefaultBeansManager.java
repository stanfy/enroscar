package com.stanfy.enroscar.assist;

import java.net.ContentHandler;
import java.net.ResponseCache;

import android.app.Application;
import android.content.Context;

import com.stanfy.enroscar.beans.BeanUtils;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.rest.request.SimpleRequestBuilder;
import com.stanfy.enroscar.rest.response.handler.GsonContentHandler;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;
import com.stanfy.enroscar.rest.response.handler.XmlGsonContentHandler;
import com.stanfy.enroscar.stats.StatsManager;

/**
 * Default beans manager. Gives access to all standard beans provided by Enroscar library.
 */
public class DefaultBeansManager extends BeansManager {

  /** Flag that indicates that manager factory has been set. */
  private static boolean configured = false;
  
  /**
   * Main constructor.
   * @param application Android application instance
   * @see BeansManager
   */
  protected DefaultBeansManager(final Application application) {
    super(application);
  }

  /**
   * Creates an instance of {@link DefaultBeansManager} and configures
   * the method {@link BeansManager#get(Context)} to return this instance.
   * @param context application context
   * @return newly create beans manager instance
   */
  public static DefaultBeansManager get(final Context context) {
    if (!configured) {
      setFactory(new Factory() {
        @Override
        public BeansManager createBeansManager(final Application app) {
          return new DefaultBeansManager(app);
        }
      });
      configured = true;
    }
    return (DefaultBeansManager) BeansManager.get(context);
  }
  
//  /** @return images manager instance */
//  public ImagesManager getImagesManager() { return getContainer().getBean(ImagesManager.BEAN_NAME, ImagesManager.class); }
  /** @return main buffers pool instance */
  public BuffersPool getMainBuffersPool() { return getContainer().getBean(BuffersPool.BEAN_NAME, BuffersPool.class); }
//  /** @return image memory cache instance */
//  public ImageMemoryCache getImageMemoryCache() { return getContainer().getBean(ImageMemoryCache.BEAN_NAME, ImageMemoryCache.class); }
  /** @return response cache instance */
  public ResponseCache getResponseCache(final String name) { return getContainer().getBean(name, ResponseCache.class); }
//  /** @return crucial GUI operation manager */
//  public CrucialGUIOperationManager getCrucialGUIOperationManager() { return getContainer().getBean(CrucialGUIOperationManager.BEAN_NAME, CrucialGUIOperationManager.class); }
//  /** @return activity behavior factory */
//  public ActivityBehaviorFactory getActivityBehaviorFactory() { return getContainer().getBean(ActivityBehaviorFactory.BEAN_NAME, ActivityBehaviorFactory.class); }
  /** @return statistics manager */
  public StatsManager getStatsManager() { return getContainer().getBean(StatsManager.BEAN_NAME, StatsManager.class); }
//  /** @return SDK dependent utilities factory */
//  public SDKDependentUtilsFactory getSdkDependentUtilsFactory() { return getContainer().getBean(SDKDependentUtilsFactory.BEAN_NAME, SDKDependentUtilsFactory.class); }
  /** @return remote server API access configuration */
  public RemoteServerApiConfiguration getRemoteServerApiConfiguration() { return getContainer().getBean(RemoteServerApiConfiguration.BEAN_NAME, RemoteServerApiConfiguration.class); }
  /** @return content handler instance */
  public ContentHandler getContentHandler(final String name) { return getContainer().getBean(name, ContentHandler.class); }

  @Override
  public Editor edit() { return new Editor(); }
  
  /**
   * Editor for {@link DefaultBeansManager}. 
   * Allows to easily add standard beans provided by Enroscar libraries.
   */
  public class Editor extends BeansManager.Editor {

    {
//      put(SDKDependentUtilsFactory.class);
      put(BuffersPool.class);
//      put(EmptyStatsManager.class);
    }
    
//    public Editor images(final EnroscarConnectionsEngine.Config config) {
//      put(ImageFileCache.class);
//      put(SupportLruImageMemoryCache.class);
//      put(ImagesManager.class);
//
//      editorActions.put("images-connections-config", new PutBean() {
//        @Override
//        public Object put() {
//          // install connections engine
//          config.install(application);
//          return null;
//        }
//      });
//      return this;
//    }
//
//    public Editor images() { return images(EnroscarConnectionsEngine.config()); }
//
//    public Editor activitiesBehavior() {
//      put(ActivityBehaviorFactory.class);
//      put(CrucialGUIOperationManager.class);
//      return this;
//    }
//
    public Editor remoteServerApi(final EnroscarConnectionsEngine.Config config, final String... formats) {
      put(RemoteServerApiConfiguration.class);
      if (formats.length > 0) {

        Class<?> mainClass = null;
        for (final String format : formats) {
          Class<?> handlerClass = null;

          if (SimpleRequestBuilder.JSON.equals(format)) {
            handlerClass = GsonContentHandler.class;
          } else if (SimpleRequestBuilder.XML.equals(format)) {
            handlerClass = XmlGsonContentHandler.class;
          } else if (SimpleRequestBuilder.STRING.equals(format)) {
            handlerClass = StringContentHandler.class;
          }

          if (handlerClass == null) {
            throw new RuntimeException("Unknown format " + format);
          }

          if (mainClass == null) { mainClass = handlerClass; }
          put(handlerClass);
        }

        final String mainContentHandlerName = BeanUtils.getBeanInfo(mainClass).value();
        getEditorActions().put("remoteServerApi-config", new PutBean() {
          @Override
          public Object put() {
            // set default content handler
            getContainer().getBean(RemoteServerApiConfiguration.BEAN_NAME, RemoteServerApiConfiguration.class)
            .setDefaultContentHandlerName(mainContentHandlerName);

            // install connections engine
            config.install(getApplication());

            return null;
          }
        });

      }
      return this;
    }

    public Editor remoteServerApi(final String... formats) {
      return remoteServerApi(EnroscarConnectionsEngine.config(), formats);
    }

//    public Editor defaults() {
//      required();
//      images();
//      activitiesBehavior();
//      remoteServerApi();
//      return this;
//    }
    
  }
  

}
