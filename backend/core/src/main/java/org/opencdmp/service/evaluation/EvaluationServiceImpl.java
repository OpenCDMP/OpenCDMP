package org.opencdmp.service.evaluation;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidatorFactory;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.EvaluationStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.UsageLimitTargetMetric;
import org.opencdmp.commons.types.evaluation.*;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.EvaluationEntity;
import org.opencdmp.data.TenantEntityManager;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.evaluatorbase.interfaces.SelectionConfiguration;
import org.opencdmp.evaluatorbase.interfaces.ValueRangeConfiguration;
import org.opencdmp.evaluatorbase.models.misc.RankConfig;
import org.opencdmp.evaluatorbase.models.misc.RankModel;
import org.opencdmp.model.builder.evaluation.EvaluationBuilder;
import org.opencdmp.model.deleter.EvaluationDeleter;
import org.opencdmp.model.evaluation.Evaluation;
import org.opencdmp.model.persist.evaluation.*;
import org.opencdmp.model.persist.EvaluationPersist;
import org.opencdmp.service.accounting.AccountingService;
import org.opencdmp.service.usagelimit.UsageLimitService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class EvaluationServiceImpl implements EvaluationService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EvaluationServiceImpl.class));
    private final TenantEntityManager entityManager;
    private final AuthorizationService authorizationService;
    private final DeleterFactory deleterFactory;
    private final BuilderFactory builderFactory;
    private final ConventionService conventionService;
    private final MessageSource messageSource;
    private final XmlHandlingService xmlHandlingService;
    private final ErrorThesaurusProperties errors;
    private final QueryFactory queryFactory;
    private final UsageLimitService usageLimitService;
    private final AccountingService accountingService;
    private final ValidatorFactory validatorFactory;

    public EvaluationServiceImpl(
            TenantEntityManager entityManager, AuthorizationService authorizationService, DeleterFactory deleterFactory, BuilderFactory builderFactory,
            ConventionService conventionService, MessageSource messageSource,
            XmlHandlingService xmlHandlingService, ErrorThesaurusProperties errors, QueryFactory queryFactory, UsageLimitService usageLimitService, AccountingService accountingService, ValidatorFactory validatorFactory) {
        this.entityManager = entityManager;
        this.authorizationService = authorizationService;
        this.deleterFactory = deleterFactory;
        this.builderFactory = builderFactory;
        this.conventionService = conventionService;
        this.messageSource = messageSource;
        this.xmlHandlingService = xmlHandlingService;
        this.errors = errors;
	    this.queryFactory = queryFactory;
        this.usageLimitService = usageLimitService;
        this.accountingService = accountingService;
        this.validatorFactory = validatorFactory;
    }


    public Evaluation persist(EvaluationPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException{
        logger.debug(new MapLogEntry("persisting data").And("model", model).And("fields", fields));

        this.authorizationService.authorizeForce(Permission.EditEvaluation, Permission.DeferredAffiliation);

        Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

        EvaluationEntity data;
        if (isUpdate) {
            data = this.entityManager.find(EvaluationEntity.class, model.getId());
            if (data == null)
                throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Evaluation.class.getSimpleName()}, LocaleContextHolder.getLocale()));
            if (!this.conventionService.hashValue(data.getUpdatedAt()).equals(model.getHash())) throw new MyValidationException(this.errors.getHashConflict().getCode(), this.errors.getHashConflict().getMessage());
        } else {
            if(model.getEntityType() == EntityType.Plan) this.usageLimitService.checkIncrease(UsageLimitTargetMetric.EVALUATION_PLAN_EXECUTION_COUNT);
            if(model.getEntityType() == EntityType.Description) this.usageLimitService.checkIncrease(UsageLimitTargetMetric.EVALUATION_DESCRIPTION_EXECUTION_COUNT);

            data = new EvaluationEntity();
            data.setId(UUID.randomUUID());
            data.setIsActive(IsActive.Active);
            data.setCreatedAt(Instant.now());
        }
        data.setEntityType(model.getEntityType());
        data.setEntityId(model.getEntityId());
        data.setEvaluatedAt(Instant.now());
        data.setData(this.xmlHandlingService.toXmlSafe(this.buildDataEntity(model.getData())));
        data.setStatus(model.getStatus());
        data.setUpdatedAt(Instant.now());
        data.setCreatedById(model.getCreatedById());

        if (isUpdate) this.entityManager.merge(data);
        else {
            this.entityManager.persist(data);
            if(model.getEntityType() == EntityType.Plan) this.accountingService.increase(UsageLimitTargetMetric.EVALUATION_PLAN_EXECUTION_COUNT.getValue());
            if(model.getEntityType() == EntityType.Description) this.accountingService.increase(UsageLimitTargetMetric.EVALUATION_DESCRIPTION_EXECUTION_COUNT.getValue());

        }

        this.entityManager.flush();

        return this.builderFactory.builder(EvaluationBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(BaseFieldSet.build(fields, Evaluation._id), data);
    }



    private @NotNull EvaluationDataEntity buildDataEntity(EvaluationDataPersist persist){
        EvaluationDataEntity data = new EvaluationDataEntity();
        if (persist == null) return data;

        data.setEvaluatorId(persist.getEvaluatorId());
        data.setRankConfig(this.buildRankConfigEntity(persist.getRankConfig()));
        data.setRankModel(this.buildRankModelEntity(persist.getRankModel()));


        return data;
    }

    private @NotNull RankModelEntity buildRankModelEntity(RankModelPersist persist){
        RankModelEntity data = new RankModelEntity();
        if (persist == null) return data;

        data.setMessages(persist.getMessages());
        data.setRank(persist.getRank());
        data.setDetails(persist.getDetails());

        return data;
    }

    private @NotNull RankConfigEntity buildRankConfigEntity(RankConfigPersist persist){
        RankConfigEntity data = new RankConfigEntity();
        if (persist == null) return data;

        data.setRankType(persist.getRankType());

        data.setValueRangeConfiguration(this.buildValueRangeConfigurationEntity(persist.getValueRangeConfiguration()));
        data.setSelectionConfiguration(this.buildSelectionConfigurationEntity(persist.getSelectionConfiguration()));


        return data;
    }

    private @NotNull ValueRangeConfigurationEntity buildValueRangeConfigurationEntity(ValueRangeConfigurationPersist persist){
        ValueRangeConfigurationEntity data = new ValueRangeConfigurationEntity();
        if (persist == null) return data;

        data.setNumberType(persist.getNumberType());
        data.setMax(persist.getMax());
        data.setMin(persist.getMin());
        data.setMinPassValue(persist.getMinPassValue());

        return data;
    }

    private @NotNull SelectionConfigurationEntity buildSelectionConfigurationEntity(SelectionConfigurationPersist persist){
        SelectionConfigurationEntity data = new SelectionConfigurationEntity();
        if (persist == null) return data;
        if (!this.conventionService.isListNullOrEmpty(persist.getValueSetList())){
            data.setValueSetList(new ArrayList<>());
            for (SelectionConfigurationPersist.ValueSetPersist fieldPersist: persist.getValueSetList()) {
                data.getValueSetList().add(this.buildValueSetEntity(fieldPersist));
            }
        }
        return data;
    }

    private @NotNull SelectionConfigurationEntity.ValueSetEntity buildValueSetEntity(SelectionConfigurationPersist.ValueSetPersist persist){
        SelectionConfigurationEntity.ValueSetEntity data = new SelectionConfigurationEntity.ValueSetEntity();
        if (persist == null) return data;

        data.setKey(persist.getKey());
        data.setSuccessStatus(persist.getSuccessStatus());

        return data;
    }


    public void persistInternal(RankModel rankModel, RankConfig rankConfig, UUID entityId, EntityType type, String evaluatorId, UUID userId) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {

        EvaluationPersist evaluationPersist = new EvaluationPersist();

        evaluationPersist.setStatus(EvaluationStatus.Completed);
        evaluationPersist.setEntityId(entityId);
        evaluationPersist.setCreatedById(userId);
        evaluationPersist.setEntityType(type);
        evaluationPersist.setData(this.buildDataPersist(rankConfig, rankModel, evaluatorId));

        this.validatorFactory.validator(EvaluationPersist.EvaluationPersistValidator.class).validateForce(evaluationPersist);
        this.persist(evaluationPersist, null);

    }

    private @NotNull EvaluationDataPersist buildDataPersist(RankConfig rankConfig, RankModel rankModel, String evaluatorId){
        EvaluationDataPersist evaluationDataPersist = new EvaluationDataPersist();


        evaluationDataPersist.setEvaluatorId(evaluatorId);
        evaluationDataPersist.setRankConfig(this.buildRankConfigPersist(rankConfig));
        evaluationDataPersist.setRankModel(this.buildRankModelPersist(rankModel));

        return evaluationDataPersist;

    }

    private @NotNull RankConfigPersist buildRankConfigPersist(RankConfig rankConfig){
        RankConfigPersist persist = new RankConfigPersist();
        if (rankConfig == null) return persist;

        persist.setRankType(rankConfig.getRankType());
        persist.setValueRangeConfiguration(this.buildValueRangeConfigurationPersist(rankConfig.getValueRangeConfiguration()));
        persist.setSelectionConfiguration(this.buildSelectionConfigurationPersist(rankConfig.getSelectionConfiguration()));

        return persist;
    }

    private @NotNull ValueRangeConfigurationPersist buildValueRangeConfigurationPersist(ValueRangeConfiguration valueRangeConfiguration){
        ValueRangeConfigurationPersist persist = new ValueRangeConfigurationPersist();
        if (valueRangeConfiguration == null) return persist;

        persist.setNumberType(valueRangeConfiguration.getNumberType());
        persist.setMax(valueRangeConfiguration.getMax());
        persist.setMin(valueRangeConfiguration.getMin());
        persist.setMinPassValue(valueRangeConfiguration.getMinPassValue());

        return persist;
    }

    private @NotNull SelectionConfigurationPersist buildSelectionConfigurationPersist(SelectionConfiguration selectionConfiguration){
        SelectionConfigurationPersist data = new SelectionConfigurationPersist();
        if (selectionConfiguration == null) return data;
        if (!this.conventionService.isListNullOrEmpty(selectionConfiguration.getValueSetList())){
            data.setValueSetList(new ArrayList<>());
            for (SelectionConfiguration.ValueSet field: selectionConfiguration.getValueSetList()) {
                data.getValueSetList().add(this.buildValueSetPersist(field));
            }
        }
        return data;
    }

    private @NotNull SelectionConfigurationPersist.ValueSetPersist buildValueSetPersist(SelectionConfiguration.ValueSet valueSet){
        SelectionConfigurationPersist.ValueSetPersist data = new SelectionConfigurationPersist.ValueSetPersist();

        data.setKey(valueSet.getKey());
        data.setSuccessStatus(valueSet.getSuccessStatus());

        return data;
    }

    private @NotNull RankModelPersist buildRankModelPersist(RankModel rankModel){
        RankModelPersist persist = new RankModelPersist();

        if(!this.conventionService.isNullOrEmpty(rankModel.getDetails()))
            persist.setDetails(rankModel.getDetails());

        persist.setRank(rankModel.getRank());
        persist.setMessages(rankModel.getMessages());
        persist.setDetails(rankModel.getDetails());

        return persist;
    }

    public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
        logger.debug("deleting : {}", id);

        this.authorizationService.authorizeForce(Permission.DeleteEvaluation, Permission.DeferredAffiliation);

        this.deleterFactory.deleter(EvaluationDeleter.class).deleteAndSaveByIds(List.of(id));
    }
}
