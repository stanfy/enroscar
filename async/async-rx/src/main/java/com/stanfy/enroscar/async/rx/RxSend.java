package com.stanfy.enroscar.async.rx;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Same as {@link com.stanfy.enroscar.async.Send} but ensures that operator deals with
 * {@link rx.Observable} even if an annotated method return type is
 * {@link com.stanfy.enroscar.async.Async}.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface RxSend {
}
