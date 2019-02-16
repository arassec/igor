package com.arassec.igor.persistence.service;

import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.job.persistence.PersistentValue;
import com.arassec.igor.core.model.service.BaseService;
import com.arassec.igor.core.model.service.ServiceException;
import com.arassec.igor.core.model.service.persistence.PersistenceService;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.core.util.ApplicationContextProvider;
import com.arassec.igor.persistence.repository.JdbcPersistentValueRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@IgorService(label = "Database")
public class DatabasePersistenceService extends BaseService implements PersistenceService {

    /**
     * The repository for access to the persistent values in igor's database. This is a spring-bean and will be
     * retrieved using the {@link ApplicationContextProvider}.
     */
    private PersistentValueRepository persistentValueRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(Long jobId, String taskId, String value) {
        if (persistentValueRepository == null) {
            loadPersistentValueRepository();
        }
        persistentValueRepository.upsert(jobId, taskId, new PersistentValue(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPersisted(Long jobId, String taskId, String value) {
        if (persistentValueRepository == null) {
            loadPersistentValueRepository();
        }
        return persistentValueRepository.isPersisted(jobId, taskId, new PersistentValue(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup(Long jobId, String taskId, int numEntriesToKeep) {
        if (persistentValueRepository == null) {
            loadPersistentValueRepository();
        }
        persistentValueRepository.cleanup(jobId, taskId, numEntriesToKeep);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testConfiguration() throws ServiceException {
        if (persistentValueRepository == null) {
            loadPersistentValueRepository();
        }
        if (persistentValueRepository == null) {
            throw new ServiceException("PersistentValueRepository could not be fetched from context!");
        }
    }

    /**
     * Loads the repository from the spring context.
     */
    private void loadPersistentValueRepository() {
        persistentValueRepository = ApplicationContextProvider.getBean(JdbcPersistentValueRepository.class);
    }

}
