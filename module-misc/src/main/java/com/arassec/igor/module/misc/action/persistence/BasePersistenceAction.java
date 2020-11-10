package com.arassec.igor.module.misc.action.persistence;

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
     * @param typeId                    The concrete action's type ID.
     * @param persistentValueRepository The repository for persisted values.
     */
    protected BasePersistenceAction(String typeId, PersistentValueRepository persistentValueRepository) {
        super("persistence-actions", typeId);
        this.persistentValueRepository = persistentValueRepository;
    }

}
