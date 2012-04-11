package com.stanfy.enroscar.test;

import android.os.Build;
import android.test.AndroidTestCase;

import com.stanfy.utils.AppUtils;
import com.stanfy.utils.SDKDependentUtils;

/**
 * Tests SDK dependent utils.
 * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
 */
public class SDKDependentUtilsTest extends AndroidTestCase {

  public final void testGetSdkDependentUtils() {
    final int version = Build.VERSION.SDK_INT;
    String clazName = null;
    if (version >= Build.VERSION_CODES.HONEYCOMB) {
      clazName = "com.stanfy.utils.HoneycombUtils";
    } else if (version >= Build.VERSION_CODES.GINGERBREAD) {
      clazName = "com.stanfy.utils.GingerbreadUtils";
    } else if (version >= Build.VERSION_CODES.ECLAIR) {
      clazName = "com.stanfy.utils.EclairUtils";
    } else {
      clazName = "com.stanfy.utils.LowestSDKDependentUtils";
    }
    final SDKDependentUtils utils = AppUtils.getSdkDependentUtils();
    assertNotNull(utils);
    assertEquals(
        "Failed to load proper SDK utils: ",
        clazName ,
        utils.getClass().getName()
        );
  }

}
