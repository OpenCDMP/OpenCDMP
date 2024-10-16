package org.opencdmp.service.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageFileProperties.class)
public class StorageFileConfiguration {

}
