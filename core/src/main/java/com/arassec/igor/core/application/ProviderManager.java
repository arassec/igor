package com.arassec.igor.core.application;

import com.arassec.igor.core.application.factory.ProviderFactory;
import com.arassec.igor.core.model.provider.Provider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class ProviderManager {

    @Autowired
    private ProviderFactory providerFactory;

    public Set<String> getTypes() {
        return providerFactory.getTypes();
    }

    public Provider createProvider(String type, Map<String, Object> parameters) {
        return providerFactory.createInstance(type, parameters, false);
    }
}
