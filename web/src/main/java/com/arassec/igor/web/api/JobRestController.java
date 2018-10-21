package com.arassec.igor.web.api;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.model.Job;
import com.arassec.igor.web.api.model.JobModel;
import com.arassec.igor.web.api.model.converter.JobConverter;
import com.github.openjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class JobRestController extends BaseRestController {

    @Autowired
    private JobManager jobManager;

    @Autowired
    @Qualifier("webJobConverter")
    private JobConverter jobConverter;

    @GetMapping("/job/id")
    public List<JobModel> getJobIds() {
        List<Job> jobs = jobManager.loadAll();
        return jobs.stream().map(job -> new JobModel(job.getId(), job.getName(), job.isActive())).collect(Collectors.toList());
    }

    @GetMapping("/job/{id}")
    public ResponseEntity<JobModel> getJob(@PathVariable("id") Long id) {
        if (StringUtils.isEmpty(id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Job job = jobManager.load(id);
        if (job != null) {
            return new ResponseEntity<>(jobConverter.convert(job), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/job")
    public ResponseEntity<String> saveJob(@RequestBody String jobProperties) {
        JSONObject properties = new JSONObject(jobProperties);
        Job job = jobConverter.convert(properties);
        job.setId(null);
        jobManager.save(job);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/job")
    public ResponseEntity<String> updateJob(@RequestBody String jobProperties) {
        JSONObject properties = new JSONObject(jobProperties);
        Job job = jobConverter.convert(properties);
        job.setId(properties.getLong("id"));
        jobManager.save(job);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/job/{id}")
    public void deleteJob(@PathVariable("id") Long id) {
        jobManager.delete(id);
    }

}
