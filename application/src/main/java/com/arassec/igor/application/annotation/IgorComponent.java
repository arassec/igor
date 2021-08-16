package com.arassec.igor.application.annotation;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class as igor component. It can be used on classes that inherit from {@link
 * com.arassec.igor.core.model.trigger.Trigger}, {@link com.arassec.igor.core.model.action.Action} or {@link
 * com.arassec.igor.core.model.connector.Connector}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Scope("prototype")
public @interface IgorComponent {

    /**
     * Name of the Type-ID member of this annotation. Used e.g. during documentation generation.
     */
    String TYPE_ID = "typeId";

    /**
     * The default category of any component. Used if no other can be determined.
     */
    String DEFAULT_CATEGORY = "miscellaneous";

    /**
     * The component's type ID.
     *
     * @return The type ID.
     */
    String typeId();

    /**
     * The component's category ID.
     *
     * @return The type ID.
     */
    String categoryId() default DEFAULT_CATEGORY;

}
