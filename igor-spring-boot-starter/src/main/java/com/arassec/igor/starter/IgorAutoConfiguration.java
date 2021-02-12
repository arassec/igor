package com.arassec.igor.starter;

import com.arassec.igor.application.ApplicationConfiguration;
import com.arassec.igor.core.CoreConfiguration;
import com.arassec.igor.module.file.IgorPluginFileConfiguration;
import com.arassec.igor.module.message.IgorPluginMessageConfiguration;
import com.arassec.igor.persistence.PersistenceConfiguration;
import com.arassec.igor.plugin.core.IgorPluginCoreConfiguration;
import com.arassec.igor.simulation.SimulationConfiguration;
import com.arassec.igor.web.WebConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Auto-Configuration for igor's Spring-Boot starter.
 */
@Configuration
@Import({CoreConfiguration.class, ApplicationConfiguration.class, PersistenceConfiguration.class, WebConfiguration.class,
    SimulationConfiguration.class,
        IgorPluginCoreConfiguration.class, IgorPluginMessageConfiguration.class, IgorPluginFileConfiguration.class})
@PropertySource("classpath:igor.properties")
public class IgorAutoConfiguration {
}
