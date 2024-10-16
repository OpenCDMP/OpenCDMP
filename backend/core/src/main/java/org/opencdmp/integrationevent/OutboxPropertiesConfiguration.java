package org.opencdmp.integrationevent;

import org.opencdmp.integrationevent.outbox.OutboxProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxPropertiesConfiguration {

}
