package com.arassec.igor.web.controller;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.Task;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.ModelPageHelper;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.model.JobListEntry;
import com.arassec.igor.web.model.ScheduleEntry;
import com.arassec.igor.web.model.simulation.SimulationActionResult;
import com.arassec.igor.web.model.simulation.SimulationJobResult;
import com.arassec.igor.web.model.simulation.SimulationTaskResult;
import com.arassec.igor.web.simulation.ActionProxy;
import com.arassec.igor.web.simulation.ProviderProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.util.StringUtils;
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
public class JobRestController {

    /**
     * Manager for Jobs.
     */
    private final JobManager jobManager;

    /**
     * Manager for Services.
     */
    private final ServiceManager serviceManager;

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
            @RequestParam(value = "nameFilter", required = false) String nameFilter) {

        ModelPage<Job> jobsPage = jobManager.loadPage(pageNumber, pageSize, nameFilter);
        if (jobsPage != null && !jobsPage.getItems().isEmpty()) {
            ModelPage<JobListEntry> result = new ModelPage<>(pageNumber, pageSize, jobsPage.getTotalPages(), null);

            result.setItems(jobsPage.getItems().stream().map(job -> new JobListEntry(job.getId(), job.getName(), job.isActive()))
                    .collect(Collectors.toList()));

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
    public Job getJob(@PathVariable("id") Long id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("ID required");
        }
        Job job = jobManager.load(id);
        if (job != null) {
            return job;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
    }

    /**
     * Creates a new job.
     *
     * @param job The job configuration.
     *
     * @return 'OK' on success.
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Job createJob(@Valid @RequestBody Job job) {
        if (jobManager.loadByName(job.getName()) == null) {
            job.setId(null);
            return jobManager.save(job);
        } else {
            throw new IllegalArgumentException(RestControllerExceptionHandler.NAME_ALREADY_EXISTS_ERROR);
        }
    }

    /**
     * Updates an existing job.
     *
     * @param job The job configuration.
     *
     * @return 'OK' on success.
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Job updateJob(@Valid @RequestBody Job job) {
        Job existingJobWithSameName = jobManager.loadByName(job.getName());
        if (existingJobWithSameName == null || existingJobWithSameName.getId().equals(job.getId())) {
            return jobManager.save(job);
        } else {
            throw new IllegalArgumentException(RestControllerExceptionHandler.NAME_ALREADY_EXISTS_ERROR);
        }
    }

    /**
     * Runs a simulation of the supplied job.
     *
     * @param jobJson The job in JSON form as String.
     *
     * @return Test results of the simulated job run.
     */
    @PostMapping("simulate")
    public SimulationJobResult simulateJob(@Valid @RequestBody String jobJson) {

        Job job;
        try {
            job = simulationObjectMapper.readValue(jobJson, Job.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not simulate job run!", e);
        }

        JobExecution jobExecution = new JobExecution();

        job.run(jobExecution);

        SimulationJobResult jobResult = new SimulationJobResult();
        if (jobExecution.getErrorCause() != null) {
            jobResult.setErrorCause(jobExecution.getErrorCause());
        }

        job.getTasks().stream().forEach(task -> {
            ProviderProxy providerProxy = (ProviderProxy) task.getProvider();

            SimulationTaskResult taskResult = new SimulationTaskResult();
            taskResult.setErrorCause(providerProxy.getErrorCause());
            providerProxy.getCollectedData().stream()
                    .forEach(jsonObject -> {
                        try {
                            Map<String, Object> item = new HashMap<>();
                            HashMap data = simulationObjectMapper.readValue(jsonObject.toString(), HashMap.class);
                            item.put(Task.DATA_KEY, data);
                            item.put(Task.META_KEY, Task.createMetaData(job.getId(), task.getId()));
                            taskResult.getResults().add(item);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            task.getActions().stream().forEach(action -> {
                ActionProxy actionProxy = (ActionProxy) action;
                SimulationActionResult actionResult = new SimulationActionResult();
                actionResult.setErrorCause(actionProxy.getErrorCause());
                actionProxy.getCollectedData().stream().forEach(jsonObject -> {
                    try {
                        actionResult.getResults().add(
                                simulationObjectMapper.readValue(jsonObject.toString(), HashMap.class));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                taskResult.getActionResults().add(actionResult);
            });

            jobResult.getTaskResults().add(taskResult);
        });

        return jobResult;
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
    public Boolean checkJobName(@PathVariable("name") String encodedName, @PathVariable("id") Long id) {
        String name = new String(Base64.getDecoder().decode(encodedName));
        Job existingJob = jobManager.loadByName(name);
        return existingJob != null && !(existingJob.getId().equals(id));
    }

    /**
     * Deletes the job with the given ID.
     *
     * @param id The job's ID.
     */
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable("id") Long id, @RequestParam Boolean deleteExclusiveServices) {
        if (deleteExclusiveServices) {
            List<Pair<Long, String>> exclusiveServices = getExclusiveServices(id);
            if (exclusiveServices != null && !exclusiveServices.isEmpty()) {
                exclusiveServices.stream().forEach(exclusiveService -> serviceManager.deleteService(exclusiveService.getKey()));
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
        if (job.isActive()) {
            jobManager.enqueue(savedJob);
        }
        return savedJob;
    }

    /**
     * Runs the job with the given ID.
     *
     * @param id The job's ID.
     *
     * @return 'OK' on success.
     */
    @PostMapping("run/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void runJobFromId(@PathVariable("id") Long id) {
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
     * Returns the services that are ONLY referenced by this job.
     *
     * @param id The job's ID.
     *
     * @return The services.
     */
    @GetMapping(value = "{id}/exclusive-service-references", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Pair<Long, String>> getExclusiveServices(@PathVariable("id") Long id) {
        List<Pair<Long, String>> result = new LinkedList<>();

        Set<Pair<Long, String>> referencedServices = jobManager.getReferencedServices(id);
        if (referencedServices != null && !referencedServices.isEmpty()) {
            referencedServices.stream().forEach(referencedService -> {
                ModelPage<Pair<Long, String>> referencingJobs = serviceManager
                        .getReferencingJobs(referencedService.getKey(), 0, Integer.MAX_VALUE);
                if (referencingJobs != null && referencingJobs.getItems().size() == 1 && referencingJobs.getItems().iterator()
                        .next().getKey().equals(id)) {
                    result.add(referencedService);
                }
            });
            Collections.sort(result, Comparator.comparing(Pair::getValue));
        }

        return result;
    }

}
