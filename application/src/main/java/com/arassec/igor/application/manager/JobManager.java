package com.arassec.igor.application.manager;

import com.arassec.igor.application.IgorApplicationProperties;
import com.arassec.igor.application.execution.JobExecutor;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.core.util.IgorException;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.ModelPageHelper;
import com.arassec.igor.core.util.Pair;
import com.arassec.igor.core.util.event.JobEvent;
import com.arassec.igor.core.util.event.JobEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Manages {@link Job}s. Entry point from outside the core package to create and maintain jobs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobManager implements ApplicationListener<ContextRefreshedEvent>, DisposableBean {

    /**
     * Igor's application configuration properties.
     */
    private final IgorApplicationProperties igorCoreProperties;

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
     * Publisher for events based on job changes.
     */
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Initializes the manager by scheduling all available jobs.
     *
     * @param contextRefreshedEvent Event indicating that the spring context has been refreshed.
     */
    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
        // If jobs are already 'running' (e.g. after a server restart) they are updated here:
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
        // Previously active jobs are simply marked as finished and re-activated.
        jobExecutions = jobExecutionRepository
            .findInState(JobExecutionState.ACTIVE, 0, Integer.MAX_VALUE);
        if (jobExecutions != null && jobExecutions.getItems() != null) {
            jobExecutions.getItems().forEach(jobExecution -> {
                jobExecution.setExecutionState(JobExecutionState.FINISHED);
                jobExecution.setFinished(Instant.now());
                jobExecutionRepository.upsert(jobExecution);
            });
        }
        jobRepository.findAll().forEach(this::activate);
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
     *
     * @return The saved {@link Job}.
     */
    public Job save(Job job) {
        var savedJob = jobRepository.upsert(job);
        if (savedJob.isActive()) {
            // The trigger might have changed, so the job is unscheduled first and then re-scheduled according to its trigger.
            deactivate(savedJob);
            activate(savedJob);
        } else {
            deactivate(savedJob);
        }
        applicationEventPublisher.publishEvent(new JobEvent(JobEventType.SAVE, job));
        return savedJob;
    }

    /**
     * Deletes the job with the given ID.
     *
     * @param id The ID of the job that should be deleted.
     */
    public void delete(String id) {
        jobExecutor.cancel(id);
        var job = jobRepository.findById(id);
        if (job != null) {
            deactivate(job);
            jobRepository.deleteById(id);
            jobExecutionRepository.deleteByJobId(id);
            persistentValueRepository.deleteByJobId(id);
            scheduledJobs.remove(id);
            applicationEventPublisher.publishEvent(new JobEvent(JobEventType.DELETE, job));
        }
    }

    /**
     * Enqueues the provided job to the execution list if no previously enqueued execution of the same job currently exists. The
     * job will be run as soon as an execution slot is available
     * <p>
     * This method should be called if the job should run immediately and only once. If the job should run regularly according to
     * its trigger configuration, {@link JobManager#activate(Job)} should be used.
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
            var jobExecution = new JobExecution();
            jobExecution.setJobId(job.getId());
            jobExecution.setCreated(Instant.now());
            jobExecution.setExecutionState(JobExecutionState.WAITING);
            jobExecutionRepository.upsert(jobExecution);
            jobExecutionRepository.cleanup(job.getId(), job.getHistoryLimit());
            applicationEventPublisher.publishEvent(new JobEvent(JobEventType.STATE_CHANGE, job));
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
        var jobExecution = jobExecutionRepository.findById(jobExecutionId);
        if (jobExecution != null) {
            if (JobExecutionState.WAITING.equals(jobExecution.getExecutionState())) {
                jobExecution.setExecutionState(JobExecutionState.CANCELLED);
                jobExecution.setFinished(Instant.now());
                jobExecutionRepository.upsert(jobExecution);
                applicationEventPublisher.publishEvent(new JobEvent(JobEventType.STATE_CHANGE,
                    jobRepository.findById(jobExecution.getJobId())));
            } else if (JobExecutionState.RUNNING.equals(jobExecution.getExecutionState())
                || JobExecutionState.ACTIVE.equals(jobExecution.getExecutionState())) {
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
     * @param pageNumber   The page number.
     * @param pageSize     The page size.
     * @param nameFilter   An optional name filter for the jobs.
     * @param stateFilters An optional state filter for the jobs.
     *
     * @return The page with jobs matching the criteria.
     */
    public ModelPage<Job> loadPage(int pageNumber, int pageSize, String nameFilter, Set<JobExecutionState> stateFilters) {
        ModelPage<Job> nameFilteredPage = jobRepository.findPage(0, Integer.MAX_VALUE, nameFilter);
        List<Job> filteredList = nameFilteredPage.getItems().stream().filter(job -> {
            if (stateFilters == null || stateFilters.isEmpty()) {
                return true;
            }
            return stateFilters.stream().filter(stateFilter -> (jobExecutionRepository.countAllOfJobInState(job.getId(),
                stateFilter) > 0)).map(result -> true).findFirst().orElse(false);
        }).toList();
        return ModelPageHelper.getModelPage(filteredList, pageNumber, pageSize);
    }

    /**
     * Loads all scheduled jobs, sorted by their next run date.
     *
     * @return List of scheduled jobs.
     */
    public List<Job> loadScheduled() {
        return jobRepository.findAll().stream().filter(Job::isActive)
            .filter(job -> job.getTrigger() instanceof ScheduledTrigger)
            .sorted((jobOne, jobTwo) -> {
                String firstCron = ((ScheduledTrigger) jobOne.getTrigger()).getCronExpression();
                String secondCron = ((ScheduledTrigger) jobTwo.getTrigger()).getCronExpression();
                var cronTriggerOne = CronExpression.parse(firstCron);
                LocalDateTime nextRunOne = cronTriggerOne.next(LocalDateTime.now());
                var cronTriggerTwo = CronExpression.parse(secondCron);
                LocalDateTime nextRunTwo = cronTriggerTwo.next(LocalDateTime.now());
                if (nextRunOne != null && nextRunTwo != null) {
                    return nextRunOne.compareTo(nextRunTwo);
                } else {
                    return 0;
                }
            }).toList();
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
        var jobExecution = jobExecutionRepository.findById(id);
        if (jobExecution != null && jobExecution.isRunningOrActive()) {
            var runningJobExecution = jobExecutor.getJobExecution(jobExecution.getJobId());
            if (runningJobExecution != null) {
                runningJobExecution.getWorkInProgress().forEach(jobExecution::addWorkInProgress);
                jobExecution.setProcessedEvents(runningJobExecution.getProcessedEvents());
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
        var jobExecution = jobExecutionRepository.findById(id);
        applicationEventPublisher.publishEvent(new JobEvent(JobEventType.STATE_CHANGE,
            jobRepository.findById(jobExecution.getJobId())));
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
        applicationEventPublisher.publishEvent(new JobEvent(JobEventType.STATE_CHANGE, jobRepository.findById(jobId)));
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
     * Searches for connectors referencing the job with the given ID.
     *
     * @param id The job's ID.
     *
     * @return Set of connectors referencing this connector.
     */
    public Set<Pair<String, String>> getReferencedConnectors(String id) {
        return jobRepository.findReferencedConnectors(id);
    }

    /**
     * Counts the job executions that are in the given state.
     *
     * @param jobExecutionState The state to count executions for.
     *
     * @return The number of executions in the requested state.
     */
    public int countJobExecutions(JobExecutionState jobExecutionState) {
        return jobExecutionRepository.countJobsWithState(jobExecutionState);
    }

    /**
     * Counts the job executions that are in the given state.
     *
     * @param jobId             The job's ID.
     * @param jobExecutionState The state to count executions for.
     *
     * @return The number of executions in the requested state.
     */
    public int countExecutionsOfJobInState(String jobId, JobExecutionState jobExecutionState) {
        return jobExecutionRepository.countAllOfJobInState(jobId, jobExecutionState);
    }

    /**
     * Activates the given job according to its trigger configuration.
     *
     * @param job The job to schedule.
     */
    private void activate(Job job) {
        if (job.getId() == null) {
            throw new IllegalArgumentException("Job with ID required for scheduling!");
        }
        if (!job.isActive()) {
            log.debug("Job '{}' is not active...", job.getName());
            return;
        }

        if (job.getTrigger() instanceof ScheduledTrigger scheduledTrigger) {
            String cronExpression = scheduledTrigger.getCronExpression();
            try {
                scheduledJobs.put(job.getId(), taskScheduler.schedule(() -> {
                    log.info("Job triggered for execution: {} ({})", job.getName(), job.getId());
                    enqueue(job);
                }, new CronTrigger(cronExpression)));
                log.info("Activated job: {} ({}).", job.getName(), job.getId());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Illegal trigger configured (" + e.getMessage() + ")");
            }
        }

        if (job.getTrigger() instanceof EventTrigger) {
            enqueue(job);
        }
    }

    /**
     * Deactivates the provided job.
     *
     * @param job The job to unschedule.
     */
    private void deactivate(Job job) {
        if (job.getId() != null && scheduledJobs.containsKey(job.getId())) {
            if (!scheduledJobs.get(job.getId()).cancel(true)) {
                throw new IgorException("Job " + job.getId() + " could not be cancelled!");
            }
            scheduledJobs.remove(job.getId());
            log.info("Deactivated job: {} ({})", job.getName(), job.getId());
        } else if (job.getId() != null && job.getTrigger() instanceof EventTrigger) {
            jobExecutor.cancel(job.getId());
        }
    }

}
