package com.arassec.igor.core.application;

import com.arassec.igor.core.application.execution.JobExecutor;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.core.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

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
public class JobManager implements InitializingBean, DisposableBean {

    /**
     * Repository for jobs.
     */
    @Autowired
    private JobRepository jobRepository;

    /**
     * Repository for job-executions.
     */
    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    /**
     * Repository for persistent values.
     */
    @Autowired
    private PersistentValueRepository persistentValueRepository;

    /**
     * The task scheduler that starts the jobs according to their trigger.
     */
    @Autowired
    private TaskScheduler taskScheduler;

    /**
     * The job executor actually running the scheduled jobs.
     */
    @Autowired
    private JobExecutor jobExecutor;

    /**
     * Keeps track of all scheduled jobs.
     */
    private Map<Long, ScheduledFuture> scheduledJobs = new ConcurrentHashMap<>();

    /**
     * Initializes the manager by scheduling all available jobs.
     */
    @Override
    public void afterPropertiesSet() {
        // If jobs are already 'running' (e.g. after a server restart) the are updated here:
        jobExecutionRepository.updateState(JobExecutionState.RUNNING, JobExecutionState.FAILED);
        jobRepository.findAll().stream().forEach(job -> schedule(job));
    }

    /**
     * Cleans the manager up by unscheduling all scheduled jobs and cancelling all running jobs.
     */
    @Override
    public void destroy() {
        scheduledJobs.values().stream().forEach(scheduledFuture -> scheduledFuture.cancel(true));
    }

    /**
     * Saves the supplied job using the {@link JobRepository}.
     *
     * @param job The job to save.
     */
    public Job save(Job job) {
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
        jobExecutor.cancel(id);
        Job job = jobRepository.findById(id);
        if (job != null) {
            unschedule(job);
            jobRepository.deleteById(id);
            jobExecutionRepository.deleteByJobId(id);
            persistentValueRepository.deleteByJobId(id);
            scheduledJobs.remove(id);
        }
    }

    /**
     * Enqueues the provided job to the exeuction list. The job will be run as soon as an execution slot is availalbe
     * and after previously enqueued executions are processed.
     * <p>
     * This method should be called if the job should run immediately and only once. If the job should run regularly
     * according to its trigger configuration, {@link JobManager#schedule(Job)} should be used.
     *
     * @param job The job to run as soon as possible.
     */
    public void enqueue(Job job) {
        if (job == null || job.getId() == null) {
            throw new IllegalArgumentException("Job with ID required for run!");
        }
        log.info("Trying to manually enqueue job: {} ({})", job.getName(), job.getId());
        enqueueJob(job);
    }

