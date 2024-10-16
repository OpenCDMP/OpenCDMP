package org.opencdmp.service.filetransformer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({FileTransformerProperties.class})
public class FileTransformerConfiguration {
}
