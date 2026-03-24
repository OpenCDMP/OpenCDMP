package org.opencdmp.model.builder.commonmodels.dto;

import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.TagModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.Tag;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.TagCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TagCommonModelBuilder extends BaseCommonModelBuilder<Tag, TagModel> {

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
    protected List<CommonModelBuilderItemResponse<Tag, TagModel>> buildInternal(List<TagModel> data) throws MyApplicationException {
        this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<CommonModelBuilderItemResponse<Tag, TagModel>> models = new ArrayList<>();
        for (TagModel d : data) {
            Tag m = new Tag();
            m.setId(d.getId());
            m.setLabel(d.getLabel());
            models.add(new CommonModelBuilderItemResponse<>(m, d));
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
