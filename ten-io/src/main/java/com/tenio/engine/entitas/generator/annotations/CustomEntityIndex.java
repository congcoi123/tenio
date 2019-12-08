package com.tenio.engine.entitas.generator.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rubentxu
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ TYPE, FIELD, METHOD })
public @interface CustomEntityIndex {

	@SuppressWarnings("rawtypes") Class contextType();

}
