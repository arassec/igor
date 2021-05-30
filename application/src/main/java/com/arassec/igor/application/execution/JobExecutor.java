package com.arassec.igor.application.execution;

import com.arassec.igor.application.IgorApplicationProperties;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.event.JobEvent;
import com.arassec.igor.core.util.event.JobEventType;
import com.arassec.igor.core.util.event.JobTriggerEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Executes jobs and keeps track of their state. Prevents parallel execution of the same job and limits the total number of jobs
 * running in parallel.
 */
@Slf4j
@Component
public class JobExecutor {

    /**
     * Igor's application configuration properties.
     */
    private final IgorApplicationProperties igorApplicationProperties;

    /**
     * Repository for jobs.
     */
    private final JobRepository jobRepository;

    /**
     * Repository for job-executions.
     */
    private final JobExecutionRepository jobExecutionRepository;

    /**
     * The {@link ThreadPoolExecutor} to run the jobs.
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    /**
     * Contains the futures of currently running or active jobs.
     */
    private final List<Future<Job>> currentlyProcessedJobFutures = new LinkedList<>();

    /**
     * Contains the currently running or active jobs, indexed by their ID.
     */
    private final Map<String, Job> currentlyProcessedJobs = new HashMap<>();

    /**
     * Publisher for events based on job changes.
     */
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Creates a new JobExecutor instance.
     *
     * @param igorApplicationProperties        Core configuration properties of igor.
     * @param jobRepository             Repository for jobs.
     * @param jobExecutionRepository    Repository for job executions.
     * @param applicationEventPublisher Publisher for application events.
     */
    public JobExecutor(IgorApplicationProperties igorApplicationProperties, JobRepository jobRepository,
                       JobExecutionRepository jobExecutionRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.igorApplicationProperties = igorApplicationProperties;
        this.jobRepository = jobRepository;
        this.jobExecutionRepository = jobExecutionRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        threadPoolExecutor = (ThreadPoolExecutor) Executors
                .newFixedThreadPool(igorApplicationProperties.getJobQueueSize(), runnable -> new Thread(runnable, "job-executor-thread"));
    }

    /**
     * Polls the job execution queue every second and handles job states.
     */
    @Scheduled(fixedDelay = 1000)
    public void update() {

        // First check the state of the running jobs:
        currentlyProcessedJobFutures.removeIf(this::processFinished);

        // Send updates on running jobs to clients:
        currentlyProcessedJobs.forEach((key, value) -> applicationEventPublisher.publishEvent(
                new JobEvent(JobEventType.STATE_REFRESH, value)));

        // Check if we can run another job:
        int freeSlots = igorApplicationProperties.getJobQueueSize() - currentlyProcessedJobs.size();

        ModelPage<JobExecution> waitingJobExecutions = jobExecutionRepository
                .findInState(JobExecutionState.WAITING, 0, Integer.MAX_VALUE);

        for (JobExecution jobExecution : waitingJobExecutions.getItems()) {
            var job = jobRepository.findById(jobExecution.getJobId());
            if (job == null) {
                continue;
            }
            if (freeSlots > 0 && !currentlyProcessedJobs.containsKey(jobExecution.getJobId())) {

                if (job.getTrigger() instanceof EventTrigger) {
                    jobExecution.setExecutionState(JobExecutionState.ACTIVE);
                } else {
                    jobExecution.setExecutionState(JobExecutionState.RUNNING);
                }

                jobExecution.setStarted(Instant.now());
                currentlyProcessedJobs.put(job.getId(), job);
                currentlyProcessedJobFutures.add(threadPoolExecutor.submit(new JobRunningCallable(job, jobExecution)));
                jobExecutionRepository.upsert(jobExecution);
                applicationEventPublisher.publishEvent(new JobEvent(JobEventType.STATE_CHANGE, job));
                freeSlots--;
            } else {
                // Send updates on waiting jobs to clients:
                applicationEventPublisher.publishEvent(new JobEvent(JobEventType.STATE_REFRESH, job));
            }
        }
    }

    /**
     * Cancels a running job.
     *
     * @param jobId The job's ID.
     */
    public void cancel(String jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("Cannot cancel a job without a job ID!");
        }
        if (currentlyProcessedJobs.containsKey(jobId)) {
            var job = currentlyProcessedJobs.get(jobId);
            job.cancel();
            applicationEventPublisher.publishEvent(new JobEvent(JobEventType.STATE_CHANGE, job));
        }
    }

    /**
     * Listens for job trigger events and starts processing them in the job.
     *
     * @param jobTriggerEvent The event containing additional data.
     */
    @EventListener
    public void onJobTriggerEvent(JobTriggerEvent jobTriggerEvent) {
        currentlyProcessedJobs.values().stream()
                .filter(job -> job.getId().equals(jobTriggerEvent.getJobId()))
                .filter(job -> job.getTrigger() instanceof EventTrigger)
                .filter(job -> ((EventTrigger) job.getTrigger()).getSupportedEventType().equals(jobTriggerEvent.getEventType()))
                .findFirst()
                .ifPresent(job -> ((EventTrigger) job.getTrigger()).processEvent(jobTriggerEvent.getEventData()));
    }

    /**
     * Returns the job execution of a currently processed job.
     *
     * @param jobId The job's ID.
     *
     * @return The {@link JobExecution} or {@code null}, if none could be found.
     */
    public JobExecution getJobExecution(String jobId) {
        if (currentlyProcessedJobs.containsKey(jobId)) {
            return currentlyProcessedJobs.get(jobId).getCurrentJobExecution();
        }
        return null;
    }

    /**
     * Checks whether the supplied job finished its work or not.
     *
     * @param jobFuture The future containing the running job.
     *
     * @return {@code true}, if the job has finished, {@code false} otherwise.
     */
    private boolean processFinished(Future<Job> jobFuture) {
        try {
            if (jobFuture.isDone()) {
                var job = jobFuture.get();
                var jobExecution = job.getCurrentJobExecution();
                if (JobExecutionState.FINISHED.equals(jobExecution.getExecutionState())
                        && job.isFaultTolerant()) {
                    jobExecutionRepository.updateAllJobExecutionsOfJob(job.getId(), JobExecutionState.FAILED, JobExecutionState.RESOLVED);
                }
                jobExecutionRepository.upsert(jobExecution);
                currentlyProcessedJobs.remove(job.getId());
                applicationEventPublisher.publishEvent(new JobEvent(JobEventType.STATE_CHANGE, job));
                return true;
            }
        } catch (InterruptedException e) {
            log.error("Interrupted during job execution!", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.warn("Exception during job execution!", e);
        }
        return false;
    }

}
