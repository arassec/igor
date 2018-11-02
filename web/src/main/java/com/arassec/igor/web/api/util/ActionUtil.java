package com.arassec.igor.web.api.util;

import com.arassec.igor.core.application.ActionManager;
import com.arassec.igor.core.model.IgorAction;
import com.arassec.igor.core.model.action.Action;
import com.arassec.igor.web.api.model.ActionModel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class ActionUtil implements InitializingBean {

    @Autowired
    private ActionManager actionManager;

    @Getter
    List<ActionModel> actionModels = new LinkedList<>();

    @Override
    public void afterPropertiesSet() {
        actionManager.getTypes().stream().forEach(type -> {
            try {
                Class<?> aClass = Class.forName(type);
                IgorAction annotation = aClass.getAnnotation(IgorAction.class);
                actionModels.add(new ActionModel(type, annotation.label()));
            } catch (ClassNotFoundException e) {
                log.error("Could not create provider class!", e);
            }
        });
    }

}
