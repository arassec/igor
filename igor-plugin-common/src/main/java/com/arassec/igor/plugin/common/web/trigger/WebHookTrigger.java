package com.arassec.igor.plugin.common.web.trigger;

import com.arassec.igor.core.model.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseEventTrigger;
import com.arassec.igor.plugin.common.CommonCategory;
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
        super(CommonCategory.WEB.getId(), "web-hook-trigger");
    }

}
