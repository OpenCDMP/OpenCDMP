package org.opencdmp.service.filetransformer;

import org.opencdmp.commons.types.filetransformer.FileTransformerSourceEntity;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "file-transformer")
public class FileTransformerProperties {

    private List<FileTransformerSourceEntity> sources;

    public List<FileTransformerSourceEntity> getSources() {
        return sources;
    }

    public void setSources(List<FileTransformerSourceEntity> sources) {
        this.sources = sources;
    }
}
