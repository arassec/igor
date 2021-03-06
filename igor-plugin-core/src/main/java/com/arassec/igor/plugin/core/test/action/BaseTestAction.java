package com.arassec.igor.plugin.core.test.action;

import com.arassec.igor.core.model.action.BaseAction;
import com.arassec.igor.plugin.core.CorePluginCategory;

/**
 * Base class for 'Test' actions.
 */
public abstract class BaseTestAction extends BaseAction {

    /**
     * Creates a new component instance.
     *
     * @param typeId     The type ID.
     */
    protected BaseTestAction(String typeId) {
        super(CorePluginCategory.TEST.getId(), typeId);
    }

}
