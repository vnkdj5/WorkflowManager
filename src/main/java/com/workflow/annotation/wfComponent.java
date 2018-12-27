package com.workflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface wfComponent {

	//for checking whether component class is complete or not. Developer has to set it to true for completed component class
	public boolean complete() default false;
}
