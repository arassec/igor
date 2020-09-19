package com.arassec.igor.core.model.annotation;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class as igor component. It can be used on classes that inherit from
 * {@link com.arassec.igor.core.model.trigger.Trigger}, {@link com.arassec.igor.core.model.action.Action} or
 * {@link com.arassec.igor.core.model.connector.Connector}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Scope("prototype")
public @interface IgorComponent {
}
