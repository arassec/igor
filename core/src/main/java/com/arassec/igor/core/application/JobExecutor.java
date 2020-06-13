package com.arassec.igor.core.application;

import com.arassec.igor.core.IgorCoreProperties;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.util.ModelPage;
import com.arassec.igor.core.util.event.JobEvent;
import com.arassec.igor.core.util.event.JobEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Executes jobs and keeps track of their state. Prevents parallel execution of the same job and limits the total number of jobs
 * running in parallel.
 */
@Slf4j
@Component
public class JobExecutor {

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
     * The {@link ThreadPoolExecutor} to run the jobs.
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    /**
     * Contains the futures of currently running jobs.
     */
    private final List<Future<Job>> runningJobFutures = new LinkedList<>();

    /**
     * Contains the currently running jobs, indexed by their ID.
     */
    private final Map<String, Job> runningJobs = new HashMap<>();

    /**
     * Publisher for events based on job changes.
     */
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Creates a new JobExecutor instance.
     *
     * @param igorCoreProperties        Core configuration properties of igor.
     * @param jobRepository             Repository for jobs.
     * @param jobExecutionRepository    Repository for job executions.
     * @param applicationEventPublisher Publisher for application events.     *
     */
    public JobExecutor(IgorCoreProperties igorCoreProperties, JobRepository jobRepository,
                       JobExecutionRepository jobExecutionRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.igorCoreProperties = igorCoreProperties;
        this.jobRepository = jobRepository;
        this.jobExecutionRepository = jobExecutionRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        threadPoolExecutor = (ThreadPoolExecutor) Executors
                .newFixedThreadPool(igorCoreProperties.getJobQueueSize(), runnable -> new Thread(runnable, "job-executor-thread"));
    }

    /**
     * Polls the job execution queue every second and handles job states.
     */
    @Scheduled(fixedDelay = 1000)
    public void update() {

        // First check the state of the running jobs:
        runningJobFutures.removeIf(this::processFinished);

        // Send updates on running jobs to clients:
        runningJobs.forEach((key, value) -> applicationEventPublisher.publishEvent(
                new JobEvent(JobEventType.STATE_REFRESH, value)));

        // Check if we can run another job:
        int freeSlots = igorCoreProperties.getJobQueueSize() - runningJobs.size();
        ModelPage<JobExecution> waitingJobExecutions = jobExecutionRepository
                .findInState(JobExecutionState.WAITING, 0, Integer.MAX_VALUE);
        if (waitingJobExecutions != null && waitingJobExecutions.getItems() != null) {
            for (JobExecution jobExecution : waitingJobExecutions.getItems()) {
                Job job = jobRepository.findById(jobExecution.getJobId());
                if (job == null) {
                    continue;
                }
                if (freeSlots > 0 && !runningJobs.containsKey(jobExecution.getJobId())) {
                    jobExecution.setExecutionState(JobExecutionState.RUNNING);
                    jobExecution.setStarted(Instant.now());
                    runningJobs.put(job.getId(), job);
                    runningJobFutures.add(threadPoolExecutor.submit(new JobRunningCallable(job, jobExecution)));
                    jobExecutionRepository.upsert(jobExecution);
                    applicationEventPublisher.publishEvent(new JobEvent(JobEventType.STATE_CHANGE, job));
                    freeSlots--;
                } else {
                    // Send updates on waiting jobs to clients:
                    applicationEventPublisher.publishEvent(new JobEvent(JobEventType.STATE_REFRESH, job));
                }
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
        if (runningJobs.containsKey(jobId)) {
            Job job = runningJobs.get(jobId);
            job.cancel();

            Object cancelLock = new Object();

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                synchronized (cancelLock) {
                    log.trace("Checking if job {} is still running: {}", job.getId(), job.getName());
                    if (!job.isRunning()) {
                        cancelLock.notifyAll();
                    }
                }
            }, 0, 100, TimeUnit.MILLISECONDS);

            synchronized (cancelLock) {
                try {
                    while (job.isRunning()) {
                        cancelLock.wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            executor.shutdown();
            applicationEventPublisher.publishEvent(new JobEvent(JobEventType.STATE_CHANGE, job));
        }
    }

    /**
     * Returns the job execution of a currently running job.
     *
     * @param jobId The job's ID.
     *
     * @return The {@link JobExecution} or {@code null}, if none could be found.
     */
    JobExecution getJobExecution(String jobId) {
        if (runningJobs.containsKey(jobId)) {
            return runningJobs.get(jobId).getCurrentJobExecution();
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
                Job job = jobFuture.get();
                JobExecution jobExecution = job.getCurrentJobExecution();
                if (JobExecutionState.FINISHED.equals(jobExecution.getExecutionState())
                        && job.isFaultTolerant()) {
                    jobExecutionRepository.updateAllJobExecutionsOfJob(job.getId(), JobExecutionState.FAILED, JobExecutionState.RESOLVED);
                }
                jobExecutionRepository.upsert(jobExecution);
                runningJobs.remove(job.getId());
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
