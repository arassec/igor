package com.arassec.igor.core.application;

import com.arassec.igor.core.model.Job;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * Manages jobs.
 */
@Slf4j
@Component
public class JobManager implements InitializingBean, DisposableBean {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    private Map<Long, ScheduledFuture> scheduledJobs = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        jobRepository.findAll().stream().forEach(job -> schedule(job));
    }

    @Override
    public void destroy() {
        scheduledJobs.values().stream().forEach(scheduledFuture -> scheduledFuture.cancel(true));
    }

    public void save(Job job) {
        if (job.isActive()) {
            schedule(job);
        } else {
            cancel(job);
        }
        jobRepository.upsert(job);
    }

    public Job load(Long id) {
        return jobRepository.findById(id);
    }

    public List<Job> loadAll() {
        return jobRepository.findAll();
    }

    public void schedule(Job job) {
        cancel(job);
        try {
            scheduledJobs.put(job.getId(), taskScheduler.schedule(new Thread(() -> job.run()),
                    new CronTrigger(job.getTrigger())));
            CronSequenceGenerator cronTrigger = new CronSequenceGenerator(job.getTrigger());
            Date nextRun = cronTrigger.next(Calendar.getInstance().getTime());
            log.info("Scheduled job: {} ({}). Next run at: {}", job.getName(), job.getId(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nextRun));
        } catch (IllegalArgumentException e) {
            log.warn("Illegal trigger configured!", e);
        }
    }

    public void delete(Long id) {
        Job job = jobRepository.findById(id);
        if (job != null) {
            cancel(job);
            jobRepository.deleteById(id);
            scheduledJobs.remove(id);
        }
    }

    public void cancel(Job job) {
        if (scheduledJobs.containsKey(job.getId())) {
            if (!scheduledJobs.get(job.getId()).cancel(true)) {
                throw new ServiceException("Job " + job.getId() + " could not be cancelled!");
            }
            scheduledJobs.remove(job.getId());
        }
    }
}
