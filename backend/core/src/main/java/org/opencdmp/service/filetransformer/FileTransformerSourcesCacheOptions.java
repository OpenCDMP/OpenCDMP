package org.opencdmp.service.filetransformer;

import gr.cite.tools.cache.CacheOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache.file-transformer-sources-by-tenant")
public class FileTransformerSourcesCacheOptions extends CacheOptions {
}
