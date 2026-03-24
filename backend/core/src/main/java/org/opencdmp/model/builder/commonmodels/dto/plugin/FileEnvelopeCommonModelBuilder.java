package org.opencdmp.model.builder.commonmodels.dto.plugin;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.plugin.PluginUserFieldModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.StorageFile;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.pluginconfiguration.PluginConfigurationUserField;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.FileEnvelopeCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FileEnvelopeCommonModelBuilder extends BaseCommonModelBuilder<StorageFile, FileEnvelopeModel> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public FileEnvelopeCommonModelBuilder(
            ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(FileEnvelopeCommonModelBuilder.class)));
    }

    public FileEnvelopeCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<StorageFile, FileEnvelopeModel>> buildInternal(List<FileEnvelopeModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<StorageFile, FileEnvelopeModel>> models = new ArrayList<>();
        for (FileEnvelopeModel d : data) {
            StorageFile m = new StorageFile();
            m.setFullName(d.getFilename());
            m.setFileRef(d.getFileRef());
            m.setMimeType(d.getMimeType());

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
