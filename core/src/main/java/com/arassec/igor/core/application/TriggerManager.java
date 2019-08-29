package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.TriggerFactory;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Manages {@link Trigger}s. Entry point from outside the core package to create and maintain triggers.
 */
@Slf4j
@Component
public class TriggerManager extends ModelManager<Trigger> {

    /**
     * Creates a new instance.
     *
     * @param triggerFactory The factory to create {@link Trigger}s.
     */
    public TriggerManager(TriggerFactory triggerFactory) {
        super(triggerFactory);
    }

}
