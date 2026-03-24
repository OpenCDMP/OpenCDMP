package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.SelectDataModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.descriptiontemplate.fielddata.SelectData;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.SelectDataCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SelectDataCommonModelBuilder extends BaseFieldDataCommonModelBuilder<SelectData, SelectDataModel> {
    private final BuilderFactory builderFactory;
    @Autowired
    public SelectDataCommonModelBuilder(
		    ConventionService conventionService, BuilderFactory builderFactory
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(SelectDataCommonModelBuilder.class)));
	    this.builderFactory = builderFactory;
    }

    protected SelectData getInstance() {
        return new SelectData();
    }

    @Override
    protected void buildChild(SelectDataModel d, SelectData m) {
        m.setMultipleSelect(d.getMultipleSelect());
        m.setOptions(this.builderFactory.builder(SelectOptionCommonModelBuilder.class).authorize(this.authorize).build(d.getOptions()));
    }

    @Component("dto.descriptiontemplate.SelectOptionCommonModelBuilder")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class SelectOptionCommonModelBuilder extends BaseCommonModelBuilder<SelectData.Option, SelectDataModel.OptionModel> {
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
        protected List<CommonModelBuilderItemResponse<SelectData.Option, SelectDataModel.OptionModel>> buildInternal(List<SelectDataModel.OptionModel> data) throws MyApplicationException {
            this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
            if (data == null || data.isEmpty()) return new ArrayList<>();

            List<CommonModelBuilderItemResponse<SelectData.Option, SelectDataModel.OptionModel>> models = new ArrayList<>();
            for (SelectDataModel.OptionModel d : data) {
                SelectData.Option m = new SelectData.Option();
                m.setLabel(d.getLabel());
                m.setValue(d.getValue());
                models.add(new CommonModelBuilderItemResponse<>(m, d));
            }

            this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

            return models;
        }
    }

}
