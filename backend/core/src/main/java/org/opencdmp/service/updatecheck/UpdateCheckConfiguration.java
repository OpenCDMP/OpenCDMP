package org.opencdmp.service.updatecheck;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(UpdateCheckProperties.class)
public class UpdateCheckConfiguration {
}
