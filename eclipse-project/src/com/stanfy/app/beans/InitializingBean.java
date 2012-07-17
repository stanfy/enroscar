package com.stanfy.app.beans;

/**
 * Interface for beans that require some final initialization.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface InitializingBean extends Bean {

  /**
   * Called when all beans are initialized.
   * @param beansContainer beans container instance
   */
  void onInitializationFinished(final BeansContainer beansContainer);

}
