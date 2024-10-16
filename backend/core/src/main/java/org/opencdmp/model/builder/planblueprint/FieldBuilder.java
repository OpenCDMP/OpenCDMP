package org.opencdmp.model.builder.planblueprint;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planblueprint.FieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planblueprint.Field;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("planblueprintdefinitionfieldbuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class FieldBuilder<Model extends Field, Entity extends FieldEntity> extends BaseBuilder<Model, Entity> {

    protected EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public FieldBuilder(
            ConventionService conventionService,
            LoggerService logger) {
        super(conventionService, logger);
    }

    public FieldBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }
    
    protected abstract Model getInstance();

    protected abstract Model buildChild(FieldSet fields, Entity data, Model model);
    @Override
    public List<Model> build(FieldSet fields, List<Entity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<Model> models = new ArrayList<>();
        for (Entity d : data) {
            Model m = this.getInstance();
            if (fields.hasField(this.asIndexer(Model._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(Model._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Model._category))) m.setCategory(d.getCategory());
            if (fields.hasField(this.asIndexer(Model._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(Model._semantics))) m.setSemantics(d.getSemantics());
            if (fields.hasField(this.asIndexer(Model._placeholder))) m.setPlaceholder(d.getPlaceholder());
            if (fields.hasField(this.asIndexer(Model._ordinal))) m.setOrdinal(d.getOrdinal());
            if (fields.hasField(this.asIndexer(Model._required))) m.setRequired(d.isRequired());
            this.buildChild(fields, d, m);
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}

