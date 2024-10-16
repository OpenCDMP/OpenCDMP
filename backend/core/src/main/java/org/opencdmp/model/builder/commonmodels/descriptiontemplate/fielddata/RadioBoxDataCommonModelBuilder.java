package org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.RadioBoxDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.RadioBoxDataEntity;
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
public class RadioBoxDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<RadioBoxDataModel, RadioBoxDataEntity> {
    private final BuilderFactory builderFactory;
    @Autowired
    public RadioBoxDataCommonModelBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(RadioBoxDataCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    protected RadioBoxDataModel getInstance() {
        return new RadioBoxDataModel();
    }

    @Override
    protected void buildChild(RadioBoxDataEntity d, RadioBoxDataModel m) {
        m.setOptions(this.builderFactory.builder(RadioBoxOptionCommonModelBuilder.class).authorize(this.authorize).build(d.getOptions()));
    }

    @Component
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class RadioBoxOptionCommonModelBuilder extends BaseCommonModelBuilder<RadioBoxDataModel.RadioBoxOptionModel, RadioBoxDataEntity.RadioBoxDataOptionEntity> {
        private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
        @Autowired
        public RadioBoxOptionCommonModelBuilder(
                ConventionService conventionService
        ) {
            super(conventionService, new LoggerService(LoggerFactory.getLogger(RadioBoxOptionCommonModelBuilder.class)));
        }

        public RadioBoxOptionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
            this.authorize = values;
            return this;
        }


        @Override
        protected List<CommonModelBuilderItemResponse<RadioBoxDataModel.RadioBoxOptionModel, RadioBoxDataEntity.RadioBoxDataOptionEntity>> buildInternal(List<RadioBoxDataEntity.RadioBoxDataOptionEntity> data) throws MyApplicationException {
            this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
            if (data == null || data.isEmpty()) return new ArrayList<>();

            List<CommonModelBuilderItemResponse<RadioBoxDataModel.RadioBoxOptionModel, RadioBoxDataEntity.RadioBoxDataOptionEntity>> models = new ArrayList<>();
            for (RadioBoxDataEntity.RadioBoxDataOptionEntity d : data) {
                RadioBoxDataModel.RadioBoxOptionModel m = new RadioBoxDataModel.RadioBoxOptionModel();
                m.setLabel(d.getLabel());
                m.setValue(d.getValue());
                models.add(new CommonModelBuilderItemResponse<>(m, d));
            }

            this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

            return models;
        }
    }

}
