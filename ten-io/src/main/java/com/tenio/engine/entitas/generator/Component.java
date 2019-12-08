package com.tenio.engine.entitas.generator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rubentxu
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Component {

	String[] pools() default { "Pool" };

	boolean isSingleEntity() default false;

	String customPrefix() default "";

	String[] customComponentName() default { "" };

}
