package com.arassec.igor.core.application.factory;

import com.arassec.igor.core.model.IgorService;
import com.arassec.igor.core.model.service.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory for Services.
 */
@Slf4j
@Component
public class ServiceFactory extends ModelFactory<Service> {

    public ServiceFactory() {
        super(Service.class, IgorService.class);
    }

}
