package com.arassec.igor.web.api;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.application.ServiceManager;
import com.arassec.igor.core.application.converter.JsonJobConverter;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.dryrun.DryRunJobResult;
import com.arassec.igor.core.model.trigger.CronTrigger;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.web.api.error.RestControllerExceptionHandler;
import com.arassec.igor.web.api.model.JobListEntry;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST-Controller for {@link Job}s.
 */
@RestController
@RequestMapping("/api/job")
public class JobRestController {

    /**
     * Manager for Jobs.
     */
    @Autowired
    private JobManager jobManager;

    /**
     * Manager for Services.
     */
    @Autowired
    private ServiceManager serviceManager;

    /**
     * Converter for Jobs.
     */
    @Autowired
    private JsonJobConverter jsonJobConverter;

    /**
     * Returns the IDs of all available jobs.
     *
     * @return List of Job-IDs.
     */
    @GetMapping
    public ModelPage<JobListEntry> getJobs(@RequestParam("pageNumber") int pageNumber, @RequestParam("pageSize") int pageSize,
                                           @RequestParam(value = "nameFilter", required = false) String nameFilter) {

        ModelPage<Job> jobsPage = jobManager.loadPage(pageNumber, pageSize, nameFilter);
        if (jobsPage != null && !jobsPage.getItems().isEmpty()) {
            ModelPage<JobListEntry> result = new ModelPage<>(pageNumber, pageSize, jobsPage.getTotalPages(), null);

            result.setItems(jobsPage.getItems().stream().map(job -> new JobListEntry(job.getId(), job.getName(),
                    job.isActive())).collect(Collectors.toList()));

            return result;
        }

        return new ModelPage<>(pageNumber, pageSize, 0, List.of());
    }

    /**
     * Returns the JSON-representation of the Job with the given ID.
     *
     * @param id The job's ID.
     * @return The job in JSON form.
     */
    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getJob(@PathVariable("id") Long id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("ID required");
        }
        Job job = jobManager.load(id);
        if (job != null) {
            return jsonJobConverter.convert(job, false, true).toString();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
    }

    /**
     * Creates a new job.
     *
     * @param jobJson The job in JSON form.
     * @return 'OK' on success.
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String createJob(@RequestBody String jobJson) {
        Job job = jsonJobConverter.convert(new JSONObject(jobJson), null, false);
        if (jobManager.loadByName(job.getName()) == null) {
            job.setId(null);
            Job savedJob = jobManager.save(job);
            return jsonJobConverter.convert(savedJob, false, true).toString();
        } else {
            throw new IllegalArgumentException(RestControllerExceptionHandler.NAME_ALREADY_EXISTS_ERROR);
        }
    }

    /**
     * Updates an existing job.
     *
     * @param jobJson The job in JSON form.
     * @return 'OK' on success.
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateJob(@RequestBody String jobJson) {
        Job job = jsonJobConverter.convert(new JSONObject(jobJson), null, false);
        Job existingJobWithSameName = jobManager.loadByName(job.getName());
        if (existingJobWithSameName == null || existingJobWithSameName.getId().equals(job.getId())) {
            Job savedJob = jobManager.save(job);
            return jsonJobConverter.convert(savedJob, false, true).toString();
        } else {
            throw new IllegalArgumentException(RestControllerExceptionHandler.NAME_ALREADY_EXISTS_ERROR);
        }
    }

    /**
     * Runs a dry-run of the supplied job.
     *
     * @param jobJson The job in JSON form.
     * @return Test results of the dry-run.
     */
    @PostMapping("test")
    public DryRunJobResult testJob(@RequestBody String jobJson) {
        Job job = jsonJobConverter.convert(new JSONObject(jobJson), null, false);
        return job.dryRun();
    }

    /**
     * Checks whether a job name has already been taken or not.
     *
     * @param encodedName The job's Base64 encoded name.
     * @param id          The job's ID.
     * @return {@code true} if a job with the provided name already exists, {@code false} otherwise.
     */
    @GetMapping("check/{name}/{id}")
    public Boolean checkJobName(@PathVariable("name") String encodedName, @PathVariable("id") Long id) {
        String name = new String(Base64.getDecoder().decode(encodedName));
        Job existingJob = jobManager.loadByName(name);
        if (existingJob != null && !(existingJob.getId().equals(id))) {
            return true;
        }
        return false;
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
     * @param jobJson The job in JSON form.
     * @return 'OK' on success.
     */
    @PostMapping(value = "run", produces = MediaType.APPLICATION_JSON_VALUE)
    public String runJob(@RequestBody String jobJson) {
        Job job = jsonJobConverter.convert(new JSONObject(jobJson), null, false);
        Job savedJob = jobManager.save(job);
        if (job.isActive()) {
            jobManager.enqueue(savedJob);
        }
        return jsonJobConverter.convert(savedJob, false, true).toString();
    }

    /**
     * Runs the job with the given ID.
     *
     * @param id The job's ID.
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
    public List<Map<String, Object>> getSchedule() {
        List<Map<String, Object>> jobSchedule = new LinkedList<>();
        jobManager.loadScheduled().stream().filter(job -> job.getTrigger() instanceof CronTrigger).forEach(job -> {
            String cronExpression = ((CronTrigger) job.getTrigger()).getCronExpression();
            CronSequenceGenerator cronTrigger = new CronSequenceGenerator(cronExpression);
            Date nextRun = cronTrigger.next(Calendar.getInstance().getTime());
            Map<String, Object> scheduleEntry = new HashMap<>();
            scheduleEntry.put("id", job.getId());
            scheduleEntry.put("name", job.getName());
            scheduleEntry.put("date", new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(nextRun));
            jobSchedule.add(scheduleEntry);
        });
        return jobSchedule;
    }

    /**
     * Returns the services that are ONLY referenced by this job.
     *
     * @param id The job's ID.
     * @return The services.
     */
    @GetMapping(value = "{id}/exclusive-service-references", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Pair<Long, String>> getExclusiveServices(@PathVariable("id") Long id) {
        List<Pair<Long, String>> result = new LinkedList<>();

        Set<Pair<Long, String>> referencedServices = jobManager.getReferencedServices(id);
        if (referencedServices != null && !referencedServices.isEmpty()) {
            referencedServices.stream().forEach(referencedService -> {
                Set<Pair<Long, String>> referencingJobs = serviceManager.getReferencingJobs(referencedService.getKey());
                if (referencingJobs != null && referencingJobs.size() == 1 && referencingJobs.iterator().next().getKey().equals(id)) {
                    result.add(referencedService);
                }
            });
            Collections.sort(result, Comparator.comparing(Pair::getValue));
        }

        return result;
    }

}
