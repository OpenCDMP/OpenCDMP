package org.opencdmp.model.builder.planupdaterequest;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.PlanUpdateRequestEntity;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.UserBuilder;
import org.opencdmp.model.builder.plan.PlanBuilder;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planupdaterequest.PlanUpdateRequest;
import org.opencdmp.model.user.User;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.UserQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlanUpdateRequestBuilder extends BaseBuilder<PlanUpdateRequest, PlanUpdateRequestEntity> {

    private final TenantScopeFactory tenantScopeFactory;

    private final BuilderFactory builderFactory;

    private final QueryFactory queryFactory;

    private final JsonHandlingService jsonHandlingService;

    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

    public PlanUpdateRequestBuilder(ConventionService conventionService, TenantScopeFactory tenantScopeFactory, BuilderFactory builderFactory, QueryFactory queryFactory, JsonHandlingService jsonHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(PlanUpdateRequestBuilder.class)));
        this.tenantScopeFactory = tenantScopeFactory;
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
        this.jsonHandlingService = jsonHandlingService;
    }

    public PlanUpdateRequestBuilder authorize(EnumSet<AuthorizationFlags> values) {
        this.authorize = values;
        return this;
    }

    @Override
    public List<PlanUpdateRequest> build(FieldSet fields, List<PlanUpdateRequestEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
        this.logger.trace(new DataLogEntry("requested fields", fields));
        if (fields == null || data == null || fields.isEmpty())
            return new ArrayList<>();

        FieldSet planFields = fields.extractPrefixed(this.asPrefix(PlanUpdateRequest._plan));
        Map<UUID, Plan> planItemsMap = this.collectPlans(planFields, data);

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(PlanUpdateRequest._approvedBy));
        Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);

        List<PlanUpdateRequest> models = new ArrayList<>();

        for (PlanUpdateRequestEntity d : data) {
            PlanUpdateRequest m = new PlanUpdateRequest();

            if (fields.hasField(this.asIndexer(PlanUpdateRequest._id)))
                m.setId(d.getId());
            if (!planFields.isEmpty() && planItemsMap != null && planItemsMap.containsKey(d.getPlanId())) m.setPlan(planItemsMap.get(d.getPlanId()));
            if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getApprovedBy())) m.setSubmitter(userItemsMap.get(d.getSubmitterId()));
            if (fields.hasField(this.asIndexer(PlanUpdateRequest._actionType)))
                m.setActionType(d.getActionType());
            if (fields.hasField(this.asIndexer(PlanUpdateRequest._status)))
                m.setStatus(d.getStatus());
            if (fields.hasField(this.asIndexer(PlanUpdateRequest._sourceCode)))
                m.setSourceCode(d.getSourceCode());
            if (fields.hasField(this.asIndexer(PlanUpdateRequest._data)))
//                m.setData(this.jsonHandlingService.fromJsonSafe(DmpRootModel.class, d.getData()));
                m.setData(m.getData());
            if (fields.hasField(this.asIndexer(PlanUpdateRequest._requestAt)))
                m.setRequestAt(d.getRequestAt());
            if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getApprovedBy())) m.setApprovedBy(userItemsMap.get(d.getApprovedBy()));
            if (fields.hasField(this.asIndexer(PlanUpdateRequest._approvedAt)))
                m.setApprovedAt(d.getApprovedAt());
            if (fields.hasField(this.asIndexer(PlanUpdateRequest._createdAt)))
                m.setCreatedAt(d.getCreatedAt());
            if (fields.hasField(this.asIndexer(PlanUpdateRequest._updatedAt)))
                m.setUpdatedAt(d.getUpdatedAt());
            if (fields.hasField(this.asIndexer(PlanUpdateRequest._isActive)))
                m.setIsActive(d.getIsActive());
            if (fields.hasField(this.asIndexer(PlanUpdateRequest._hash)))
                m.setHash(this.hashValue(d.getUpdatedAt()));
            if (fields.hasField(this.asIndexer(PlanUpdateRequest._belongsToCurrentTenant)))
                m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScopeFactory.getInstance()));

            models.add(m);
        }

        return models;
    }

    private Map<UUID, Plan> collectPlans(FieldSet fields, List<PlanUpdateRequestEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", Plan.class.getSimpleName());

        Map<UUID, Plan> itemMap;
        if (!fields.hasOtherField(this.asIndexer(Plan._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanUpdateRequestEntity::getPlanId).distinct().collect(Collectors.toList()),
                    x -> {
                        Plan item = new Plan();
                        item.setId(x);
                        return item;
                    },
                    Plan::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Plan._id);
            PlanQuery q = this.queryFactory.query(PlanQuery.class).authorize(this.authorize).disableTracking().ids(data.stream().map(PlanUpdateRequestEntity::getPlanId).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(PlanBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Plan::getId);
        }
        if (!fields.hasField(Plan._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }

    private Map<UUID, User> collectUsers(FieldSet fields, List<PlanUpdateRequestEntity> data) throws MyApplicationException {
        if (fields.isEmpty() || data.isEmpty())
            return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, User> itemMap;
        if (!fields.hasOtherField(this.asIndexer(User._id))) {
            itemMap = this.asEmpty(
                    data.stream().map(PlanUpdateRequestEntity::getApprovedBy).distinct().collect(Collectors.toList()),
                    x -> {
                        User item = new User();
                        item.setId(x);
                        return item;
                    },
                    User::getId);
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(data.stream().map(PlanUpdateRequestEntity::getApprovedBy).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(UserBuilder.class).authorize(this.authorize).asForeignKey(q, clone, User::getId);
        }
        if (!fields.hasField(User._id)) {
            itemMap.forEach((id, item) -> {
                if (item != null)
                    item.setId(null);
            });
        }

        return itemMap;
    }
}
