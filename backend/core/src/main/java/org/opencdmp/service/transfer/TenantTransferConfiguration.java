package org.opencdmp.service.transfer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TenantTransferProperties.class)
public class TenantTransferConfiguration {
}
