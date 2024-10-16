package org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.SelectDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SelectDataEntity;
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
public class SelectDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<SelectDataModel, SelectDataEntity> {
    private final BuilderFactory builderFactory;
    @Autowired
    public SelectDataCommonModelBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(SelectDataCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    protected SelectDataModel getInstance() {
        return new SelectDataModel();
    }

    @Override
    protected void buildChild(SelectDataEntity d, SelectDataModel m) {
        m.setMultipleSelect(d.getMultipleSelect());
        m.setOptions(this.builderFactory.builder(SelectOptionCommonModelBuilder.class).authorize(this.authorize).build(d.getOptions()));
    }

    @Component
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class SelectOptionCommonModelBuilder extends BaseCommonModelBuilder<SelectDataModel.OptionModel, SelectDataEntity.OptionEntity> {
        private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
        @Autowired
        public SelectOptionCommonModelBuilder(
                ConventionService conventionService
        ) {
            super(conventionService, new LoggerService(LoggerFactory.getLogger(SelectOptionCommonModelBuilder.class)));
        }

        public SelectOptionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
            this.authorize = values;
            return this;
        }


        @Override
        protected List<CommonModelBuilderItemResponse<SelectDataModel.OptionModel, SelectDataEntity.OptionEntity>> buildInternal(List<SelectDataEntity.OptionEntity> data) throws MyApplicationException {
            this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
            if (data == null || data.isEmpty()) return new ArrayList<>();

            List<CommonModelBuilderItemResponse<SelectDataModel.OptionModel, SelectDataEntity.OptionEntity>> models = new ArrayList<>();
            for (SelectDataEntity.OptionEntity d : data) {
                SelectDataModel.OptionModel m = new SelectDataModel.OptionModel();
                m.setLabel(d.getLabel());
                m.setValue(d.getValue());
                models.add(new CommonModelBuilderItemResponse<>(m, d));
            }

            this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

            return models;
        }
    }

}
