package com.stanfy.enroscar.async;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;

/**
 * This interface contains methods you can use to build an operator object.
 * You should not implement this class.
 */
public interface OperatorBuilder<T, W> {

  OperatorBuilder<T, W> operations(W operations);

  OperatorBuilder<T, W> loaderManager(LoaderManager loaderManager);

  OperatorBuilder<T, W> context(Context context);

  OperatorBuilder<T, W> withinActivity(FragmentActivity activity);

  OperatorBuilder<T, W> withinFragment(Fragment fragment);

  T get();

}
