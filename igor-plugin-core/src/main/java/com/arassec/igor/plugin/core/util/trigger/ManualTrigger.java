package com.arassec.igor.plugin.core.util.trigger;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseTrigger;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.CoreType;

/**
 * <h2>Manual Trigger</h2>
 *
 * <h3>Description</h3>
 * This trigger doesn't start a job automatically. The user has to start the job manually each time.
 */
@IgorComponent(categoryId = CoreCategory.UTIL, typeId = CoreType.MANUAL_TRIGGER)
public class ManualTrigger extends BaseTrigger {
}
