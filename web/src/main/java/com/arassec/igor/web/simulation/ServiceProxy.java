package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.service.Service;
import org.mockito.Mockito;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Proxy for {@link Service}s. This proxy either calls the original method, if it is safe to do so during a simulated job run, or
 * a mock method, to avoid problems for regular job runs.
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * The service to proxy.
     */
    private Service service;

    /**
     * A mock of the proxied service. This is used, when the proxied method is missing the {@link IgorSimulationSafe} annotation.
     */
    private Service serviceMock;

    /**
     * Creates a new proxy instance.
     *
     * @param service The proxied {@link Service}.
     */
    public ServiceProxy(Service service) {
        this.service = service;
        this.serviceMock = Mockito.mock(service.getClass());
    }

    /**
     * Calls the proxied service's method, if the annotation {@link IgorSimulationSafe} is present. Otherwise, a mock is invoked.
     */
    @Override
    public Object invoke(Object o, Method method, Object[] arguments) throws Throwable {
        if (AnnotationUtils.findAnnotation(method, IgorSimulationSafe.class) != null) {
            return method.invoke(service, arguments);
        }
        return method.invoke(serviceMock, arguments);
    }

}
