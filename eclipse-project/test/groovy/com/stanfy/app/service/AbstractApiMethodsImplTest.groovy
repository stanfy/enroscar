package com.stanfy.app.service


import android.content.Intent;
import android.os.IBinder;

/**
 * Base Groovy test for {@link ApiMethodsImpl}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
abstract class AbstractApiMethodsImplTest extends AbstractAppServiceTest {

  /** API methods instance. */
  ApiMethodsImpl apiMethodsImpl

  @Override
  void setup() {
    super.setup();
    final Intent bindIntent = new Intent(ApiMethods.class.getName());
    final IBinder binder = appService.onBind(bindIntent);
    apiMethodsImpl = (ApiMethodsImpl)binder;
    assertThat apiMethodsImpl, notNullValue()
  }

}
