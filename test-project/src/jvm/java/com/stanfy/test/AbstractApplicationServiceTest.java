package com.stanfy.test;

import java.lang.Thread.UncaughtExceptionHandler;

import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;

import com.stanfy.app.beans.BeansManager.Editor;
import com.stanfy.app.loader.RequestBuilderLoader;
import com.stanfy.enroscar.net.test.AbstractMockServerTest;
import com.stanfy.enroscar.rest.test.loader.LoaderAccess;
import com.stanfy.enroscar.shared.test.EnroscarConfiguration;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.test.Application.ApplicationService;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowLooper;

/**
 * Base class for tests that require {@link ApplicationService} communication.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 */
@EnroscarConfiguration(connectionEngineRequired = true)
public abstract class AbstractApplicationServiceTest extends AbstractMockServerTest {

  @Override
  protected void configureBeansManager(final Editor editor) {
    editor.required().remoteServerApi();
  }

  @Override
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    final Application application = (Application)Robolectric.application;
    application.setAppServiceInstance(createApplicationService(application));
  }

  protected ApplicationService createApplicationService(final Application app) { return app.new ApplicationService(); }

}
