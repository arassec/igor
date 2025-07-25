package com.arassec.igor.web.controller;

import com.arassec.igor.application.manager.ConnectorManager;
import com.arassec.igor.application.manager.JobManager;
import com.arassec.igor.application.registry.IgorComponentRegistry;
import com.arassec.igor.application.simulation.JobSimulator;
import com.arassec.igor.application.simulation.SimulationResult;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.ModelPageHelper;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.core.util.event.JobEvent;
import com.arassec.igor.core.util.event.JobEventType;
import com.arassec.igor.web.model.JobExecutionOverview;
import com.arassec.igor.web.model.JobListEntry;
import com.arassec.igor.web.model.ScheduleEntry;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * REST-Controller for {@link Job}s.
 */
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
@Slf4j
public class JobRestController extends BaseRestController {

    /**
     * SSE message indicating a job state update.
     */
    private static final String SSE_STATE_UPDATE = "state-update";

    /**
     * SSE message containing execution overview information.
     */
    private static final String SSE_EXECUTION_OVERVIEW = "execution-overview";

    /**
     * SSE message indicating job updates.
     */
    private static final String SSE_CRUD = "crud";

    /**
     * Manager for Jobs.
     */
    private final JobManager jobManager;

    /**
     * Manager for connectors.
     */
    private final ConnectorManager connectorManager;

    /**
     * The registry for igor components.
     */
    private final IgorComponentRegistry igorComponentRegistry;

    /**
     * Simulator for job executions.
     */
    private final JobSimulator jobSimulator;

    /**
     * Contains {@link SseEmitter} of job stream requests.
     */
    private final CopyOnWriteArrayList<SseEmitter> jobStreamEmitters = new CopyOnWriteArrayList<>();

