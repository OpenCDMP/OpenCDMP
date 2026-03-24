package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.DefaultValueModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.descriptiontemplate.DefaultValue;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.DefaultValueCommonModelBuilder")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefaultValueCommonModelBuilder extends BaseCommonModelBuilder<DefaultValue, DefaultValueModel> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public DefaultValueCommonModelBuilder(
		    ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DefaultValueCommonModelBuilder.class)));
    }

    public DefaultValueCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }
    @Override
    protected List<CommonModelBuilderItemResponse<DefaultValue, DefaultValueModel>> buildInternal(List<DefaultValueModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();


        List<CommonModelBuilderItemResponse<DefaultValue, DefaultValueModel>> models = new ArrayList<>();
        for (DefaultValueModel d : data) {
            DefaultValue m = new DefaultValue();
            m.setDateValue(d.getDateValue());
            m.setBooleanValue(d.getBooleanValue());
            m.setTextValue(d.getTextValue());

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }

}
