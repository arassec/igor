package com.arassec.igor.core.application.execution;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.repository.JobExecutionRepository;
import com.arassec.igor.core.repository.JobRepository;
import com.arassec.igor.core.util.ModelPage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Executes jobs and keeps track of their state. Prevents parallel execution and limits the number of jobs running in
 * parallel.
 */
@Slf4j
@Component
public class JobExecutor {

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
     * Maximum number of jobs executed in parallel.s
     */
    @Getter
    private int jobQueueSize = 5;

    /**
     * The executor service to run the jobs.
     */
    private ThreadPoolExecutor executorService;

    /**
     * Contains the futures of currently running jobs.
     */
    private List<Future<Job>> runningJobFutures = new LinkedList<>();

    /**
     * Contains the currently running jobs, indexed by their ID.
     */
    private Map<Long, Job> runningJobs = new HashMap<>();

    /**
     * Creates a new JobExecutor instance.
     */
    public JobExecutor() {
        executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(jobQueueSize,
                runnable -> new Thread(runnable, "job-executor-thread"));
    }

    /**
     * Polls the job execution queue every second and handles job states.
     */
    @Scheduled(fixedDelay = 1000)
    public void update() {

        // First check the state of the running jobs:
        Iterator<Future<Job>> runningJobFuturesIterator = runningJobFutures.iterator();
        while (runningJobFuturesIterator.hasNext()) {
            Future<Job> jobFuture = runningJobFuturesIterator.next();
            try {
                if (jobFuture.isDone()) {
                    Job job = jobFuture.get();
                    JobExecution jobExecution = job.getCurrentJobExecution();
                    jobExecutionRepository.upsert(jobExecution);
                    runningJobs.remove(job.getId());
                    runningJobFuturesIterator.remove();
                }
            } catch (InterruptedException | ExecutionException e) {
                log.warn("Interrupted during job execution!", e);
            }
        }

        // If the queue is still full, no more job executions are processed:
        if (runningJobFutures.size() == jobQueueSize) {
            return;
        }

        // At this point we run the next jobs if necessary:
        int freeSlots = jobQueueSize - runningJobs.size();
        ModelPage<JobExecution> waitingJobExecutions = jobExecutionRepository
                .findInState(JobExecutionState.WAITING, 0, Integer.MAX_VALUE);
        if (waitingJobExecutions != null && waitingJobExecutions.getItems() != null) {
            for (int i = 0; i < waitingJobExecutions.getItems().size(); i++) {
                JobExecution jobExecution = waitingJobExecutions.getItems().get(i);
                if (!runningJobs.containsKey(jobExecution.getJobId())) {
                    Job job = jobRepository.findById(jobExecution.getJobId());
                    if (job != null) {
                        jobExecution.setExecutionState(JobExecutionState.RUNNING);
                        jobExecution.setStarted(Instant.now());
                        runningJobs.put(job.getId(), job);
                        runningJobFutures.add(executorService.submit(new JobRunningCallable(job, jobExecution)));
                        jobExecutionRepository.upsert(jobExecution);
                        freeSlots--;
                    }
                }
                if (freeSlots == 0) {
                    break;
                }
            }
        }
    }

    /**
     * Cancels a running job.
     *
     * @param jobId The job's ID.
     */
    public void cancel(Long jobId) {
        if (jobId == null) {
            throw new IllegalStateException("Cannot cancel a job without a job ID!");
        }
        if (runningJobs.containsKey(jobId)) {
            Job job = runningJobs.get(jobId);
            job.cancel();
            while (job.isRunning()) {
                log.trace("Job {} is still running: {}", job.getId(), job.getName());
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    // Not important, just waiting for the job to finish in an endless loop...
                }
            }
        }
    }

}
