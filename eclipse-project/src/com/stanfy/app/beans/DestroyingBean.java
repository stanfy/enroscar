package com.stanfy.app.beans;

/**
 * Destroying bean.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public interface DestroyingBean extends Bean {

  void onDestroy();

}
