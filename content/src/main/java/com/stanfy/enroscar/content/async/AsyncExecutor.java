package com.stanfy.enroscar.content.async;

import android.content.Context;

/**
 * @param <D> data type
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface AsyncExecutor<D> {

  Async<D> startExecution();

  Context provideContext();

}
