package com.arassec.igor.core.model.annotation.validation;

import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.repository.JobRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates a job's name and checks, that it is unique.
 * <p>
 * This could not be implemented as field validator because the ID of the job is required, additional to the name. The bean
 * validation API does not support access to other fields within a field validator.
 */
public class UniqueJobNameValidator implements ConstraintValidator<UniqueJobName, Job> {

    /**
     * The repository for jobs.
     */
    private final JobRepository jobRepository;

    /**
     * Creates a new validator instance.
     *
     * @param jobRepository The repository for jobs.
     */
    public UniqueJobNameValidator(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Takes the supplied job's name and loads an existing job with the same name (if any). The existing job must have the same ID
     * as the new job or else it will be rejected.
     *
     * @param job                        The job to check.
     * @param constraintValidatorContext The validator context.
     *
     * @return {@code true} if the supplied job is valid, {@code false} if another job with the same name already exists.
     */
    @Override
    public boolean isValid(Job job, ConstraintValidatorContext constraintValidatorContext) {
        if (job == null) {
            return true;
        }

        var existingJobWithSameName = jobRepository.findByName(job.getName());

        if (existingJobWithSameName != null) {
            return existingJobWithSameName.getId().equals(job.getId());
        }

        return true;
    }

}
