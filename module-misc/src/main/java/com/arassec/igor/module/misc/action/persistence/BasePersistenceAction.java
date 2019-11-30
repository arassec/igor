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
        super("69b6ab78-9ab4-4282-91bd-8a78bff1aa7b", typeId);
        this.persistentValueRepository = persistentValueRepository;
    }

}
