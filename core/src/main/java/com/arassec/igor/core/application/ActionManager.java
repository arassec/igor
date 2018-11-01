package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ActionFactory;
import com.arassec.igor.core.model.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class ActionManager {

    @Autowired
    private ActionFactory actionFactory;

    public Set<String> getTypes() {
        return actionFactory.getTypes();
    }

    public Action createAction(String type, Map<String, Object> parameters) {
        return actionFactory.createInstance(type, parameters, false);
    }

}
