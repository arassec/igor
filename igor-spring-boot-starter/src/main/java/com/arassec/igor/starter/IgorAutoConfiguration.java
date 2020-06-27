package com.arassec.igor.starter;

import com.arassec.igor.core.CoreConfiguration;
import com.arassec.igor.module.file.ModuleFileConfiguration;
import com.arassec.igor.module.message.ModuleMessageConfiguration;
import com.arassec.igor.module.misc.ModuleMiscConfiguration;
import com.arassec.igor.persistence.PersistenceConfiguration;
import com.arassec.igor.web.WebConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Auto-Configuration for igor's Spring-Boot starter.
 */
@Configuration
@Import({CoreConfiguration.class, PersistenceConfiguration.class, WebConfiguration.class, ModuleMiscConfiguration.class,
        ModuleMessageConfiguration.class, ModuleFileConfiguration.class})
@PropertySource("classpath:igor.properties")
public class IgorAutoConfiguration {
}
