package org.opencdmp.model.builder;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanStatusEntity;
import org.opencdmp.model.PublicPlanStatus;
import org.opencdmp.model.builder.planstatus.PlanStatusDefinitionBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PublicPlanStatusBuilder extends BaseBuilder<PublicPlanStatus, PlanStatusEntity> {

    private final XmlHandlingService xmlHandlingService;
    private final BuilderFactory builderFactory;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public PublicPlanStatusBuilder(
            ConventionService conventionService, XmlHandlingService xmlHandlingService, BuilderFactory builderFactory) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PublicPlanStatusBuilder.class)));
        this.xmlHandlingService = xmlHandlingService;
        this.builderFactory = builderFactory;
    }

    public PublicPlanStatusBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PublicPlanStatus> build(FieldSet fields, List<PlanStatusEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet definitionFields = fields.extractPrefixed(this.asPrefix(PublicPlanStatus._definition));
        List<PublicPlanStatus> models = new ArrayList<>();

        for (PlanStatusEntity d : data) {
            PublicPlanStatus m = new PublicPlanStatus();
            if (fields.hasField(this.asIndexer(PublicPlanStatus._id)))  m.setId(d.getId());
            if (fields.hasField(this.asIndexer(PublicPlanStatus._name)))  m.setName(d.getName());
            if (fields.hasField(this.asIndexer(PublicPlanStatus._internalStatus)))  m.setInternalStatus(d.getInternalStatus());
            if (!definitionFields.isEmpty() && d.getDefinition() != null) {
                PlanStatusDefinitionEntity definition = this.xmlHandlingService.fromXmlSafe(PlanStatusDefinitionEntity.class, d.getDefinition());
                m.setDefinition(this.builderFactory.builder(PublicPlanStatusDefinitionBuilder.class).authorize(this.authorize).build(definitionFields, definition));
            }
            models.add(m);
        }
        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
    }
}
