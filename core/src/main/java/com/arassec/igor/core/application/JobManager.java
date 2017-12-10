package com.arassec.igor.core.application;

import com.arassec.igor.core.model.Job;
import com.arassec.igor.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Manages jobs.
 */
@Component
public class JobManager {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    private Map<String, ScheduledFuture> scheduledJobs = new HashMap<>();

    @PostConstruct
    private void initialize() {
        jobRepository.findAll().stream().forEach(job -> schedule(job));
    }

    @PreDestroy
    private void shutdown() {
        scheduledJobs.values().stream().forEach(scheduledFuture -> scheduledFuture.cancel(true));
    }

    public void save(Job job) {
        jobRepository.upsert(job);
        schedule(job);
    }

    public Job load(String id) {
        return jobRepository.findById(id);
    }

    public List<Job> loadAll() {
        return jobRepository.findAll();
    }

    public void schedule(Job job) {
        cancel(job);
        scheduledJobs.put(job.getId(), taskScheduler.schedule(new Thread(() -> job.run()),
                new CronTrigger(job.getTrigger())));
    }

    public void cancel(Job job) {
        if (scheduledJobs.containsKey(job.getId())) {
            if (!scheduledJobs.get(job.getId()).cancel(true)) {
                throw new IllegalStateException("Job " + job.getId() + " could not be cancelled!");
            }
        }
    }

}
