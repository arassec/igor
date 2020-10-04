package com.arassec.igor.web.simulation;

import com.arassec.igor.core.model.annotation.IgorSimulationSafe;
import com.arassec.igor.core.model.connector.Connector;
import org.mockito.Mockito;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Proxy for {@link Connector}s. This proxy either calls the original method, if it is safe to do so during a simulated job run,
 * or a mock method, to avoid problems for regular job runs.
 */
public class ConnectorProxy implements InvocationHandler {

    /**
     * The connector to proxy.
     */
    private Connector connector;

    /**
     * A mock of the proxied connector. This is used, when the proxied method is missing the {@link IgorSimulationSafe}
     * annotation.
     */
    private Connector connectorMock;

    /**
     * Creates a new proxy instance.
     *
     * @param connector The proxied {@link Connector}.
     */
    public ConnectorProxy(Connector connector) {
        this.connector = connector;
        this.connectorMock = Mockito.mock(connector.getClass());
    }

    /**
     * Calls the proxied connector's method, if the annotation {@link IgorSimulationSafe} is present. Otherwise, a mock is
     * invoked.
     */
    @Override
    public Object invoke(Object o, Method method, Object[] arguments) throws Throwable {
        if (AnnotationUtils.findAnnotation(method, IgorSimulationSafe.class) != null) {
            return method.invoke(connector, arguments);
        }
        if (method.getName().equals("initialize") || method.getName().equals("shutdown")) {
            return method.invoke(connector, arguments);
        }
        return method.invoke(connectorMock, arguments);
    }

}
