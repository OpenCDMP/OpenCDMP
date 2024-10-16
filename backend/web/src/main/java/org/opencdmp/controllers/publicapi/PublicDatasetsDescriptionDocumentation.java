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
import org.opencdmp.controllers.publicapi.request.dataset.DatasetPublicTableRequest;
import org.opencdmp.controllers.publicapi.response.DataTableData;
import org.opencdmp.controllers.publicapi.types.ApiMessageCode;
import org.opencdmp.controllers.publicapi.types.ResponseItem;
import org.opencdmp.data.DescriptionEntity;
import org.opencdmp.model.PlanUser;
import org.opencdmp.model.builder.description.DescriptionBuilder;
import org.opencdmp.model.builder.plan.PlanBuilder;
import org.opencdmp.model.description.Description;
import org.opencdmp.model.descriptionreference.DescriptionReference;
import org.opencdmp.model.descriptiontemplate.*;
import org.opencdmp.model.descriptiontemplate.fielddata.BaseFieldData;
import org.opencdmp.model.mapper.publicapi.DescriptionToPublicApiDatasetListingMapper;
import org.opencdmp.model.mapper.publicapi.DescriptionToPublicApiDatasetMapper;
import org.opencdmp.model.mapper.publicapi.PlanToPublicApiPlanListingMapper;
import org.opencdmp.model.plan.Plan;
import org.opencdmp.model.planreference.PlanReference;
import org.opencdmp.model.publicapi.listingmodels.DatasetPublicListingModel;
import org.opencdmp.model.publicapi.overviewmodels.DatasetPublicModel;
import org.opencdmp.model.reference.Reference;
import org.opencdmp.model.user.User;
import org.opencdmp.query.DescriptionQuery;
import org.opencdmp.query.PlanQuery;
import org.opencdmp.query.lookup.DescriptionLookup;
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

@Tag(name = "Datasets Description", description = "Provides Dataset description public API's.")
@RestController
@CrossOrigin
@RequestMapping("/api/public/datasets/")
public class PublicDatasetsDescriptionDocumentation {

    private static final Logger logger = LoggerFactory.getLogger(PublicDatasetsDescriptionDocumentation.class);

    private final QueryFactory queryFactory;

    private final BuilderFactory builderFactory;

    private final MessageSource messageSource;

    private final DescriptionToPublicApiDatasetMapper descriptionToPublicApiDatasetMapper;

    private final DescriptionToPublicApiDatasetListingMapper descriptionToPublicApiDatasetListingMapper;

    private final PublicApiProperties config;

    private final PlanToPublicApiPlanListingMapper planToPublicApiPlanListingMapper;

    @Autowired
    public PublicDatasetsDescriptionDocumentation(
		    QueryFactory queryFactory,
		    BuilderFactory builderFactory,
		    MessageSource messageSource,
		    DescriptionToPublicApiDatasetMapper descriptionToPublicApiDatasetMapper,
		    DescriptionToPublicApiDatasetListingMapper descriptionToPublicApiDatasetListingMapper, PublicApiProperties config, PlanToPublicApiPlanListingMapper planToPublicApiPlanListingMapper) {
        this.queryFactory = queryFactory;
        this.builderFactory = builderFactory;
        this.messageSource = messageSource;
        this.descriptionToPublicApiDatasetMapper = descriptionToPublicApiDatasetMapper;
        this.descriptionToPublicApiDatasetListingMapper = descriptionToPublicApiDatasetListingMapper;
	    this.config = config;
	    this.planToPublicApiPlanListingMapper = planToPublicApiPlanListingMapper;
    }

