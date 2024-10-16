package org.opencdmp.model.builder.planblueprint;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planblueprint.DescriptionTemplateEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planblueprint.DescriptionTemplate;
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

@Component("planblueprintdefinitiondescriptiontemplatebuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DescriptionTemplateBuilder extends BaseBuilder<DescriptionTemplate, DescriptionTemplateEntity> {

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public DescriptionTemplateBuilder(
            ConventionService conventionService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(DescriptionTemplateBuilder.class)));
    }

    public DescriptionTemplateBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<DescriptionTemplate> build(FieldSet fields, List<DescriptionTemplateEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<DescriptionTemplate> models = new ArrayList<>();
        for (DescriptionTemplateEntity d : data) {
            DescriptionTemplate m = new DescriptionTemplate();
            if (fields.hasField(this.asIndexer(DescriptionTemplate._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._descriptionTemplateGroupId))) m.setDescriptionTemplateGroupId(d.getDescriptionTemplateGroupId());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._maxMultiplicity))) m.setMaxMultiplicity(d.getMaxMultiplicity());
            if (fields.hasField(this.asIndexer(DescriptionTemplate._minMultiplicity))) m.setMinMultiplicity(d.getMinMultiplicity());
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
