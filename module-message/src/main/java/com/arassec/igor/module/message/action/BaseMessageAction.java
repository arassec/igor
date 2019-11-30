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
    public BaseMessageAction(String typeId) {
        super("02e1da3f-cdaa-4b4b-9a60-460480c7b87d", typeId);
    }

}
