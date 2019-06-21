package com.arassec.igor.core.application.converter;

import com.arassec.igor.core.application.converter.util.EncryptionUtil;
import com.arassec.igor.core.repository.ServiceRepository;
import com.github.openjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
     * @param parameter     The parameter in JSON form.
     * @param applySecurity Set to {@code true} to decrypt secured properties. If set to {@code false}, secured
     *                      properties will be kept in encrypted form.
     * @param target        The target map where the converted parameter should be stored in.
     */
    @Override
    protected void convertParameter(JSONObject parameter, boolean applySecurity, Map<String, Object> target) {
        Object value = parameter.opt(JsonKeys.VALUE);
        boolean isService = parameter.getBoolean(JsonKeys.SERVICE);
        if (isService && value != null && value instanceof Integer) {
            target.put(parameter.getString(JsonKeys.NAME), serviceRepository.findById(Long.valueOf((Integer) value)));
        } else {
            super.convertParameter(parameter, applySecurity, target);
        }
    }

}
