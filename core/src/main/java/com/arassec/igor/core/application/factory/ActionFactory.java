package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

/**
 * Factory for Actions.
 */
@Component
public class ActionFactory extends ModelFactory<Action> {

    private static final Logger LOG = LoggerFactory.getLogger(ActionFactory.class);

    public ActionFactory() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(IgorAction.class));
        // TODO: Get this from "Igorfile"...
        for (BeanDefinition beanDefinition : scanner.findCandidateComponents("com.arassec.igor")) {
            try {
                Class<?> c = Class.forName(beanDefinition.getBeanClassName());
                if (Action.class.isAssignableFrom(c)) {
                    String className = beanDefinition.getBeanClassName();
                    typeToClass.put(Class.forName(className).getAnnotation(IgorAction.class).type(), className);
                    LOG.debug("Registered action: {}", beanDefinition.getBeanClassName());
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }


}
