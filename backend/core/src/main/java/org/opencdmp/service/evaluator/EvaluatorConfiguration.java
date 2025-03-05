package org.opencdmp.service.evaluator;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({EvaluatorProperties.class})
public class EvaluatorConfiguration {
}
