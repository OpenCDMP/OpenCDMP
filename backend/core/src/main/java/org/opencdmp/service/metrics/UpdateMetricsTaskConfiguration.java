package org.opencdmp.service.metrics;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(UpdateMetricsTaskProperties.class)
public class UpdateMetricsTaskConfiguration {
}
