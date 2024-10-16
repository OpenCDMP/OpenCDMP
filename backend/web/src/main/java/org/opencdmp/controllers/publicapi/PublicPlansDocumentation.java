package org.opencdmp.controllers.publicapi;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.controllers.publicapi.request.plan.DataManagmentPlanPublicTableRequest;
import org.opencdmp.controllers.publicapi.response.DataTableData;
import org.opencdmp.controllers.publicapi.types.ApiMessageCode;
import org.opencdmp.controllers.publicapi.types.ResponseItem;
import org.opencdmp.data.PlanEntity;
import org.opencdmp.model.PlanDescriptionTemplate;
import org.opencdmp.model.PlanUser;
import org.opencdmp.model.builder.PlanDescriptionTemplateBuilder;
import org.opencdmp.model.builder.plan.PlanBuilder;
import org.opencdmp.model.mapper.publicapi.PlanToPublicApiPlanListingMapper;
import org.opencdmp.model.mapper.publicapi.PlanToPublicApiPlanMapper;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planblueprint.Section;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.publicapi.listingmodels.DataManagementPlanPublicListingModel;
import org.opencdmp.model.publicapi.overviewmodels.DataManagementPlanPublicModel;
import org.opencdmp.model.reference.Definition;
import org.opencdmp.model.reference.Field;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.referencetype.ReferenceType;
import org.opencdmp.model.user.User;
import org.opencdmp.query.EntityDoiQuery;
import org.opencdmp.query.PlanDescriptionTemplateQuery;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.lookup.PlanLookup;
import org.opencdmp.service.publicapi.PublicApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "DMPs", description = "Provides DMP public API's.")
@RestController
@CrossOrigin
@RequestMapping("/api/public/dmps")
public class PublicPlansDocumentation {

    private static final Logger logger = LoggerFactory.getLogger(PublicPlansDocumentation.class);

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;

    private final MessageSource messageSource;

    private final PlanToPublicApiPlanMapper planToPublicApiPlanMapper;

    private final PublicApiProperties config;
    private final PlanToPublicApiPlanListingMapper planToPublicApiPlanListingMapper;

    @Autowired
    public PublicPlansDocumentation(
            QueryFactory queryFactory,
            BuilderFactory builderFactory,
            MessageSource messageSource,
            PlanToPublicApiPlanMapper planToPublicApiPlanMapper, PublicApiProperties config,
            PlanToPublicApiPlanListingMapper planToPublicApiPlanListingMapper) {
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.messageSource = messageSource;
        this.planToPublicApiPlanMapper = planToPublicApiPlanMapper;
	    this.config = config;
	    this.planToPublicApiPlanListingMapper = planToPublicApiPlanListingMapper;
    }

