package com.arassec.igor.plugin.core.persistence.action;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.repository.PersistentValueRepository;

/**
 * Base class for actions that work with persistence connectors.
 */
public abstract class BasePersistenceAction extends BaseAction {

    /**
     * The repository for access to the persistent values in igor's database.
     */
    final PersistentValueRepository persistentValueRepository;

    /**
     * Creates a new instance.
     *
     * @param persistentValueRepository The repository for persisted values.
     */
    protected BasePersistenceAction(PersistentValueRepository persistentValueRepository) {
        this.persistentValueRepository = persistentValueRepository;
    }

}
