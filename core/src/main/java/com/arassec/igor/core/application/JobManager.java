package com.arassec.igor.core.application;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.JobListener;
import com.arassec.igor.core.model.job.execution.JobExecution;
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
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Manages jobs.
 */
@Slf4j
@Component
public class JobManager implements InitializingBean, DisposableBean, JobListener {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    private Map<Long, ScheduledFuture> scheduledJobFutures = new HashMap<>();

    private Map<Long, Job> runningJobs = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        jobRepository.findAll().stream().forEach(job -> schedule(job));
    }

    @Override
    public void destroy() {
        scheduledJobFutures.values().stream().forEach(scheduledFuture -> scheduledFuture.cancel(true));
    }

    public void save(Job job) {
        if (job.isActive()) {
            schedule(job);
        } else {
            unschedule(job);
        }
        jobRepository.upsert(job);
    }

    public Job load(Long id) {
        return jobRepository.findById(id);
    }

    public List<Job> loadAll() {
        return jobRepository.findAll();
    }

    public void run(Job job) {
        if (runningJobs.containsKey(job.getId())) {
            log.debug("Job already running: {} ({})", job.getName(), job.getId());
            return;
        }
        log.info("Manually running job: {} ({})", job.getName(), job.getId());
        job.setJobListener(this);
        unschedule(job);
        // The job will be re-scheduled according to its trigger after its execution with the notify-finished-callback mechanism.
        taskScheduler.schedule(new Thread(() -> job.run()), Instant.now());
    }

    public void cancel(Long id) {
        if (runningJobs.containsKey(id)) {
            Job job = runningJobs.get(id);
            log.info("Manually cancelling job: {} ({})", job.getName(), job.getId());
            job.getJobExecution().cancel();
        }
    }

    public void schedule(Job job) {
        job.setJobListener(this);
        unschedule(job);
        if (!job.isActive()) {
            log.debug("Job '{}' is not active and will not be scheduled...", job.getName());
            return;
        }
        try {
            scheduledJobFutures.put(job.getId(), taskScheduler.schedule(new Thread(() -> job.run()),
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
            unschedule(job);
            jobRepository.deleteById(id);
            scheduledJobFutures.remove(id);
        }
    }

    public void unschedule(Job job) {
        if (scheduledJobFutures.containsKey(job.getId())) {
            if (!scheduledJobFutures.get(job.getId()).cancel(true)) {
                throw new ServiceException("Job " + job.getId() + " could not be cancelled!");
            }
            scheduledJobFutures.remove(job.getId());
            log.info("Canceled job: {} ({})", job.getName(), job.getId());
        }
    }

    public JobExecution getJobExecution(Long id) {
        if (runningJobs.containsKey(id)) {
            return runningJobs.get(id).getJobExecution();
        }
        return null;
    }

    /**
     * Saves the reference to the running job for state querying.
     */
    @Override
    public void notifyStarted(Job job) {
        if (job == null) {
            return;
        }

        if (runningJobs.containsKey(job.getId())) {
            log.warn("Parallel job execution detected: {} ({}).", job.getName(), job.getId());
            return;
        }

        runningJobs.put(job.getId(), job);
    }

    /**
     * Re-Schedules the finished job if necessary (e.g. because it was started manually).
     */
    @Override
    public void notifyFinished(Job job) {
        if (job == null) {
            return;
        }

        if (!scheduledJobFutures.containsKey(job.getId()) && job.isActive()) {
            schedule(job);
        }

        if (runningJobs.containsKey(job.getId())) {
            runningJobs.remove(job.getId());
        }
    }

}
