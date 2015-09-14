package com.stanfy.enroscar.beans;

/**
 * Manager aware bean.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 * @deprecated Beans subsystem is deprecated now. Use some dependency injection framework (e.g. Dagger) instead.
 */
@Deprecated
public interface ManagerAwareBean extends Bean {

  /** @param beansManager beans manager */
  void setBeansManager(final BeansManager beansManager);

}
