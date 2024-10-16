package org.opencdmp.model.builder.tenantconfiguration;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.tenantconfiguration.FileTransformerTenantConfigurationEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.filetransformer.FileTransformerSourceBuilder;
import org.opencdmp.model.tenantconfiguration.FileTransformerTenantConfiguration;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FileTransformerTenantConfigurationBuilder extends BaseBuilder<FileTransformerTenantConfiguration, FileTransformerTenantConfigurationEntity> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    private final BuilderFactory builderFactory;

    @Autowired
    public FileTransformerTenantConfigurationBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FileTransformerTenantConfigurationBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    public FileTransformerTenantConfigurationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<FileTransformerTenantConfiguration> build(FieldSet fields, List<FileTransformerTenantConfigurationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet sourcesFields = fields.extractPrefixed(this.asPrefix(FileTransformerTenantConfiguration._sources));
        
        List<FileTransformerTenantConfiguration> models = new ArrayList<>();
        for (FileTransformerTenantConfigurationEntity d : data) {
            FileTransformerTenantConfiguration m = new FileTransformerTenantConfiguration();
            if (!sourcesFields.isEmpty() && d.getSources() != null){
                if (fields.hasField(this.asIndexer(FileTransformerTenantConfiguration._disableSystemSources))) m.setDisableSystemSources(d.getDisableSystemSources());
                m.setSources(this.builderFactory.builder(FileTransformerSourceBuilder.class).encrypted(true).authorize(this.authorize).build(sourcesFields, d.getSources()));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
