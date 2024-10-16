package org.opencdmp.model.builder.commonmodels.description;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.description.VisibilityStateModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.service.visibility.FieldKey;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("commonmodels.VisibilityStateModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VisibilityStateModelBuilder extends BaseCommonModelBuilder<VisibilityStateModel, Map.Entry<FieldKey, Boolean>> {
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public VisibilityStateModelBuilder(
		    ConventionService conventionService
    ) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(VisibilityStateModelBuilder.class)));
    }

    public VisibilityStateModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<VisibilityStateModel, Map.Entry<FieldKey, Boolean>>> buildInternal(List<Map.Entry<FieldKey, Boolean>> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<VisibilityStateModel, Map.Entry<FieldKey, Boolean>>> models = new ArrayList<>();
        for (Map.Entry<FieldKey, Boolean> d : data) {
            VisibilityStateModel m = new VisibilityStateModel();
            if (d.getKey() != null) {
                m.setFieldId(d.getKey().getFieldId());
                m.setOrdinal(d.getKey().getOrdinal());
            }
            m.setVisible(d.getValue());

            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

        return models;
    }
}
