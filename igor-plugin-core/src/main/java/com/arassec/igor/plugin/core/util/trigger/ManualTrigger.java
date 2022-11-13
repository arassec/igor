package com.arassec.igor.plugin.core.util.trigger;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseTrigger;
import com.arassec.igor.plugin.core.CoreCategory;

/**
 * <h2>Manual Trigger</h2>
 *
 * <h3>Description</h3>
 * This trigger doesn't start a job automatically. The user has to start the job manually each time.
 */
@IgorComponent(typeId = "manual-trigger", categoryId = CoreCategory.UTIL)
public class ManualTrigger extends BaseTrigger {
}
