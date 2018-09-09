package com.arassec.igor.web.api.util;

import com.arassec.igor.core.application.ProviderManager;
import com.arassec.igor.core.model.IgorProvider;
import com.arassec.igor.web.api.model.ProviderModel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class ProviderUtil implements InitializingBean {

    @Autowired
    private ProviderManager providerManager;

    @Getter
    List<ProviderModel> providerModels = new LinkedList<>();

    @Override
    public void afterPropertiesSet() {
        providerManager.getTypes().stream().forEach(type -> {
            try {
                Class<?> aClass = Class.forName(type);
                IgorProvider annotation = aClass.getAnnotation(IgorProvider.class);
                providerModels.add(new ProviderModel(type, annotation.label()));
            } catch (ClassNotFoundException e) {
                log.error("Could not create provider class!", e);
            }
        });
    }

}
