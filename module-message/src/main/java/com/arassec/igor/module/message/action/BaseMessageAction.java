package com.arassec.igor.module.message.action;

import com.arassec.igor.core.model.action.BaseAction;

/**
 * Base class for message based actions.
 */
abstract class BaseMessageAction extends BaseAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCategoryId() {
        return "02e1da3f-cdaa-4b4b-9a60-460480c7b87d";
    }

}
