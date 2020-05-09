package com.arassec.igor.module.misc.action.persistence;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.core.repository.PersistentValueRepository;

/**
 * Base class for actions that work with persistence services.
 */
public abstract class BasePersistenceAction extends BaseAction {

    /**
     * The repository for access to the persistent values in igor's database.
     */
    final PersistentValueRepository persistentValueRepository;

    public BasePersistenceAction(String typeId, PersistentValueRepository persistentValueRepository) {
        super("persistence-actions", typeId);
        this.persistentValueRepository = persistentValueRepository;
    }

}
