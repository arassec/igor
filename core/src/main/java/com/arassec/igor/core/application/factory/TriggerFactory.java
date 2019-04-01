package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.model.IgorTrigger;
import com.arassec.igor.core.model.IgorTriggerCategory;
import com.arassec.igor.core.model.trigger.Trigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory for Triggers.
 */
@Slf4j
@Component
public class TriggerFactory extends ModelFactory<Trigger> {

    /**
     * Creates a new {@link TriggerFactory}.
     */
    public TriggerFactory() {
        super(Trigger.class, IgorTriggerCategory.class, IgorTrigger.class);
    }

}
