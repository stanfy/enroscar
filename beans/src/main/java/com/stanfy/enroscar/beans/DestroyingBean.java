package com.stanfy.enroscar.beans;

/**
 * Destroying bean.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 * @deprecated Beans subsystem is deprecated now. Use some dependency injection framework (e.g. Dagger) instead.
 */
@Deprecated
public interface DestroyingBean extends Bean {

  /**
   * @param beansContainer beans container instance
   */
  void onDestroy(final BeansContainer beansContainer);

}
