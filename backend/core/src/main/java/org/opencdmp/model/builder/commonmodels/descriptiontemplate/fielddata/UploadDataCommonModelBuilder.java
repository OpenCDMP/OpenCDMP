package org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.UploadDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.UploadDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<UploadDataModel, UploadDataEntity> {
    private final BuilderFactory builderFactory;
    @Autowired
    public UploadDataCommonModelBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UploadDataCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    protected UploadDataModel getInstance() {
        return new UploadDataModel();
    }

    @Override
    protected void buildChild(UploadDataEntity d, UploadDataModel m) {
        m.setTypes(this.builderFactory.builder(UploadOptionCommonModelBuilder.class).authorize(this.authorize).build(d.getTypes()));
    }

    @Component
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UploadOptionCommonModelBuilder extends BaseCommonModelBuilder<UploadDataModel.UploadOptionModel, UploadDataEntity.UploadDataOptionEntity> {
        private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
        @Autowired
        public UploadOptionCommonModelBuilder(
                ConventionService conventionService
        ) {
            super(conventionService, new LoggerService(LoggerFactory.getLogger(UploadOptionCommonModelBuilder.class)));
        }

        public UploadOptionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
            this.authorize = values;
            return this;
        }


        @Override
        protected List<CommonModelBuilderItemResponse<UploadDataModel.UploadOptionModel, UploadDataEntity.UploadDataOptionEntity>> buildInternal(List<UploadDataEntity.UploadDataOptionEntity> data) throws MyApplicationException {
            this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
            if (data == null || data.isEmpty()) return new ArrayList<>();

            List<CommonModelBuilderItemResponse<UploadDataModel.UploadOptionModel, UploadDataEntity.UploadDataOptionEntity>> models = new ArrayList<>();
            for (UploadDataEntity.UploadDataOptionEntity d : data) {
                UploadDataModel.UploadOptionModel m = new UploadDataModel.UploadOptionModel();
                m.setLabel(d.getLabel());
                m.setValue(d.getValue());
                models.add(new CommonModelBuilderItemResponse<>(m, d));
            }

            this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

            return models;
        }
    }

}
