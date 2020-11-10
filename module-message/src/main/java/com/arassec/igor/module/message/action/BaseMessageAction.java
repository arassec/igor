package com.arassec.igor.module.message.action;

import com.arassec.igor.core.model.action.BaseAction;

/**
 * Base class for message based actions.
 */
abstract class BaseMessageAction extends BaseAction {

    /**
     * Creates a new component instance.
     *
     * @param typeId The type ID.
     */
    protected BaseMessageAction(String typeId) {
        super("message-actions", typeId);
    }

}
