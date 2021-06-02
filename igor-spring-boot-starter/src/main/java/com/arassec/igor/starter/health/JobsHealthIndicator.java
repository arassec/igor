package com.arassec.igor.starter.health;

import com.arassec.igor.application.manager.JobManager;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.model.job.execution.JobExecutionState;
import com.arassec.igor.core.util.ModelPage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Spring-Boot health indicator for job health.
 */
@Component
@RequiredArgsConstructor
public class JobsHealthIndicator implements HealthIndicator {

    /**
     * The manager for jobs.
     */
    private final JobManager jobManager;

    /**
     * Examines job executions and creates a health status based on them.
     *
     * @return The {@link Health} with regard to job executions.
     */
    @Override
    public Health health() {
        ModelPage<JobExecution> failedJobExecutions = jobManager.getJobExecutionsInState(JobExecutionState.FAILED, 0, Integer.MAX_VALUE);

        if (!failedJobExecutions.getItems().isEmpty()) {
            List<Map<String, String>> details = new LinkedList<>();

            failedJobExecutions.getItems().stream()
                .map(JobExecution::getJobId)
                .distinct()
                .forEach(jobId -> {
                    var job = jobManager.load(jobId);
                    if (job.isActive()) {
                        details.add(Map.of(
                            "jobId", jobId,
                            "jobName", job.getName(),
                            "jobExecutionState", JobExecutionState.FAILED.name()
                        ));
                    }
                });

            if (!details.isEmpty()) {
                var healthBuilder = Health.down();
                healthBuilder.withDetail("jobs", details);
                return healthBuilder.build();
            }
        }

        return Health.up().build();
    }

}
