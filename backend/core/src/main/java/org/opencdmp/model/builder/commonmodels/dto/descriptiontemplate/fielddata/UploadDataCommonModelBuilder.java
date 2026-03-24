package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.UploadDataModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.descriptiontemplate.fielddata.UploadData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.UploadDataCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<UploadData, UploadDataModel> {
    private final BuilderFactory builderFactory;
    @Autowired
    public UploadDataCommonModelBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(UploadDataCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    protected UploadData getInstance() {
        return new UploadData();
    }

    @Override
    protected void buildChild(UploadDataModel d, UploadData m) {
        m.setTypes(this.builderFactory.builder(UploadOptionCommonModelBuilder.class).authorize(this.authorize).build(d.getTypes()));
    }

    @Component("dto.descriptiontemplate.UploadOptionCommonModelBuilder")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class UploadOptionCommonModelBuilder extends BaseCommonModelBuilder<UploadData.UploadOption, UploadDataModel.UploadOptionModel> {
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
        protected List<CommonModelBuilderItemResponse<UploadData.UploadOption, UploadDataModel.UploadOptionModel>> buildInternal(List<UploadDataModel.UploadOptionModel> data) throws MyApplicationException {
            this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
            if (data == null || data.isEmpty()) return new ArrayList<>();

            List<CommonModelBuilderItemResponse<UploadData.UploadOption, UploadDataModel.UploadOptionModel>> models = new ArrayList<>();
            for (UploadDataModel.UploadOptionModel d : data) {
                UploadData.UploadOption m = new UploadData.UploadOption();
                m.setLabel(d.getLabel());
                m.setValue(d.getValue());
                models.add(new CommonModelBuilderItemResponse<>(m, d));
            }

            this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

            return models;
        }
    }

}
