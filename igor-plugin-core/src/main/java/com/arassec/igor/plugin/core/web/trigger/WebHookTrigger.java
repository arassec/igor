package com.arassec.igor.plugin.core.web.trigger;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseEventTrigger;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.plugin.core.CoreCategory;
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
        super(CoreCategory.WEB.getId(), "web-hook-trigger");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventType getSupportedEventType() {
        return EventType.WEB_HOOK;
    }

}
