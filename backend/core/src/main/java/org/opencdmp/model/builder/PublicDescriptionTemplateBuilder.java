package org.opencdmp.model.builder;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.DescriptionTemplateEntity;
import org.opencdmp.model.PublicDescriptionTemplate;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicDescriptionTemplateBuilder extends BaseBuilder<PublicDescriptionTemplate, DescriptionTemplateEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public PublicDescriptionTemplateBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicDescriptionTemplateBuilder.class)));
    }

    public PublicDescriptionTemplateBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicDescriptionTemplate> build(FieldSet fields, List<DescriptionTemplateEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();
        List<PublicDescriptionTemplate> models = new ArrayList<>();
        for (DescriptionTemplateEntity d : data) {
            PublicDescriptionTemplate m = new PublicDescriptionTemplate();
            if (fields.hasField(this.asIndexer(PublicDescriptionTemplate._id)))  m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicDescriptionTemplate._groupId)))  m.setGroupId(d.getGroupId());
            if (fields.hasField(this.asIndexer(PublicDescriptionTemplate._label)))  m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(PublicDescriptionTemplate._description)))  m.setDescription(d.getDescription());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
