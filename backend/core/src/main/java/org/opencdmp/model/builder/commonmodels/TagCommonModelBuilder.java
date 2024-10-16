package org.opencdmp.model.builder.commonmodels;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.TagModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.TagEntity;
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
public class TagCommonModelBuilder extends BaseCommonModelBuilder<TagModel, TagEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public TagCommonModelBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(TagCommonModelBuilder.class)));
    }

    public TagCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    protected List<CommonModelBuilderItemResponse<TagModel, TagEntity>> buildInternal(List<TagEntity> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<TagModel, TagEntity>> models = new ArrayList<>();
        for (TagEntity d : data) {
            TagModel m = new TagModel();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
