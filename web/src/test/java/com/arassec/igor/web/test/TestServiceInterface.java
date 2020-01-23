package com.arassec.igor.web.test;

import com.arassec.igor.core.model.service.Service;

/**
 * A service interface for testing. The {@link TestService} cannot use {@link com.arassec.igor.core.model.service.Service}
 * directly, since that is filtered during category extraction in the
 * {@link com.arassec.igor.core.application.IgorComponentRegistry}.
 */
public interface TestServiceInterface extends Service {
}
