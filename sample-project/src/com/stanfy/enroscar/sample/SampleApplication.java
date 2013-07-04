package com.stanfy.enroscar.sample;

import android.app.Application;
import android.util.Log;

import com.stanfy.enroscar.activities.ActivityBehaviorFactory;
import com.stanfy.enroscar.activities.CrucialGUIOperationManager;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.images.ViewImageConsumerFactory;
import com.stanfy.enroscar.images.ImagesManager;
import com.stanfy.enroscar.images.cache.ImageFileCache;
import com.stanfy.enroscar.images.cache.SupportLruImageMemoryCache;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.net.EnroscarConnectionsEngine;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.rest.response.handler.GsonContentHandler;
import com.stanfy.enroscar.rest.response.handler.StringContentHandler;
import com.stanfy.enroscar.sdkdep.SDKDependentUtilsFactory;
import com.stanfy.enroscar.stats.EmptyStatsManager;
import com.stanfy.enroscar.views.ImageConsumers;

/**
 * Sample application.
 */
public class SampleApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

      Log.d("Test", "Start SampleApp");


    BeansManager manager = BeansManager.get(this);
    manager.edit()
           .put(SDKDependentUtilsFactory.class)
           .put(BuffersPool.class)
           .put(EmptyStatsManager.class)
           .put(ActivityBehaviorFactory.class)
           .put(ImagesManager.IMAGE_CONSUMER_FACTORY_NAME, new ImageConsumers())
           .put(ImageFileCache.class)
           .put(SupportLruImageMemoryCache.class)
           .put(ImagesManager.class)
           .put(CrucialGUIOperationManager.class)
           .put("defaultContentHandlerName", new GsonContentHandler(this))
           .put(RemoteServerApiConfiguration.class)
           .commit();

//            .defaults().remoteServerApi("json").commit();


      RemoteServerApiConfiguration config = manager.getContainer().getBean(RemoteServerApiConfiguration.class);
      config.setDefaultContentHandlerName("defaultContentHandlerName");

      EnroscarConnectionsEngine.config().install(this);

      Log.d("Test", "DD: " + manager.getContainer().getBean(ActivityBehaviorFactory.class));
  }

}
