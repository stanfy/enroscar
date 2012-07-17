package com.stanfy.app.beans;

/**
 * Destroying bean.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface DestroyingBean extends Bean {

  /**
   * @param beansContainer beans container instance
   */
  void onDestroy(final BeansContainer beansContainer);

}
