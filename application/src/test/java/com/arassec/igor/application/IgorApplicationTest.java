package com.arassec.igor.application;

import com.arassec.igor.core.application.IgorComponentRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests the {@link IgorApplication}.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:file:./target/dbdata/igor;AUTO_SERVER=true"})
class IgorApplicationTest {

    /**
     * The spring application context.
     */
    @Autowired
    private ApplicationContext context;

    /**
     * Simply tests Spring context creation.
     */
    @Test
    void testContextCreation() {
        assertNotNull(context.getBean(IgorComponentRegistry.class));
    }

}
