package com.arassec.igor.web.api;

import com.arassec.igor.core.application.ActionManager;
import com.arassec.igor.web.api.model.ActionModel;
import com.arassec.igor.web.api.model.ParameterDefinition;
import com.arassec.igor.web.api.util.ActionUtil;
import com.arassec.igor.web.api.util.ParameterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ActionRestController extends BaseRestController {

    @Autowired
    private ActionUtil actionUtil;

    @Autowired
    private ActionManager actionManager;

    @Autowired
    private ParameterUtil parameterUtil;

    @GetMapping("/actiontype")
    public List<ActionModel> getProviderTypes() {
        return actionUtil.getActionModels();
    }

    @GetMapping("/actionparams/{type}")
    public List<ParameterDefinition> getProviderParameters(@PathVariable("type") String type) {
        return parameterUtil.getParameters(actionManager.createAction(type, null));
    }

}
