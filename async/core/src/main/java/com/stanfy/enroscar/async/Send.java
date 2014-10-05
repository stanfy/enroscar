package com.stanfy.enroscar.async;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Annotates a method that builds an asynchronous operation that sends data somewhere.
 * Result of such an operation is not cached.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface Send {
}
