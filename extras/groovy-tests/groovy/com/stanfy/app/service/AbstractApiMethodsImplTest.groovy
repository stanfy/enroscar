package com.stanfy.app.service


import android.content.Intent

/**
 * Base Groovy test for {@link ApiMethodsImpl}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
abstract class AbstractApiMethodsImplTest extends AbstractAppServiceTest {

  /** API methods instance. */
  ApiMethods apiMethods

  @Override
  void setup() {
    super.setup()
    final Intent bindIntent = new Intent(ApiMethods.class.getName())
    apiMethods = appService.onBind(bindIntent).apiMethods
    assertThat apiMethods, notNullValue()
  }

}
