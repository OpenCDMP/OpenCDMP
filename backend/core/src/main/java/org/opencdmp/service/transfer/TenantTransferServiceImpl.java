package org.opencdmp.service.transfer;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationConfiguration;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.enums.ContactInfoType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.commons.enums.StorageType;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.data.*;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.integrationevent.outbox.annotationentitytouch.AnnotationEntityTouchedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.plantouch.PlanTouchedIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.usertouched.UserTouchedIntegrationEventHandler;
import org.opencdmp.query.*;
import org.opencdmp.service.elastic.ElasticService;
import org.opencdmp.service.keycloak.KeycloakService;
import org.opencdmp.service.storage.StorageFileService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class TenantTransferServiceImpl implements TenantTransferService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantTransferServiceImpl.class));

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private static final String MANIFEST_ENTRY = "manifest.json";
    private static final String ANNOTATIONS_ENTRY = "annotations.json";
    private static final String FILES_PREFIX = "files/";
    private static final int ID_BATCH_SIZE = 500;
    private static final int FLUSH_BATCH_SIZE = 50;

    private final QueryFactory queryFactory;
    private final JsonHandlingService jsonHandlingService;
    private final TenantScopeFactory tenantScopeFactory;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final StorageFileService storageFileService;
    private final ElasticService elasticService;
    private final UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler;
    private final PlanTouchedIntegrationEventHandler planTouchedIntegrationEventHandler;
    private final AnnotationEntityTouchedIntegrationEventHandler annotationEntityTouchedIntegrationEventHandler;
    private final ErrorThesaurusProperties errors;
    private final AnnotationTransferClient annotationTransferClient;
    private final KeycloakService keycloakService;
    private final AuthorizationConfiguration authorizationConfiguration;
    private final ObjectProvider<Flyway> flyway;

    public TenantTransferServiceImpl(
            QueryFactory queryFactory,
            JsonHandlingService jsonHandlingService,
            TenantScopeFactory tenantScopeFactory,
            TenantEntityManagerFactory tenantEntityManagerFactory,
            StorageFileService storageFileService,
            ElasticService elasticService,
            UserTouchedIntegrationEventHandler userTouchedIntegrationEventHandler,
            PlanTouchedIntegrationEventHandler planTouchedIntegrationEventHandler,
            AnnotationEntityTouchedIntegrationEventHandler annotationEntityTouchedIntegrationEventHandler,
            ErrorThesaurusProperties errors,
            AnnotationTransferClient annotationTransferClient,
            KeycloakService keycloakService,
            AuthorizationConfiguration authorizationConfiguration,
            ObjectProvider<Flyway> flyway) {
        this.queryFactory = queryFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.tenantScopeFactory = tenantScopeFactory;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.storageFileService = storageFileService;
        this.elasticService = elasticService;
        this.userTouchedIntegrationEventHandler = userTouchedIntegrationEventHandler;
        this.planTouchedIntegrationEventHandler = planTouchedIntegrationEventHandler;
        this.annotationEntityTouchedIntegrationEventHandler = annotationEntityTouchedIntegrationEventHandler;
        this.errors = errors;
        this.annotationTransferClient = annotationTransferClient;
        this.keycloakService = keycloakService;
        this.authorizationConfiguration = authorizationConfiguration;
        this.flyway = flyway;
    }

    //region export

    private static class Collector {
        final Map<TransferEntityType, LinkedHashMap<UUID, Object>> rows = new EnumMap<>(TransferEntityType.class);
        final ArrayDeque<AbstractMap.SimpleEntry<TransferEntityType, Object>> frontier = new ArrayDeque<>();
        final Set<String> languageCodes = new HashSet<>();
        final Set<UUID> expandedPlanGroups = new HashSet<>();
        final Set<UUID> expandedBlueprintGroups = new HashSet<>();
        final Set<UUID> expandedTemplateGroups = new HashSet<>();

        void add(TransferEntityType type, UUID id, Object entity) {
            if (id == null || entity == null) return;
            LinkedHashMap<UUID, Object> typeRows = this.rows.computeIfAbsent(type, t -> new LinkedHashMap<>());
            if (typeRows.containsKey(id)) return;
            typeRows.put(id, entity);
            this.frontier.add(new AbstractMap.SimpleEntry<>(type, entity));
        }

        Set<UUID> ids(TransferEntityType type) {
            LinkedHashMap<UUID, Object> typeRows = this.rows.get(type);
            return typeRows == null ? Collections.emptySet() : typeRows.keySet();
        }

        @SuppressWarnings("unchecked")
        <T> List<T> list(TransferEntityType type) {
            LinkedHashMap<UUID, Object> typeRows = this.rows.get(type);
            if (typeRows == null) return new ArrayList<>();
            return new ArrayList<>((Collection<T>) typeRows.values());
        }
    }

    @Override
    public byte[] exportBundle(TransferExportRequest request) throws IOException, InvalidApplicationException {
        logger.debug("exporting tenant transfer bundle");
        this.ensureTenantScope();
        Collector collector = new Collector();

        // roots: only rows OWNED by the current tenant (the default visibility filter would also
        // match shared tenant-null rows, leaking the default tenant's content into the bundle)
        TenantEntityManager entityManager = this.tenantEntityManagerFactory.getInstance();
        try {
            entityManager.loadExplicitTenantFilters();
            this.collectRoots(request, collector);
        } finally {
            entityManager.reloadTenantFilters();
        }
        // closure: visibility filter on purpose - shared dependencies actually referenced by the
        // selected content (e.g. a shared blueprint used by a tenant plan) must travel with it
        this.expandClosure(collector);
        this.collectLanguages(request, collector);

        byte[] annotationsJson = this.fetchAnnotationsExport(collector);
        return this.buildZip(collector, annotationsJson);
    }

    /**
     * Comments live in the external annotation service. When a base url is configured, export the
     * annotations of every bundled plan/description through the service's transfer endpoint (the
     * caller's bearer token is forwarded - it already carries the annotation audience).
     */
    private byte[] fetchAnnotationsExport(Collector collector) {
        if (!this.annotationTransferClient.isConfigured()) return null;
        List<UUID> entityIds = new ArrayList<>();
        entityIds.addAll(collector.ids(TransferEntityType.Plan));
        entityIds.addAll(collector.ids(TransferEntityType.Description));
        if (entityIds.isEmpty()) return null;
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("entityIds", entityIds);
            String response = this.annotationTransferClient.post("/api/annotation/transfer/export", this.jsonHandlingService.toJson(request));
            return response == null ? null : response.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("failed to export annotations for transfer bundle", e);
            throw new MyApplicationException("Could not export annotations from the annotation service; aborting export so comments are not silently lost. Configure or check " + this.annotationTransferClient.baseUrl());
        }
    }

    private void collectRoots(TransferExportRequest request, Collector collector) {
        if (this.isTrue(request.getIncludeAllReferenceTypes()))
            this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().isActive(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.ReferenceType, x.getId(), x));
        if (this.isTrue(request.getIncludeAllPrefillingSources()))
            this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().isActive(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.PrefillingSource, x.getId(), x));
        if (this.isTrue(request.getIncludeAllDescriptionTemplateTypes()))
            this.queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking().isActive(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.DescriptionTemplateType, x.getId(), x));
        if (this.isTrue(request.getIncludeAllPlanBlueprintTypes()))
            this.queryFactory.query(PlanBlueprintTypeQuery.class).disableTracking().isActive(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.PlanBlueprintType, x.getId(), x));
        if (this.isTrue(request.getIncludeAllStatusesAndWorkflows())) {
            this.queryFactory.query(PlanStatusQuery.class).disableTracking().isActives(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.PlanStatus, x.getId(), x));
            this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().isActive(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.DescriptionStatus, x.getId(), x));
            this.queryFactory.query(PlanWorkflowQuery.class).disableTracking().isActives(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.PlanWorkflow, x.getId(), x));
            this.queryFactory.query(DescriptionWorkflowQuery.class).disableTracking().isActives(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.DescriptionWorkflow, x.getId(), x));
        }
        if (this.isTrue(request.getIncludeSupportiveMaterial()))
            this.queryFactory.query(SupportiveMaterialQuery.class).disableTracking().isActive(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.SupportiveMaterial, x.getId(), x));
        if (this.isTrue(request.getIncludeAllLanguages()))
            this.queryFactory.query(LanguageQuery.class).disableTracking().isActive(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.Language, x.getId(), x));

        if (this.isTrue(request.getIncludeAllDescriptionTemplates()))
            this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().isActive(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.DescriptionTemplate, x.getId(), x));
        else if (!this.isNullOrEmpty(request.getDescriptionTemplateIds()))
            this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(request.getDescriptionTemplateIds()).collect()
                    .forEach(x -> collector.add(TransferEntityType.DescriptionTemplate, x.getId(), x));

        if (this.isTrue(request.getIncludeAllPlanBlueprints()))
            this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().isActive(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.PlanBlueprint, x.getId(), x));
        else if (!this.isNullOrEmpty(request.getPlanBlueprintIds()))
            this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(request.getPlanBlueprintIds()).collect()
                    .forEach(x -> collector.add(TransferEntityType.PlanBlueprint, x.getId(), x));

        if (this.isTrue(request.getIncludeAllPlans()))
            this.queryFactory.query(PlanQuery.class).disableTracking().isActive(IsActive.Active).collect()
                    .forEach(x -> collector.add(TransferEntityType.Plan, x.getId(), x));
        else if (!this.isNullOrEmpty(request.getPlanIds()))
            this.queryFactory.query(PlanQuery.class).disableTracking().ids(request.getPlanIds()).collect()
                    .forEach(x -> collector.add(TransferEntityType.Plan, x.getId(), x));
    }

    private void expandClosure(Collector collector) {
        while (!collector.frontier.isEmpty()) {
            Map<TransferEntityType, List<Object>> round = new EnumMap<>(TransferEntityType.class);
            while (!collector.frontier.isEmpty()) {
                AbstractMap.SimpleEntry<TransferEntityType, Object> item = collector.frontier.poll();
                round.computeIfAbsent(item.getKey(), t -> new ArrayList<>()).add(item.getValue());
            }
            this.handleRound(round, collector);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleRound(Map<TransferEntityType, List<Object>> round, Collector collector) {
        Set<UUID> blobCandidates = new HashSet<>();
        Set<UUID> userIds = new HashSet<>();
        Set<UUID> planGroupIds = new HashSet<>();
        Set<UUID> blueprintIds = new HashSet<>();
        Set<UUID> blueprintGroupIds = new HashSet<>();
        Set<UUID> templateIds = new HashSet<>();
        Set<UUID> templateGroupIds = new HashSet<>();
        Set<UUID> templateTypeIds = new HashSet<>();
        Set<UUID> blueprintTypeIds = new HashSet<>();
        Set<UUID> planStatusIds = new HashSet<>();
        Set<UUID> descriptionStatusIds = new HashSet<>();
        Set<UUID> referenceIds = new HashSet<>();
        Set<UUID> referenceTypeIds = new HashSet<>();
        Set<UUID> tagIds = new HashSet<>();
        Set<UUID> planChildLookupIds = new HashSet<>();
        Set<UUID> descriptionChildLookupIds = new HashSet<>();
        Set<UUID> doiEntityIds = new HashSet<>();

        for (Map.Entry<TransferEntityType, List<Object>> entry : round.entrySet()) {
            switch (entry.getKey()) {
                case Plan -> {
                    for (Object item : entry.getValue()) {
                        PlanEntity plan = (PlanEntity) item;
                        planChildLookupIds.add(plan.getId());
                        doiEntityIds.add(plan.getId());
                        if (plan.getGroupId() != null && collector.expandedPlanGroups.add(plan.getGroupId())) planGroupIds.add(plan.getGroupId());
                        if (plan.getBlueprintId() != null) blueprintIds.add(plan.getBlueprintId());
                        if (plan.getStatusId() != null) planStatusIds.add(plan.getStatusId());
                        if (plan.getCreatorId() != null) userIds.add(plan.getCreatorId());
                        if (plan.getLanguage() != null) collector.languageCodes.add(plan.getLanguage());
                        this.scanBlob(plan.getProperties(), blobCandidates);
                    }
                }
                case Description -> {
                    for (Object item : entry.getValue()) {
                        DescriptionEntity description = (DescriptionEntity) item;
                        descriptionChildLookupIds.add(description.getId());
                        doiEntityIds.add(description.getId());
                        if (description.getDescriptionTemplateId() != null) templateIds.add(description.getDescriptionTemplateId());
                        if (description.getStatusId() != null) descriptionStatusIds.add(description.getStatusId());
                        if (description.getCreatedById() != null) userIds.add(description.getCreatedById());
                        this.scanBlob(description.getProperties(), blobCandidates);
                    }
                }
                case PlanDescriptionTemplate -> {
                    for (Object item : entry.getValue()) {
                        PlanDescriptionTemplateEntity pdt = (PlanDescriptionTemplateEntity) item;
                        if (pdt.getDescriptionTemplateGroupId() != null) templateGroupIds.add(pdt.getDescriptionTemplateGroupId());
                    }
                }
                case PlanUser -> {
                    for (Object item : entry.getValue()) {
                        PlanUserEntity planUser = (PlanUserEntity) item;
                        if (planUser.getUserId() != null) userIds.add(planUser.getUserId());
                    }
                }
                case PlanReference -> {
                    for (Object item : entry.getValue()) {
                        PlanReferenceEntity planReference = (PlanReferenceEntity) item;
                        if (planReference.getReferenceId() != null) referenceIds.add(planReference.getReferenceId());
                    }
                }
                case DescriptionReference -> {
                    for (Object item : entry.getValue()) {
                        DescriptionReferenceEntity descriptionReference = (DescriptionReferenceEntity) item;
                        if (descriptionReference.getReferenceId() != null) referenceIds.add(descriptionReference.getReferenceId());
                    }
                }
                case DescriptionTag -> {
                    for (Object item : entry.getValue()) {
                        DescriptionTagEntity descriptionTag = (DescriptionTagEntity) item;
                        if (descriptionTag.getTagId() != null) tagIds.add(descriptionTag.getTagId());
                    }
                }
                case Tag -> {
                    for (Object item : entry.getValue()) {
                        TagEntity tag = (TagEntity) item;
                        if (tag.getCreatedById() != null) userIds.add(tag.getCreatedById());
                    }
                }
                case Reference -> {
                    for (Object item : entry.getValue()) {
                        ReferenceEntity reference = (ReferenceEntity) item;
                        if (reference.getTypeId() != null) referenceTypeIds.add(reference.getTypeId());
                        if (reference.getCreatedById() != null) userIds.add(reference.getCreatedById());
                    }
                }
                case ReferenceType -> {
                    for (Object item : entry.getValue()) this.scanBlob(((ReferenceTypeEntity) item).getDefinition(), blobCandidates);
                }
                case PrefillingSource -> {
                    for (Object item : entry.getValue()) this.scanBlob(((PrefillingSourceEntity) item).getDefinition(), blobCandidates);
                }
                case PlanStatus -> {
                    for (Object item : entry.getValue()) this.scanBlob(((PlanStatusEntity) item).getDefinition(), blobCandidates);
                }
                case DescriptionStatus -> {
                    for (Object item : entry.getValue()) this.scanBlob(((DescriptionStatusEntity) item).getDefinition(), blobCandidates);
                }
                case PlanWorkflow -> {
                    for (Object item : entry.getValue()) this.scanBlob(((PlanWorkflowEntity) item).getDefinition(), blobCandidates);
                }
                case DescriptionWorkflow -> {
                    for (Object item : entry.getValue()) this.scanBlob(((DescriptionWorkflowEntity) item).getDefinition(), blobCandidates);
                }
                case DescriptionTemplate -> {
                    for (Object item : entry.getValue()) {
                        DescriptionTemplateEntity template = (DescriptionTemplateEntity) item;
                        if (template.getTypeId() != null) templateTypeIds.add(template.getTypeId());
                        if (template.getGroupId() != null && collector.expandedTemplateGroups.add(template.getGroupId())) templateGroupIds.add(template.getGroupId());
                        if (template.getLanguage() != null) collector.languageCodes.add(template.getLanguage());
                        this.scanBlob(template.getDefinition(), blobCandidates);
                    }
                }
                case PlanBlueprint -> {
                    for (Object item : entry.getValue()) {
                        PlanBlueprintEntity blueprint = (PlanBlueprintEntity) item;
                        if (blueprint.getTypeId() != null) blueprintTypeIds.add(blueprint.getTypeId());
                        if (blueprint.getGroupId() != null && collector.expandedBlueprintGroups.add(blueprint.getGroupId())) blueprintGroupIds.add(blueprint.getGroupId());
                        this.scanBlob(blueprint.getDefinition(), blobCandidates);
                    }
                }
                case StorageFile -> {
                    for (Object item : entry.getValue()) {
                        StorageFileEntity storageFile = (StorageFileEntity) item;
                        if (storageFile.getOwnerId() != null) userIds.add(storageFile.getOwnerId());
                    }
                }
                case User -> {
                    for (Object item : entry.getValue()) this.scanBlob(((UserEntity) item).getAdditionalInfo(), blobCandidates);
                }
                default -> {
                }
            }
        }

        // plan children
        if (!planChildLookupIds.isEmpty()) {
            for (List<UUID> batch : this.partition(planChildLookupIds)) {
                this.queryFactory.query(DescriptionQuery.class).disableTracking().planIds(batch).isActive(IsActive.Active).collect()
                        .forEach(x -> collector.add(TransferEntityType.Description, x.getId(), x));
                this.queryFactory.query(PlanUserQuery.class).disableTracking().planIds(batch).isActives(IsActive.Active).collect()
                        .forEach(x -> collector.add(TransferEntityType.PlanUser, x.getId(), x));
                this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().planIds(batch).isActive(IsActive.Active).collect()
                        .forEach(x -> collector.add(TransferEntityType.PlanDescriptionTemplate, x.getId(), x));
                this.queryFactory.query(PlanReferenceQuery.class).disableTracking().planIds(batch).isActives(IsActive.Active).collect()
                        .forEach(x -> collector.add(TransferEntityType.PlanReference, x.getId(), x));
            }
        }
        if (!descriptionChildLookupIds.isEmpty()) {
            for (List<UUID> batch : this.partition(descriptionChildLookupIds)) {
                this.queryFactory.query(DescriptionReferenceQuery.class).disableTracking().descriptionIds(batch).isActive(IsActive.Active).collect()
                        .forEach(x -> collector.add(TransferEntityType.DescriptionReference, x.getId(), x));
                this.queryFactory.query(DescriptionTagQuery.class).disableTracking().descriptionIds(batch).isActive(IsActive.Active).collect()
                        .forEach(x -> collector.add(TransferEntityType.DescriptionTag, x.getId(), x));
            }
        }
        if (!doiEntityIds.isEmpty()) {
            for (List<UUID> batch : this.partition(doiEntityIds)) {
                this.queryFactory.query(EntityDoiQuery.class).disableTracking().entityIds(batch).isActive(IsActive.Active).collect()
                        .forEach(x -> collector.add(TransferEntityType.EntityDoi, x.getId(), x));
            }
        }

        // version group expansion
        if (!planGroupIds.isEmpty()) {
            for (List<UUID> batch : this.partition(planGroupIds))
                this.queryFactory.query(PlanQuery.class).disableTracking().groupIds(batch).isActive(IsActive.Active).collect()
                        .forEach(x -> collector.add(TransferEntityType.Plan, x.getId(), x));
        }
        if (!blueprintGroupIds.isEmpty()) {
            for (List<UUID> batch : this.partition(blueprintGroupIds))
                this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().groupIds(batch).isActive(IsActive.Active).collect()
                        .forEach(x -> collector.add(TransferEntityType.PlanBlueprint, x.getId(), x));
        }
        if (!templateGroupIds.isEmpty()) {
            for (List<UUID> batch : this.partition(templateGroupIds))
                this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().groupIds(batch).isActive(IsActive.Active).collect()
                        .forEach(x -> collector.add(TransferEntityType.DescriptionTemplate, x.getId(), x));
        }

        // direct FK probes
        this.probe(collector, TransferEntityType.PlanBlueprint, blueprintIds, ids -> this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(ids).collect(), x -> ((PlanBlueprintEntity) x).getId());
        this.probe(collector, TransferEntityType.DescriptionTemplate, templateIds, ids -> this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(ids).collect(), x -> ((DescriptionTemplateEntity) x).getId());
        this.probe(collector, TransferEntityType.DescriptionTemplateType, templateTypeIds, ids -> this.queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking().ids(ids).collect(), x -> ((DescriptionTemplateTypeEntity) x).getId());
        this.probe(collector, TransferEntityType.PlanBlueprintType, blueprintTypeIds, ids -> this.queryFactory.query(PlanBlueprintTypeQuery.class).disableTracking().ids(ids).collect(), x -> ((PlanBlueprintTypeEntity) x).getId());
        this.probe(collector, TransferEntityType.PlanStatus, planStatusIds, ids -> this.queryFactory.query(PlanStatusQuery.class).disableTracking().ids(ids).collect(), x -> ((PlanStatusEntity) x).getId());
        this.probe(collector, TransferEntityType.DescriptionStatus, descriptionStatusIds, ids -> this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().ids(ids).collect(), x -> ((DescriptionStatusEntity) x).getId());
        this.probe(collector, TransferEntityType.Reference, referenceIds, ids -> this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(ids).collect(), x -> ((ReferenceEntity) x).getId());
        this.probe(collector, TransferEntityType.ReferenceType, referenceTypeIds, ids -> this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(ids).collect(), x -> ((ReferenceTypeEntity) x).getId());
        this.probe(collector, TransferEntityType.Tag, tagIds, ids -> this.queryFactory.query(TagQuery.class).disableTracking().ids(ids).collect(), x -> ((TagEntity) x).getId());

        // blob candidate probes: any UUID found inside definition/properties blobs that resolves
        // to a supported dependency row gets pulled into the bundle
        if (!blobCandidates.isEmpty()) {
            this.probe(collector, TransferEntityType.ReferenceType, blobCandidates, ids -> this.queryFactory.query(ReferenceTypeQuery.class).disableTracking().ids(ids).collect(), x -> ((ReferenceTypeEntity) x).getId());
            this.probe(collector, TransferEntityType.PrefillingSource, blobCandidates, ids -> this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().ids(ids).collect(), x -> ((PrefillingSourceEntity) x).getId());
            this.probe(collector, TransferEntityType.StorageFile, blobCandidates, ids -> this.queryFactory.query(StorageFileQuery.class).disableTracking().ids(ids).collect(), x -> ((StorageFileEntity) x).getId());
            this.probe(collector, TransferEntityType.Tag, blobCandidates, ids -> this.queryFactory.query(TagQuery.class).disableTracking().ids(ids).collect(), x -> ((TagEntity) x).getId());
            this.probe(collector, TransferEntityType.Reference, blobCandidates, ids -> this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(ids).collect(), x -> ((ReferenceEntity) x).getId());
            this.probe(collector, TransferEntityType.DescriptionTemplate, blobCandidates, ids -> this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(ids).collect(), x -> ((DescriptionTemplateEntity) x).getId());
            this.probe(collector, TransferEntityType.PlanStatus, blobCandidates, ids -> this.queryFactory.query(PlanStatusQuery.class).disableTracking().ids(ids).collect(), x -> ((PlanStatusEntity) x).getId());
            this.probe(collector, TransferEntityType.DescriptionStatus, blobCandidates, ids -> this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().ids(ids).collect(), x -> ((DescriptionStatusEntity) x).getId());
            // description template groups referenced from blueprint definitions
            Set<UUID> candidateGroups = new HashSet<>(blobCandidates);
            candidateGroups.removeAll(collector.expandedTemplateGroups);
            if (!candidateGroups.isEmpty()) {
                for (List<UUID> batch : this.partition(candidateGroups))
                    this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().groupIds(batch).isActive(IsActive.Active).collect()
                            .forEach(x -> collector.add(TransferEntityType.DescriptionTemplate, x.getId(), x));
            }
        }

        // users referenced by this round
        userIds.removeAll(collector.ids(TransferEntityType.User));
        if (!userIds.isEmpty()) {
            for (List<UUID> batch : this.partition(userIds)) {
                this.queryFactory.query(UserQuery.class).disableTracking().ids(batch).collect()
                        .forEach(x -> collector.add(TransferEntityType.User, x.getId(), x));
                this.queryFactory.query(UserContactInfoQuery.class).disableTracking().userIds(batch).collect()
                        .forEach(x -> collector.add(TransferEntityType.UserContactInfo, x.getId(), x));
            }
        }
    }

    private void probe(Collector collector, TransferEntityType type, Set<UUID> candidateIds, java.util.function.Function<List<UUID>, List<?>> query, java.util.function.Function<Object, UUID> idOf) {
        if (candidateIds == null || candidateIds.isEmpty()) return;
        Set<UUID> pending = new HashSet<>(candidateIds);
        pending.removeAll(collector.ids(type));
        if (pending.isEmpty()) return;
        for (List<UUID> batch : this.partition(pending)) {
            for (Object item : query.apply(batch)) {
                collector.add(type, idOf.apply(item), item);
            }
        }
    }

    private void collectLanguages(TransferExportRequest request, Collector collector) {
        if (collector.languageCodes.isEmpty()) return;
        this.queryFactory.query(LanguageQuery.class).disableTracking().codes(collector.languageCodes).isActive(IsActive.Active).collect()
                .forEach(x -> collector.add(TransferEntityType.Language, x.getId(), x));
    }

    private void scanBlob(String blob, Set<UUID> candidates) {
        if (blob == null || blob.isBlank()) return;
        Matcher matcher = UUID_PATTERN.matcher(blob);
        while (matcher.find()) {
            try {
                candidates.add(UUID.fromString(matcher.group().toLowerCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                // not a parseable uuid, ignore
            }
        }
    }

    private byte[] buildZip(Collector collector, byte[] annotationsJson) throws IOException, InvalidApplicationException {
        TransferBundleManifest manifest = new TransferBundleManifest();
        manifest.setBundleFormatVersion(TransferBundleManifest.CURRENT_FORMAT_VERSION);
        manifest.setExportedAt(Instant.now());
        manifest.setSourceTenantCode(this.currentTenantCode());
        manifest.setSourceDbVersion(this.currentDbVersion());
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (TransferEntityType type : TransferEntityType.values()) {
            int size = collector.ids(type).size();
            if (size > 0) counts.put(type.name(), size);
        }
        manifest.setEntityCounts(counts);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int fileCount = 0;
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            for (TransferEntityType type : TransferEntityType.values()) {
                List<Object> items = collector.list(type);
                if (items.isEmpty()) continue;
                zip.putNextEntry(new ZipEntry(type.bundleEntryName()));
                zip.write(this.jsonHandlingService.toJson(items).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            for (Object item : collector.list(TransferEntityType.StorageFile)) {
                StorageFileEntity storageFile = (StorageFileEntity) item;
                if (storageFile.getPurgedAt() != null) continue;
                byte[] payload = this.storageFileService.readByFileRefAsBytesSafe(storageFile.getFileRef(), storageFile.getStorageType());
                if (payload == null) continue;
                zip.putNextEntry(new ZipEntry(FILES_PREFIX + storageFile.getFileRef()));
                zip.write(payload);
                zip.closeEntry();
                fileCount++;
            }
            manifest.setFileCount(fileCount);
            if (annotationsJson != null) {
                zip.putNextEntry(new ZipEntry(ANNOTATIONS_ENTRY));
                zip.write(annotationsJson);
                zip.closeEntry();
            }
            zip.putNextEntry(new ZipEntry(MANIFEST_ENTRY));
            zip.write(this.jsonHandlingService.toJson(manifest).getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return outputStream.toByteArray();
    }

    private String currentTenantCode() throws InvalidApplicationException {
        if (this.tenantScopeFactory.getInstance().isDefaultTenant()) return this.tenantScopeFactory.getInstance().getDefaultTenantCode();
        return this.tenantScopeFactory.getInstance().getTenantCode();
    }

    private String currentDbVersion() {
        Flyway flyway = this.flyway.getIfAvailable();
        if (flyway == null) return null;
        try {
            MigrationInfo current = flyway.info().current();
            return current == null || current.getVersion() == null ? null : current.getVersion().getVersion();
        } catch (Exception e) {
            logger.warn("could not read the schema version for transfer manifest", e);
            return null;
        }
    }

    //endregion

    //region import

    private static class ParsedBundle {
        TransferBundleManifest manifest;
        byte[] annotationsJson;
        final Map<TransferEntityType, List<Object>> entities = new EnumMap<>(TransferEntityType.class);
        final Map<String, byte[]> files = new HashMap<>();

        List<Object> list(TransferEntityType type) {
            return this.entities.getOrDefault(type, Collections.emptyList());
        }
    }

    private static class ImportContext {
        final Map<UUID, UUID> idRemap = new HashMap<>();
        final Map<TransferEntityType, List<Object>> toCreate = new EnumMap<>(TransferEntityType.class);
        final Map<String, String> fileRefRemap = new HashMap<>();
        final Set<UUID> createdUserIds = new HashSet<>();
        final Set<UUID> matchedUserIds = new HashSet<>();
        final List<UUID> createdPlanIds = new ArrayList<>();
        final List<UUID> createdDescriptionIds = new ArrayList<>();

        void create(TransferEntityType type, Object entity) {
            this.toCreate.computeIfAbsent(type, t -> new ArrayList<>()).add(entity);
        }

        List<Object> creates(TransferEntityType type) {
            return this.toCreate.getOrDefault(type, Collections.emptyList());
        }

        UUID remap(UUID id) {
            if (id == null) return null;
            return this.idRemap.getOrDefault(id, id);
        }
    }

    @Override
    public TransferImportReport analyzeImport(byte[] bundle) throws IOException, InvalidApplicationException {
        return this.doImport(bundle, false);
    }

    @Override
    public TransferImportReport importBundle(byte[] bundle) throws IOException, InvalidApplicationException {
        return this.doImport(bundle, true);
    }

    /**
     * loadExplicitTenantFilters/getTenant are only meaningful with a resolved tenant scope; without
     * one the filters silently fall open, so these operations must never run unscoped.
     */
    private void ensureTenantScope() {
        if (!this.tenantScopeFactory.getInstance().isSet())
            throw new MyForbiddenException(this.errors.getMissingTenant().getCode(), this.errors.getMissingTenant().getMessage());
    }

    private TransferImportReport doImport(byte[] data, boolean apply) throws IOException, InvalidApplicationException {
        this.ensureTenantScope();
        ParsedBundle bundle = this.parseBundle(data);
        this.validateManifest(bundle);

        TransferImportReport report = new TransferImportReport();
        report.setApplied(apply);
        report.setSourceTenantCode(bundle.manifest.getSourceTenantCode());

        UUID targetTenantId = this.tenantScopeFactory.getInstance().isDefaultTenant() ? null : this.tenantScopeFactory.getInstance().getTenant();

        TenantEntityManager entityManager = this.tenantEntityManagerFactory.getInstance();
        try {
            entityManager.disableTenantFilters();
            ImportContext context = new ImportContext();

            this.analyzeCodeKeyed(bundle, context, report, TransferEntityType.ReferenceType,
                    x -> ((ReferenceTypeEntity) x).getId(), x -> ((ReferenceTypeEntity) x).getCode(),
                    () -> this.queryFactory.query(ReferenceTypeQuery.class).isActive(IsActive.Active).disableTracking().collect(), x -> ((ReferenceTypeEntity) x).getCode(), x -> ((ReferenceTypeEntity) x).getId(), x -> ((ReferenceTypeEntity) x).getTenantId(), targetTenantId);
            this.analyzeCodeKeyed(bundle, context, report, TransferEntityType.PrefillingSource,
                    x -> ((PrefillingSourceEntity) x).getId(), x -> ((PrefillingSourceEntity) x).getCode(),
                    () -> this.queryFactory.query(PrefillingSourceQuery.class).disableTracking().isActive(IsActive.Active).collect(), x -> ((PrefillingSourceEntity) x).getCode(), x -> ((PrefillingSourceEntity) x).getId(), x -> ((PrefillingSourceEntity) x).getTenantId(), targetTenantId);
            this.analyzeCodeKeyed(bundle, context, report, TransferEntityType.DescriptionTemplateType,
                    x -> ((DescriptionTemplateTypeEntity) x).getId(), x -> ((DescriptionTemplateTypeEntity) x).getCode(),
                    () -> this.queryFactory.query(DescriptionTemplateTypeQuery.class).disableTracking().isActive(IsActive.Active).collect(), x -> ((DescriptionTemplateTypeEntity) x).getCode(), x -> ((DescriptionTemplateTypeEntity) x).getId(), x -> ((DescriptionTemplateTypeEntity) x).getTenantId(), targetTenantId);
            this.analyzeCodeKeyed(bundle, context, report, TransferEntityType.PlanBlueprintType,
                    x -> ((PlanBlueprintTypeEntity) x).getId(), x -> ((PlanBlueprintTypeEntity) x).getCode(),
                    () -> this.queryFactory.query(PlanBlueprintTypeQuery.class).disableTracking().isActive(IsActive.Active).collect(), x -> ((PlanBlueprintTypeEntity) x).getCode(), x -> ((PlanBlueprintTypeEntity) x).getId(), x -> ((PlanBlueprintTypeEntity) x).getTenantId(), targetTenantId);
            this.analyzeCodeKeyed(bundle, context, report, TransferEntityType.PlanStatus,
                    x -> ((PlanStatusEntity) x).getId(), x -> ((PlanStatusEntity) x).getName(),
                    () -> this.queryFactory.query(PlanStatusQuery.class).disableTracking().isActives(IsActive.Active).collect(), x -> ((PlanStatusEntity) x).getName(), x -> ((PlanStatusEntity) x).getId(), x -> ((PlanStatusEntity) x).getTenantId(), targetTenantId);
            this.analyzeCodeKeyed(bundle, context, report, TransferEntityType.DescriptionStatus,
                    x -> ((DescriptionStatusEntity) x).getId(), x -> ((DescriptionStatusEntity) x).getName(),
                    () -> this.queryFactory.query(DescriptionStatusQuery.class).disableTracking().isActive(IsActive.Active).collect(), x -> ((DescriptionStatusEntity) x).getName(), x -> ((DescriptionStatusEntity) x).getId(), x -> ((DescriptionStatusEntity) x).getTenantId(), targetTenantId);

            this.analyzeWorkflows(bundle, context, report, targetTenantId);

            this.analyzeCodeKeyed(bundle, context, report, TransferEntityType.Language,
                    x -> ((LanguageEntity) x).getId(), x -> ((LanguageEntity) x).getCode(),
                    () -> this.queryFactory.query(LanguageQuery.class).disableTracking().isActive(IsActive.Active).collect(), x -> ((LanguageEntity) x).getCode(), x -> ((LanguageEntity) x).getId(), x -> ((LanguageEntity) x).getTenantId(), targetTenantId);
            this.analyzeCodeKeyed(bundle, context, report, TransferEntityType.SupportiveMaterial,
                    x -> ((SupportiveMaterialEntity) x).getId(), x -> ((SupportiveMaterialEntity) x).getType().name() + "::" + ((SupportiveMaterialEntity) x).getLanguageCode(),
                    () -> this.queryFactory.query(SupportiveMaterialQuery.class).disableTracking().isActive(IsActive.Active).collect(), x -> ((SupportiveMaterialEntity) x).getType().name() + "::" + ((SupportiveMaterialEntity) x).getLanguageCode(), x -> ((SupportiveMaterialEntity) x).getId(), x -> ((SupportiveMaterialEntity) x).getTenantId(), targetTenantId);

            this.analyzeUsers(bundle, context, report);
            this.analyzeStorageFiles(bundle, context, report);
            this.analyzeVersioned(bundle, context, report, TransferEntityType.DescriptionTemplate, targetTenantId);
            this.analyzeVersioned(bundle, context, report, TransferEntityType.PlanBlueprint, targetTenantId);
            this.analyzeTags(bundle, context, report, targetTenantId);
            this.analyzeReferences(bundle, context, report, targetTenantId);

            this.analyzePlain(bundle, context, report, TransferEntityType.Plan, x -> ((PlanEntity) x).getId(), ids -> this.queryFactory.query(PlanQuery.class).disableTracking().ids(ids).collect(), x -> ((PlanEntity) x).getId());
            this.analyzePlain(bundle, context, report, TransferEntityType.PlanUser, x -> ((PlanUserEntity) x).getId(), ids -> this.queryFactory.query(PlanUserQuery.class).disableTracking().ids(ids).collect(), x -> ((PlanUserEntity) x).getId());
            this.analyzePlain(bundle, context, report, TransferEntityType.PlanDescriptionTemplate, x -> ((PlanDescriptionTemplateEntity) x).getId(), ids -> this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().ids(ids).collect(), x -> ((PlanDescriptionTemplateEntity) x).getId());
            this.analyzePlain(bundle, context, report, TransferEntityType.PlanReference, x -> ((PlanReferenceEntity) x).getId(), ids -> this.queryFactory.query(PlanReferenceQuery.class).disableTracking().ids(ids).collect(), x -> ((PlanReferenceEntity) x).getId());
            this.analyzePlain(bundle, context, report, TransferEntityType.Description, x -> ((DescriptionEntity) x).getId(), ids -> this.queryFactory.query(DescriptionQuery.class).disableTracking().ids(ids).collect(), x -> ((DescriptionEntity) x).getId());
            this.analyzePlain(bundle, context, report, TransferEntityType.DescriptionReference, x -> ((DescriptionReferenceEntity) x).getId(), ids -> this.queryFactory.query(DescriptionReferenceQuery.class).disableTracking().ids(ids).collect(), x -> ((DescriptionReferenceEntity) x).getId());
            this.analyzePlain(bundle, context, report, TransferEntityType.DescriptionTag, x -> ((DescriptionTagEntity) x).getId(), ids -> this.queryFactory.query(DescriptionTagQuery.class).disableTracking().ids(ids).collect(), x -> ((DescriptionTagEntity) x).getId());
            this.analyzePlain(bundle, context, report, TransferEntityType.EntityDoi, x -> ((EntityDoiEntity) x).getId(), ids -> this.queryFactory.query(EntityDoiQuery.class).disableTracking().ids(ids).collect(), x -> ((EntityDoiEntity) x).getId());
            this.analyzePlain(bundle, context, report, TransferEntityType.UserDescriptionTemplate, x -> ((UserDescriptionTemplateEntity) x).getId(), ids -> this.queryFactory.query(UserDescriptionTemplateQuery.class).disableTracking().ids(ids).collect(), x -> ((UserDescriptionTemplateEntity) x).getId());
            this.analyzePlain(bundle, context, report, TransferEntityType.UserPlanBlueprint, x -> ((UserPlanBlueprintEntity) x).getId(), ids -> this.queryFactory.query(UserPlanBlueprintQuery.class).disableTracking().ids(ids).collect(), x -> ((UserPlanBlueprintEntity) x).getId());

            Map<String, String> remapStrings = new HashMap<>();
            for (Map.Entry<UUID, UUID> entry : context.idRemap.entrySet()) {
                remapStrings.put(entry.getKey().toString(), entry.getValue().toString());
            }

            if (apply) {
                this.applyCreates(context, remapStrings, targetTenantId, entityManager);
                this.ensureTenantUsers(context, targetTenantId, entityManager);
                entityManager.flush();
                this.writeStorageFilePayloads(bundle, context);
                this.linkUsersToTenantInKeycloak(context, targetTenantId, report);
                this.publishPostImportEvents(context, report);
            }
            this.importAnnotations(bundle, report, remapStrings, apply);
            if (apply) {
                report.getWarnings().add("Run the accounting/KPI recalculation tasks from the Maintenance page to refresh usage metrics.");
                if (!this.elasticEnabledSafe()) report.getWarnings().add("Elasticsearch is disabled or unreachable; run the Maintenance index rebuild after enabling it.");
            }
            return report;
        } finally {
            entityManager.reloadTenantFilters();
        }
    }

    private ParsedBundle parseBundle(byte[] data) throws IOException {
        ParsedBundle bundle = new ParsedBundle();
        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                entries.put(entry.getName(), zip.readAllBytes());
            }
        }
        byte[] manifestBytes = entries.get(MANIFEST_ENTRY);
        if (manifestBytes == null) throw new MyValidationException(this.errors.getTransferBundleInvalid().getCode(), this.errors.getTransferBundleInvalid().getMessage());
        bundle.manifest = this.jsonHandlingService.fromJsonSafe(TransferBundleManifest.class, new String(manifestBytes, StandardCharsets.UTF_8));
        if (bundle.manifest == null) throw new MyValidationException(this.errors.getTransferBundleInvalid().getCode(), this.errors.getTransferBundleInvalid().getMessage());

        for (TransferEntityType type : TransferEntityType.values()) {
            byte[] entryBytes = entries.get(type.bundleEntryName());
            if (entryBytes == null) continue;
            Object[] items = (Object[]) this.jsonHandlingService.fromJsonSafe(type.getArrayClass(), new String(entryBytes, StandardCharsets.UTF_8));
            if (items == null) throw new MyValidationException(this.errors.getTransferBundleInvalid().getCode(), this.errors.getTransferBundleInvalid().getMessage());
            bundle.entities.put(type, new ArrayList<>(Arrays.asList(items)));
        }
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            if (entry.getKey().startsWith(FILES_PREFIX)) bundle.files.put(entry.getKey().substring(FILES_PREFIX.length()), entry.getValue());
        }
        bundle.annotationsJson = entries.get(ANNOTATIONS_ENTRY);
        return bundle;
    }

    private void validateManifest(ParsedBundle bundle) {
        if (bundle.manifest.getBundleFormatVersion() != TransferBundleManifest.CURRENT_FORMAT_VERSION)
            throw new MyValidationException(this.errors.getTransferBundleInvalid().getCode(), this.errors.getTransferBundleInvalid().getMessage());
        String targetDbVersion = this.currentDbVersion();
        String sourceDbVersion = bundle.manifest.getSourceDbVersion();
        if (targetDbVersion != null && sourceDbVersion != null && !targetDbVersion.equals(sourceDbVersion))
            throw new MyValidationException(this.errors.getTransferDbVersionMismatch().getCode(),
                    this.errors.getTransferDbVersionMismatch().getMessage() + " (bundle: " + sourceDbVersion + ", installation: " + targetDbVersion + ")");
    }

    private boolean visibleToTenant(UUID rowTenantId, UUID targetTenantId) {
        return rowTenantId == null || rowTenantId.equals(targetTenantId);
    }

    /**
     * Platform/config entities: match by id first, then by business key against rows visible to the
     * target tenant (own rows + shared tenant-null rows); otherwise create preserving the source id.
     */
    private void analyzeCodeKeyed(ParsedBundle bundle, ImportContext context, TransferImportReport report, TransferEntityType type,
                                  java.util.function.Function<Object, UUID> idOf, java.util.function.Function<Object, String> keyOf,
                                  java.util.function.Supplier<List<?>> existingSupplier, java.util.function.Function<Object, String> existingKeyOf,
                                  java.util.function.Function<Object, UUID> existingIdOf, java.util.function.Function<Object, UUID> existingTenantOf,
                                  UUID targetTenantId) {
        List<Object> items = bundle.list(type);
        if (items.isEmpty()) return;
        TransferImportReport.TypeReport typeReport = report.typeReport(type);
        typeReport.setTotal(items.size());

        Map<UUID, Object> existingById = new HashMap<>();
        Map<String, UUID> existingByKey = new HashMap<>();
        for (Object existing : existingSupplier.get()) {
            existingById.put(existingIdOf.apply(existing), existing);
            String key = existingKeyOf.apply(existing);
            if (key != null && this.visibleToTenant(existingTenantOf.apply(existing), targetTenantId))
                existingByKey.putIfAbsent(key.trim().toLowerCase(Locale.ROOT), existingIdOf.apply(existing));
        }

        for (Object item : items) {
            UUID id = idOf.apply(item);
            if (existingById.containsKey(id)) {
                typeReport.setMatchedById(typeReport.getMatchedById() + 1);
                continue;
            }
            String key = keyOf.apply(item);
            UUID matched = key == null ? null : existingByKey.get(key.trim().toLowerCase(Locale.ROOT));
            if (matched != null) {
                context.idRemap.put(id, matched);
                typeReport.setMatchedByKey(typeReport.getMatchedByKey() + 1);
            } else {
                context.create(type, item);
                typeReport.setToCreate(typeReport.getToCreate() + 1);
            }
        }
    }

    /**
     * Workflows: a tenant resolves its workflow as "own row, else shared default". If the target tenant
     * already resolves to a workflow, importing another would shadow or duplicate it - skip with a warning.
     */
    private void analyzeWorkflows(ParsedBundle bundle, ImportContext context, TransferImportReport report, UUID targetTenantId) {
        this.analyzeWorkflow(bundle, context, report, TransferEntityType.PlanWorkflow,
                this.queryFactory.query(PlanWorkflowQuery.class).disableTracking().isActives(IsActive.Active).collect().stream().anyMatch(x -> this.visibleToTenant(x.getTenantId(), targetTenantId)),
                x -> ((PlanWorkflowEntity) x).getId());
        this.analyzeWorkflow(bundle, context, report, TransferEntityType.DescriptionWorkflow,
                this.queryFactory.query(DescriptionWorkflowQuery.class).disableTracking().isActives(IsActive.Active).collect().stream().anyMatch(x -> this.visibleToTenant(x.getTenantId(), targetTenantId)),
                x -> ((DescriptionWorkflowEntity) x).getId());
    }

    private void analyzeWorkflow(ParsedBundle bundle, ImportContext context, TransferImportReport report, TransferEntityType type, boolean targetHasWorkflow, java.util.function.Function<Object, UUID> idOf) {
        List<Object> items = bundle.list(type);
        if (items.isEmpty()) return;
        TransferImportReport.TypeReport typeReport = report.typeReport(type);
        typeReport.setTotal(items.size());
        if (targetHasWorkflow) {
            typeReport.setMatchedByKey(items.size());
            typeReport.getMessages().add("Target tenant already resolves a workflow; the bundled workflow was skipped. Review status transitions manually.");
            return;
        }
        for (Object item : items) {
            context.create(type, item);
            typeReport.setToCreate(typeReport.getToCreate() + 1);
        }
    }

    /**
     * Users are global and matched by email; the Keycloak credential attaches on first login.
     */
    private void analyzeUsers(ParsedBundle bundle, ImportContext context, TransferImportReport report) {
        List<Object> users = bundle.list(TransferEntityType.User);
        if (users.isEmpty()) return;
        TransferImportReport.TypeReport userReport = report.typeReport(TransferEntityType.User);
        userReport.setTotal(users.size());
        TransferImportReport.TypeReport contactReport = report.typeReport(TransferEntityType.UserContactInfo);
        contactReport.setTotal(bundle.list(TransferEntityType.UserContactInfo).size());

        Map<UUID, List<UserContactInfoEntity>> bundleContactsByUser = new HashMap<>();
        for (Object item : bundle.list(TransferEntityType.UserContactInfo)) {
            UserContactInfoEntity contact = (UserContactInfoEntity) item;
            bundleContactsByUser.computeIfAbsent(contact.getUserId(), u -> new ArrayList<>()).add(contact);
        }

        Set<String> allEmails = new HashSet<>();
        for (List<UserContactInfoEntity> contacts : bundleContactsByUser.values()) {
            for (UserContactInfoEntity contact : contacts) {
                if (ContactInfoType.Email.equals(contact.getType()) && contact.getValue() != null) allEmails.add(contact.getValue().trim().toLowerCase(Locale.ROOT));
            }
        }
        Map<String, List<UserContactInfoEntity>> targetContactsByEmail = new HashMap<>();
        if (!allEmails.isEmpty()) {
            for (List<String> batch : this.partitionStrings(allEmails)) {
                for (UserContactInfoEntity contact : this.queryFactory.query(UserContactInfoQuery.class).disableTracking().types(ContactInfoType.Email).values(batch).collect()) {
                    if (contact.getValue() == null) continue;
                    targetContactsByEmail.computeIfAbsent(contact.getValue().trim().toLowerCase(Locale.ROOT), e -> new ArrayList<>()).add(contact);
                }
            }
        }

        Set<UUID> existingUserIds = new HashSet<>();
        for (List<UUID> batch : this.partition(users.stream().map(x -> ((UserEntity) x).getId()).toList())) {
            this.queryFactory.query(UserQuery.class).disableTracking().ids(batch).collect().forEach(x -> existingUserIds.add(x.getId()));
        }
        Set<UUID> existingContactIds = new HashSet<>();
        List<UUID> bundleContactIds = bundle.list(TransferEntityType.UserContactInfo).stream().map(x -> ((UserContactInfoEntity) x).getId()).toList();
        if (!bundleContactIds.isEmpty()) {
            for (List<UUID> batch : this.partition(bundleContactIds)) {
                this.queryFactory.query(UserContactInfoQuery.class).disableTracking().ids(batch).collect().forEach(x -> existingContactIds.add(x.getId()));
            }
        }

        for (Object item : users) {
            UserEntity user = (UserEntity) item;
            List<UserContactInfoEntity> contacts = bundleContactsByUser.getOrDefault(user.getId(), Collections.emptyList());

            UUID matchedByEmail = null;
            for (UserContactInfoEntity contact : contacts) {
                if (!ContactInfoType.Email.equals(contact.getType()) || contact.getValue() == null) continue;
                List<UserContactInfoEntity> targetContacts = targetContactsByEmail.get(contact.getValue().trim().toLowerCase(Locale.ROOT));
                if (targetContacts == null || targetContacts.isEmpty()) continue;
                if (targetContacts.stream().map(UserContactInfoEntity::getUserId).distinct().count() > 1) {
                    userReport.setConflicts(userReport.getConflicts() + 1);
                    userReport.getMessages().add("Email " + contact.getValue() + " belongs to more than one user on this installation; matched the oldest account.");
                }
                matchedByEmail = targetContacts.stream()
                        .sorted(Comparator.comparing(UserContactInfoEntity::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(UserContactInfoEntity::getUserId).findFirst().orElse(null);
                if (matchedByEmail != null) break;
            }

            if (matchedByEmail != null) {
                if (!matchedByEmail.equals(user.getId())) context.idRemap.put(user.getId(), matchedByEmail);
                context.matchedUserIds.add(matchedByEmail);
                userReport.setMatchedByKey(userReport.getMatchedByKey() + 1);
                contactReport.setMatchedByKey(contactReport.getMatchedByKey() + contacts.size());
            } else if (existingUserIds.contains(user.getId())) {
                context.matchedUserIds.add(user.getId());
                userReport.setMatchedById(userReport.getMatchedById() + 1);
                contactReport.setMatchedById(contactReport.getMatchedById() + contacts.size());
            } else {
                if (contacts.stream().noneMatch(c -> ContactInfoType.Email.equals(c.getType()) && c.getValue() != null && !c.getValue().isBlank())) {
                    userReport.setConflicts(userReport.getConflicts() + 1);
                    userReport.getMessages().add("User '" + user.getName() + "' has no email contact; the account is created but cannot be claimed at login until an email is set.");
                }
                context.create(TransferEntityType.User, user);
                context.createdUserIds.add(user.getId());
                userReport.setToCreate(userReport.getToCreate() + 1);
                for (UserContactInfoEntity contact : contacts) {
                    if (existingContactIds.contains(contact.getId())) contact.setId(UUID.randomUUID());
                    context.create(TransferEntityType.UserContactInfo, contact);
                    contactReport.setToCreate(contactReport.getToCreate() + 1);
                }
            }
        }
    }

    private void analyzeStorageFiles(ParsedBundle bundle, ImportContext context, TransferImportReport report) {
        List<Object> items = bundle.list(TransferEntityType.StorageFile);
        if (items.isEmpty()) return;
        TransferImportReport.TypeReport typeReport = report.typeReport(TransferEntityType.StorageFile);
        typeReport.setTotal(items.size());

        Set<UUID> existingIds = new HashSet<>();
        for (List<UUID> batch : this.partition(items.stream().map(x -> ((StorageFileEntity) x).getId()).toList())) {
            this.queryFactory.query(StorageFileQuery.class).disableTracking().ids(batch).collect().forEach(x -> existingIds.add(x.getId()));
        }
        for (Object item : items) {
            StorageFileEntity storageFile = (StorageFileEntity) item;
            if (existingIds.contains(storageFile.getId())) {
                typeReport.setMatchedById(typeReport.getMatchedById() + 1);
                continue;
            }
            if (this.storageFileService.fileRefExists(storageFile.getFileRef(), storageFile.getStorageType())) {
                context.fileRefRemap.put(storageFile.getFileRef(), UUID.randomUUID().toString().replace("-", ""));
            }
            context.create(TransferEntityType.StorageFile, storageFile);
            typeReport.setToCreate(typeReport.getToCreate() + 1);
        }
    }

    /**
     * Blueprints and description templates preserve their ids and group ids. A code collision with a
     * different group on the target is resolved by renaming the incoming code - the code is only a
     * human-facing identity, every DB relation uses the preserved uuids.
     */
    private void analyzeVersioned(ParsedBundle bundle, ImportContext context, TransferImportReport report, TransferEntityType type, UUID targetTenantId) {
        List<Object> items = bundle.list(type);
        if (items.isEmpty()) return;
        TransferImportReport.TypeReport typeReport = report.typeReport(type);
        typeReport.setTotal(items.size());

        boolean isTemplate = type == TransferEntityType.DescriptionTemplate;
        Set<UUID> existingIds = new HashSet<>();
        Map<String, UUID> existingCodeToGroup = new HashMap<>();
        List<UUID> bundleIds = items.stream().map(x -> isTemplate ? ((DescriptionTemplateEntity) x).getId() : ((PlanBlueprintEntity) x).getId()).toList();
        Set<String> bundleCodes = new HashSet<>();
        for (Object item : items) {
            String code = isTemplate ? ((DescriptionTemplateEntity) item).getCode() : ((PlanBlueprintEntity) item).getCode();
            if (code != null) bundleCodes.add(code);
        }
        for (List<UUID> batch : this.partition(bundleIds)) {
            if (isTemplate) this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().ids(batch).collect().forEach(x -> existingIds.add(x.getId()));
            else this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().ids(batch).collect().forEach(x -> existingIds.add(x.getId()));
        }
        if (!bundleCodes.isEmpty()) {
            for (List<String> batch : this.partitionStrings(bundleCodes)) {
                if (isTemplate) this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().codes(batch).collect()
                        .forEach(x -> { if (this.visibleToTenant(x.getTenantId(), targetTenantId) && x.getCode() != null) existingCodeToGroup.putIfAbsent(x.getCode(), x.getGroupId()); });
                else this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().codes(batch).collect()
                        .forEach(x -> { if (this.visibleToTenant(x.getTenantId(), targetTenantId) && x.getCode() != null) existingCodeToGroup.putIfAbsent(x.getCode(), x.getGroupId()); });
            }
        }

        Map<UUID, String> groupRenames = new HashMap<>();
        for (Object item : items) {
            UUID id = isTemplate ? ((DescriptionTemplateEntity) item).getId() : ((PlanBlueprintEntity) item).getId();
            UUID groupId = isTemplate ? ((DescriptionTemplateEntity) item).getGroupId() : ((PlanBlueprintEntity) item).getGroupId();
            String code = isTemplate ? ((DescriptionTemplateEntity) item).getCode() : ((PlanBlueprintEntity) item).getCode();
            if (existingIds.contains(id)) {
                typeReport.setMatchedById(typeReport.getMatchedById() + 1);
                continue;
            }
            UUID targetGroupOfCode = code == null ? null : existingCodeToGroup.get(code);
            if (targetGroupOfCode != null && !targetGroupOfCode.equals(groupId)) {
                String renamed = groupRenames.computeIfAbsent(groupId, g -> code + "-imported-" + UUID.randomUUID().toString().substring(0, 8));
                if (isTemplate) ((DescriptionTemplateEntity) item).setCode(renamed);
                else ((PlanBlueprintEntity) item).setCode(renamed);
                typeReport.setConflicts(typeReport.getConflicts() + 1);
                typeReport.getMessages().add("Code '" + code + "' already exists on this installation for different content; imported as '" + renamed + "'.");
            }
            context.create(type, item);
            typeReport.setToCreate(typeReport.getToCreate() + 1);
        }
    }

    private void analyzeTags(ParsedBundle bundle, ImportContext context, TransferImportReport report, UUID targetTenantId) {
        List<Object> items = bundle.list(TransferEntityType.Tag);
        if (items.isEmpty()) return;
        TransferImportReport.TypeReport typeReport = report.typeReport(TransferEntityType.Tag);
        typeReport.setTotal(items.size());

        Set<UUID> existingIds = new HashSet<>();
        Map<String, UUID> existingByLabel = new HashMap<>();
        for (List<UUID> batch : this.partition(items.stream().map(x -> ((TagEntity) x).getId()).toList())) {
            this.queryFactory.query(TagQuery.class).disableTracking().ids(batch).collect().forEach(x -> existingIds.add(x.getId()));
        }
        Set<String> labels = new HashSet<>();
        for (Object item : items) if (((TagEntity) item).getLabel() != null) labels.add(((TagEntity) item).getLabel());
        if (!labels.isEmpty()) {
            for (List<String> batch : this.partitionStrings(labels)) {
                this.queryFactory.query(TagQuery.class).disableTracking().tags(batch).isActive(IsActive.Active).collect()
                        .forEach(x -> { if (this.visibleToTenant(x.getTenantId(), targetTenantId) && x.getLabel() != null) existingByLabel.putIfAbsent(x.getLabel().trim().toLowerCase(Locale.ROOT), x.getId()); });
            }
        }
        for (Object item : items) {
            TagEntity tag = (TagEntity) item;
            if (existingIds.contains(tag.getId())) {
                typeReport.setMatchedById(typeReport.getMatchedById() + 1);
                continue;
            }
            UUID matched = tag.getLabel() == null ? null : existingByLabel.get(tag.getLabel().trim().toLowerCase(Locale.ROOT));
            if (matched != null) {
                context.idRemap.put(tag.getId(), matched);
                typeReport.setMatchedByKey(typeReport.getMatchedByKey() + 1);
            } else {
                context.create(TransferEntityType.Tag, tag);
                typeReport.setToCreate(typeReport.getToCreate() + 1);
            }
        }
    }

    private void analyzeReferences(ParsedBundle bundle, ImportContext context, TransferImportReport report, UUID targetTenantId) {
        List<Object> items = bundle.list(TransferEntityType.Reference);
        if (items.isEmpty()) return;
        TransferImportReport.TypeReport typeReport = report.typeReport(TransferEntityType.Reference);
        typeReport.setTotal(items.size());

        Set<UUID> existingIds = new HashSet<>();
        for (List<UUID> batch : this.partition(items.stream().map(x -> ((ReferenceEntity) x).getId()).toList())) {
            this.queryFactory.query(ReferenceQuery.class).disableTracking().ids(batch).collect().forEach(x -> existingIds.add(x.getId()));
        }
        Set<String> referenceValues = new HashSet<>();
        for (Object item : items) if (((ReferenceEntity) item).getReference() != null) referenceValues.add(((ReferenceEntity) item).getReference());
        Map<String, UUID> existingByKey = new HashMap<>();
        if (!referenceValues.isEmpty()) {
            for (List<String> batch : this.partitionStrings(referenceValues)) {
                this.queryFactory.query(ReferenceQuery.class).disableTracking().references(batch).isActive(IsActive.Active).collect()
                        .forEach(x -> { if (this.visibleToTenant(x.getTenantId(), targetTenantId)) existingByKey.putIfAbsent(this.referenceKey(x.getTypeId(), x.getReference(), x.getSource()), x.getId()); });
            }
        }
        for (Object item : items) {
            ReferenceEntity reference = (ReferenceEntity) item;
            if (existingIds.contains(reference.getId())) {
                typeReport.setMatchedById(typeReport.getMatchedById() + 1);
                continue;
            }
            UUID matched = existingByKey.get(this.referenceKey(context.remap(reference.getTypeId()), reference.getReference(), reference.getSource()));
            if (matched != null) {
                context.idRemap.put(reference.getId(), matched);
                typeReport.setMatchedByKey(typeReport.getMatchedByKey() + 1);
            } else {
                context.create(TransferEntityType.Reference, reference);
                typeReport.setToCreate(typeReport.getToCreate() + 1);
            }
        }
    }

    private String referenceKey(UUID typeId, String reference, String source) {
        return (typeId == null ? "" : typeId.toString()) + "::" + (reference == null ? "" : reference.trim().toLowerCase(Locale.ROOT)) + "::" + (source == null ? "" : source.trim().toLowerCase(Locale.ROOT));
    }

    private void analyzePlain(ParsedBundle bundle, ImportContext context, TransferImportReport report, TransferEntityType type,
                              java.util.function.Function<Object, UUID> idOf, java.util.function.Function<List<UUID>, List<?>> existingByIds,
                              java.util.function.Function<Object, UUID> existingIdOf) {
        List<Object> items = bundle.list(type);
        if (items.isEmpty()) return;
        TransferImportReport.TypeReport typeReport = report.typeReport(type);
        typeReport.setTotal(items.size());
        Set<UUID> existingIds = new HashSet<>();
        for (List<UUID> batch : this.partition(items.stream().map(idOf).toList())) {
            existingByIds.apply(batch).forEach(x -> existingIds.add(existingIdOf.apply(x)));
        }
        for (Object item : items) {
            if (existingIds.contains(idOf.apply(item))) {
                typeReport.setMatchedById(typeReport.getMatchedById() + 1);
            } else {
                context.create(type, item);
                typeReport.setToCreate(typeReport.getToCreate() + 1);
                if (type == TransferEntityType.Plan) context.createdPlanIds.add(idOf.apply(item));
                if (type == TransferEntityType.Description) context.createdDescriptionIds.add(idOf.apply(item));
            }
        }
    }

    private void applyCreates(ImportContext context, Map<String, String> remapStrings, UUID targetTenantId, TenantEntityManager entityManager) throws InvalidApplicationException {
        for (TransferEntityType type : TransferEntityType.values()) {
            int flushCounter = 0;
            for (Object item : context.creates(type)) {
                this.prepareForInsert(type, item, context, remapStrings, targetTenantId);
                entityManager.persist(item);
                if (++flushCounter % FLUSH_BATCH_SIZE == 0) entityManager.flush();
            }
            entityManager.flush();
        }
    }

    private void prepareForInsert(TransferEntityType type, Object item, ImportContext context, Map<String, String> remapStrings, UUID targetTenantId) {
        switch (type) {
            case ReferenceType -> ((ReferenceTypeEntity) item).setDefinition(this.applyRemap(((ReferenceTypeEntity) item).getDefinition(), remapStrings));
            case PrefillingSource -> ((PrefillingSourceEntity) item).setDefinition(this.applyRemap(((PrefillingSourceEntity) item).getDefinition(), remapStrings));
            case PlanStatus -> ((PlanStatusEntity) item).setDefinition(this.applyRemap(((PlanStatusEntity) item).getDefinition(), remapStrings));
            case DescriptionStatus -> ((DescriptionStatusEntity) item).setDefinition(this.applyRemap(((DescriptionStatusEntity) item).getDefinition(), remapStrings));
            case PlanWorkflow -> ((PlanWorkflowEntity) item).setDefinition(this.applyRemap(((PlanWorkflowEntity) item).getDefinition(), remapStrings));
            case DescriptionWorkflow -> ((DescriptionWorkflowEntity) item).setDefinition(this.applyRemap(((DescriptionWorkflowEntity) item).getDefinition(), remapStrings));
            case Language, SupportiveMaterial, DescriptionTemplateType, PlanBlueprintType -> {
            }
            case User -> ((UserEntity) item).setAdditionalInfo(this.applyRemap(((UserEntity) item).getAdditionalInfo(), remapStrings));
            case UserContactInfo -> {
                UserContactInfoEntity contact = (UserContactInfoEntity) item;
                contact.setUserId(context.remap(contact.getUserId()));
            }
            case StorageFile -> {
                StorageFileEntity storageFile = (StorageFileEntity) item;
                storageFile.setOwnerId(context.remap(storageFile.getOwnerId()));
                String newRef = context.fileRefRemap.get(storageFile.getFileRef());
                if (newRef != null) storageFile.setFileRef(newRef);
            }
            case DescriptionTemplate -> {
                DescriptionTemplateEntity template = (DescriptionTemplateEntity) item;
                template.setTypeId(context.remap(template.getTypeId()));
                template.setGroupId(context.remap(template.getGroupId()));
                template.setDefinition(this.applyRemap(template.getDefinition(), remapStrings));
            }
            case PlanBlueprint -> {
                PlanBlueprintEntity blueprint = (PlanBlueprintEntity) item;
                blueprint.setTypeId(context.remap(blueprint.getTypeId()));
                blueprint.setGroupId(context.remap(blueprint.getGroupId()));
                blueprint.setDefinition(this.applyRemap(blueprint.getDefinition(), remapStrings));
            }
            case Tag -> ((TagEntity) item).setCreatedById(context.remap(((TagEntity) item).getCreatedById()));
            case Reference -> {
                ReferenceEntity reference = (ReferenceEntity) item;
                reference.setTypeId(context.remap(reference.getTypeId()));
                reference.setCreatedById(context.remap(reference.getCreatedById()));
                reference.setDefinition(this.applyRemap(reference.getDefinition(), remapStrings));
            }
            case Plan -> {
                PlanEntity plan = (PlanEntity) item;
                plan.setStatusId(context.remap(plan.getStatusId()));
                plan.setCreatorId(context.remap(plan.getCreatorId()));
                plan.setBlueprintId(context.remap(plan.getBlueprintId()));
                plan.setGroupId(context.remap(plan.getGroupId()));
                plan.setProperties(this.applyRemap(plan.getProperties(), remapStrings));
            }
            case PlanUser -> {
                PlanUserEntity planUser = (PlanUserEntity) item;
                planUser.setPlanId(context.remap(planUser.getPlanId()));
                planUser.setUserId(context.remap(planUser.getUserId()));
            }
            case PlanDescriptionTemplate -> {
                PlanDescriptionTemplateEntity pdt = (PlanDescriptionTemplateEntity) item;
                pdt.setPlanId(context.remap(pdt.getPlanId()));
                pdt.setDescriptionTemplateGroupId(context.remap(pdt.getDescriptionTemplateGroupId()));
            }
            case PlanReference -> {
                PlanReferenceEntity planReference = (PlanReferenceEntity) item;
                planReference.setPlanId(context.remap(planReference.getPlanId()));
                planReference.setReferenceId(context.remap(planReference.getReferenceId()));
                planReference.setData(this.applyRemap(planReference.getData(), remapStrings));
            }
            case Description -> {
                DescriptionEntity description = (DescriptionEntity) item;
                description.setStatusId(context.remap(description.getStatusId()));
                description.setCreatedById(context.remap(description.getCreatedById()));
                description.setPlanId(context.remap(description.getPlanId()));
                description.setPlanDescriptionTemplateId(context.remap(description.getPlanDescriptionTemplateId()));
                description.setDescriptionTemplateId(context.remap(description.getDescriptionTemplateId()));
                description.setProperties(this.applyRemap(description.getProperties(), remapStrings));
            }
            case DescriptionReference -> {
                DescriptionReferenceEntity descriptionReference = (DescriptionReferenceEntity) item;
                descriptionReference.setDescriptionId(context.remap(descriptionReference.getDescriptionId()));
                descriptionReference.setReferenceId(context.remap(descriptionReference.getReferenceId()));
                descriptionReference.setData(this.applyRemap(descriptionReference.getData(), remapStrings));
            }
            case DescriptionTag -> {
                DescriptionTagEntity descriptionTag = (DescriptionTagEntity) item;
                descriptionTag.setDescriptionId(context.remap(descriptionTag.getDescriptionId()));
                descriptionTag.setTagId(context.remap(descriptionTag.getTagId()));
            }
            case EntityDoi -> ((EntityDoiEntity) item).setEntityId(context.remap(((EntityDoiEntity) item).getEntityId()));
            case UserDescriptionTemplate -> {
                UserDescriptionTemplateEntity userTemplate = (UserDescriptionTemplateEntity) item;
                userTemplate.setUserId(context.remap(userTemplate.getUserId()));
                userTemplate.setDescriptionTemplateId(context.remap(userTemplate.getDescriptionTemplateId()));
            }
            case UserPlanBlueprint -> {
                UserPlanBlueprintEntity userBlueprint = (UserPlanBlueprintEntity) item;
                userBlueprint.setUserId(context.remap(userBlueprint.getUserId()));
                userBlueprint.setPlanBlueprintId(context.remap(userBlueprint.getPlanBlueprintId()));
            }
        }
        if (item instanceof TenantScopedBaseEntity tenantScoped) {
            tenantScoped.setTenantId(targetTenantId);
        }
    }

    /**
     * Single pass over the blob: every uuid-shaped token is looked up in the remap once, so the
     * cost stays linear in the blob size no matter how many ids were remapped during analysis.
     */
    private String applyRemap(String blob, Map<String, String> remapStrings) {
        if (blob == null || blob.isEmpty() || remapStrings.isEmpty()) return blob;
        Matcher matcher = UUID_PATTERN.matcher(blob);
        StringBuilder result = null;
        int last = 0;
        while (matcher.find()) {
            String replacement = remapStrings.get(matcher.group().toLowerCase(Locale.ROOT));
            if (replacement == null) continue;
            if (result == null) result = new StringBuilder(blob.length());
            result.append(blob, last, matcher.start()).append(replacement);
            last = matcher.end();
        }
        if (result == null) return blob;
        result.append(blob, last, blob.length());
        return result.toString();
    }

    /**
     * Sends the bundled annotations to the annotation service transfer endpoint after applying the
     * id remap (uuids in the raw json - entity ids, author ids, mention tokens - are string-replaced).
     * Failures never abort the main import: rerunning the same bundle retries annotations idempotently.
     */
    private void importAnnotations(ParsedBundle bundle, TransferImportReport report, Map<String, String> remapStrings, boolean apply) {
        if (bundle.annotationsJson == null) return;
        if (!this.annotationTransferClient.isConfigured()) {
            report.getWarnings().add("The bundle contains annotations but no annotation service url is configured (tenant-transfer.annotation-service-url); comments were not imported.");
            return;
        }
        try {
            String payload = this.applyRemap(new String(bundle.annotationsJson, StandardCharsets.UTF_8), remapStrings);
            String response = this.annotationTransferClient.post("/api/annotation/transfer/import?dryRun=" + !apply, payload);
            AnnotationTransferReportModel annotationReport = this.jsonHandlingService.fromJsonSafe(AnnotationTransferReportModel.class, response);
            if (annotationReport == null) {
                report.getWarnings().add("The annotation service returned an unreadable transfer report; verify the annotations manually.");
                return;
            }
            this.mergeAnnotationCounts(report, "Annotation", annotationReport.getAnnotations());
            this.mergeAnnotationCounts(report, "AnnotationStatus", annotationReport.getAnnotationStatuses());
            this.mergeAnnotationCounts(report, "AnnotationStatusDefinition", annotationReport.getStatuses());
        } catch (Exception e) {
            logger.error("failed to import annotations from transfer bundle", e);
            report.getWarnings().add("Importing annotations failed (" + e.getMessage() + "); the rest of the bundle was " + (apply ? "imported" : "analyzed") + ". Re-running the import on the same bundle will retry the annotations.");
        }
    }

    private void mergeAnnotationCounts(TransferImportReport report, String label, AnnotationTransferReportModel.Counts counts) {
        if (counts == null || counts.getTotal() == 0) return;
        TransferImportReport.TypeReport typeReport = new TransferImportReport.TypeReport();
        typeReport.setEntityType(label);
        typeReport.setTotal(counts.getTotal());
        typeReport.setToCreate(counts.getCreated());
        typeReport.setMatchedById(counts.getExisting());
        typeReport.setMatchedByKey(counts.getMatchedByKey());
        report.getTypes().add(typeReport);
    }

    private void ensureTenantUsers(ImportContext context, UUID targetTenantId, TenantEntityManager entityManager) {
        if (targetTenantId == null) return;
        Set<UUID> allUserIds = this.allBundleUserIds(context);
        if (allUserIds.isEmpty()) return;
        Set<UUID> alreadyMembers = new HashSet<>();
        for (List<UUID> batch : this.partition(allUserIds)) {
            // tracked on purpose: a previously removed (inactive) membership is reactivated instead
            // of a second row being created for the same user/tenant pair
            for (TenantUserEntity existing : this.queryFactory.query(TenantUserQuery.class).userIds(batch).tenantIds(targetTenantId).collect()) {
                alreadyMembers.add(existing.getUserId());
                if (!IsActive.Active.equals(existing.getIsActive())) {
                    existing.setIsActive(IsActive.Active);
                    existing.setUpdatedAt(Instant.now());
                }
            }
        }
        for (UUID userId : allUserIds) {
            if (alreadyMembers.contains(userId)) continue;
            TenantUserEntity tenantUser = new TenantUserEntity();
            tenantUser.setId(UUID.randomUUID());
            tenantUser.setUserId(userId);
            tenantUser.setTenantId(targetTenantId);
            tenantUser.setIsActive(IsActive.Active);
            tenantUser.setCreatedAt(Instant.now());
            tenantUser.setUpdatedAt(Instant.now());
            entityManager.persist(tenantUser);
        }
    }

    private Set<UUID> allBundleUserIds(ImportContext context) {
        Set<UUID> allUserIds = new HashSet<>();
        allUserIds.addAll(context.createdUserIds);
        allUserIds.addAll(context.matchedUserIds);
        return allUserIds;
    }

    /**
     * The tenant gate is the token claim coming from Keycloak tenant group membership - the
     * TenantUser row alone does not let a user enter the tenant. Users that already have a login on
     * this installation are added to the tenant role group here; users created by the import have no
     * credential yet, so they cannot be added until they log in for the first time.
     */
    private void linkUsersToTenantInKeycloak(ImportContext context, UUID targetTenantId, TransferImportReport report) {
        if (targetTenantId == null) return;
        Set<UUID> allUserIds = this.allBundleUserIds(context);
        if (allUserIds.isEmpty()) return;

        String tenantCode;
        try {
            tenantCode = this.tenantScopeFactory.getInstance().getTenantCode();
        } catch (InvalidApplicationException e) {
            throw new MyApplicationException("could not resolve tenant code for keycloak linking", e);
        }
        String tenantUserRole = this.authorizationConfiguration.getAuthorizationProperties().getTenantUserRole();

        Set<UUID> usersWithCredential = new HashSet<>();
        int linkFailures = 0;
        for (List<UUID> batch : this.partition(allUserIds)) {
            for (UserCredentialEntity credential : this.queryFactory.query(UserCredentialQuery.class).disableTracking().userIds(batch).collect()) {
                if (credential.getExternalId() == null || credential.getExternalId().isBlank()) continue;
                usersWithCredential.add(credential.getUserId());
                try {
                    this.keycloakService.addUserToTenantRoleGroup(credential.getExternalId(), tenantCode, tenantUserRole);
                } catch (Exception e) {
                    linkFailures++;
                    logger.error("failed to add user " + credential.getUserId() + " to keycloak tenant group " + tenantCode, e);
                }
            }
        }
        if (linkFailures > 0)
            report.getWarnings().add(linkFailures + " user(s) could not be added to the tenant's Keycloak group; grant them tenant access from Tenant Users.");
        long withoutCredential = allUserIds.stream().filter(x -> !usersWithCredential.contains(x)).count();
        if (withoutCredential > 0)
            report.getWarnings().add(withoutCredential + " imported user(s) have never logged into this installation; after their first login, grant them tenant access from Tenant Users or re-run this import to link them automatically.");
    }

    private void writeStorageFilePayloads(ParsedBundle bundle, ImportContext context) {
        for (Object item : context.creates(TransferEntityType.StorageFile)) {
            StorageFileEntity storageFile = (StorageFileEntity) item;
            String originalRef = storageFile.getFileRef();
            for (Map.Entry<String, String> entry : context.fileRefRemap.entrySet()) {
                if (entry.getValue().equals(originalRef)) {
                    originalRef = entry.getKey();
                    break;
                }
            }
            byte[] payload = bundle.files.get(originalRef);
            if (payload == null) continue;
            try {
                this.storageFileService.writeBytesByFileRef(storageFile.getFileRef(), storageFile.getStorageType(), payload);
            } catch (Exception e) {
                logger.error("failed to write transferred storage file " + storageFile.getFileRef(), e);
            }
        }
    }

    private void publishPostImportEvents(ImportContext context, TransferImportReport report) {
        // matched users included: their tenant membership changed, so the satellite services'
        // user/tenant-user mirrors must be refreshed too
        for (UUID userId : this.allBundleUserIds(context)) {
            try {
                this.userTouchedIntegrationEventHandler.handle(context.remap(userId));
            } catch (Exception e) {
                logger.error("failed to send user touched event for imported user " + userId, e);
            }
        }
        if (!context.createdPlanIds.isEmpty()) {
            try {
                for (List<UUID> batch : this.partition(context.createdPlanIds)) this.planTouchedIntegrationEventHandler.handlePlan(batch);
            } catch (Exception e) {
                logger.error("failed to send plan touched events for imported plans", e);
            }
            for (UUID planId : context.createdPlanIds) {
                try {
                    this.annotationEntityTouchedIntegrationEventHandler.handlePlan(planId);
                } catch (Exception e) {
                    logger.error("failed to send annotation entity touched event for imported plan " + planId, e);
                }
            }
        }
        for (UUID descriptionId : context.createdDescriptionIds) {
            try {
                this.annotationEntityTouchedIntegrationEventHandler.handleDescription(descriptionId);
            } catch (Exception e) {
                logger.error("failed to send annotation entity touched event for imported description " + descriptionId, e);
            }
        }
        this.indexImported(context, report);
    }

    private void indexImported(ImportContext context, TransferImportReport report) {
        if (!this.elasticEnabledSafe()) return;
        try {
            for (Object item : context.creates(TransferEntityType.Plan)) this.elasticService.persistPlan((PlanEntity) item);
            for (Object item : context.creates(TransferEntityType.Description)) this.elasticService.persistDescription((DescriptionEntity) item);
        } catch (Exception e) {
            logger.error("failed to index imported entities", e);
            report.getWarnings().add("Indexing the imported plans failed; run the Maintenance Elasticsearch rebuild.");
        }
    }

    private boolean elasticEnabledSafe() {
        try {
            return this.elasticService.enabled();
        } catch (Exception e) {
            return false;
        }
    }

    //endregion

    //region same-installation copy

    private static class FileCopy {
        final String oldRef;
        final String newRef;
        final StorageType storageType;

        FileCopy(String oldRef, String newRef, StorageType storageType) {
            this.oldRef = oldRef;
            this.newRef = newRef;
            this.storageType = storageType;
        }
    }

    @Override
    public TransferImportReport analyzeCopy(TenantCopyRequest request) throws InvalidApplicationException {
        return this.doCopy(request, false);
    }

    @Override
    public TransferImportReport copyTenant(TenantCopyRequest request) throws InvalidApplicationException {
        return this.doCopy(request, true);
    }

    /**
     * Same-installation tenant-to-tenant copy. Rows OWNED by the source tenant are duplicated with
     * fresh identities (ids, group ids, file refs, suffixed codes); shared tenant-null rows are not
     * duplicated - the copies keep referencing them, since the target tenant sees them anyway.
     * Users are global: they are only linked to the target tenant.
     */
    private TransferImportReport doCopy(TenantCopyRequest request, boolean apply) throws InvalidApplicationException {
        this.ensureTenantScope();

        String sourceTenantCode = request == null || request.getSourceTenantCode() == null ? null : request.getSourceTenantCode().trim();
        if (sourceTenantCode == null || sourceTenantCode.isEmpty())
            throw new MyValidationException(this.errors.getMissingTenant().getCode(), "The source tenant code is required");

        UUID targetTenantId = this.tenantScopeFactory.getInstance().isDefaultTenant() ? null : this.tenantScopeFactory.getInstance().getTenant();
        UUID sourceTenantId = null;
        if (!sourceTenantCode.equalsIgnoreCase(this.tenantScopeFactory.getInstance().getDefaultTenantCode())) {
            TenantEntity sourceTenant = this.queryFactory.query(TenantQuery.class).disableTracking().codes(sourceTenantCode).isActive(IsActive.Active).first();
            if (sourceTenant == null)
                throw new MyValidationException(this.errors.getMissingTenant().getCode(), "Source tenant not found: " + sourceTenantCode);
            sourceTenantId = sourceTenant.getId();
            sourceTenantCode = sourceTenant.getCode();
        } else {
            sourceTenantCode = this.tenantScopeFactory.getInstance().getDefaultTenantCode();
        }
        if (Objects.equals(sourceTenantId, targetTenantId))
            throw new MyValidationException(this.errors.getMissingTenant().getCode(), "Source and target tenant are the same");

        TransferImportReport report = new TransferImportReport();
        report.setApplied(apply);
        report.setSourceTenantCode(sourceTenantCode);

        TenantEntityManager entityManager = this.tenantEntityManagerFactory.getInstance();
        Collector collector = new Collector();
        try {
            this.tenantScopeFactory.getInstance().setTempTenant(entityManager, sourceTenantId, sourceTenantCode);
            try {
                entityManager.loadExplicitTenantFilters();
                this.collectRoots(request, collector);
            } finally {
                entityManager.reloadTenantFilters();
            }
            this.expandClosure(collector);
            this.collectLanguages(request, collector);
        } finally {
            this.tenantScopeFactory.getInstance().removeTempTenant(entityManager);
        }

        ImportContext context = new ImportContext();
        List<FileCopy> fileCopies = this.analyzeCopyRows(collector, context, report, sourceTenantId, entityManager);

        Map<String, String> remapStrings = new HashMap<>();
        for (Map.Entry<UUID, UUID> entry : context.idRemap.entrySet()) {
            remapStrings.put(entry.getKey().toString(), entry.getValue().toString());
        }

        if (apply) {
            try {
                entityManager.disableTenantFilters();
                this.applyCreates(context, remapStrings, targetTenantId, entityManager);
                this.ensureTenantUsers(context, targetTenantId, entityManager);
                entityManager.flush();
            } finally {
                entityManager.reloadTenantFilters();
            }
            for (FileCopy fileCopy : fileCopies) {
                byte[] payload = this.storageFileService.readByFileRefAsBytesSafe(fileCopy.oldRef, fileCopy.storageType);
                if (payload != null) this.storageFileService.writeBytesByFileRef(fileCopy.newRef, fileCopy.storageType, payload);
            }
            this.linkUsersToTenantInKeycloak(context, targetTenantId, report);
            this.publishPostImportEvents(context, report);
        }
        this.copyAnnotations(collector, report, remapStrings, sourceTenantCode, apply);
        if (apply) {
            report.getWarnings().add("Run the accounting/KPI recalculation tasks from the Maintenance page to refresh usage metrics.");
            if (!this.elasticEnabledSafe()) report.getWarnings().add("Elasticsearch is disabled or unreachable; run the Maintenance index rebuild after enabling it.");
        }
        return report;
    }

    private List<FileCopy> analyzeCopyRows(Collector collector, ImportContext context, TransferImportReport report, UUID sourceTenantId, TenantEntityManager entityManager) {
        List<FileCopy> fileCopies = new ArrayList<>();
        boolean doisSkipped = false;
        for (TransferEntityType type : TransferEntityType.values()) {
            List<Object> rows = collector.list(type);
            if (rows.isEmpty()) continue;
            TransferImportReport.TypeReport typeReport = report.typeReport(type);
            typeReport.setTotal(rows.size());

            for (Object item : rows) {
                // users are installation-global: only tenant membership is granted, nothing is copied
                if (type == TransferEntityType.User) {
                    context.matchedUserIds.add(((UserEntity) item).getId());
                    typeReport.setMatchedById(typeReport.getMatchedById() + 1);
                    continue;
                }
                if (type == TransferEntityType.UserContactInfo) {
                    typeReport.setMatchedById(typeReport.getMatchedById() + 1);
                    continue;
                }
                // a DOI identifies the deposited plan itself - a copy is a new, undeposited plan
                if (type == TransferEntityType.EntityDoi) {
                    typeReport.setMatchedById(typeReport.getMatchedById() + 1);
                    doisSkipped = true;
                    continue;
                }
                // language codes are referenced by string from plans/templates - never duplicated
                if (type == TransferEntityType.Language || type == TransferEntityType.SupportiveMaterial) {
                    typeReport.setMatchedById(typeReport.getMatchedById() + 1);
                    continue;
                }
                TenantScopedBaseEntity tenantScoped = (TenantScopedBaseEntity) item;
                if (!Objects.equals(tenantScoped.getTenantId(), sourceTenantId)) {
                    // shared row: the copy keeps referencing it, the target tenant sees it anyway
                    typeReport.setMatchedById(typeReport.getMatchedById() + 1);
                    continue;
                }
                // rows were fetched into this same persistence context (the temp-tenant queries share
                // the request's entityManager) - mutating the @Id of a still-managed instance is what
                // triggers Hibernate's "identifier was altered" error at flush, so detach before assigning
                // the new copy identity.
                entityManager.getEntityManager().detach(item);
                UUID newId = UUID.randomUUID();
                context.idRemap.put(this.copyIdOf(item), newId);
                this.assignCopyIdentity(type, item, newId, context, fileCopies);
                context.create(type, item);
                typeReport.setToCreate(typeReport.getToCreate() + 1);
                if (type == TransferEntityType.Plan) context.createdPlanIds.add(newId);
                if (type == TransferEntityType.Description) context.createdDescriptionIds.add(newId);
            }
        }
        if (doisSkipped) report.getWarnings().add("DOIs are not copied: a copied plan is a new plan, not the deposited one.");
        return fileCopies;
    }

    private void assignCopyIdentity(TransferEntityType type, Object item, UUID newId, ImportContext context, List<FileCopy> fileCopies) {
        switch (type) {
            case ReferenceType -> { ReferenceTypeEntity entity = (ReferenceTypeEntity) item; entity.setId(newId); entity.setCode(this.copyCode(entity.getCode(), newId)); }
            case PrefillingSource -> { PrefillingSourceEntity entity = (PrefillingSourceEntity) item; entity.setId(newId); entity.setCode(this.copyCode(entity.getCode(), newId)); }
            case DescriptionTemplateType -> { DescriptionTemplateTypeEntity entity = (DescriptionTemplateTypeEntity) item; entity.setId(newId); entity.setCode(this.copyCode(entity.getCode(), newId)); }
            case PlanBlueprintType -> { PlanBlueprintTypeEntity entity = (PlanBlueprintTypeEntity) item; entity.setId(newId); entity.setCode(this.copyCode(entity.getCode(), newId)); }
            case PlanStatus -> ((PlanStatusEntity) item).setId(newId);
            case DescriptionStatus -> ((DescriptionStatusEntity) item).setId(newId);
            case PlanWorkflow -> ((PlanWorkflowEntity) item).setId(newId);
            case DescriptionWorkflow -> ((DescriptionWorkflowEntity) item).setId(newId);
            case StorageFile -> {
                StorageFileEntity entity = (StorageFileEntity) item;
                entity.setId(newId);
                String newRef = UUID.randomUUID().toString().replace("-", "");
                if (entity.getPurgedAt() == null && entity.getFileRef() != null) fileCopies.add(new FileCopy(entity.getFileRef(), newRef, entity.getStorageType()));
                entity.setFileRef(newRef);
            }
            case DescriptionTemplate -> {
                DescriptionTemplateEntity entity = (DescriptionTemplateEntity) item;
                entity.setId(newId);
                entity.setCode(this.copyCode(entity.getCode(), newId));
                this.mintGroup(context, entity.getGroupId());
            }
            case PlanBlueprint -> {
                PlanBlueprintEntity entity = (PlanBlueprintEntity) item;
                entity.setId(newId);
                entity.setCode(this.copyCode(entity.getCode(), newId));
                this.mintGroup(context, entity.getGroupId());
            }
            case Tag -> ((TagEntity) item).setId(newId);
            case Reference -> ((ReferenceEntity) item).setId(newId);
            case Plan -> {
                PlanEntity entity = (PlanEntity) item;
                entity.setId(newId);
                this.mintGroup(context, entity.getGroupId());
            }
            case PlanUser -> ((PlanUserEntity) item).setId(newId);
            case PlanDescriptionTemplate -> ((PlanDescriptionTemplateEntity) item).setId(newId);
            case PlanReference -> ((PlanReferenceEntity) item).setId(newId);
            case Description -> ((DescriptionEntity) item).setId(newId);
            case DescriptionReference -> ((DescriptionReferenceEntity) item).setId(newId);
            case DescriptionTag -> ((DescriptionTagEntity) item).setId(newId);
            case UserDescriptionTemplate -> ((UserDescriptionTemplateEntity) item).setId(newId);
            case UserPlanBlueprint -> ((UserPlanBlueprintEntity) item).setId(newId);
            default -> {
            }
        }
    }

    private UUID copyIdOf(Object item) {
        if (item instanceof PlanEntity entity) return entity.getId();
        if (item instanceof DescriptionEntity entity) return entity.getId();
        if (item instanceof PlanUserEntity entity) return entity.getId();
        if (item instanceof PlanDescriptionTemplateEntity entity) return entity.getId();
        if (item instanceof PlanReferenceEntity entity) return entity.getId();
        if (item instanceof DescriptionReferenceEntity entity) return entity.getId();
        if (item instanceof DescriptionTagEntity entity) return entity.getId();
        if (item instanceof TagEntity entity) return entity.getId();
        if (item instanceof ReferenceEntity entity) return entity.getId();
        if (item instanceof ReferenceTypeEntity entity) return entity.getId();
        if (item instanceof PrefillingSourceEntity entity) return entity.getId();
        if (item instanceof DescriptionTemplateTypeEntity entity) return entity.getId();
        if (item instanceof PlanBlueprintTypeEntity entity) return entity.getId();
        if (item instanceof PlanStatusEntity entity) return entity.getId();
        if (item instanceof DescriptionStatusEntity entity) return entity.getId();
        if (item instanceof PlanWorkflowEntity entity) return entity.getId();
        if (item instanceof DescriptionWorkflowEntity entity) return entity.getId();
        if (item instanceof StorageFileEntity entity) return entity.getId();
        if (item instanceof DescriptionTemplateEntity entity) return entity.getId();
        if (item instanceof PlanBlueprintEntity entity) return entity.getId();
        if (item instanceof UserDescriptionTemplateEntity entity) return entity.getId();
        if (item instanceof UserPlanBlueprintEntity entity) return entity.getId();
        throw new MyApplicationException("unsupported copy entity " + item.getClass().getSimpleName());
    }

    private String copyCode(String code, UUID newId) {
        if (code == null || code.isBlank()) return code;
        return code + "-copy-" + newId.toString().substring(0, 8);
    }

    private void mintGroup(ImportContext context, UUID groupId) {
        if (groupId == null) return;
        context.idRemap.computeIfAbsent(groupId, g -> UUID.randomUUID());
    }

    private void copyAnnotations(Collector collector, TransferImportReport report, Map<String, String> remapStrings, String sourceTenantCode, boolean apply) {
        List<UUID> entityIds = new ArrayList<>();
        entityIds.addAll(collector.ids(TransferEntityType.Plan));
        entityIds.addAll(collector.ids(TransferEntityType.Description));
        if (entityIds.isEmpty()) return;
        if (!this.annotationTransferClient.isConfigured()) {
            report.getWarnings().add("No annotation service url is configured (tenant-transfer.annotation-service-url); comments were not copied.");
            return;
        }
        try {
            Map<String, Object> exportRequest = new HashMap<>();
            exportRequest.put("entityIds", entityIds);
            String exportJson = this.annotationTransferClient.post("/api/annotation/transfer/export", this.jsonHandlingService.toJson(exportRequest), sourceTenantCode);
            String payload = this.applyRemap(exportJson, remapStrings);
            String response = this.annotationTransferClient.post("/api/annotation/transfer/import?remint=true&dryRun=" + !apply, payload);
            AnnotationTransferReportModel annotationReport = this.jsonHandlingService.fromJsonSafe(AnnotationTransferReportModel.class, response);
            if (annotationReport == null) {
                report.getWarnings().add("The annotation service returned an unreadable transfer report; verify the copied comments manually.");
                return;
            }
            this.mergeAnnotationCounts(report, "Annotation", annotationReport.getAnnotations());
            this.mergeAnnotationCounts(report, "AnnotationStatus", annotationReport.getAnnotationStatuses());
            this.mergeAnnotationCounts(report, "AnnotationStatusDefinition", annotationReport.getStatuses());
        } catch (Exception e) {
            logger.error("failed to copy annotations", e);
            report.getWarnings().add("Copying annotations failed (" + e.getMessage() + "); the rest of the copy was " + (apply ? "applied" : "analyzed") + ".");
        }
    }

    //endregion

    private boolean isTrue(Boolean value) {
        return value != null && value;
    }

    private boolean isNullOrEmpty(List<?> value) {
        return value == null || value.isEmpty();
    }

    private List<List<UUID>> partition(Collection<UUID> ids) {
        List<UUID> all = new ArrayList<>(ids);
        List<List<UUID>> batches = new ArrayList<>();
        for (int i = 0; i < all.size(); i += ID_BATCH_SIZE) {
            batches.add(all.subList(i, Math.min(all.size(), i + ID_BATCH_SIZE)));
        }
        return batches;
    }

    private List<List<String>> partitionStrings(Collection<String> values) {
        List<String> all = new ArrayList<>(values);
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < all.size(); i += ID_BATCH_SIZE) {
            batches.add(all.subList(i, Math.min(all.size(), i + ID_BATCH_SIZE)));
        }
        return batches;
    }
}