    @Operation(summary = "This method is used to get a listing of public datasets.", description = PublicApiStaticHelpers.Description.getPagedNotes)
    @io.swagger.v3.oas.annotations.responses.ApiResponses(@ApiResponse(
		    responseCode = "200",
		    description = "The following example is generated using body: *{\"criteria\": {},\"length\": 2,\"offset\": 0,\"orderings\": {\"fields\": []} }*",
		    content = @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(PublicApiStaticHelpers.Description.getPagedResponseExample))
    ))
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public @ResponseBody ResponseEntity<ResponseItem<DataTableData<DatasetPublicListingModel>>> getPaged(
            @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = PublicApiStaticHelpers.Description.getPagedRequestBodyDescription) DatasetPublicTableRequest datasetTableRequest
    ) throws Exception {
        DescriptionLookup lookup = getDescriptionLookup();
        DescriptionQuery query = lookup.enrich(this.queryFactory).authorize(EnumSet.of(AuthorizationFlags.Public)).isActive(IsActive.Active);
        long count = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(EnumSet.of(AuthorizationFlags.Public)).isActive(IsActive.Active).count();

        List<DescriptionEntity> data = query.collectAs(lookup.getProject());
        List<Description> models = this.builderFactory.builder(DescriptionBuilder.class).build(lookup.getProject(), data);
        DataTableData<DatasetPublicListingModel> dataTableData = new DataTableData<>();
        dataTableData.setData(models.stream().map(x-> this.descriptionToPublicApiDatasetListingMapper.toPublicListingModel(x, this.config.getReferenceTypeMap())).toList());
        dataTableData.setTotalCount(count);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseItem<DataTableData<DatasetPublicListingModel>>().status(ApiMessageCode.NO_MESSAGE).payload(dataTableData));
    }

    @Operation(summary = "This method is used to get the overview of a public dataset.", description = PublicApiStaticHelpers.Description.getOverviewSinglePublicNotes)
    @io.swagger.v3.oas.annotations.responses.ApiResponses(@ApiResponse(
		    responseCode = "200",
		    description = "The following example is generated using id: *ef7dfbdc-c5c1-46a7-a37b-c8d8692f1c0e*",
		    content = @Content(mediaType = APPLICATION_JSON_VALUE, examples = @ExampleObject(PublicApiStaticHelpers.Description.getOverviewSinglePublicResponseExample))
    ))
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "application/json")
    public @ResponseBody ResponseEntity<ResponseItem<DatasetPublicModel>> getOverviewSinglePublic(
            @PathVariable @Parameter(description = "fetch the dataset with the given id", example = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx") String id
    ) {
        DescriptionLookup lookup = getDescriptionLookup();
        DescriptionQuery query = this.queryFactory.query(DescriptionQuery.class).disableTracking().authorize(EnumSet.of(AuthorizationFlags.Public)).ids(UUID.fromString(id)).isActive(IsActive.Active);
        Description model = this.builderFactory.builder(DescriptionBuilder.class).build(lookup.getProject(), query.firstAs(lookup.getProject()));
        if (model == null)
            throw new MyNotFoundException(this.messageSource.getMessage("General_ItemNotFound", new Object[]{id, Description.class.getSimpleName()}, LocaleContextHolder.getLocale()));

        PlanQuery planQuery = this.queryFactory.query(PlanQuery.class).disableTracking().ids(model.getPlan().getId()).isActive(IsActive.Active);
        PlanLookup planLookup = getPlanLookup();
        Plan plan = this.builderFactory.builder(PlanBuilder.class).build(planLookup.getProject(), planQuery.firstAs(planLookup.getProject()));

        DatasetPublicModel dataset = this.descriptionToPublicApiDatasetMapper.toPublicModel(model, this.planToPublicApiPlanListingMapper.toPublicListingModel(plan, this.config.getReferenceTypeMap()), this.config.getReferenceTypeMap());
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseItem<DatasetPublicModel>().status(ApiMessageCode.NO_MESSAGE).payload(dataset));
    }

    @NotNull
    private static DescriptionLookup getDescriptionLookup() {
        BaseFieldSet descriptionFieldSet = new BaseFieldSet();
        Set<String> descriptionFields = Set.of(
                Description._id,
                Description._label,
                Description._description,
                Description._status,
                String.join(".", Description._plan, Plan._id),
                String.join(".", Description._plan, Plan._label),
                String.join(".", Description._createdBy, User._id),
                String.join(".", Description._createdBy, User._name),
                String.join(".", Description._descriptionReferences, DescriptionReference._reference, Reference._id),
                String.join(".", Description._descriptionReferences, DescriptionReference._reference, Reference._type),
                String.join(".", Description._descriptionReferences, DescriptionReference._reference, Reference._type, Reference._id),
                String.join(".", Description._descriptionReferences, DescriptionReference._reference, Reference._reference),
                String.join(".", Description._descriptionReferences, DescriptionReference._reference, Reference._label),
                String.join(".", Description._descriptionReferences, DescriptionReference._reference, Reference._abbreviation),
                String.join(".", Description._descriptionReferences, DescriptionReference._reference, Reference._description),
                String.join(".", Description._descriptionReferences, DescriptionReference._reference, Reference._definition),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._status),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._id),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._ordinal),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._title),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._id),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._description),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._extendedDescription),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._title),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._ordinal),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._sections),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._id),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._description),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._extendedDescription),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._ordinal),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._additionalInformation),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._hasMultiplicity),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._multiplicity, Multiplicity._min),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._multiplicity, Multiplicity._max),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._multiplicity, Multiplicity._placeholder),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._multiplicity, Multiplicity._tableView),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._title),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._id),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._ordinal),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._data, BaseFieldData._label),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._data, BaseFieldData._fieldType),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._semantics),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._defaultValue, DefaultValue._dateValue),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._defaultValue, DefaultValue._booleanValue),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._defaultValue, DefaultValue._textValue),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._includeInExport),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._validations),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._visibilityRules, Rule._target),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._visibilityRules, Rule._dateValue),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._visibilityRules, Rule._booleanValue),
                String.join(".", Description._descriptionTemplate, DescriptionTemplate._definition, Definition._pages, Page._sections, Section._fieldSets, FieldSet._fields, Field._visibilityRules, Rule._textValue),
                Description._createdAt,
                Description._updatedAt,
                Description._finalizedAt

        );
        descriptionFieldSet.setFields(descriptionFields);
        DescriptionLookup lookup = new DescriptionLookup();
        lookup.setProject(descriptionFieldSet);
        return lookup;
    }

    @NotNull
    private static PlanLookup getPlanLookup() {
        BaseFieldSet fieldSet = new BaseFieldSet();
        Set<String> fields = Set.of(
                    Plan._id,
                    Plan._label,
                    Plan._description,
                    Plan._version,
                    Plan._groupId,
                    String.join(".", Plan._planUsers, String.join(".", PlanUser._user, User._id)),
                    String.join(".", Plan._planUsers, String.join(".", PlanUser._user, User._name)),
                    String.join(".", Plan._planUsers, PlanUser._role),
                    String.join(".", Plan._planReferences, String.join(".", PlanReference._reference, Reference._id)),
                    String.join(".", Plan._planReferences, String.join(".", PlanReference._reference, Reference._reference)),
                    String.join(".", Plan._planReferences, String.join(".", PlanReference._reference, Reference._label)),
                    Plan._planReferences,
                    Plan._createdAt,
                    Plan._updatedAt,
                    Plan._finalizedAt
        );
        fieldSet.setFields(fields);
        PlanLookup lookup = new PlanLookup();
        lookup.setProject(fieldSet);
        return lookup;
    }

}
