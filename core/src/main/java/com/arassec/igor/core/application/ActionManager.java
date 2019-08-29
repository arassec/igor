package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ActionFactory;
import com.arassec.igor.core.model.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Manages actions. Entry point from outside the core package to create and maintain actions.
 */
@Slf4j
@Component
public class ActionManager extends ModelManager<Action> {

    /**
     * Creates a new instance.
     *
     * @param actionFactory The factory for {@link Action}s.
     */
    public ActionManager(ActionFactory actionFactory) {
        super(actionFactory);
    }

}
