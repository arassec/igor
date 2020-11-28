package com.arassec.igor.plugin.common.message.action;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.plugin.common.CommonCategory;

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
        super(CommonCategory.MESSAGE.getId(), typeId);
    }

}
