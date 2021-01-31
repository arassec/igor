package com.arassec.igor.web.controller;

import com.arassec.igor.core.model.trigger.EventType;
import com.arassec.igor.core.util.event.JobTriggerEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for web-hooks that can be used e.g. in job triggers.
 */
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebHookController {

    /**
     * Publisher for events based on job changes.
     */
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Webhook for jobs using HTTP-GET.
     *
     * @param jobId            The job's ID.
     * @param allRequestParams Request params that are passed to the job.
     */
    @GetMapping(value = "/{jobId}")
    public void webhookGet(@PathVariable("jobId") String jobId, @RequestParam Map<String, String> allRequestParams) {
        handleWebHookCall(jobId, allRequestParams);
    }

    /**
     * Webhook for jobs using HTTP-POST.
     *
     * @param jobId            The job's ID.
     * @param allRequestParams Request params that are passed to the job.
     */
    @PostMapping(value = "/{jobId}")
    public void webhookPost(@PathVariable("jobId") String jobId, @RequestParam Map<String, String> allRequestParams) {
        handleWebHookCall(jobId, allRequestParams);
    }

    /**
     * Handles the web-hook call and fires an application event with the supplied data.
     *
     * @param jobId            The job's ID.
     * @param allRequestParams Request params that are passed to the job.
     */
    private void handleWebHookCall(String jobId, Map<String, String> allRequestParams) {
        Map<String, Object> params = new HashMap<>();
        allRequestParams.forEach(params::put);
        // A bit unclean, but we don't know if the job is in simulation mode or not. By sending two events, both listeners
        // will catch the event and decide what to do...
        applicationEventPublisher.publishEvent(new JobTriggerEvent(jobId, params, EventType.WEB_HOOK));
    }

}
