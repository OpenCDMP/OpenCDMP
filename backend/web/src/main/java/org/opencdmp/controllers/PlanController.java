package org.opencdmp.controllers;

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
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.xml.bind.JAXBException;
import org.opencdmp.audit.AuditableAction;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.PlanAccessType;
import org.opencdmp.commons.enums.PlanStatus;
import org.opencdmp.controllers.swagger.SwaggerHelpers;
import org.opencdmp.controllers.swagger.annotation.OperationWithTenantHeader;
import org.opencdmp.controllers.swagger.annotation.Swagger400;
import org.opencdmp.controllers.swagger.annotation.Swagger404;
import org.opencdmp.controllers.swagger.annotation.SwaggerCommonErrorResponses;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
import org.opencdmp.model.*;
import org.opencdmp.model.builder.PublicPlanBuilder;
import org.opencdmp.model.builder.plan.PlanBuilder;
import org.opencdmp.model.censorship.PublicPlanCensor;
import org.opencdmp.model.censorship.plan.PlanCensor;
import org.opencdmp.model.persist.*;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.result.QueryResult;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.PlanStatusQuery;
import org.opencdmp.query.lookup.PlanLookup;
import org.opencdmp.service.elastic.ElasticQueryHelperService;
import org.opencdmp.service.plan.PlanService;
import org.opencdmp.service.fieldsetexpander.FieldSetExpanderService;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.management.InvalidApplicationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.opencdmp.authorization.AuthorizationFlags.Public;

