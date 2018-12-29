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
    private Map<Long, ScheduledFuture> scheduledJobFutures = new HashMap<>();

    /**
     * Keeps track of all isRunning jobs.
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
     * Cleans the manager up by unscheduling all scheduled jobs and cancelling all isRunning jobs.
     */
    @Override
    public void destroy() {
        scheduledJobFutures.values().stream().forEach(scheduledFuture -> scheduledFuture.cancel(true));
        runningJobs.values().stream().forEach(job -> job.cancel());
    }

    /**
     * Saves the supplied job using the {@link JobRepository}.
     *
     * @param job The job to save.
     */
    public void save(Job job) {
        if (job.isActive()) {
            schedule(job);
        } else {
            unschedule(job);
        }
        jobRepository.upsert(job);
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
     * Runs the provided job if it is not already isRunning. The job is unscheduled during it's run and re-scheduled
     * according to its trigger after it finishes.
     * <p>
     * This method should be called if the job should run immediately and only once. If the job should run regularly
     * according to its trigger configuration, {@link JobManager#schedule(Job)} should be used.
     *
     * @param job The job to run.
     */
    public void run(Job job) {
        if (runningJobs.containsKey(job.getId())) {
            log.debug("Job already isRunning: {} ({})", job.getName(), job.getId());
            return;
        }
        log.info("Manually isRunning job: {} ({})", job.getName(), job.getId());
        job.setJobListener(this);
        unschedule(job);
        // The job will be re-scheduled according to its trigger after its execution with the notify-finished-callback mechanism.
        taskScheduler.schedule(new Thread(() -> job.run()), Instant.now());
    }

    /**
     * Cancels a isRunning job.
     *
     * @param id The ID of the job that should be cancelled.
     */
    public void cancel(Long id) {
        if (runningJobs.containsKey(id)) {
            Job job = runningJobs.get(id);
            log.info("Manually cancelling job: {} ({})", job.getName(), job.getId());
            job.getJobExecution().cancel();
        }
    }

    /**
     * Schedules the given job according to its trigger configuration.
     *
     * @param job The job to schedule.
     */
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
     * Unschedules the provided job.
     *
     * @param job The job to unschedule.
     */
    public void unschedule(Job job) {
        if (scheduledJobFutures.containsKey(job.getId())) {
            if (!scheduledJobFutures.get(job.getId()).cancel(true)) {
                throw new ServiceException("Job " + job.getId() + " could not be cancelled!");
            }
            scheduledJobFutures.remove(job.getId());
            log.info("Canceled job: {} ({})", job.getName(), job.getId());
        }
    }

    /**
     * Returns a job's execution state for the job with the given ID.
     *
     * @param id The job's ID.
     * @return The current {@link JobExecution} of the job, if it is isRunning, or {@code null} otherwise.
     */
    public JobExecution getJobExecution(Long id) {
        if (runningJobs.containsKey(id)) {
            return runningJobs.get(id).getJobExecution();
        }
        return null;
    }

    /**
     * Saves the reference to the isRunning job for state querying.
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
            log.warn("Parallel job execution detected: {} ({}).", job.getName(), job.getId());
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
