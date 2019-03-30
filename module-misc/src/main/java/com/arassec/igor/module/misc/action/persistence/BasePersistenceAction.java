package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.repository.PersistentValueRepository;
import com.arassec.igor.core.util.ApplicationContextProvider;

/**
 * Base class for actions that work with persistence services.
 */
public abstract class BasePersistenceAction extends BaseAction implements PersistenceAction {

    /**
     * The repository for access to the persistent values in igor's database. This is a spring-bean and will be
     * retrieved using the {@link ApplicationContextProvider}.
     */
    protected PersistentValueRepository persistentValueRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        super.initialize();
        persistentValueRepository = ApplicationContextProvider.getBean(PersistentValueRepository.class);
    }

}
