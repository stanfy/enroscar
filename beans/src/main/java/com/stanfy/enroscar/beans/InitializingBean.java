package com.stanfy.enroscar.beans;

/**
 * Interface for beans that require some final initialization.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 * @deprecated Beans subsystem is deprecated now. Use some dependency injection framework (e.g. Dagger) instead.
 */
@Deprecated
public interface InitializingBean extends Bean {

  /**
   * Called when all beans are initialized.
   * @param beansContainer beans container instance
   */
  void onInitializationFinished(final BeansContainer beansContainer);

}
