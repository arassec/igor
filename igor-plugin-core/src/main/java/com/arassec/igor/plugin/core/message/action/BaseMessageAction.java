package com.arassec.igor.plugin.core.message.action;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.plugin.core.CoreCategory;

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
        super(CoreCategory.MESSAGE.getId(), typeId);
    }

}