    /**
     * Cancels a running or waiting job-execution.
     *
     * @param jobExecutionId The ID of the job-execution that should be cancelled.
     */
    public void cancel(Long jobExecutionId) {
        if (jobExecutionId == null) {
            throw new IllegalArgumentException("ID required to cancel job-execution!");
        }
        JobExecution jobExecution = jobExecutionRepository.findById(jobExecutionId);
        if (jobExecution != null) {
            if (JobExecutionState.WAITING.equals(jobExecution.getExecutionState())) {
                jobExecution.setExecutionState(JobExecutionState.CANCELLED);
                jobExecution.setFinished(Instant.now());
                jobExecutionRepository.upsert(jobExecution);
            } else if (JobExecutionState.RUNNING.equals(jobExecution.getExecutionState())) {
                jobExecutor.cancel(jobExecution.getJobId());
            }
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
     * Loads a job with the given name.
     *
     * @param name The job's name.
     * @return The job with the given name or {@code null}, if none exists.
     */
    public Job loadByName(String name) {
        return jobRepository.findByName(name);
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
        return jobRepository.findAll().stream()
                .filter(job -> job.isActive())
                .filter(job -> job.getTrigger() instanceof com.arassec.igor.core.model.trigger.CronTrigger)
                .sorted((o1, o2) -> {
                    String firstCron = ((com.arassec.igor.core.model.trigger.CronTrigger) o1.getTrigger()).getCronExpression();
                    String secondCron = ((com.arassec.igor.core.model.trigger.CronTrigger) o1.getTrigger()).getCronExpression();
                    CronSequenceGenerator cronTriggerOne = new CronSequenceGenerator(firstCron);
                    Date nextRunOne = cronTriggerOne.next(Calendar.getInstance().getTime());
                    CronSequenceGenerator cronTriggerTwo = new CronSequenceGenerator(secondCron);
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
    public List<JobExecution> getJobExecutionsOfJob(Long id) {
        return jobExecutionRepository.findAllOfJob(id);
    }

    /**
     * Returns the job-execution with the given ID.
     *
     * @param id The job-execution's ID.
     * @return The {@link JobExecution}.
     */
    public JobExecution getJobExecution(Long id) {
        return jobExecutionRepository.findById(id);
    }

    /**
     * Returns all job executions which are in the desired state.
     *
     * @param state The state the job executions must be in to be listed.
     * @return The list of job executions in that state.
     */
    public List<JobExecution> getJobExecutionsInState(JobExecutionState state) {
        if (state == null) {
            throw new IllegalArgumentException("JobExecutionState required!");
        }
        return jobExecutionRepository.findInState(state);
    }

    /**
     * Returns the number of slots available for parallel job execution.
     *
     * @return The number of slots.
     */
    public int getNumSlots() {
        return jobExecutor.getJobQueueSize();
    }

    /**
     * Schedules the given job according to its trigger configuration.
     *
     * @param job The job to schedule.
     */
    private void schedule(Job job) {
        if (job.getId() == null) {
            throw new IllegalArgumentException("Job with ID required for scheduling!");
        }
        if (!job.isActive()) {
            log.debug("Job '{}' is not active and will not be scheduled...", job.getName());
            return;
        }

        if (job.getTrigger() instanceof com.arassec.igor.core.model.trigger.CronTrigger) {
            String cronExpression = ((com.arassec.igor.core.model.trigger.CronTrigger) job.getTrigger()).getCronExpression();
            try {
                scheduledJobs.put(job.getId(), taskScheduler.schedule(
                        new Thread(() -> {
                            log.info("Trying to automatically enqueue job: {} ({})", job.getName(), job.getId());
                            enqueueJob(job);
                        }),
                        new CronTrigger(cronExpression)));
                log.info("Scheduled job: {} ({}).", job.getName(), job.getId());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Illegal trigger configured (" + e.getMessage() + ")");
            }
        }
    }

    /**
     * Unschedules the provided job.
     *
     * @param job The job to unschedule.
     */
    private void unschedule(Job job) {
        if (job.getId() != null && scheduledJobs.containsKey(job.getId())) {
            if (!scheduledJobs.get(job.getId()).cancel(true)) {
                throw new ServiceException("Job " + job.getId() + " could not be cancelled!");
            }
            scheduledJobs.remove(job.getId());
            log.info("Unscheduled job: {} ({})", job.getName(), job.getId());
        }
    }

    /**
     * Enqueues the supplied job if there is no other execution of this job waiting for its run.
     *
     * @param job The Job to enqueue.
     */
    private void enqueueJob(Job job) {
        List<JobExecution> waitingJobExecutions = jobExecutionRepository.findAllOfJobInState(job.getId(), JobExecutionState.WAITING);
        if (waitingJobExecutions == null || waitingJobExecutions.isEmpty()) {
            JobExecution jobExecution = new JobExecution();
            jobExecution.setJobId(job.getId());
            jobExecution.setCreated(Instant.now());
            jobExecution.setExecutionState(JobExecutionState.WAITING);
            jobExecutionRepository.upsert(jobExecution);
            jobExecutionRepository.cleanup(job.getId(), job.getExecutionHistoryLimit());
        } else {
            log.info("Job '{}' ({}) already waiting for execution. Skipped execution until next time.", job.getName(), job.getId());
        }
    }

    /**
     * Searches for services referencing the job with the given ID.
     *
     * @param id The job's ID.
     * @return Set of services referencing this service.
     */
    public Set<Pair<Long, String>> getReferencedServices(Long id) {
        return jobRepository.findReferencedServices(id);
    }

}
