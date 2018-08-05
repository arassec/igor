package com.arassec.igor.web.api;

import com.arassec.igor.core.application.JobManager;
import com.arassec.igor.core.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class JobRestController extends BaseRestController {

    @Autowired
    private JobManager jobManager;


    @GetMapping("/job/ids")
    public List<String> getServiceIds() {
        List<Job> jobs = jobManager.loadAll();
        return jobs.stream().map(job -> job.getId()).collect(Collectors.toList());
    }

}
