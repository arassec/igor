package com.arassec.igor.module.web.trigger;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseEventTrigger;
import lombok.extern.slf4j.Slf4j;

/**
 * Event based trigger that is triggered when the corresponding web-hook is called.
 */
@Slf4j
@IgorComponent
public class WebHookTrigger extends BaseEventTrigger {

    /**
     * Creates a new component instance.
     */
    public WebHookTrigger() {
        super("web-triggers", "web-hook-trigger");
    }

}
