package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.IgorActionCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory for Actions.
 */
@Slf4j
@Component
public class ActionFactory extends ModelFactory<Action> {

    /**
     * Creates a new {@link ActionFactory}.
     */
    public ActionFactory() {
        super(Action.class, IgorActionCategory.class, IgorAction.class);
    }

}
