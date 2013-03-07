package com.stanfy.enroscar.assist;

import android.app.Application;
import android.content.Context;

import com.stanfy.enroscar.beans.BeansManager;

/**
 * Default beans manager. Gives access to all standard beans provided by Enroscar library.
 */
public class DefaultBeansManager extends BeansManager {

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
  public static DefaultBeansManager use(final Context context) {
    setFactory(new Factory() {
      @Override
      public BeansManager createBeansManager(final Application app) {
        return new DefaultBeansManager(app);
      }
    });
    return get(context);
  }
  
  public static DefaultBeansManager get(final Context context) {
    return (DefaultBeansManager) BeansManager.get(context);
  }
  
//  /** @return images manager instance */
//  public ImagesManager getImagesManager() { return container.getBean(ImagesManager.BEAN_NAME, ImagesManager.class); }
//  /** @return main buffers pool instance */
//  public BuffersPool getMainBuffersPool() { return container.getBean(BuffersPool.BEAN_NAME, BuffersPool.class); }
//  /** @return image memory cache instance */
//  public ImageMemoryCache getImageMemoryCache() { return container.getBean(ImageMemoryCache.BEAN_NAME, ImageMemoryCache.class); }
//  /** @return response cache instance */
//  public ResponseCache getResponseCache(final String name) { return container.getBean(name, ResponseCache.class); }
//  /** @return crucial GUI operation manager */
//  public CrucialGUIOperationManager getCrucialGUIOperationManager() { return container.getBean(CrucialGUIOperationManager.BEAN_NAME, CrucialGUIOperationManager.class); }
//  /** @return activity behavior factory */
//  public ActivityBehaviorFactory getActivityBehaviorFactory() { return container.getBean(ActivityBehaviorFactory.BEAN_NAME, ActivityBehaviorFactory.class); }
//  /** @return statistics manager */
//  public StatsManager getStatsManager() { return container.getBean(StatsManager.BEAN_NAME, StatsManager.class); }
//  /** @return SDK dependent utilities factory */
//  public SDKDependentUtilsFactory getSdkDependentUtilsFactory() { return container.getBean(SDKDependentUtilsFactory.BEAN_NAME, SDKDependentUtilsFactory.class); }
//  /** @return remote server API access configuration */
//  public RemoteServerApiConfiguration getRemoteServerApiConfiguration() { return container.getBean(RemoteServerApiConfiguration.BEAN_NAME, RemoteServerApiConfiguration.class); }
//  /** @return content handler instance */
//  public ContentHandler getContentHandler(final String name) { return container.getBean(name, ContentHandler.class); }
//  /** @return application database manager instance */
//  public AppDatabaseManager getAppDatabaseManager() { return container.getBean(AppDatabaseManager.class); }

  @Override
  public Editor edit() { return new Editor(); }
  
  /**
   * Editor for {@link DefaultBeansManager}. 
   * Allows to easily add standard beans provided by Enroscar libraries.
   */
  public class Editor extends BeansManager.Editor {
    
//    public Editor required() {
//      put(SDKDependentUtilsFactory.class);
//      put(BuffersPool.class);
//      put(EmptyStatsManager.class);
//      return this;
//    }
//
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
//    public Editor remoteServerApi(final EnroscarConnectionsEngine.Config config, final String... formats) {
//      put(RemoteServerApiConfiguration.class);
//      if (formats.length > 0) {
//
//        Class<?> mainClass = null;
//        for (final String format : formats) {
//          Class<?> handlerClass = null;
//
//          if (SimpleRequestBuilder.JSON.equals(format)) {
//            handlerClass = GsonContentHandler.class;
//          } else if (SimpleRequestBuilder.XML.equals(format)) {
//            handlerClass = XmlGsonContentHandler.class;
//          } else if (SimpleRequestBuilder.STRING.equals(format)) {
//            handlerClass = StringContentHandler.class;
//          }
//
//          if (handlerClass == null) {
//            throw new RuntimeException("Unknown format " + format);
//          }
//
//          if (mainClass == null) { mainClass = handlerClass; }
//          put(handlerClass);
//        }
//
//        final String mainContentHandlerName = AppUtils.getBeanInfo(mainClass).value();
//        editorActions.put("remoteServerApi-config", new PutBean() {
//          @Override
//          public Object put() {
//            // set default content handler
//            getContainer().getBean(RemoteServerApiConfiguration.BEAN_NAME, RemoteServerApiConfiguration.class)
//            .setDefaultContentHandlerName(mainContentHandlerName);
//
//            // install connections engine
//            config.install(application);
//
//            return null;
//          }
//        });
//
//      }
//      return this;
//    }
//
//    public Editor remoteServerApi(final String... formats) {
//      return remoteServerApi(EnroscarConnectionsEngine.config(), formats);
//    }
//
//    public Editor defaults() {
//      required();
//      images();
//      activitiesBehavior();
//      remoteServerApi();
//      return this;
//    }
    
  }
  

}
