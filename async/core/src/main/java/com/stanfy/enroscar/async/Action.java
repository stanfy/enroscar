package com.stanfy.enroscar.async;

/**
 * Action that processes some data. Boilerplate because of lack of lambdas.
 * Used to define what action should be taken when a subscribed observer is invoked.
 */
public interface Action<D> {

  void act(D data);

}
