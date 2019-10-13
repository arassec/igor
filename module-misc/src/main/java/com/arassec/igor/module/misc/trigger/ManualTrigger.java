package com.arassec.igor.module.misc.trigger;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Trigger for manual job executions.
 */
@Component
@Scope("prototype")
public class ManualTrigger extends BaseMiscTrigger {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeId() {
        return "4af90cde-1da2-4d1e-a582-21443af3955b";
    }

}
