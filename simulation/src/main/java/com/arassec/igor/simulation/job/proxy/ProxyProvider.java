package com.arassec.igor.simulation.job.proxy;

import com.arassec.igor.core.model.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.connector.Connector;
import com.arassec.igor.core.model.job.Job;
import com.arassec.igor.core.model.trigger.EventTrigger;
import com.arassec.igor.core.util.IgorException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.stream.Collectors;

/**
 * A utility class for proxy handling.
 */
@Component
public class ProxyProvider implements ApplicationContextAware {

    /**
     * The Spring application context.
     */
    private ApplicationContext applicationContext;

    /**
     * Sets the application context. The context is required to inject Spring-Beans into proxied connectors.
     *
     * @param applicationContext The Spring application context.
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Applies proxy instances around {@link IgorComponent}s of the job.
     *
     * @param job The job to apply proxies to.
     */
    public void applyProxies(Job job) {
        if (job.getTrigger() instanceof EventTrigger) {
            job.setTrigger(new EventTriggerProxy((EventTrigger) proxyConnectors(job.getTrigger()), job.getSimulationLimit()));
        } else {
            job.setTrigger(new TriggerProxy(proxyConnectors(job.getTrigger()), job.getSimulationLimit()));
        }

        job.setActions(job.getActions()
                .stream()
                .map(action -> new ActionProxy(proxyConnectors(action), job.getSimulationLimit()))
                .collect(Collectors.toList()));
    }

    /**
     * Wraps a {@link ConnectorProxy} around every {@link Connector} of the supplied {@link IgorComponent}.
     *
     * @param igorComponent The component to apply connector proxies to.
     * @param <T>           The component's type.
     *
     * @return The component with applied connector proxies.
     */
    private <T extends IgorComponent> T proxyConnectors(T igorComponent) {
        ReflectionUtils.doWithFields(igorComponent.getClass(), field -> {
            if (field.isAnnotationPresent(IgorParam.class) && Connector.class.isAssignableFrom(field.getType())) {
                ReflectionUtils.makeAccessible(field);
                Connector connector = (Connector) ReflectionUtils.getField(field, igorComponent);
                Connector connectorProxy = createConnectorProxy(connector);
                ReflectionUtils.setField(field, igorComponent, connectorProxy);
            }
        });
        return igorComponent;
    }

    /**
     * Wraps a {@link ConnectorProxy} around the supplied {@link Connector} and returns it.
     *
     * @param connector The Connector to wrap the proxy around.
     *
     * @return The newly created Proxy wrapping the original connector.
     */
    private Connector createConnectorProxy(Connector connector) {
        if (connector == null) {
            throw new IllegalArgumentException("No connector supplied for proxy creation!");
        }
        try {
            Constructor<?>[] declaredConstructors = connector.getClass().getDeclaredConstructors();
            Constructor<?> constructor = declaredConstructors[0];

            // Get constructor parameters from the spring context if possible:
            Parameter[] parameters = constructor.getParameters();

            Class<?>[] parameterTypes = new Class<?>[parameters.length];
            Object[] parameterValues = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                parameterTypes[i] = parameters[i].getType();
                parameterValues[i] = getSpringBean(parameters[i].getType());
            }

            return new ByteBuddy()
                    .subclass(connector.getClass())
                    .method(ElementMatchers.any())
                    .intercept(InvocationHandlerAdapter.of(new ConnectorProxy(connector)))
                    .make()
                    .load(connector.getClass().getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor(parameterTypes)
                    .newInstance(parameterValues);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IgorException("Could not create connector proxy!", e);
        }
    }

    /**
     * Retrieves a spring bean from the context.
     *
     * @param clazz The class to get the bean of.
     *
     * @return The bean from the spring application context.
     */
    private Object getSpringBean(Class<?> clazz) {
        // The application context itself is used as bean of type ApplicationEventPublisher by Spring...
        if (ApplicationEventPublisher.class.equals(clazz)) {
            return applicationContext;
        }
        return applicationContext.getBean(clazz);
    }

}
