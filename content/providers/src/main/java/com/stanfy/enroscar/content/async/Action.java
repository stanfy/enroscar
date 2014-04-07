package com.stanfy.enroscar.content.async;

/**
 * Action that processes some data. Boilerplate because of lack of lambdas.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface Action<D> {

  void act(D data);

}