@RestController
@RequestMapping(path = "api/plan")
@Tag(name = "Plans", description = "Manage plans")
@SwaggerCommonErrorResponses
public class PlanController {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PlanController.class));

    private final BuilderFactory builderFactory;

    private final AuditService auditService;

    private final PlanService planService;

    private final CensorFactory censorFactory;

    private final QueryFactory queryFactory;

    private final MessageSource messageSource;

    private final ElasticQueryHelperService elasticQueryHelperService;

    private final FieldSetExpanderService fieldSetExpanderService;

    public PlanController(
            BuilderFactory builderFactory,
            AuditService auditService,
            PlanService planService,
            CensorFactory censorFactory,
            QueryFactory queryFactory,
            MessageSource messageSource,
            FieldSetExpanderService fieldSetExpanderService,
            ElasticQueryHelperService elasticQueryHelperService) {
        this.builderFactory = builderFactory;
        this.auditService = auditService;
        this.planService = planService;
        this.censorFactory = censorFactory;
        this.queryFactory = queryFactory;
        this.messageSource = messageSource;
        this.elasticQueryHelperService = elasticQueryHelperService;
        this.fieldSetExpanderService = fieldSetExpanderService;
    }

    @PostMapping("public/query")
    @OperationWithTenantHeader(summary = "Query public published plans")
    @Hidden
    public QueryResult<PublicPlan> publicQuery(@RequestBody PlanLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", Plan.class.getSimpleName());

        this.censorFactory.censor(PublicPlanCensor.class).censor(lookup.getProject());

        QueryResult<PublicPlan> queryResult = this.elasticQueryHelperService.collectPublic(lookup, EnumSet.of(Public), null);

        this.auditService.track(AuditableAction.Plan_PublicQuery, "lookup", lookup);

        return queryResult;
    }

    @GetMapping("public/{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific public published plan by id")
    @Hidden
    public PublicPlan publicGet(@PathVariable("id") UUID id, FieldSet fieldSet, Locale locale) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + Plan.class.getSimpleName()).And("id", id).And("fields", fieldSet));

        this.censorFactory.censor(PublicPlanCensor.class).censor(fieldSet);

        PlanStatusQuery statusQuery = this.queryFactory.query(PlanStatusQuery.class).disableTracking().internalStatuses(PlanStatus.Finalized).isActives(IsActive.Active);
        PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(EnumSet.of(Public)).ids(id).isActive(IsActive.Active).planStatusSubQuery(statusQuery).accessTypes(PlanAccessType.Public);

        PublicPlan model = this.builderFactory.builder(PublicPlanBuilder.class).authorize(EnumSet.of(Public)).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Plan_PublicLookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("query")
    @OperationWithTenantHeader(summary = "Query all plans", description = SwaggerHelpers.Plan.endpoint_query, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = SwaggerHelpers.Plan.endpoint_query_request_body, content = @Content(examples = @ExampleObject(
		    name = SwaggerHelpers.Commons.pagination_example,
		    description = SwaggerHelpers.Commons.pagination_example_description,
		    value = SwaggerHelpers.Plan.endpoint_query_request_body_example
    ))), responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
            array = @ArraySchema(
                    schema = @Schema(
                            implementation = Plan.class
                    )
            ),
            examples = @ExampleObject(
                    name = SwaggerHelpers.Commons.pagination_response_example,
                    description = SwaggerHelpers.Commons.pagination_response_example_description,
                    value = SwaggerHelpers.Plan.endpoint_query_response_example
            ))))
    public QueryResult<Plan> Query(@RequestBody PlanLookup lookup) throws MyApplicationException, MyForbiddenException {
        logger.debug("querying {}", Plan.class.getSimpleName());

        this.censorFactory.censor(PlanCensor.class).censor(lookup.getProject(), null);

        QueryResult<Plan> queryResult = this.elasticQueryHelperService.collect(lookup, AuthorizationFlags.AllExceptPublic, null);

        this.auditService.track(AuditableAction.Plan_Query, "lookup", lookup);

        return queryResult;
    }

    @GetMapping("{id}")
    @OperationWithTenantHeader(summary = "Fetch a specific plan by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Plan.class
                    ))
            ))
    @Swagger404
    public Plan Get(
            @Parameter(name = "id", description = "The id of a plan to fetch", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet,
            Locale locale
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
        logger.debug(new MapLogEntry("retrieving" + Plan.class.getSimpleName()).And("id", id).And("fields", fieldSet));
        fieldSet = this.fieldSetExpanderService.expand(fieldSet);
        this.censorFactory.censor(PlanCensor.class).censor(fieldSet, null);

        PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(AuthorizationFlags.AllExceptPublic).ids(id);
        Plan model = this.builderFactory.builder(PlanBuilder.class).authorize(AuthorizationFlags.AllExceptPublic).build(fieldSet, query.firstAs(fieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        this.auditService.track(AuditableAction.Plan_Lookup, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return model;
    }

    @PostMapping("persist")
    @OperationWithTenantHeader(summary = "Create a new or update an existing plan", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Plan.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = PlanPersist.PlanPersistValidator.ValidatorName, argumentName = "model")
    public Plan Persist(
            @RequestBody PlanPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException, JAXBException {
        logger.debug(new MapLogEntry("persisting" + Plan.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));

        Plan persisted = this.planService.persist(model, fieldSet);

        this.auditService.track(AuditableAction.Plan_Persist, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @DeleteMapping("{id}")
    @OperationWithTenantHeader(summary = "Delete a plan by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public void Delete(
            @Parameter(name = "id", description = "The id of a plan to delete", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id
    ) throws MyForbiddenException, InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("retrieving" + Plan.class.getSimpleName()).And("id", id));

        this.planService.deleteAndSave(id);

        this.auditService.track(AuditableAction.Plan_Delete, "id", id);
    }

    @PostMapping("set-status/{id}/{newStatusId}")
    @OperationWithTenantHeader(summary = "set status for a plan", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    @Transactional
    public boolean SetStatus(
            @Parameter(name = "id", description = "The id of a plan", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "newStatusId", description = "The new status of a plan", example = "f1a3da63-0bff-438f-8b46-1a81ca176115", required = true) @PathVariable("newStatusId") UUID newStatusId,
            @RequestBody DescriptionsToBeFinalized descriptions
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("set status" + Plan.class.getSimpleName()).And("id", id).And("newStatusId", newStatusId).And("descriptionIds", descriptions.getDescriptionIds()));

        this.planService.setStatus(id, newStatusId, descriptions.getDescriptionIds());

        this.auditService.track(AuditableAction.Plan_SetStatus, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("newStatusId", newStatusId),
                new AbstractMap.SimpleEntry<String, Object>("descriptionIds", descriptions.getDescriptionIds())
        ));

        return true;
    }

    @GetMapping("validate/{id}")
    @OperationWithTenantHeader(summary = "Validate if a plan is ready for finalization by id")
    @Hidden
    public PlanValidationResult validate(@PathVariable("id") UUID id) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
        logger.debug(new MapLogEntry("validating" + Plan.class.getSimpleName()).And("id", id));

        this.censorFactory.censor(PlanCensor.class).censor(null, null);

        PlanValidationResult result = this.planService.validate(id);

        this.auditService.track(AuditableAction.Plan_Validate, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id)
        ));

        return result;
    }

    @PostMapping("clone")
    @OperationWithTenantHeader(summary = "Create a clone of an existing plan", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Plan.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = ClonePlanPersist.ClonePlanPersistValidator.ValidatorName, argumentName = "model")
    public Plan buildClone(
            @RequestBody ClonePlanPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, IOException, InvalidApplicationException {
        logger.debug(new MapLogEntry("clone" + Plan.class.getSimpleName()).And("model", model).And("fields", fieldSet));

        this.censorFactory.censor(PlanCensor.class).censor(fieldSet, null);

        Plan clone = this.planService.buildClone(model, fieldSet);

        this.auditService.track(AuditableAction.Plan_Clone, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return clone;
    }

    @PostMapping("public-clone")
    @OperationWithTenantHeader(summary = "Create a clone of an existing public plan", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Plan.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = ClonePlanPersist.ClonePlanPersistValidator.ValidatorName, argumentName = "model")
    public Plan buildPublicClone(
            @RequestBody ClonePlanPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, IOException, InvalidApplicationException {
        logger.debug(new MapLogEntry("clone public" + PublicPlan.class.getSimpleName()).And("model", model).And("fields", fieldSet));

        this.censorFactory.censor(PublicPlanCensor.class).censor(fieldSet);

        Plan clone = this.planService.buildPublicClone(model, fieldSet);

        this.auditService.track(AuditableAction.Plan_PublicClone, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return clone;
    }

    @PostMapping("new-version")
    @OperationWithTenantHeader(summary = "Create a new version of an existing plan", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Plan.class
                    ))
            ))
    @Swagger400
    @Swagger404
    @Transactional
    @ValidationFilterAnnotation(validator = NewVersionPlanPersist.NewVersionPlanPersistValidator.ValidatorName, argumentName = "model")
    public Plan createNewVersion(
            @RequestBody NewVersionPlanPersist model,
            @Parameter(name = "fieldSet", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fieldSet
    ) throws MyApplicationException, MyForbiddenException, MyNotFoundException, JAXBException, IOException, TransformerException, InvalidApplicationException, ParserConfigurationException {
        logger.debug(new MapLogEntry("persisting" + NewVersionPlanPersist.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));

        Plan persisted = this.planService.createNewVersion(model, fieldSet);

        this.auditService.track(AuditableAction.Plan_PersistNewVersion, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return persisted;
    }

    @PostMapping("{id}/assign-users")
    @OperationWithTenantHeader(summary = "Assign users to the plan by id")
    @Transactional
    @ValidationFilterAnnotation(validator = PlanUserPersist.PlanUserPersistValidator.ValidatorName, argumentName = "model")
    @Hidden
    public QueryResult<PlanUser> assignUsers(@PathVariable("id") UUID id, @RequestBody List<PlanUserPersist> model, FieldSet fieldSet) throws InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("assigning users to plan").And("model", model).And("fieldSet", fieldSet));

        List<PlanUser> persisted = this.planService.assignUsers(id, model, fieldSet, false);

        this.auditService.track(AuditableAction.Plan_Assign_Users, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return new QueryResult<>(persisted);
    }

    @PostMapping("remove-user")
    @OperationWithTenantHeader(summary = "Remove a user association with the plan")
    @Transactional
    @ValidationFilterAnnotation(validator = PlanUserRemovePersist.PlanUserRemovePersistValidator.ValidatorName, argumentName = "model")
    @Hidden
    public QueryResult<Plan> removeUser(@RequestBody PlanUserRemovePersist model, FieldSet fieldSet) throws InvalidApplicationException, IOException {
        logger.debug(new MapLogEntry("remove user from plan").And("model", model).And("fieldSet", fieldSet));

        Plan persisted = this.planService.removeUser(model, fieldSet);

        this.auditService.track(AuditableAction.Plan_RemoveUser, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model),
                new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
        ));

        return new QueryResult<>(persisted);
    }

    @GetMapping("{id}/export/{transformerId}/{type}")
    @OperationWithTenantHeader(summary = "Export a plan in various formats by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    public ResponseEntity<byte[]> export(
            @Parameter(name = "id", description = "The id of a plan to export", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @PathVariable("transformerId") String transformerId,
            @PathVariable("type") String exportType
    ) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting plan").And("id", id).And("transformerId", transformerId).And("exportType", exportType));

        ResponseEntity<byte[]> bytes = this.planService.export(id, transformerId, exportType, false);
        this.auditService.track(AuditableAction.Plan_Export, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("transformerId", transformerId),
                new AbstractMap.SimpleEntry<String, Object>("exportType", exportType)
        ));
        return bytes;
    }

    @GetMapping("{id}/export-public/{transformerId}/{type}")
    @OperationWithTenantHeader(summary = "Export a public published plan in various formats by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    public ResponseEntity<byte[]> exportPublic(
            @Parameter(name = "id", description = "The id of a public published plan to export", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable("id") UUID id,
            @PathVariable("transformerId") String transformerId,
            @PathVariable("type") String exportType
    ) throws InvalidApplicationException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("exporting plan").And("id", id).And("transformerId", transformerId).And("exportType", exportType));

        ResponseEntity<byte[]> bytes = this.planService.export(id, transformerId, exportType, true);
        this.auditService.track(AuditableAction.Plan_ExportPublic, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id),
                new AbstractMap.SimpleEntry<String, Object>("transformerId", transformerId),
                new AbstractMap.SimpleEntry<String, Object>("exportType", exportType)
        ));
        return bytes;
    }

    @PostMapping("{id}/invite-users")
    @OperationWithTenantHeader(summary = "Send user invitations for the plan by id")
    @Transactional
    @ValidationFilterAnnotation(validator = PlanUserInvitePersist.PlanUserInvitePersistValidator.ValidatorName, argumentName = "model")
    @Hidden
    public boolean inviteUsers(@PathVariable("id") UUID id, @RequestBody PlanUserInvitePersist model) throws InvalidApplicationException, JAXBException, IOException {
        logger.debug(new MapLogEntry("inviting users to plan").And("model", model));

        this.planService.inviteUserOrAssignUsers(id, model.getUsers());

        this.auditService.track(AuditableAction.Plan_Invite_Users, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("model", model)
        ));

        return true;
    }

    @GetMapping("token/{token}/invite-accept")
    @OperationWithTenantHeader(summary = "Accept an invitation token for a plan by token")
    @Transactional
    @Hidden
    public PlanInvitationResult acceptInvitation(@PathVariable("token") String token) throws InvalidApplicationException, JAXBException, IOException {
        logger.debug(new MapLogEntry("inviting users to plan").And("token", token));

        PlanInvitationResult planInvitationResult = this.planService.planInvitationAccept(token);

        this.auditService.track(AuditableAction.Plan_Invite_Accept, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("token", token)
        ));

        return planInvitationResult;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/xml/export/{id}", produces = "application/xml")
    @OperationWithTenantHeader(summary = "Export a plan in xml format by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    public @ResponseBody ResponseEntity<byte[]> getXml(
            @Parameter(name = "id", description = "The id of a plan to export", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable UUID id
    ) throws JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export" + Plan.class.getSimpleName()).And("id", id));

        ResponseEntity<byte[]> response = this.planService.exportXml(id);

        this.auditService.track(AuditableAction.Plan_GetXml, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id)
        ));
        return response;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/xml/export-public/{id}", produces = "application/xml")
    @OperationWithTenantHeader(summary = "Export a public published plan in xml format by id", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200"))
    @Swagger404
    public @ResponseBody ResponseEntity<byte[]> getPublicXml(
            @Parameter(name = "id", description = "The id of a public published plan to export", example = "c0c163dc-2965-45a5-9608-f76030578609", required = true) @PathVariable UUID id
    ) throws JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException {
        logger.debug(new MapLogEntry("export public" + PublicPlan.class.getSimpleName()).And("id", id));

        ResponseEntity<byte[]> response = this.planService.exportPublicXml(id);

        this.auditService.track(AuditableAction.Plan_GetPublicXml, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("id", id)
        ));
        return response;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/xml/import")
    @OperationWithTenantHeader(summary = "Import a plan from an xml file", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Plan.class
                    ))
            ))
    @Transactional
    public Plan importXml(
            @RequestParam("file") MultipartFile file,
            @RequestParam("label") String label,
            @Parameter(name = "fields", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fields
    ) throws JAXBException, ParserConfigurationException, IOException, InstantiationException, IllegalAccessException, SAXException, InvalidApplicationException, TransformerException {
        logger.debug(new MapLogEntry("import xml" + Plan.class.getSimpleName()).And("file", file).And("label", label));

        Plan model = this.planService.importXml(file.getBytes(), label, fields);

        this.auditService.track(AuditableAction.Plan_Import, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("file", file),
                new AbstractMap.SimpleEntry<String, Object>("fields", fields)
        ));
        return model;
    }

    @PostMapping("json/preprocessing")
    @OperationWithTenantHeader(summary = "Preprocess a plan from a json file", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = PreprocessingPlanModel.class
                    ))
            ))
    @Transactional
    public PreprocessingPlanModel preprocessing(
            @RequestParam("fileId") UUID fileId,
            @RequestParam("repositoryId") String repositoryId
    ) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        logger.debug(new MapLogEntry("preprocessing plan" + Plan.class.getSimpleName()).And("transformerId", repositoryId).And("fileId", fileId));

        PreprocessingPlanModel model = this.planService.preprocessingPlan(fileId, repositoryId);

        this.auditService.track(AuditableAction.Plan_Import, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("transformerId", repositoryId),
                new AbstractMap.SimpleEntry<String, Object>("fileId", fileId)
        ));

        return model;
    }

    @PostMapping("json/import")
    @OperationWithTenantHeader(summary = "Import a plan from an json file", description = "",
            responses = @ApiResponse(description = "OK", responseCode = "200", content = @Content(
                    schema = @Schema(
                            implementation = Plan.class
                    ))
            ))
    @ValidationFilterAnnotation(validator = PlanCommonModelConfig.PlanCommonModelConfigValidator.ValidatorName, argumentName = "model")
    @Transactional
    public Plan importJson(
            @RequestBody PlanCommonModelConfig planCommonModelConfig,
            @Parameter(name = "fields", description = SwaggerHelpers.Commons.fieldset_description, required = true) FieldSet fields
    ) throws InvalidAlgorithmParameterException, JAXBException, NoSuchPaddingException, IllegalBlockSizeException, InvalidApplicationException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ParserConfigurationException, TransformerException, InstantiationException, IllegalAccessException, SAXException {
        logger.debug(new MapLogEntry("import json" + Plan.class.getSimpleName()).And("transformerId", planCommonModelConfig.getRepositoryId()).And("file id", planCommonModelConfig.getFileId()).And("label", planCommonModelConfig.getLabel()));

        Plan model = this.planService.importJson(planCommonModelConfig, fields);

        this.auditService.track(AuditableAction.Plan_Import, Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("transformerId", planCommonModelConfig.getRepositoryId()),
                new AbstractMap.SimpleEntry<String, Object>("file id", planCommonModelConfig.getFileId()),
                new AbstractMap.SimpleEntry<String, Object>("fields", fields)
        ));

        return model;
    }

}
