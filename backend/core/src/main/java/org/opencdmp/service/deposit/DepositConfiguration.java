package org.opencdmp.service.deposit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({DepositProperties.class})
public class DepositConfiguration {
}
