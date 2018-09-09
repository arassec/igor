package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

/**
 * Factory for Actions.
 */
@Slf4j
@Component
public class ActionFactory extends ModelFactory<Action> {

    public ActionFactory() {
        super(Action.class, IgorAction.class);
    }

}