    /**
     * Returns job data for all available jobs.
     *
     * @param pageNumber  The number of the page to retrieve.
     * @param pageSize    The size of the page to retrieve.
     * @param nameFilter  Optional String to use as filter for job names.
     * @param stateFilter Optional {@link JobExecutionState} to use for filtering jobs.
     * @return List of Job-IDs.
     */
    @GetMapping
    public ModelPage<JobListEntry> getJobs(
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "2147483647") int pageSize,
        @RequestParam(value = "nameFilter", required = false) String nameFilter,
        @RequestParam(value = "stateFilter", required = false) JobExecutionState[] stateFilter) {

        Set<JobExecutionState> jobExecutionStateFilter = new HashSet<>();
        if (stateFilter != null && stateFilter.length > 0) {
            jobExecutionStateFilter = Set.of(stateFilter);
        }

        ModelPage<Job> jobsPage = jobManager.loadPage(pageNumber, pageSize, nameFilter, jobExecutionStateFilter);
        if (jobsPage != null && !jobsPage.getItems().isEmpty()) {
            ModelPage<JobListEntry> result = new ModelPage<>(pageNumber, pageSize, jobsPage.getTotalPages(), null);

            result.setItems(jobsPage.getItems().stream()
                .map(job -> {
                    var jobExecution = determineJobExecution(jobManager, job);
                    return new JobListEntry(job.getId(), job.getName(), job.isActive(),
                        (jobManager.countExecutionsOfJobInState(job.getId(), JobExecutionState.FAILED) > 0), job.isFaultTolerant(),
                        convert(jobExecution, null));
                })
                .toList()
            );

            return result;
        }

        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

    /**
     * Returns a newly created job instance as prototype for new jobs.
     *
     * @return A new {@link Job} instance.
     */
    @GetMapping("prototype")
    public Job getJobPrototype() {
        return igorComponentRegistry.createJobPrototype();
    }

    /**
     * Returns a newly created action instance as prototype for new actions.
     *
     * @return A new {@link Action} instance.
     */
    @GetMapping("action/prototype")
    public Action getActionPrototype() {
        return igorComponentRegistry.createActionPrototype();
    }

    /**
     * Returns an {@link SseEmitter} that will be used to send SSE job messages to the client.
     *
     * @param response The {@link HttpServletResponse} of the request.
     * @return SSE emitter for job messages.
     */
    @GetMapping("stream")
    public SseEmitter getJobStream(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader(HttpHeaders.CONNECTION, "keep-alive");
        response.setHeader(HttpHeaders.CONTENT_TYPE, "text/event-stream");

        var emitter = new SseEmitter(-1L);
        emitter.onCompletion(() -> jobStreamEmitters.remove(emitter));
        emitter.onTimeout(() -> jobStreamEmitters.remove(emitter));

        // The first event is an overview of the job executions:
        try {
            emitter.send(SseEmitter.event().name(SSE_EXECUTION_OVERVIEW).data(createJobExecutionOverview()));
            jobStreamEmitters.add(emitter);
            return emitter;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the JSON-representation of the Job with the given ID.
     *
     * @param id The job's ID.
     * @return The job in JSON form.
     */
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Job getJob(@PathVariable("id") String id) {
        var job = jobManager.load(id);
        if (job != null) {
            return job;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
    }

    /**
     * Saves a job.
     *
     * @param job The job configuration.
     * @return The saved job.
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Job saveJob(@Valid @RequestBody Job job) {
        return jobManager.save(job);
    }

    /**
     * Runs a simulation of the supplied job.
     *
     * @param job The job configuration.
     * @return Test results of the simulated job execution.
     */
    @PostMapping("simulate")
    public Map<String, SimulationResult> simulateJob(@Valid @RequestBody Job job) {
        Future<Map<String, SimulationResult>> simulationResultFuture = jobSimulator.simulateJob(job);
        try {
            return simulationResultFuture.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IgorException("Interrupted during job simulation!", e);
        } catch (ExecutionException e) {
            throw new IgorException("Exception during job simulation!", e);
        }
    }

    /**
     * Cancels a simulation of the supplied job.
     *
     * @param jobId The job's ID.
     */
    @DeleteMapping("simulate/{jobId}")
    public void cancelSimulations(@PathVariable String jobId) {
        jobSimulator.cancelAllSimulations(jobId);
    }

    /**
     * Checks whether a job name has already been taken or not.
     *
     * @param encodedName The job's Base64 encoded name.
     * @param id          The job's ID.
     * @return {@code true} if a job with the provided name already exists, {@code false} otherwise.
     */
    @GetMapping("check/{name}/{id}")
    public Boolean checkJobName(@PathVariable("name") String encodedName, @PathVariable("id") String id) {
        var name = new String(Base64.getDecoder().decode(encodedName));
        var existingJob = jobManager.loadByName(name);
        return existingJob != null && !(existingJob.getId().equals(id));
    }

    /**
     * Deletes the job with the given ID.
     *
     * @param id                        The job's ID.
     * @param deleteExclusiveConnectors Set to {@code true} to delete connectors only used by this job.
     */
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable("id") String id, @RequestParam Boolean deleteExclusiveConnectors) {
        if (Boolean.TRUE.equals(deleteExclusiveConnectors)) {
            List<Pair<String, String>> exclusiveConnectors = getExclusiveConnectors(id);
            if (exclusiveConnectors != null && !exclusiveConnectors.isEmpty()) {
                exclusiveConnectors.forEach(exclusiveConnector -> connectorManager.deleteConnector(exclusiveConnector.getKey()));
            }
        }
        jobManager.delete(id);
    }

    /**
     * Runs the supplied job.
     *
     * @param job The job configuration.
     * @return 'OK' on success.
     */
    @PostMapping(value = "run", produces = MediaType.APPLICATION_JSON_VALUE)
    public Job runJob(@Valid @RequestBody Job job) {
        var savedJob = jobManager.save(job);
        if (savedJob.isActive()) {
            jobManager.enqueue(savedJob);
        }
        return savedJob;
    }

    /**
     * Runs the job with the given ID.
     *
     * @param id The job's ID.
     */
    @PostMapping("run/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void runJobFromId(@PathVariable("id") String id) {
        var job = jobManager.load(id);
        if (job != null) {
            if (job.isActive()) {
                jobManager.enqueue(job);
            }
            return;
        }
        throw new IllegalArgumentException("Invalid job ID");
    }

    /**
     * Gets the schedule of all active jobs.
     *
     * @param pageNumber The number of the page to return.
     * @param pageSize   The size of the page to return.
     * @return List of schedules.
     */
    @GetMapping("schedule")
    public ModelPage<ScheduleEntry> getSchedule(
        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", required = false, defaultValue = "2147483647") int pageSize) {
        List<ScheduleEntry> jobSchedule = new LinkedList<>();
        jobManager.loadScheduled().stream().filter(job -> job.getTrigger() instanceof ScheduledTrigger).forEach(job -> {
            String cronExpression = ((ScheduledTrigger) job.getTrigger()).getCronExpression();
            var cronTrigger = CronExpression.parse(cronExpression);
            var offsetDateTime =
                Optional.ofNullable(cronTrigger.next(OffsetDateTime.now()))
                    .orElseThrow(() -> new IgorException("Unable to determine schedule for job!"));
            jobSchedule.add(new ScheduleEntry(job.getId(), job.getName(), offsetDateTime.toInstant()));
        });
        jobSchedule.sort(Comparator.comparing(ScheduleEntry::getNextRun));
        return ModelPageHelper.getModelPage(jobSchedule, pageNumber, pageSize);
    }

    /**
     * Returns the connectors that are ONLY referenced by this job.
     *
     * @param id The job's ID.
     * @return The connectors.
     */
    @GetMapping(value = "{id}/exclusive-connector-references", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Pair<String, String>> getExclusiveConnectors(@PathVariable("id") String id) {
        List<Pair<String, String>> result = new LinkedList<>();

        Set<Pair<String, String>> referencedConnectors = jobManager.getReferencedConnectors(id);
        if (referencedConnectors != null && !referencedConnectors.isEmpty()) {
            referencedConnectors.forEach(referencedConnector -> {
                ModelPage<Pair<String, String>> referencingJobs = connectorManager
                    .getReferencingJobs(referencedConnector.getKey(), 0, Integer.MAX_VALUE);
                if (referencingJobs != null && referencingJobs.getItems().size() == 1 && referencingJobs.getItems()
                    .getFirst().getKey().equals(id)) {
                    result.add(referencedConnector);
                }
            });
            result.sort(Comparator.comparing(Pair::getValue));
        }

        return result;
    }

    /**
     * Listens for job events and publishes them as SSE.
     *
     * @param jobEvent The event containing more information.
     */
    @EventListener
    public void onJobEvent(JobEvent jobEvent) {
        List<SseEmitter.SseEventBuilder> events = new LinkedList<>();

        if (JobEventType.STATE_CHANGE.equals(jobEvent.type()) ||
            JobEventType.STATE_REFRESH.equals(jobEvent.type())) {
            var jobExecution = determineJobExecution(jobManager, jobEvent.job());
            events.add(SseEmitter.event().name(SSE_STATE_UPDATE).data(
                new JobListEntry(jobEvent.job().getId(), jobEvent.job().getName(),
                    jobEvent.job().isActive(), (jobManager.countExecutionsOfJobInState(jobEvent.job().getId(),
                    JobExecutionState.FAILED) > 0), jobEvent.job().isFaultTolerant(), convert(jobExecution, null))
            ));
            if (JobEventType.STATE_CHANGE.equals(jobEvent.type())) {
                events.add(SseEmitter.event().name(SSE_EXECUTION_OVERVIEW).data(createJobExecutionOverview()));
            }
        } else {
            events.add(SseEmitter.event().name(SSE_CRUD).data(jobEvent.job().getId()));
            events.add(SseEmitter.event().name(SSE_EXECUTION_OVERVIEW).data(createJobExecutionOverview()));
        }

        List<SseEmitter> deadJobStreamEmitters = new LinkedList<>();
        jobStreamEmitters.forEach(emitter -> events.forEach(event -> {
            try {
                emitter.send(event);
            } catch (IOException | IllegalStateException e) {
                deadJobStreamEmitters.add(emitter);
            }
        }));
        jobStreamEmitters.removeAll(deadJobStreamEmitters);
    }

    /**
     * Creates a job execution overview instance with information about e.g. running or failed job executions.
     *
     * @return A {@link JobExecutionOverview}.
     */
    private JobExecutionOverview createJobExecutionOverview() {
        var jobExecutionOverview = new JobExecutionOverview();
        jobExecutionOverview.setNumSlots(jobManager.getNumSlots());
        jobExecutionOverview.setNumRunning(jobManager.countJobExecutions(JobExecutionState.RUNNING)
            + jobManager.countJobExecutions(JobExecutionState.ACTIVE));
        jobExecutionOverview.setNumWaiting(jobManager.countJobExecutions(JobExecutionState.WAITING));
        jobExecutionOverview.setNumFailed(jobManager.countJobExecutions(JobExecutionState.FAILED));
        return jobExecutionOverview;
    }

}
