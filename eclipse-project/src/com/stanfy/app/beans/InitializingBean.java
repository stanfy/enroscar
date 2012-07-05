package com.stanfy.app.beans;

/**
 * Interface for beans that require some final initialization.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface InitializingBean {

  /**
   * Called when all beans are initialized.
   */
  void onInititializationFinished();

}
