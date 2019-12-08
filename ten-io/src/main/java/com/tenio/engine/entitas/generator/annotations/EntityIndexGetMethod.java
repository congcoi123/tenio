package com.tenio.engine.entitas.generator.annotations;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rubentxu
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ METHOD })
public @interface EntityIndexGetMethod {

}
