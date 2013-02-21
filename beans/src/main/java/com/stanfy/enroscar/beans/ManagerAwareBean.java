package com.stanfy.app.beans;

/**
 * Manager aware bean.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface ManagerAwareBean extends Bean {

  /** @param beansManager beans manager */
  void setBeansManager(final BeansManager beansManager);

}
