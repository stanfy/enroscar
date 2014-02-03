package com.stanfy.enroscar.net;

import android.os.Parcelable;

/**
 * Operates on some subject.
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface Predicate<T> extends Parcelable {

  void apply(T subject);

}
