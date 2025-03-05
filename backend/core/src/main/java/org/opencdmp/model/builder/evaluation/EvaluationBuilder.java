package org.opencdmp.model.builder.evaluation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.evaluation.EvaluationDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.EvaluationEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.evaluation.Evaluation;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EvaluationBuilder extends BaseBuilder<Evaluation, EvaluationEntity> {

    private final BuilderFactory builderFactory;
    private final XmlHandlingService xmlHandlingService;
    private final TenantScope tenantScope;
    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


    @Autowired
    public EvaluationBuilder(
            ConventionService conventionService,
            BuilderFactory builderFactory, XmlHandlingService xmlHandlingService, TenantScope tenantScope) {
        super(conventionService,  new LoggerService(LoggerFactory.getLogger(EvaluationBuilder.class)));
        this.xmlHandlingService = xmlHandlingService;
        this.tenantScope = tenantScope;
        this.builderFactory = builderFactory;


    }

    public EvaluationBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<Evaluation> build(FieldSet fields, List<EvaluationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet dataFields = fields.extractPrefixed(this.asPrefix(Evaluation._data));

        List<Evaluation> models = new ArrayList<>();
        for (EvaluationEntity d : data) {
            Evaluation m = new Evaluation();
            if (fields.hasField(this.asIndexer(Evaluation._id))) m.setId(d.getId());
            if (fields.hasField(this.asIndexer(Evaluation._entityType))) m.setEntityType(d.getEntityType());
            if (fields.hasField(this.asIndexer(Evaluation._entityId))) m.setEntityId(d.getEntityId());
            if (fields.hasField(this.asIndexer(Evaluation._evaluatedAt))) m.setEvaluatedAt(d.getEvaluatedAt());
            if (fields.hasField(this.asIndexer(Evaluation._status))) m.setStatus(d.getStatus());
            if (fields.hasField(this.asIndexer(Evaluation._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(Evaluation._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(Evaluation._isActive))) m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(Evaluation._createdById))) m.setCreatedById(d.getCreatedById());
            if (fields.hasField(this.asIndexer(Evaluation._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(Evaluation._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));

            if (!dataFields.isEmpty() && d.getData() != null){
                EvaluationDataEntity evaluationData = this.xmlHandlingService.fromXmlSafe(EvaluationDataEntity.class, d.getData());
                m.setData(this.builderFactory.builder(EvaluationDataBuilder.class).authorize(this.authorize).build(dataFields, evaluationData));
            }

            models.add(m);
        }

        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
        return models;
        }
}
