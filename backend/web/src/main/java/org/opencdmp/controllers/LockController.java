package org.opencdmp.controllers;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.ValidationFilterAnnotation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AffiliatedResource;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.authorization.Permission;
import org.opencdmp.authorization.authorizationcontentresolver.AuthorizationContentResolver;
import org.opencdmp.commons.enums.LockTargetType;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.data.LockEntity;
import org.opencdmp.model.Lock;
import org.opencdmp.model.LockStatus;
import org.opencdmp.model.builder.LockBuilder;
import org.opencdmp.model.censorship.LockCensor;
import org.opencdmp.model.persist.LockPersist;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.model.user.User;
import org.opencdmp.query.LockQuery;
import org.opencdmp.query.lookup.LockLookup;
import org.opencdmp.service.lock.LockService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/lock")
@Tag(name = "Locks", description = "Manage locked entities")
@SwaggerCommonErrorResponses
public class LockController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(LockController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final LockService lockService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    private final AuthorizationService authService;

    private final AuthorizationContentResolver authorizationContentResolver;

    @Autowired
    public LockController(BuilderFactory builderFactory,
                          AuditService auditService,
                          LockService lockService,
                          CensorFactory censorFactory,
                          QueryFactory queryFactory,
                          MessageSource messageSource, AuthorizationService authService, AuthorizationContentResolver authorizationContentResolver) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.lockService = lockService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
        this.authService = authService;
        this.authorizationContentResolver = authorizationContentResolver;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all locked entities", description = SwaggerHelpers.Lock.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.Lock.endpoint_query_request_body, content = @Content(
            examples = {
                    @ExampleObject(
                            name = SwaggerHelpers.Commons.pagination_example,
                            description = SwaggerHelpers.Commons.pagination_example_description,
                            value = SwaggerHelpers.Lock.endpoint_query_request_body_example
                    )
            }
    )), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = Lock.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.Lock.endpoint_query_response_example
            ))))
    public QueryResult<Lock> query(@RequestBody LockLookup lookup) throws MyApplicationException, MyForbiddenException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug("querying {}", Lock.class.getSimpleName());

        this.censorFactory.censor(LockCensor.class).censor(lookup.getProject(), null);

        LockQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.AllExceptPublic);
        List<LockEntity> data = query.collectAs(lookup.getProject());
        List<Lock> models = this.builderFactory.builder(LockBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(lookup.getProject(), data);
        long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

        this.auditService.track(AuditableAction.Lock_Query, "lookup", lookup);

        return new QueryResult<>(models, count);
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific lock by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Lock.class
                    ))
            ))
    @Swagger404
    public Lock get(
            @Parameter(name = "id", description = "The id of a user to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + Lock.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(LockCensor.class).censor(fieldSet, null);

        LockQuery query = this.queryFactory.query(LockQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        Lock model = this.builderFactory.builder(LockBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Lock.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Lock_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing lock", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Lock.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = LockPersist.LockPersistValidator.ValidatorName, argumentName = "model")
    public Lock persist(
            @RequestBody LockPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting" + Lock.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
        this.censorFactory.censor(LockCensor.class).censor(fieldSet, null);

        Lock persisted = this.lockService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.Lock_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @GetMapping("target/{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific lock by target id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Lock.class
                    ))
            ))
    @Swagger404
    public Lock getWithTarget(
            @Parameter(name = "id", description = "The target id of a lock to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID targetId,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + Lock.class.getSimpleName()).And("targetId", targetId).And("fields", fieldSet));

        this.censorFactory.censor(LockCensor.class).censor(fieldSet, null);

        LockQuery query = this.queryFactory.query(LockQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).targetIds(targetId);
        Lock model = this.builderFactory.builder(LockBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{targetId, Lock.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Lock_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("targetId", targetId),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @Transactional
    @GetMapping("target/status/{id}")
    @OperationWithTenantHeader(summary = "Fetch a lock status by target id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = LockStatus.class
                    ))
            ))
    public LockStatus getLocked(
            @Parameter(name = "id", description = "The target id of a lock to fetch the status", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID targetId,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws Exception {
        logger.debug(new MapLogEntry("is locked" + Lock.class.getSimpleName()).And("targetId", targetId).And("fields", fieldSet));
        this.authService.authorizeForce(Permission.BrowseLock);

        LockStatus lockStatus = this.lockService.isLocked(targetId, fieldSet);
        this.auditService.track(AuditableAction.Lock_IsLocked, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("targetId", targetId),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));
        return lockStatus;
    }

    @Transactional
    @GetMapping("target/lock/{id}/{targetType}")
    @OperationWithTenantHeader(summary = "Lock a target", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Boolean.class
                    ))
            ))
    public boolean lock(
            @Parameter(name = "id", description = "The target id to be locked", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID targetId,
            @Parameter(name = "targetType", description = "The target type to be locked", example = "0", required = true) @PathVariable("targetType") int targetType
    ) throws Exception {
        AffiliatedResource affiliatedResourcePlan = this.authorizationContentResolver.planAffiliation(targetId);
        AffiliatedResource affiliatedResourceDescription = this.authorizationContentResolver.descriptionAffiliation(targetId);
        AffiliatedResource affiliatedResourceDescriptionTemplate = this.authorizationContentResolver.descriptionTemplateAffiliation(targetId);
        this.authService.authorizeAtLeastOneForce(List.of(affiliatedResourcePlan, affiliatedResourceDescription, affiliatedResourceDescriptionTemplate), Permission.EditLock);

        this.lockService.lock(targetId, LockTargetType.of((short) targetType));
        this.auditService.track(AuditableAction.Lock_Locked, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("targetId", targetId),
                new AbstractMap.SimpleEntry<String, Object>("targetType", targetType)
        ));
        return true;
    }

    @Transactional
    @GetMapping("target/touch/{id}")
    @OperationWithTenantHeader(summary = "Touch a locked target", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Boolean.class
                    ))
            ))
    public boolean touch(
            @Parameter(name = "id", description = "The target id to be touched", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID targetId
    ) throws Exception {
        AffiliatedResource affiliatedResourcePlan = this.authorizationContentResolver.planAffiliation(targetId);
        AffiliatedResource affiliatedResourceDescription = this.authorizationContentResolver.descriptionAffiliation(targetId);
        AffiliatedResource affiliatedResourceDescriptionTemplate = this.authorizationContentResolver.descriptionTemplateAffiliation(targetId);
        this.authService.authorizeAtLeastOneForce(List.of(affiliatedResourcePlan, affiliatedResourceDescription, affiliatedResourceDescriptionTemplate), Permission.EditLock);

        this.lockService.touch(targetId);
        this.auditService.track(AuditableAction.Lock_Touched, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("targetId", targetId)
        ));
        return true;
    }

    @Transactional
    @DeleteMapping("target/unlock/{id}")
    @OperationWithTenantHeader(summary = "Unlock a target", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Boolean.class
                    ))
            ))
    public boolean unlock(
            @Parameter(name = "id", description = "The target id to be unlocked", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID targetId
    ) throws Exception {
        AffiliatedResource affiliatedResourcePlan = this.authorizationContentResolver.planAffiliation(targetId);
        AffiliatedResource affiliatedResourceDescription = this.authorizationContentResolver.descriptionAffiliation(targetId);
        AffiliatedResource affiliatedResourceDescriptionTemplate = this.authorizationContentResolver.descriptionTemplateAffiliation(targetId);
        this.authService.authorizeAtLeastOneForce(List.of(affiliatedResourcePlan, affiliatedResourceDescription, affiliatedResourceDescriptionTemplate), Permission.EditLock);

        this.lockService.unlock(targetId);
        this.auditService.track(AuditableAction.Lock_UnLocked, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("targetId", targetId)
        ));
        return true;
    }

    @DeleteMapping("{id}/{target}")
    @OperationWithTenantHeader(summary = "Delete a lock by id and target", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Transactional
    public void delete(
            @Parameter(name = "id", description = "The id of the lock to be deleted", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "target", description = "The target id of the lock", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("target") UUID target
    ) throws MyForbiddenException, InvalidApplicationException {
        logger.debug(new MapLogEntry("retrieving" + Lock.class.getSimpleName()).And("id", id));

        this.lockService.deleteAndSave(id, target);

        this.auditService.track(AuditableAction.Lock_Delete, "id", id);
    }
}
