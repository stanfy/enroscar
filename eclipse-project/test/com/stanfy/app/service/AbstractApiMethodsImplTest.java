package com.stanfy.app.service;

import android.content.Intent;
import android.os.IBinder;

/**
 * Basic tests for  {@link ApiMethodsImpl}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class AbstractApiMethodsImplTest extends AbstractAppServiceTest {

  /** API methods implementation. */
  ApiMethodsImpl apiMethodsImpl;

  @Override
  public void setup() {
    super.setup();
    final Intent bindIntent = new Intent(ApiMethods.class.getName());
    final IBinder binder = appService.onBind(bindIntent);
    apiMethodsImpl = (ApiMethodsImpl)binder;
  }


}
