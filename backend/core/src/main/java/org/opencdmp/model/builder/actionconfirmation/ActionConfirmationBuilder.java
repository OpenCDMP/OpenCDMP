package org.opencdmp.model.builder.actionconfirmation;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.XmlHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScope;
import org.opencdmp.commons.types.actionconfirmation.PlanInvitationEntity;
import org.opencdmp.commons.types.actionconfirmation.MergeAccountConfirmationEntity;
import org.opencdmp.commons.types.actionconfirmation.RemoveCredentialRequestEntity;
import org.opencdmp.commons.types.actionconfirmation.UserInviteToTenantRequestEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.data.ActionConfirmationEntity;
import org.opencdmp.model.actionconfirmation.ActionConfirmation;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.builder.UserBuilder;
import org.opencdmp.model.user.User;
import org.opencdmp.query.UserQuery;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ActionConfirmationBuilder extends BaseBuilder<ActionConfirmation, ActionConfirmationEntity> {

    private final BuilderFactory builderFactory;
    private final QueryFactory queryFactory;
    private final TenantScope tenantScope;
    private final XmlHandlingService xmlHandlingService;
    private EnumSet<AuthorizationFlags> authorize  = EnumSet.of(AuthorizationFlags.None);
    @Autowired
    public ActionConfirmationBuilder(ConventionService conventionService, BuilderFactory builderFactory, QueryFactory queryFactory, TenantScope tenantScope, XmlHandlingService xmlHandlingService) {
        super(conventionService, new LoggerService(LoggerFactory.getLogger(ActionConfirmationBuilder.class)));
        this.builderFactory = builderFactory;
        this.queryFactory = queryFactory;
	    this.tenantScope = tenantScope;
	    this.xmlHandlingService = xmlHandlingService;
    }

    public ActionConfirmationBuilder authorize(EnumSet<AuthorizationFlags> values){
        this.authorize = values;
        return this;
    }
    
    @Override
    public List<ActionConfirmation> build(FieldSet fields, List<ActionConfirmationEntity> data) throws MyApplicationException {
        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0),Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size) .orElse(0));
        this.logger.trace(new DataLogEntry("requested fields",fields));
        if(fields == null || data == null || fields.isEmpty()) return new ArrayList<>();

        FieldSet mergeAccountConfirmationFields = fields.extractPrefixed(this.asPrefix(ActionConfirmation._mergeAccountConfirmation));
        FieldSet removeCredentialRequestFields = fields.extractPrefixed(this.asPrefix(ActionConfirmation._removeCredentialRequest));
        FieldSet userInviteToTenantRequestFields = fields.extractPrefixed(this.asPrefix(ActionConfirmation._userInviteToTenantRequest));
        FieldSet planInvitationFields = fields.extractPrefixed(this.asPrefix(ActionConfirmation._planInvitation));

        FieldSet userFields = fields.extractPrefixed(this.asPrefix(ActionConfirmation._createdBy));
        Map<UUID, User> userMap = this.collectUsers(userFields, data);

        List<ActionConfirmation> models =  new ArrayList<>();
        for(ActionConfirmationEntity d : data){
            ActionConfirmation m = new ActionConfirmation();
            if(fields.hasField(this.asIndexer(ActionConfirmation._id))) m.setId(d.getId());
            if(fields.hasField(this.asIndexer(ActionConfirmation._type))) m.setType(d.getType());
            if(fields.hasField(this.asIndexer(ActionConfirmation._status))) m.setStatus(d.getStatus());
            if(fields.hasField(this.asIndexer(ActionConfirmation._isActive))) m.setIsActive(d.getIsActive());
            if(fields.hasField(this.asIndexer(ActionConfirmation._expiresAt))) m.setExpiresAt(d.getExpiresAt());
            if (fields.hasField(this.asIndexer(ActionConfirmation._belongsToCurrentTenant))) m.setBelongsToCurrentTenant(this.getBelongsToCurrentTenant(d, this.tenantScope));
            if (!removeCredentialRequestFields.isEmpty() && d.getData() != null){
                switch (d.getType())
                {
                    case MergeAccount -> {
                        MergeAccountConfirmationEntity emailConfirmation = this.xmlHandlingService.fromXmlSafe(MergeAccountConfirmationEntity.class, d.getData());
                        m.setMergeAccountConfirmation(this.builderFactory.builder(MergeAccountConfirmationBuilder.class).authorize(this.authorize).build(mergeAccountConfirmationFields, emailConfirmation));
                    }
                    case PlanInvitation -> {
                        PlanInvitationEntity planInvitation = this.xmlHandlingService.fromXmlSafe(PlanInvitationEntity.class, d.getData());
                        m.setPlanInvitation(this.builderFactory.builder(PlanInvitationBuilder.class).authorize(this.authorize).build(planInvitationFields, planInvitation));
                    }
                    case RemoveCredential -> {
                        RemoveCredentialRequestEntity emailConfirmation = this.xmlHandlingService.fromXmlSafe(RemoveCredentialRequestEntity.class, d.getData());
                        m.setRemoveCredentialRequest(this.builderFactory.builder(RemoveCredentialRequestBuilder.class).authorize(this.authorize).build(removeCredentialRequestFields, emailConfirmation));
                    }
                    case UserInviteToTenant -> {
                        UserInviteToTenantRequestEntity emailConfirmation = this.xmlHandlingService.fromXmlSafe(UserInviteToTenantRequestEntity.class, d.getData());
                        m.setUserInviteToTenantRequest(this.builderFactory.builder(UserInviteToTenantRequestBuilder.class).authorize(this.authorize).build(userInviteToTenantRequestFields, emailConfirmation));
                    }
                    default -> throw new InternalError("unknown type: " + d.getType());
                }

            }
            if (!userFields.isEmpty() && userMap != null && userMap.containsKey(d.getCreatedById())) m.setCreatedBy(userMap.get(d.getCreatedById()));
            if(fields.hasField(this.asIndexer(ActionConfirmation._createdAt))) m.setCreatedAt(d.getCreatedAt());
            if(fields.hasField(this.asIndexer(ActionConfirmation._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
            if(fields.hasField(this.asIndexer(ActionConfirmation._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
            models.add(m);
        }
        this.logger.debug("build {} items",Optional.of(models).map(List::size).orElse(0));
        return models;
    }


    private Map<UUID, User> collectUsers(FieldSet fields, List<ActionConfirmationEntity> datas) throws MyApplicationException {
        if (fields.isEmpty() || datas.isEmpty()) return null;
        this.logger.debug("checking related - {}", User.class.getSimpleName());

        Map<UUID, User> itemMap = null;
        if (!fields.hasOtherField(this.asIndexer(User._id))) {
            itemMap = this.asEmpty(
                    datas.stream().map(x -> x.getCreatedById()).distinct().collect(Collectors.toList()),
                    x -> {
                        User item = new User();
                        item.setId(x);
                        return item;
                    },
                    x -> x.getId());
        } else {
            FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
            UserQuery q = this.queryFactory.query(UserQuery.class).disableTracking().authorize(this.authorize).ids(datas.stream().map(x -> x.getCreatedById()).distinct().collect(Collectors.toList()));
            itemMap = this.builderFactory.builder(UserBuilder.class).authorize(this.authorize).asForeignKey(q, clone, x -> x.getId());
        }
        if (!fields.hasField(User._id)) {
            itemMap.values().stream().filter(x -> x != null).map(x -> {
                x.setId(null);
                return x;
            }).collect(Collectors.toList());
        }

        return itemMap;
    }

}