    @Operation(summary = "This method is used to get a listing of public dmps.", description = PublicApiStaticHelpers.Plan.getPagedNotes)
    @io.swagger.v3.oas.annotations.responses.ApiResponses(@ApiResponse(
		    responseCode = "200",
		    description = """
				    The following example is generated using:
				    a) body: *{"criteria": {},"length": 2,"offset": 0,"orderings": {"fields": []} }*
				    b) fieldsGroup: listing""",
		    content = @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(PublicApiStaticHelpers.Plan.getPagedResponseExample))
    ))
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody ResponseEntity<ResponseItem<DataTableData<DataManagementPlanPublicListingModel>>> getPaged(
            @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = PublicApiStaticHelpers.Plan.getPagedRequestBodyDescription) DataManagmentPlanPublicTableRequest planTableRequest,
            @RequestParam @Parameter(description = PublicApiStaticHelpers.Plan.getPagedRequestParamDescription, example = "listing") String fieldsGroup
    ) throws Exception {
        PlanLookup lookup = getPlanLookup(fieldsGroup, planTableRequest);
        PlanQuery query = lookup.enrich(this.queryFactory).authorize(EnumSet.of(AuthorizationFlags.Public)).isActive(IsActive.Active);
        long count = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(EnumSet.of(AuthorizationFlags.Public)).isActive(IsActive.Active).count();

        List<PlanEntity> data = query.collectAs(lookup.getProject());
        List<Plan> models = this.builderFactory.builder(PlanBuilder.class).build(lookup.getProject(), data);
        DataTableData<DataManagementPlanPublicListingModel> dataTableData = new DataTableData<>();
        dataTableData.setData(models.stream().map(x-> this.planToPublicApiPlanListingMapper.toPublicListingModel(x, this.config.getReferenceTypeMap())).toList());
        dataTableData.setTotalCount(count);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseItem<DataTableData<DataManagementPlanPublicListingModel>>().status(ApiMessageCode.NO_MESSAGE).payload(dataTableData));
    }

    @NotNull
    private static PlanLookup getPlanLookup(String fieldsGroup, DataManagmentPlanPublicTableRequest request) {
        BaseFieldSet fieldSet = new BaseFieldSet();
        Set<String> fields;
        if ("listing".equals(fieldsGroup)) {
            fields = Set.of(
                    Plan._id,
                    Plan._label,
                    Plan._description,
                    Plan._version,
                    Plan._groupId,
                    String.join(".", Plan._planUsers, PlanUser._user, User._id),
                    String.join(".", Plan._planUsers, PlanUser._user, User._name),
                    String.join(".", Plan._planUsers, PlanUser._role),
                    String.join(".", Plan._planReferences, PlanReference._reference, Reference._id),
                    String.join(".", Plan._planReferences, PlanReference._reference, Reference._type),
                    String.join(".", Plan._planReferences, PlanReference._reference, Reference._type),
                    String.join(".", Plan._planReferences, PlanReference._reference, Reference._type, ReferenceType._id),
                    String.join(".", Plan._planReferences, PlanReference._reference, Reference._label),
                    String.join(".", Plan._planReferences, PlanReference._reference, Reference._definition, Definition._fields, Field._code),
                    String.join(".", Plan._planReferences, PlanReference._reference, Reference._definition, Definition._fields, Field._dataType),
                    String.join(".", Plan._planReferences, PlanReference._reference, Reference._definition, Definition._fields, Field._value),
                    Plan._planReferences,
                    Plan._createdAt,
                    Plan._updatedAt,
                    Plan._finalizedAt
            );
        } else {
            fields = Set.of(
                    Plan._id,
                    Plan._label,
                    Plan._groupId,
                    Plan._createdAt
            );
        }
        fieldSet.setFields(fields);
        PlanLookup lookup = new PlanLookup();
        lookup.setProject(fieldSet);
        return lookup;
    }

    @Operation(summary = "This method is used to get the overview of a public dmp.", description = PublicApiStaticHelpers.Plan.getOverviewSinglePublicNotes)
    @io.swagger.v3.oas.annotations.responses.ApiResponses(@ApiResponse(
		    responseCode = "200",
		    description = "The following example is generated using id: *e9a73d77-adfa-4546-974f-4a4a623b53a8*",
		    content = @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(PublicApiStaticHelpers.Plan.getOverviewSinglePublicResponseExample))
    ))
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "application/json")
    public @ResponseBody ResponseEntity<ResponseItem<DataManagementPlanPublicModel>> getOverviewSinglePublic(
            @PathVariable @Parameter(description = "fetch the dmp with the given id", example = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx") String id
    ) {
        BaseFieldSet planFieldSet = new BaseFieldSet();
        Set<String> planFields = Set.of(
                Plan._id,
                Plan._label,
                Plan._description,
                Plan._version,
                Plan._groupId,
                String.join(".", Plan._planUsers, PlanUser._user, User._id),
                String.join(".", Plan._planUsers, PlanUser._user, User._name),
                String.join(".", Plan._planUsers, PlanUser._role),
                String.join(".", Plan._planReferences, PlanReference._reference, Reference._id),
                String.join(".", Plan._planReferences, PlanReference._reference, Reference._type),
                String.join(".", Plan._planReferences, PlanReference._reference, Reference._type, ReferenceType._id),
                String.join(".", Plan._planReferences, PlanReference._reference, Reference._reference),
                String.join(".", Plan._planReferences, PlanReference._reference, Reference._label),
                String.join(".", Plan._planReferences, PlanReference._reference, Reference._abbreviation),
                String.join(".", Plan._planReferences, PlanReference._reference, Reference._description),
                String.join(".", Plan._planReferences, PlanReference._reference, Reference._definition),
                String.join(".", Plan._blueprint, org.opencdmp.model.planblueprint.Definition._sections, Section._id),
                String.join(".", Plan._blueprint, org.opencdmp.model.planblueprint.Definition._sections, Section._ordinal),
                Plan._createdAt,
                Plan._updatedAt,
                Plan._finalizedAt
        );
        planFieldSet.setFields(planFields);
        PlanQuery query = this.queryFactory.query(PlanQuery.class).disableTracking().authorize(EnumSet.of(AuthorizationFlags.Public)).ids(UUID.fromString(id)).isActive(IsActive.Active);
        Plan model = this.builderFactory.builder(PlanBuilder.class).build(planFieldSet, query.firstAs(planFieldSet));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Plan.class.getSimpleName()}, LocaleContextHolder.getLocale()));
        EntityDoiQuery entityDoiQuery = this.queryFactory.query(EntityDoiQuery.class).disableTracking().entityIds(UUID.fromString(id)).isActive(IsActive.Active);
        BaseFieldSet templateFieldSet = new BaseFieldSet();
        PlanDescriptionTemplateQuery planDescriptionTemplateQuery = this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().planIds(UUID.fromString(id)).isActive(IsActive.Active);
        List<PlanDescriptionTemplate> planDescriptionTemplates = this.builderFactory.builder(PlanDescriptionTemplateBuilder.class).build(templateFieldSet, planDescriptionTemplateQuery.collectAs(templateFieldSet));
        DataManagementPlanPublicModel dataManagementPlan = this.planToPublicApiPlanMapper.toPublicModel(model, entityDoiQuery.collect(), planDescriptionTemplates, this.config.getReferenceTypeMap());
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseItem<DataManagementPlanPublicModel>().status(ApiMessageCode.NO_MESSAGE).payload(dataManagementPlan));
    }

}
