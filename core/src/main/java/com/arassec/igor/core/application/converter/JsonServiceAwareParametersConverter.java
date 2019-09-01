package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.converter.simulation.ServiceProxy;
import com.arassec.igor.core.application.converter.util.EncryptionUtil;
import com.arassec.igor.core.model.service.Service;
import com.arassec.igor.core.repository.ServiceRepository;
import com.github.openjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Converts parameters from and to their JSON representation.
 */
@Slf4j
@Component
public class JsonServiceAwareParametersConverter extends JsonParametersConverter {

    /**
     * Repository for services. Required to get a service instance as a service parameter's value.
     */
    private final ServiceRepository serviceRepository;

    /**
     * Creates a new instance.
     *
     * @param encryptionUtil    The encryption util for securing parameter values.
     * @param serviceRepository The service repository to create services.
     */
    public JsonServiceAwareParametersConverter(EncryptionUtil encryptionUtil, ServiceRepository serviceRepository) {
        super(encryptionUtil);
        this.serviceRepository = serviceRepository;
    }

    /**
     * Converts the supplied parameter. If it is a service reference, a new service instance is created.
     *
     * @param parameter The parameter in JSON form.
     * @param target    The target map where the converted parameter should be stored in.
     * @param config    The converter configuration.
     */
    @Override
    protected void convertParameter(JSONObject parameter, Map<String, Object> target, ConverterConfig config) {
        Object value = parameter.opt(JsonKeys.VALUE);
        boolean isService = parameter.getBoolean(JsonKeys.SERVICE);
        if (isService && value != null && value instanceof Integer) {
            Service service = serviceRepository.findById(Long.valueOf((Integer) value));
            if (config.getSimulationDataCollector() != null) {
                Object proxyService = Proxy.newProxyInstance(JsonServiceAwareParametersConverter.class.getClassLoader(),
                        ClassUtils.getAllInterfaces(service), new ServiceProxy(service));
                target.put(parameter.getString(JsonKeys.NAME), proxyService);
            } else {
                target.put(parameter.getString(JsonKeys.NAME), service);
            }
        } else {
            super.convertParameter(parameter, target, config);
        }
    }

}
