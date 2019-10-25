package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.model.job.execution.JobExecution;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.core.util.ApplicationContextProvider;

/**
 * Base class for actions that work with persistence services.
 */
public abstract class BasePersistenceAction extends BaseAction {

    /**
     * The persistence category.
     */
    private static final String CATEGORY_ID = "69b6ab78-9ab4-4282-91bd-8a78bff1aa7b";

    /**
     * The repository for access to the persistent values in igor's database. This is a spring-bean and will be
     * retrieved using the {@link ApplicationContextProvider}.
     */
    PersistentValueRepository persistentValueRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String jobId, String taskId, JobExecution jobExecution) {
        super.initialize(jobId, taskId, jobExecution);
        persistentValueRepository = ApplicationContextProvider.getBean(PersistentValueRepository.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCategoryId() {
        return CATEGORY_ID;
    }

}
