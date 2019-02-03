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
import java.util.stream.Collectors;

/**
 * Manages {@link Job}s. Entry point from outside the core package to create and maintain jobs.
 */
@Slf4j
@Component
public class JobManager implements InitializingBean, DisposableBean, JobListener {

    /**
     * Repository for jobs.
     */
    @Autowired
    private JobRepository jobRepository;

    /**
     * The task scheduler that starts the jobs according to their trigger.
     */
    @Autowired
    private TaskScheduler taskScheduler;

    /**
     * Keeps track of all scheduled jobs.
     */
    private Map<Long, ScheduledFuture> scheduledJobFutures = new ConcurrentHashMap<>();

    /**
     * Keeps track of all running jobs.
     */
    private Map<Long, Job> runningJobs = new ConcurrentHashMap<>();

    /**
     * Initializes the manager by scheduling all available jobs.
     */
    @Override
    public void afterPropertiesSet() {
        jobRepository.findAll().stream().forEach(job -> schedule(job));
    }

    /**
     * Cleans the manager up by unscheduling all scheduled jobs and cancelling all running jobs.
     */
    @Override
    public void destroy() {
        scheduledJobFutures.values().stream().forEach(scheduledFuture -> scheduledFuture.cancel(true));
        runningJobs.values().stream().forEach(job -> job.cancel());
    }

    /**
     * Saves the reference to the running job for state querying.
     * <p>
     * This method is called by every {@link Job} during the beginning of its run. The {@link JobListener} interface
     * is used to provide this callback method to the job.
     *
     * @param job The job that just started.
     */
    @Override
    public void notifyStarted(Job job) {
        if (job == null) {
            return;
        }
        if (runningJobs.containsKey(job.getId())) {
            log.error("Parallel job execution detected: {} ({}). Cancelling instance.", job.getName(), job.getId());
            job.cancel();
            return;
        }
        runningJobs.put(job.getId(), job);
    }

    /**
     * Re-Schedules the finished job if necessary (e.g. because it was started manually).
     * <p>
     * Thie method is called by every {@link Job} after finishing its run. The {@link JobListener} interface is used
     * to provide this callback method to the job.
     *
     * @param job The job that just finished.
     */
    @Override
    public void notifyFinished(Job job) {
        if (job == null || job.getId() == null) {
            return;
        }
        if (!scheduledJobFutures.containsKey(job.getId()) && job.isActive()) {
            schedule(job);
        }
        if (runningJobs.containsKey(job.getId())) {
            runningJobs.remove(job.getId());
        }
    }

    /**
     * Saves the supplied job using the {@link JobRepository}.
     *
     * @param job The job to save.
     */
    public Job save(Job job) {
        if (job.getId() != null && runningJobs.containsKey(job.getId())) {
            throw new IllegalStateException("Job currently running: " + job.getId() + " / " + job.getName());
        }
        Job savedJob = jobRepository.upsert(job);
        if (savedJob.isActive()) {
            // The cron trigger might have changed, so the job is unscheduled first and then re-scheduled according
            // to its trigger.
            unschedule(savedJob);
            schedule(savedJob);
        } else {
            unschedule(savedJob);
        }
        return savedJob;
    }

    /**
     * Deletes the job with the given ID.
     *
     * @param id The ID of the job that should be deleted.
     */
    public void delete(Long id) {
        Job job = jobRepository.findById(id);
        if (job != null) {
            unschedule(job);
            jobRepository.deleteById(id);
            scheduledJobFutures.remove(id);
        }
    }

    /**
     * Runs the provided job if it is not already running. The job is unscheduled during it's run and re-scheduled
     * according to its trigger after it finishes.
     * <p>
     * This method should be called if the job should run immediately and only once. If the job should run regularly
     * according to its trigger configuration, {@link JobManager#schedule(Job)} should be used.
     *
     * @param job The job to run.
     */
    public void run(Job job) {
        if (job == null || job.getId() == null) {
            throw new IllegalArgumentException("Job with ID required for run!");
        }
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

    /**
     * Cancels a running job.
     *
     * @param id The ID of the job that should be cancelled.
     */
    public void cancel(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID required to cancel job!");
        }
        if (runningJobs.containsKey(id)) {
            Job job = runningJobs.get(id);
            log.info("Manually cancelling job: {} ({})", job.getName(), job.getId());
            job.getJobExecution().cancel();
        }
    }

    /**
     * Loads the job with the given ID.
     *
     * @param id The job's ID.
     * @return The job with the given ID or {@code null}, if none was found.
     */
    public Job load(Long id) {
        return jobRepository.findById(id);
    }

    /**
     * Loads all jobs.
     *
     * @return List of {@link Job}s.
     */
    public List<Job> loadAll() {
        return jobRepository.findAll();
    }

    /**
     * Loads all scheduled jobs, sorted by their next run date.
     *
     * @return List of scheduled jobs.
     */
    public List<Job> loadScheduled() {
        return jobRepository.findAll().stream().filter(job -> job.isActive()).filter(job -> !runningJobs.containsKey(job.getId())).sorted((o1, o2) -> {
            CronSequenceGenerator cronTriggerOne = new CronSequenceGenerator(o1.getTrigger());
            Date nextRunOne = cronTriggerOne.next(Calendar.getInstance().getTime());
            CronSequenceGenerator cronTriggerTwo = new CronSequenceGenerator(o2.getTrigger());
            Date nextRunTwo = cronTriggerTwo.next(Calendar.getInstance().getTime());
            return nextRunOne.compareTo(nextRunTwo);
        }).collect(Collectors.toList());
    }

    /**
     * Returns a job's execution state for the job with the given ID.
     *
     * @param id The job's ID.
     * @return The current {@link JobExecution} of the job, if it is running, or {@code null} otherwise.
     */
    public JobExecution getJobExecution(Long id) {
        if (runningJobs.containsKey(id)) {
            return runningJobs.get(id).getJobExecution();
        }
        return null;
    }

    /**
     * Schedules the given job according to its trigger configuration.
     *
     * @param job The job to schedule.
     */
    private void schedule(Job job) {
        job.setJobListener(this);
        if (!job.isActive()) {
            log.debug("Job '{}' is not active and will not be scheduled...", job.getName());
            return;
        }
        try {
            scheduledJobFutures.put(job.getId(), taskScheduler.schedule(new Thread(() -> job.run()),
                    new CronTrigger(job.getTrigger())));
            log.info("Scheduled job: {} ({}).", job.getName(), job.getId());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Illegal trigger configured (" + e.getMessage() + ")");
        }
    }

    /**
     * Unschedules the provided job.
     *
     * @param job The job to unschedule.
     */
    private void unschedule(Job job) {
        if (job.getId() != null && scheduledJobFutures.containsKey(job.getId())) {
            if (!scheduledJobFutures.get(job.getId()).cancel(true)) {
                throw new ServiceException("Job " + job.getId() + " could not be cancelled!");
            }
            scheduledJobFutures.remove(job.getId());
            log.info("Unscheduled job: {} ({})", job.getName(), job.getId());
        }
    }

}
