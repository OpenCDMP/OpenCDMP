package org.opencdmp.model.builder.commonmodels.dto.description;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.ExternalIdentifierModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.description.ExternalIdentifier;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.ExternalIdentifierCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExternalIdentifierCommonModelBuilder extends BaseCommonModelBuilder<ExternalIdentifier, ExternalIdentifierModel> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public ExternalIdentifierCommonModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ExternalIdentifierCommonModelBuilder.class)));
    }

    public ExternalIdentifierCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }


    @Override
    protected List<CommonModelBuilderItemResponse<ExternalIdentifier, ExternalIdentifierModel>> buildInternal(List<ExternalIdentifierModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<ExternalIdentifier, ExternalIdentifierModel>> models = new ArrayList<>();
        for (ExternalIdentifierModel d : data) {
            ExternalIdentifier m = new ExternalIdentifier();
            m.setIdentifier(d.getIdentifier());
            m.setType(d.getType());
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
