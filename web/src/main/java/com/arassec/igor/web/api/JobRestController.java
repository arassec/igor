package com.arassec.igor.web.api;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.application.converter.JsonJobConverter;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.dryrun.DryRunJobResult;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.web.api.model.JobListEntry;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
    public List<JobListEntry> getJobIds() {
        List<Job> jobs = jobManager.loadAll();
        return jobs.stream().map(job -> new JobListEntry(job.getId(), job.getName(), job.isActive()))
                .collect(Collectors.toList());
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
     * Saves a job.
     *
     * @param jobJson The job in JSON form.
     * @return 'OK' on success.
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String saveJob(@RequestBody String jobJson) {
        Job job = jsonJobConverter.convert(new JSONObject(jobJson), false);
        job.setId(null);
        Job savedJob = jobManager.save(job);
        return jsonJobConverter.convert(savedJob, false, true).toString();
    }

    /**
     * Updates an existing job.
     *
     * @param jobJson The job in JSON form.
     * @return 'OK' on success.
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String updateJob(@RequestBody String jobJson) {
        Job job = jsonJobConverter.convert(new JSONObject(jobJson), false);
        Job savedJob = jobManager.save(job);
        return jsonJobConverter.convert(savedJob, false, true).toString();
    }

    /**
     * Runs a dry-run of the supplied job.
     *
     * @param jobJson The job in JSON form.
     * @return Test results of the dry-run.
     */
    @PostMapping("test")
    public DryRunJobResult testJob(@RequestBody String jobJson) {
        Job job = jsonJobConverter.convert(new JSONObject(jobJson), false);
        return job.dryRun();
    }

    /**
     * Deletes the job with the given ID.
     *
     * @param id The job's ID.
     */
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable("id") Long id) {
        jobManager.delete(id);
    }

    /**
     * Runs the supplied job.
     *
     * @param jobJson The job in JSON form.
     * @return 'OK' on success.
     */
    @PostMapping("run")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void runJob(@RequestBody String jobJson) {
        Job job = jsonJobConverter.convert(new JSONObject(jobJson), false);
        jobManager.run(job);
    }

    /**
     * Returns the execution state of a certain job.
     *
     * @param id The job's ID.
     * @return The current {@link JobExecution} with information about a running job or {@code null}, if the job is not
     * running.
     */
    @GetMapping("{id}/execution")
    public JobExecution getExecution(@PathVariable("id") Long id) {
        JobExecution jobExecution = jobManager.getJobExecution(id);
        if (jobExecution != null) {
            return jobExecution;
        }
        return new JobExecution();
    }

    /**
     * Cancels a running job.
     *
     * @param id The job's ID.
     * @return 'OK' on success.
     */
    @PostMapping("{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelJob(@PathVariable("id") Long id) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("ID required");
        }
        jobManager.cancel(id);
    }

}
