package com.stanfy.enroscar.goro.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Annotates a method which invocation should cause binding to
 * {@link com.stanfy.enroscar.goro.GoroService}.
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface BindGoro {
}
