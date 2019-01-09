package com.arassec.igor.web.api;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.application.converter.JsonJobConverter;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * REST-Controller for {@link Job}s.
 */
public class JobRestController extends BaseRestController {

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
//    @GetMapping("/job/id")
//    public List<String> getJobIds() {
//        List<Job> jobs = jobManager.loadAll();
//        return jobs.stream().map(job -> new JobModel(job.getId(), job.getName(), job.isActive())).collect(Collectors.toList());
//    }

    /**
     * Returns the JSON-representation of the Job with the given ID.
     *
     * @param id The job's ID.
     * @return The job in JSON form.
     */
//    @GetMapping("/job/{id}")
//    public ResponseEntity<JobModel> getJob(@PathVariable("id") Long id) {
//        if (StringUtils.isEmpty(id)) {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//        Job job = jobManager.load(id);
//        if (job != null) {
//            return new ResponseEntity<>(jobConverter.convert(job), HttpStatus.OK);
//        }
//        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//    }

    /**
     * Saves a job.
     *
     * @param jobProperties The job in JSON form.
     * @return 'OK' on success.
     */
//    @PostMapping("/job")
//    public ResponseEntity<String> saveJob(@RequestBody String jobProperties) {
//        JSONObject properties = new JSONObject(jobProperties);
//        Job job = jobConverter.convert(properties);
//        job.setId(null);
//        jobManager.save(job);
//        return new ResponseEntity<>("OK", HttpStatus.OK);
//    }

    /**
     * Updates an existing job.
     *
     * @param jobProperties The job in JSON form.
     * @return 'OK' on success.
     */
//    @PutMapping("/job")
//    public ResponseEntity<String> updateJob(@RequestBody String jobProperties) {
//        JSONObject properties = new JSONObject(jobProperties);
//        Job job = jobConverter.convert(properties);
//        jobManager.save(job);
//        return new ResponseEntity<>("OK", HttpStatus.OK);
//    }

    /**
     * Runs a dry-run of the supplied job.
     *
     * @param jobProperties The job in JSON form.
     * @return Test results of the dry-run.
     */
//    @PostMapping("/job/test")
//    public DryRunJobResult testJob(@RequestBody String jobProperties) {
//        Job job = jobConverter.convert(new JSONObject(jobProperties));
//        return job.dryRun();
//    }

    /**
     * Deletes the job with the given ID.
     *
     * @param id The job's ID.
     */
    @DeleteMapping("/job/{id}")
    public void deleteJob(@PathVariable("id") Long id) {
        jobManager.delete(id);
    }

    /**
     * Runs the supplied job.
     *
     * @param jobProperties The job in JSON form.
     * @return 'OK' on success.
     */
//    @PostMapping("/job/run")
//    public ResponseEntity<String> runJob(@RequestBody String jobProperties) {
//        JSONObject properties = new JSONObject(jobProperties);
//        Job job = jobConverter.convert(properties);
//        jobManager.run(job);
//        return new ResponseEntity<>("OK", HttpStatus.OK);
//    }

    /**
     * Returns the execution state of a certain job.
     *
     * @param id The job's ID.
     * @return The current {@link JobExecution} with information about a running job or {@code null}, if the job is not
     * running.
     */
//    @GetMapping("/job/{id}/execution")
//    public ResponseEntity<JobExecution> getExecution(@PathVariable("id") Long id) {
//        JobExecution jobExecution = jobManager.getJobExecution(id);
//        if (jobExecution != null) {
//            return new ResponseEntity<>(jobExecution, HttpStatus.OK);
//        }
//        return new ResponseEntity<>(HttpStatus.OK);
//    }

    /**
     * Cancels a running job.
     *
     * @param id The job's ID.
     * @return 'OK' on success.
     */
//    @PostMapping("/job/{id}/cancel")
//    public ResponseEntity<String> cancelJob(@PathVariable("id") Long id) {
//        if (id != null) {
//            jobManager.cancel(id);
//            return new ResponseEntity<>("OK", HttpStatus.OK);
//        }
//        return new ResponseEntity<>("ERROR", HttpStatus.BAD_REQUEST);
//    }

}
