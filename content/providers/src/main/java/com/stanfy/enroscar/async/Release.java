package com.stanfy.enroscar.async;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Annotates a method which releases data loaded by an operation created with {@link Load} method.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface Release {
}
