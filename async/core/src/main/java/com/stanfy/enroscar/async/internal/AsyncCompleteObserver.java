package com.stanfy.enroscar.async.internal;

import com.stanfy.enroscar.async.AsyncObserver;

/**
 * Extends {@link AsyncObserver} provisind onCompleted method.
 * The interface is used from {@code onReset()} method of loader callbacks.
 * Used by {@code async-rx} module to map {@code onReset()}
 * to {@code Observer}'s {@code onCompleted}.
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
interface AsyncCompleteObserver<D> extends AsyncObserver<D> {

  void onCompleted();

}
