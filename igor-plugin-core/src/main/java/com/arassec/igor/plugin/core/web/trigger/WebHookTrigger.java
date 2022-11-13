package com.arassec.igor.plugin.core.web.trigger;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.trigger.BaseEventTrigger;
import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.plugin.core.CoreCategory;
import lombok.extern.slf4j.Slf4j;

/**
 * <h2>Web-Hook Trigger</h2>
 *
 * <h3>Description</h3>
 * The web-hook trigger is an event based trigger that runs a job after an HTTP request has been made to igor's web-hook
 * interface.<br>
 * <p>
 * Igor's web-hook interface can be called with HTTP-GET or HTTP-POST requests. The call's parameters will be added to the job's
 * data items.<br>
 * <p>
 * The interface is reachable under the following URL:
 *
 * <pre><code>
 * http(s)://[HOST:PORT]/webhook/[JOB-ID]
 * </code></pre>
 *
 * <h3>Examples</h3>
 * Let's assume igor is running on localhost and reachable under port 8080.
 * <p>
 * A job with ID `01d11f89-1b89-4fa0-8da4-cdd75229f8b5`, and the web-hook trigger attached to it, can be triggered by calling:
 * <pre><code>
 * http://localhost:8080/webhook/01d11f89-1b89-4fa0-8da4-cdd75229f8b5
 * </code></pre>
 * This can be done by either using HTTP-GET or HTTP-POST.
 * <p>
 * If the request contains parameters, they are added to each data item which is processed by the job.
 * <p>
 * E.g. the following request:
 * <pre><code>
 * http://localhost:8080/webhook/01d11f89-1b89-4fa0-8da4-cdd75229f8b5?alpha=42
 * </code></pre>
 * <p>
 * will result in the following data items:
 * <pre><code>
 * {
 *   "data": {
 *     "alpha": "42"
 *   },
 *   "meta": {
 *     "jobId": "01d11f89-1b89-4fa0-8da4-cdd75229f8b5",
 *     "timestamp": 1599580925108
 *   }
 * }
 * </code></pre>
 *
 * <h3>Limitations and Caveats</h3>
 * Not all actions are available for event-triggered jobs. E.g. sorting by timestamp requires all data items, that should be
 * sorted, to be known to the action. Since event-triggered jobs process a continuous stream of incoming events, there is no fixed
 * number of data items to sort.
 */
@Slf4j
@IgorComponent(typeId = "web-hook-trigger", categoryId = CoreCategory.WEB)
public class WebHookTrigger extends BaseEventTrigger {

    /**
     * {@inheritDoc}
     */
    @Override
    public EventType getSupportedEventType() {
        return EventType.WEB_HOOK;
    }

}
