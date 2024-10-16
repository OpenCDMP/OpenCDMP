package org.opencdmp.model.builder.referencetype;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.referencetype.ReferenceTypeFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.referencetype.ReferenceTypeField;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReferenceTypeFieldBuilder extends BaseBuilder<ReferenceTypeField, ReferenceTypeFieldEntity> {

    private final BuilderFactory builderFactory;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    @Autowired
    public ReferenceTypeFieldBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ReferenceTypeFieldBuilder.class)));
        this.builderFactory = builderFactory;
    }

    public ReferenceTypeFieldBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<ReferenceTypeField> build(FieldSet fields, List<ReferenceTypeFieldEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        List<ReferenceTypeField> models = new ArrayList<>();
        for (ReferenceTypeFieldEntity d : data) {
            ReferenceTypeField m = new ReferenceTypeField();
            if (fields.hasField(this.asIndexer(ReferenceTypeField._code))) m.setCode(d.getCode());
            if (fields.hasField(this.asIndexer(ReferenceTypeField._label))) m.setLabel(d.getLabel());
            if (fields.hasField(this.asIndexer(ReferenceTypeField._description))) m.setDescription(d.getDescription());
            if (fields.hasField(this.asIndexer(ReferenceTypeField._dataType))) m.setDataType(d.getDataType());

            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
