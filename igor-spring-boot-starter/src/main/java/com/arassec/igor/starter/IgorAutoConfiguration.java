package com.arassec.igor.starter;

import com.arassec.igor.application.ApplicationConfiguration;
import com.arassec.igor.persistence.PersistenceConfiguration;
import com.arassec.igor.plugin.core.IgorCorePluginConfiguration;
import com.arassec.igor.plugin.data.IgorDataPluginConfiguration;
import com.arassec.igor.plugin.file.IgorFilePluginConfiguration;
import com.arassec.igor.plugin.message.IgorMessagePluginConfiguration;
import com.arassec.igor.simulation.SimulationConfiguration;
import com.arassec.igor.web.WebConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Auto-Configuration for igor's Spring-Boot starter.
 */
@Configuration
@ComponentScan
@Import({ApplicationConfiguration.class, PersistenceConfiguration.class, WebConfiguration.class, SimulationConfiguration.class,
    IgorCorePluginConfiguration.class, IgorMessagePluginConfiguration.class, IgorFilePluginConfiguration.class, IgorDataPluginConfiguration.class})
@PropertySource("classpath:/igor.properties")
public class IgorAutoConfiguration {
}
