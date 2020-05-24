package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.ConnectorManager;
import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.model.DataKey;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.Task;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.ModelPageHelper;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.model.JobListEntry;
import com.arassec.igor.web.model.ScheduleEntry;
import com.arassec.igor.web.model.simulation.SimulationResult;
import com.arassec.igor.web.simulation.ActionProxy;
import com.arassec.igor.web.simulation.ProviderProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST-Controller for {@link Job}s.
 */
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
@Slf4j
public class JobRestController extends BaseRestController {

    /**
     * Manager for Jobs.
     */
    private final JobManager jobManager;

    /**
     * Manager for connectors.
     */
    private final ConnectorManager connectorManager;

    /**
     * Job-Mapper for simulation runs.
     */
    private final ObjectMapper simulationObjectMapper;

    /**
     * Returns the IDs of all available jobs.
     *
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

            result.setItems(jobsPage.getItems().stream().map(job -> {

                JobExecution jobExecution = job.getCurrentJobExecution();
                if (jobExecution == null) {
                    ModelPage<JobExecution> jobExecutionsOfJob = jobManager.getJobExecutionsOfJob(job.getId(), 0, 1);
                    if (jobExecutionsOfJob != null && !jobExecutionsOfJob.getItems().isEmpty()) {
                        jobExecution = jobExecutionsOfJob.getItems().get(0);
                    }
                }

                return new JobListEntry(job.getId(), job.getName(), job.isActive(), convert(jobExecution, null));
            }).collect(Collectors.toList()));

            return result;
        }

        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

    /**
     * Returns the JSON-representation of the Job with the given ID.
     *
     * @param id The job's ID.
     *
     * @return The job in JSON form.
     */
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Job getJob(@PathVariable("id") String id) {
        Job job = jobManager.load(id);
        if (job != null) {
            return job;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
    }

    /**
     * Saves a job.
     *
     * @param job The job configuration.
     *
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
     *
     * @return Test results of the simulated job execution.
     */
    @PostMapping("simulate")
    public Map<String, SimulationResult> simulateJob(@Valid @RequestBody Job job) {

        Map<String, SimulationResult> result = new HashMap<>();

        Job simulationJob;
        try {
            // The method parameter has been deserialized with the regular object mapper. Thus validation is supported. But to
            // simulate the job, proxies are required, which are added by the 'simulation object mapper'. Thus we have to first
            // convert the original job to a JSON string and afterwards back to a Job object, but this time with simpulation
            // proxies inside...
            simulationJob = simulationObjectMapper.readValue(simulationObjectMapper.writeValueAsString(job), Job.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not simulate job run!", e);
        }

        JobExecution jobExecution = new JobExecution();

        simulationJob.run(jobExecution);

        SimulationResult jobResult = new SimulationResult();
        if (jobExecution.getErrorCause() != null) {
            jobResult.setErrorCause(jobExecution.getErrorCause());
            result.put("job-result", jobResult);
        }

        simulationJob.getTasks().forEach(task -> {
            ProviderProxy providerProxy = (ProviderProxy) task.getProvider();

            SimulationResult taskResult = new SimulationResult();
            taskResult.setErrorCause(providerProxy.getErrorCause());
            providerProxy.getCollectedData()
                    .forEach(jsonObject -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put(DataKey.DATA.getKey(), jsonObject);
                        item.put(DataKey.META.getKey(), Task.createMetaData(simulationJob.getId(), task.getId()));
                        taskResult.getResults().add(item);
                    });
            if (task.getId() != null) {
                result.put(task.getId(), taskResult);
            }

            task.getActions().forEach(action -> {
                ActionProxy actionProxy = (ActionProxy) action;
                SimulationResult actionResult = new SimulationResult();
                actionResult.setErrorCause(actionProxy.getErrorCause());
                actionProxy.getCollectedData().forEach(jsonObject -> actionResult.getResults().add(jsonObject));
                if (action.getId() != null) {
                    result.put(action.getId(), actionResult);
                }
            });
        });

        return result;
    }

    /**
     * Checks whether a job name has already been taken or not.
     *
     * @param encodedName The job's Base64 encoded name.
     * @param id          The job's ID.
     *
     * @return {@code true} if a job with the provided name already exists, {@code false} otherwise.
     */
    @GetMapping("check/{name}/{id}")
    public Boolean checkJobName(@PathVariable("name") String encodedName, @PathVariable("id") String id) {
        String name = new String(Base64.getDecoder().decode(encodedName));
        Job existingJob = jobManager.loadByName(name);
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
     *
     * @return 'OK' on success.
     */
    @PostMapping(value = "run", produces = MediaType.APPLICATION_JSON_VALUE)
    public Job runJob(@Valid @RequestBody Job job) {
        Job savedJob = jobManager.save(job);
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
        Job job = jobManager.load(id);
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
     * @return List of schedules.
     */
    @GetMapping("schedule")
    public ModelPage<ScheduleEntry> getSchedule(
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "2147483647") int pageSize) {
        List<ScheduleEntry> jobSchedule = new LinkedList<>();
        jobManager.loadScheduled().stream().filter(job -> job.getTrigger() instanceof ScheduledTrigger).forEach(job -> {
            String cronExpression = ((ScheduledTrigger) job.getTrigger()).getCronExpression();
            CronSequenceGenerator cronTrigger = new CronSequenceGenerator(cronExpression);
            Date nextRun = cronTrigger.next(Calendar.getInstance().getTime());
            jobSchedule.add(new ScheduleEntry(job.getId(), job.getName(), Instant.ofEpochMilli(nextRun.getTime())));
        });
        jobSchedule.sort(Comparator.comparing(ScheduleEntry::getNextRun));
        return ModelPageHelper.getModelPage(jobSchedule, pageNumber, pageSize);
    }

    /**
     * Returns the connectors that are ONLY referenced by this job.
     *
     * @param id The job's ID.
     *
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
                if (referencingJobs != null && referencingJobs.getItems().size() == 1 && referencingJobs.getItems().iterator()
                        .next().getKey().equals(id)) {
                    result.add(referencedConnector);
                }
            });
            result.sort(Comparator.comparing(Pair::getValue));
        }

        return result;
    }

}
