package com.stanfy.enroscar.goro.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static java.lang.annotation.ElementType.FIELD;

/**
 * TODO
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
@Retention(SOURCE)
@Target(FIELD)
public @interface Bound {
}
