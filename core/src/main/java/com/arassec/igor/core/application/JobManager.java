package com.arassec.igor.core.application;

import com.arassec.igor.core.IgorCoreProperties;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.ModelPageHelper;
import com.arassec.igor.core.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
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
@RequiredArgsConstructor
public class JobManager implements ApplicationListener<ContextRefreshedEvent>, DisposableBean {

    /**
     * Igor's core configuration properties.
     */
    private final IgorCoreProperties igorCoreProperties;

    /**
     * Repository for jobs.
     */
    private final JobRepository jobRepository;

    /**
     * Repository for job-executions.
     */
    private final JobExecutionRepository jobExecutionRepository;

    /**
     * Repository for persistent values.
     */
    private final PersistentValueRepository persistentValueRepository;

    /**
     * The task scheduler that starts the jobs according to their trigger.
     */
    private final TaskScheduler taskScheduler;

    /**
     * The job executor actually running the scheduled jobs.
     */
    private final JobExecutor jobExecutor;

    /**
     * Keeps track of all scheduled jobs.
     */
    private final Map<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();

    /**
     * Initializes the manager by scheduling all available jobs.
     *
     * @param contextRefreshedEvent Event indicating that the spring context has been refreshed.
     */
    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
        // If jobs are already 'running' (e.g. after a server restart) the are updated here:
        ModelPage<JobExecution> jobExecutions = jobExecutionRepository
                .findInState(JobExecutionState.RUNNING, 0, Integer.MAX_VALUE);
        if (jobExecutions != null && jobExecutions.getItems() != null) {
            jobExecutions.getItems().forEach(jobExecution -> {
                jobExecution.setErrorCause("Job interrupted due to application restart!");
                jobExecution.setExecutionState(JobExecutionState.FAILED);
                jobExecution.setFinished(Instant.now());
                jobExecutionRepository.upsert(jobExecution);
            });
        }
        jobRepository.findAll().forEach(this::schedule);
    }

    /**
     * Cleans the manager up by unscheduling all scheduled jobs and cancelling all running jobs.
     */
    @Override
    public void destroy() {
        scheduledJobs.values().forEach(scheduledFuture -> scheduledFuture.cancel(true));
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
    public void delete(String id) {
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
     * Enqueues the provided job to the exeuction list if no previously enqueued execution of the same job currently exists. The
     * job will be run as soon as an execution slot is availalbe
     * <p>
     * This method should be called if the job should run immediately and only once. If the job should run regularly according to
     * its trigger configuration, {@link JobManager#schedule(Job)} should be used.
     *
     * @param job The job to run as soon as possible.
     */
    public void enqueue(Job job) {
        if (job == null || job.getId() == null) {
            throw new IllegalArgumentException("Job with ID required for run!");
        }
        List<JobExecution> runningJobExecutions = jobExecutionRepository
                .findAllOfJobInState(job.getId(), JobExecutionState.RUNNING);
        List<JobExecution> waitingJobExecutions = jobExecutionRepository
                .findAllOfJobInState(job.getId(), JobExecutionState.WAITING);
        List<JobExecution> failedJobExecutions = jobExecutionRepository
                .findAllOfJobInState(job.getId(), JobExecutionState.FAILED);
        if (failedJobExecutions != null && !failedJobExecutions.isEmpty() && !job.isFaultTolerant()) {
            log.info("Job not enqueued due to previously failed executions and job's fault intolerance.");
        } else if ((runningJobExecutions == null || runningJobExecutions
                .isEmpty()) && (waitingJobExecutions == null || waitingJobExecutions.isEmpty())) {
            log.info("Enqueueing job: {} ({})", job.getName(), job.getId());
            JobExecution jobExecution = new JobExecution();
            jobExecution.setJobId(job.getId());
            jobExecution.setCreated(Instant.now());
            jobExecution.setExecutionState(JobExecutionState.WAITING);
            jobExecutionRepository.upsert(jobExecution);
            jobExecutionRepository.cleanup(job.getId(), job.getHistoryLimit());
        } else {
            log.info("Job '{}' ({}) already executing or waiting for execution. Skipped execution until next time.",
                    job.getName(), job.getId());
        }
    }


    /**
     * Cancels a running or waiting job-execution.
     *
     * @param jobExecutionId The ID of the job-execution that should be cancelled.
     */
    public void cancelExecution(Long jobExecutionId) {
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
     *
     * @return The job with the given ID or {@code null}, if none was found.
     */
    public Job load(String id) {
        return jobRepository.findById(id);
    }

    /**
     * Loads a job with the given name.
     *
     * @param name The job's name.
     *
     * @return The job with the given name or {@code null}, if none exists.
     */
    public Job loadByName(String name) {
        return jobRepository.findByName(name);
    }

    /**
     * Loads a page of jobs matching the supplied criteria.
     *
     * @param pageNumber The page number.
     * @param pageSize   The page size.
     * @param nameFilter An optional name filter for the jobs.
     * @param stateFilter An optional state filter for the jobs.
     *
     * @return The page with jobs matching the criteria.
     */
    public ModelPage<Job> loadPage(int pageNumber, int pageSize, String nameFilter, Set<JobExecutionState> stateFilter) {
        ModelPage<Job> nameFilteredPage = jobRepository.findPage(0, Integer.MAX_VALUE, nameFilter);
        List<Job> filteredList = nameFilteredPage.getItems().stream().filter(job -> {
            if (stateFilter == null || stateFilter.isEmpty()) {
                return true;
            }
            if (job.getCurrentJobExecution() != null) {
                return stateFilter.contains(job.getCurrentJobExecution().getExecutionState());
            } else {
                ModelPage<JobExecution> lastJobExecution = jobExecutionRepository.findAllOfJob(job.getId(), 0, 1);
                return !lastJobExecution.getItems().isEmpty() && stateFilter.contains(lastJobExecution.getItems().get(0).getExecutionState());
            }
        }).collect(Collectors.toList());
        return ModelPageHelper.getModelPage(filteredList, pageNumber, pageSize);
    }

    /**
     * Loads all scheduled jobs, sorted by their next run date.
     *
     * @return List of scheduled jobs.
     */
    public List<Job> loadScheduled() {
        return jobRepository.findAll().stream().filter(Job::isActive)
                .filter(job -> job.getTrigger() instanceof ScheduledTrigger).sorted((o1, o2) -> {
                    String firstCron = ((ScheduledTrigger) o1.getTrigger()).getCronExpression();
                    String secondCron = ((ScheduledTrigger) o2.getTrigger()).getCronExpression();
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
     * @param jobId      The job's ID.
     * @param pageNumber The page to load.
     * @param pageSize   The number of items on the page.
     *
     * @return A page with {@link JobExecution}s of the job.
     */
    public ModelPage<JobExecution> getJobExecutionsOfJob(String jobId, int pageNumber, int pageSize) {
        return jobExecutionRepository.findAllOfJob(jobId, pageNumber, pageSize);
    }

    /**
     * Returns the job-execution with the given ID.
     *
     * @param id The job-execution's ID.
     *
     * @return The {@link JobExecution}.
     */
    public JobExecution getJobExecution(Long id) {
        JobExecution jobExecution = jobExecutionRepository.findById(id);
        if (jobExecution != null && JobExecutionState.RUNNING.equals(jobExecution.getExecutionState())) {
            JobExecution runningJobExecution = jobExecutor.getJobExecution(jobExecution.getJobId());
            if (runningJobExecution != null) {
                jobExecution.setCurrentTask(runningJobExecution.getCurrentTask());
                runningJobExecution.getWorkInProgress().forEach(jobExecution::addWorkInProgress);
            }
        }
        return jobExecution;
    }

    /**
     * Returns all job executions which are in the desired state.
     *
     * @param state      The state the job executions must be in to be listed.
     * @param pageNumber The page to load.
     * @param pageSize   The number of items on the page.
     *
     * @return The list of job executions in that state.
     */
    public ModelPage<JobExecution> getJobExecutionsInState(JobExecutionState state, int pageNumber, int pageSize) {
        if (state == null) {
            throw new IllegalArgumentException("JobExecutionState required!");
        }
        return jobExecutionRepository.findInState(state, pageNumber, pageSize);
    }

    /**
     * Sets a new state to a job execution with the given ID.
     *
     * @param id       The job execution's ID.
     * @param newState The new state to set.
     */
    public void updateJobExecutionState(Long id, JobExecutionState newState) {
        jobExecutionRepository.updateJobExecutionState(id, newState);
    }

    /**
     * Sets the new state to all job executions of the given job in the supplied state.
     *
     * @param jobId    The job's ID.
     * @param oldState The old state to change.
     * @param newState The new state to set.
     */
    public void updateAllJobExecutionsOfJob(String jobId, JobExecutionState oldState, JobExecutionState newState) {
        jobExecutionRepository.updateAllJobExecutionsOfJob(jobId, oldState, newState);
    }

    /**
     * Returns the number of slots available for parallel job execution.
     *
     * @return The number of slots.
     */
    public int getNumSlots() {
        return igorCoreProperties.getJobQueueSize();
    }

    /**
     * Searches for services referencing the job with the given ID.
     *
     * @param id The job's ID.
     *
     * @return Set of services referencing this service.
     */
    public Set<Pair<String, String>> getReferencedServices(String id) {
        return jobRepository.findReferencedServices(id);
    }

    /**
     * Counts the job executions that are in the given state.
     *
     * @param jobExecutionState The state to count executions for.
     * @return The number of executions in the requested state.
     */
    public int countJobExecutions(JobExecutionState jobExecutionState) {
        return jobExecutionRepository.countJobsWithState(jobExecutionState);
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

        if (job.getTrigger() instanceof ScheduledTrigger) {
            String cronExpression = ((ScheduledTrigger) job.getTrigger()).getCronExpression();
            try {
                scheduledJobs.put(job.getId(), taskScheduler.schedule(() -> {
                    log.info("Job triggered for execution: {} ({})", job.getName(), job.getId());
                    enqueue(job);
                }, new CronTrigger(cronExpression)));
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
                throw new IgorException("Job " + job.getId() + " could not be cancelled!");
            }
            scheduledJobs.remove(job.getId());
            log.info("Unscheduled job: {} ({})", job.getName(), job.getId());
        }
    }

}
