package com.stanfy.enroscar.async.internal;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;

/** Stub. */
public class AsyncStub implements Async<String> {
  @Override
  public void subscribe(final AsyncObserver<String> observer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void cancel() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Async<String> replicate() {
    return new AsyncStub();
  }
}
