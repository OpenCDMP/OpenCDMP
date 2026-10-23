package org.opencdmp.service.transfer;

import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.commons.scope.tenant.TenantScopeFactory;
import org.opencdmp.data.*;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.integrationevent.outbox.annotationentityremoval.AnnotationEntityRemovalIntegrationEventHandler;
import org.opencdmp.integrationevent.outbox.planremoval.PlanRemovalIntegrationEventHandler;
import org.opencdmp.query.*;
import org.opencdmp.service.elastic.ElasticService;
import org.opencdmp.service.storage.StorageFileService;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.util.*;

/**
 * Deletes content owned by the current tenant: rows with tenant = current tenant, or - on the
 * default tenant - the shared tenant-null rows. When purging the default tenant, rows that are
 * still referenced by other tenants' data are kept (reported as such) so their content keeps working.
 */
@Service
public class TenantPurgeServiceImpl implements TenantPurgeService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(TenantPurgeServiceImpl.class));

    private static final int FLUSH_BATCH_SIZE = 50;
    private static final int ID_BATCH_SIZE = 500;

    private static final String TYPE_STORAGE_FILE = "StorageFile";

    private final QueryFactory queryFactory;
    private final JsonHandlingService jsonHandlingService;
    private final TenantScopeFactory tenantScopeFactory;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;
    private final StorageFileService storageFileService;
    private final ElasticService elasticService;
    private final PlanRemovalIntegrationEventHandler planRemovalIntegrationEventHandler;
    private final AnnotationEntityRemovalIntegrationEventHandler annotationEntityRemovalIntegrationEventHandler;
    private final AnnotationTransferClient annotationTransferClient;
    private final ErrorThesaurusProperties errors;

    public TenantPurgeServiceImpl(
            QueryFactory queryFactory,
            JsonHandlingService jsonHandlingService,
            TenantScopeFactory tenantScopeFactory,
            TenantEntityManagerFactory tenantEntityManagerFactory,
            StorageFileService storageFileService,
            ElasticService elasticService,
            PlanRemovalIntegrationEventHandler planRemovalIntegrationEventHandler,
            AnnotationEntityRemovalIntegrationEventHandler annotationEntityRemovalIntegrationEventHandler,
            AnnotationTransferClient annotationTransferClient,
            ErrorThesaurusProperties errors) {
        this.queryFactory = queryFactory;
        this.jsonHandlingService = jsonHandlingService;
        this.tenantScopeFactory = tenantScopeFactory;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
        this.storageFileService = storageFileService;
        this.elasticService = elasticService;
        this.planRemovalIntegrationEventHandler = planRemovalIntegrationEventHandler;
        this.annotationEntityRemovalIntegrationEventHandler = annotationEntityRemovalIntegrationEventHandler;
        this.annotationTransferClient = annotationTransferClient;
        this.errors = errors;
    }

    @Override
    public TenantPurgeReport analyzePurge(TenantPurgeRequest request) throws InvalidApplicationException {
        return this.doPurge(request, false);
    }

    @Override
    public TenantPurgeReport purge(TenantPurgeRequest request) throws InvalidApplicationException {
        return this.doPurge(request, true);
    }

    private TenantPurgeReport doPurge(TenantPurgeRequest request, boolean apply) throws InvalidApplicationException {
        // loadExplicitTenantFilters silently falls open when no tenant scope is resolved - never purge unscoped
        if (!this.tenantScopeFactory.getInstance().isSet())
            throw new MyForbiddenException(this.errors.getMissingTenant().getCode(), this.errors.getMissingTenant().getMessage());
        TenantPurgeRequest normalized = this.normalize(request);
        boolean isDefaultTenant = this.tenantScopeFactory.getInstance().isDefaultTenant();

        TenantPurgeReport report = new TenantPurgeReport();
        report.setApplied(apply);
        report.setTenantCode(isDefaultTenant ? this.tenantScopeFactory.getInstance().getDefaultTenantCode() : this.tenantScopeFactory.getInstance().getTenantCode());

        TenantEntityManager entityManager = this.tenantEntityManagerFactory.getInstance();

        // phase 1: collect the current tenant's rows per selected category, in child-first delete order
        Map<String, List<? extends TenantScopedBaseEntity>> candidates = new LinkedHashMap<>();
        try {
            entityManager.loadExplicitTenantFilters();
            if (this.isTrue(normalized.getDeletePlans())) {
                candidates.put("EntityDoi", this.queryFactory.query(EntityDoiQuery.class).collect());
                candidates.put("DescriptionTag", this.queryFactory.query(DescriptionTagQuery.class).collect());
                candidates.put("DescriptionReference", this.queryFactory.query(DescriptionReferenceQuery.class).collect());
                candidates.put("Description", this.queryFactory.query(DescriptionQuery.class).collect());
                candidates.put("PlanReference", this.queryFactory.query(PlanReferenceQuery.class).collect());
                candidates.put("PlanDescriptionTemplate", this.queryFactory.query(PlanDescriptionTemplateQuery.class).collect());
                candidates.put("PlanUser", this.queryFactory.query(PlanUserQuery.class).collect());
                candidates.put("Evaluation", this.queryFactory.query(EvaluationQuery.class).collect());
                candidates.put("PlanUpdateRequest", this.queryFactory.query(PlanUpdateRequestQuery.class).collect());
                candidates.put("Lock", this.queryFactory.query(LockQuery.class).collect());
                candidates.put("ActionConfirmation", this.queryFactory.query(ActionConfirmationQuery.class).collect());
                candidates.put("Plan", this.queryFactory.query(PlanQuery.class).collect());
            }
            if (this.isTrue(normalized.getDeleteReferences())) candidates.put("Reference", this.queryFactory.query(ReferenceQuery.class).collect());
            if (this.isTrue(normalized.getDeleteTags())) candidates.put("Tag", this.queryFactory.query(TagQuery.class).collect());
            if (this.isTrue(normalized.getDeleteDescriptionTemplates())) {
                candidates.put("UserDescriptionTemplate", this.queryFactory.query(UserDescriptionTemplateQuery.class).collect());
                candidates.put("DescriptionTemplate", this.queryFactory.query(DescriptionTemplateQuery.class).collect());
            }
            if (this.isTrue(normalized.getDeletePlanBlueprints())) {
                candidates.put("UserPlanBlueprint", this.queryFactory.query(UserPlanBlueprintQuery.class).collect());
                candidates.put("PlanBlueprint", this.queryFactory.query(PlanBlueprintQuery.class).collect());
            }
            if (this.isTrue(normalized.getDeletePrefillingSources())) candidates.put("PrefillingSource", this.queryFactory.query(PrefillingSourceQuery.class).collect());
            if (this.isTrue(normalized.getDeleteReferenceTypes())) candidates.put("ReferenceType", this.queryFactory.query(ReferenceTypeQuery.class).collect());
            if (this.isTrue(normalized.getDeleteStatusesAndWorkflows())) {
                candidates.put("PlanWorkflow", this.queryFactory.query(PlanWorkflowQuery.class).collect());
                candidates.put("DescriptionWorkflow", this.queryFactory.query(DescriptionWorkflowQuery.class).collect());
                candidates.put("PlanStatus", this.queryFactory.query(PlanStatusQuery.class).collect());
                candidates.put("DescriptionStatus", this.queryFactory.query(DescriptionStatusQuery.class).collect());
            }
            if (this.isTrue(normalized.getDeleteBlueprintTypes())) candidates.put("PlanBlueprintType", this.queryFactory.query(PlanBlueprintTypeQuery.class).collect());
            if (this.isTrue(normalized.getDeleteTemplateTypes())) candidates.put("DescriptionTemplateType", this.queryFactory.query(DescriptionTemplateTypeQuery.class).collect());
            if (this.isTrue(normalized.getDeleteStorageFiles())) candidates.put(TYPE_STORAGE_FILE, this.queryFactory.query(StorageFileQuery.class).collect());
        } finally {
            entityManager.reloadTenantFilters();
        }

        // phase 2: on the default tenant, keep shared rows still referenced by other tenants
        Map<String, Set<UUID>> keptIds = new HashMap<>();
        if (isDefaultTenant) {
            try {
                entityManager.disableTenantFilters();
                this.computeKeptSets(candidates, keptIds, report, normalized);
            } finally {
                entityManager.reloadTenantFilters();
            }
        }

        List<PlanEntity> deletedPlans = this.survivors(candidates.get("Plan"), keptIds.get("Plan"));
        List<DescriptionEntity> deletedDescriptions = this.survivors(candidates.get("Description"), keptIds.get("Description"));

        // phase 3: delete
        try {
            entityManager.loadExplicitTenantFilters();
            for (Map.Entry<String, List<? extends TenantScopedBaseEntity>> entry : candidates.entrySet()) {
                Set<UUID> kept = keptIds.getOrDefault(entry.getKey(), Collections.emptySet());
                int toDelete = 0;
                int counter = 0;
                for (TenantScopedBaseEntity item : entry.getValue()) {
                    UUID id = this.idOf(item);
                    if (id != null && kept.contains(id)) continue;
                    toDelete++;
                    if (!apply) continue;
                    if (TYPE_STORAGE_FILE.equals(entry.getKey())) {
                        StorageFileEntity file = (StorageFileEntity) item;
                        if (file.getPurgedAt() == null) this.storageFileService.deletePhysicalFileByRefSafe(file.getFileRef(), file.getStorageType());
                    }
                    entityManager.remove(item);
                    if (++counter % FLUSH_BATCH_SIZE == 0) entityManager.flush();
                }
                report.addType(entry.getKey(), toDelete, kept.size());
                if (apply) entityManager.flush();
            }
        } finally {
            entityManager.reloadTenantFilters();
        }

        if (this.isTrue(normalized.getDeletePlans())) {
            this.purgeAnnotations(report, deletedPlans, deletedDescriptions, apply);
        }
        if (apply) {
            this.publishRemovalEvents(deletedPlans, deletedDescriptions);
            this.removeFromElastic(deletedPlans, deletedDescriptions, report);
            report.getWarnings().add("Run the accounting/KPI recalculation tasks from the Maintenance page to refresh usage metrics.");
            if (this.isTrue(normalized.getDeleteStorageFiles()))
                report.getWarnings().add("Tenant configuration entries (logo, status icons, plugin files) that referenced deleted files may need to be re-uploaded.");
        }
        if (isDefaultTenant && (this.isTrue(normalized.getDeleteStatusesAndWorkflows()) || this.isTrue(normalized.getDeletePrefillingSources()) || this.isTrue(normalized.getDeleteStorageFiles())))
            report.getWarnings().add("Shared workflows, prefilling sources or files of the default tenant may still be used by other tenants through configuration; review other tenants after the purge.");
        return report;
    }

    /**
     * On the default tenant the purged rows are the shared (tenant-null) ones; anything directly
     * referenced by another tenant's rows is kept, and kept rows keep their own dependencies alive.
     */
    private void computeKeptSets(Map<String, List<? extends TenantScopedBaseEntity>> candidates, Map<String, Set<UUID>> keptIds, TenantPurgeReport report, TenantPurgeRequest normalized) {
        // blueprints used by other tenants' plans
        List<? extends TenantScopedBaseEntity> blueprints = candidates.get("PlanBlueprint");
        if (blueprints != null && !blueprints.isEmpty()) {
            Set<UUID> used = new HashSet<>();
            for (List<UUID> batch : this.partition(this.idsOf(blueprints)))
                this.queryFactory.query(PlanQuery.class).disableTracking().planBlueprintSubQuery(this.queryFactory.query(PlanBlueprintQuery.class).ids(batch)).collect()
                        .forEach(x -> { if (x.getTenantId() != null && x.getBlueprintId() != null) used.add(x.getBlueprintId()); });
            keptIds.put("PlanBlueprint", used);
        }
        // templates used by other tenants' descriptions or plan sections
        List<? extends TenantScopedBaseEntity> templates = candidates.get("DescriptionTemplate");
        if (templates != null && !templates.isEmpty()) {
            Set<UUID> candidateIds = new HashSet<>();
            Map<UUID, List<UUID>> byGroup = new HashMap<>();
            for (TenantScopedBaseEntity item : templates) {
                DescriptionTemplateEntity template = (DescriptionTemplateEntity) item;
                candidateIds.add(template.getId());
                if (template.getGroupId() != null) byGroup.computeIfAbsent(template.getGroupId(), g -> new ArrayList<>()).add(template.getId());
            }
            Set<UUID> used = new HashSet<>();
            for (List<UUID> batch : this.partition(candidateIds))
                this.queryFactory.query(DescriptionQuery.class).disableTracking().descriptionTemplateSubQuery(this.queryFactory.query(DescriptionTemplateQuery.class).ids(batch)).collect()
                        .forEach(x -> { if (x.getTenantId() != null && x.getDescriptionTemplateId() != null) used.add(x.getDescriptionTemplateId()); });
            for (List<UUID> batch : this.partition(byGroup.keySet()))
                this.queryFactory.query(PlanDescriptionTemplateQuery.class).disableTracking().descriptionTemplateGroupIds(batch).collect()
                        .forEach(x -> { if (x.getTenantId() != null && x.getDescriptionTemplateGroupId() != null) used.addAll(byGroup.getOrDefault(x.getDescriptionTemplateGroupId(), Collections.emptyList())); });
            keptIds.put("DescriptionTemplate", used);
        }
        // references used by other tenants' plans/descriptions
        List<? extends TenantScopedBaseEntity> references = candidates.get("Reference");
        if (references != null && !references.isEmpty()) {
            Set<UUID> used = new HashSet<>();
            for (List<UUID> batch : this.partition(this.idsOf(references))) {
                this.queryFactory.query(PlanReferenceQuery.class).disableTracking().referenceIds(batch).collect()
                        .forEach(x -> { if (x.getTenantId() != null) used.add(x.getReferenceId()); });
                this.queryFactory.query(DescriptionReferenceQuery.class).disableTracking().referenceIds(batch).collect()
                        .forEach(x -> { if (x.getTenantId() != null) used.add(x.getReferenceId()); });
            }
            keptIds.put("Reference", used);
        }
        // tags used by other tenants' descriptions
        List<? extends TenantScopedBaseEntity> tags = candidates.get("Tag");
        if (tags != null && !tags.isEmpty()) {
            Set<UUID> used = new HashSet<>();
            for (List<UUID> batch : this.partition(this.idsOf(tags)))
                this.queryFactory.query(DescriptionTagQuery.class).disableTracking().tagIds(batch).collect()
                        .forEach(x -> { if (x.getTenantId() != null) used.add(x.getTagId()); });
            keptIds.put("Tag", used);
        }
        // reference types used by other tenants' references, or by kept references
        List<? extends TenantScopedBaseEntity> referenceTypes = candidates.get("ReferenceType");
        if (referenceTypes != null && !referenceTypes.isEmpty()) {
            Set<UUID> used = new HashSet<>();
            for (List<UUID> batch : this.partition(this.idsOf(referenceTypes)))
                this.queryFactory.query(ReferenceQuery.class).disableTracking().typeIds(batch).collect()
                        .forEach(x -> { if (x.getTenantId() != null && x.getTypeId() != null) used.add(x.getTypeId()); });
            Set<UUID> keptReferences = keptIds.getOrDefault("Reference", Collections.emptySet());
            if (references != null) {
                for (TenantScopedBaseEntity item : references) {
                    ReferenceEntity reference = (ReferenceEntity) item;
                    if (keptReferences.contains(reference.getId()) && reference.getTypeId() != null) used.add(reference.getTypeId());
                }
            }
            keptIds.put("ReferenceType", used);
        }
        // statuses used by other tenants' plans/descriptions
        List<? extends TenantScopedBaseEntity> planStatuses = candidates.get("PlanStatus");
        if (planStatuses != null && !planStatuses.isEmpty()) {
            Set<UUID> used = new HashSet<>();
            for (List<UUID> batch : this.partition(this.idsOf(planStatuses)))
                this.queryFactory.query(PlanQuery.class).disableTracking().statusIds(batch).collect()
                        .forEach(x -> { if (x.getTenantId() != null && x.getStatusId() != null) used.add(x.getStatusId()); });
            keptIds.put("PlanStatus", used);
        }
        List<? extends TenantScopedBaseEntity> descriptionStatuses = candidates.get("DescriptionStatus");
        if (descriptionStatuses != null && !descriptionStatuses.isEmpty()) {
            Set<UUID> used = new HashSet<>();
            for (List<UUID> batch : this.partition(this.idsOf(descriptionStatuses)))
                this.queryFactory.query(DescriptionQuery.class).disableTracking().statusIds(batch).collect()
                        .forEach(x -> { if (x.getTenantId() != null && x.getStatusId() != null) used.add(x.getStatusId()); });
            keptIds.put("DescriptionStatus", used);
        }
        // types used by other tenants' (or kept) blueprints/templates
        List<? extends TenantScopedBaseEntity> blueprintTypes = candidates.get("PlanBlueprintType");
        if (blueprintTypes != null && !blueprintTypes.isEmpty()) {
            Set<UUID> used = new HashSet<>();
            Set<UUID> keptBlueprints = keptIds.getOrDefault("PlanBlueprint", Collections.emptySet());
            for (List<UUID> batch : this.partition(this.idsOf(blueprintTypes)))
                this.queryFactory.query(PlanBlueprintQuery.class).disableTracking().typeIds(batch).collect()
                        .forEach(x -> { if ((x.getTenantId() != null || keptBlueprints.contains(x.getId())) && x.getTypeId() != null) used.add(x.getTypeId()); });
            keptIds.put("PlanBlueprintType", used);
        }
        List<? extends TenantScopedBaseEntity> templateTypes = candidates.get("DescriptionTemplateType");
        if (templateTypes != null && !templateTypes.isEmpty()) {
            Set<UUID> used = new HashSet<>();
            Set<UUID> keptTemplates = keptIds.getOrDefault("DescriptionTemplate", Collections.emptySet());
            for (List<UUID> batch : this.partition(this.idsOf(templateTypes)))
                this.queryFactory.query(DescriptionTemplateQuery.class).disableTracking().typeIds(batch).collect()
                        .forEach(x -> { if ((x.getTenantId() != null || keptTemplates.contains(x.getId())) && x.getTypeId() != null) used.add(x.getTypeId()); });
            keptIds.put("DescriptionTemplateType", used);
        }
    }

    private List<List<UUID>> partition(Collection<UUID> ids) {
        List<UUID> all = new ArrayList<>(ids);
        List<List<UUID>> batches = new ArrayList<>();
        for (int i = 0; i < all.size(); i += ID_BATCH_SIZE) {
            batches.add(all.subList(i, Math.min(all.size(), i + ID_BATCH_SIZE)));
        }
        return batches;
    }

    private Set<UUID> idsOf(List<? extends TenantScopedBaseEntity> items) {
        Set<UUID> ids = new HashSet<>();
        for (TenantScopedBaseEntity item : items) {
            UUID id = this.idOf(item);
            if (id != null) ids.add(id);
        }
        return ids;
    }

    private UUID idOf(TenantScopedBaseEntity item) {
        if (item instanceof PlanEntity entity) return entity.getId();
        if (item instanceof DescriptionEntity entity) return entity.getId();
        if (item instanceof EntityDoiEntity entity) return entity.getId();
        if (item instanceof DescriptionTagEntity entity) return entity.getId();
        if (item instanceof DescriptionReferenceEntity entity) return entity.getId();
        if (item instanceof PlanReferenceEntity entity) return entity.getId();
        if (item instanceof PlanDescriptionTemplateEntity entity) return entity.getId();
        if (item instanceof PlanUserEntity entity) return entity.getId();
        if (item instanceof EvaluationEntity entity) return entity.getId();
        if (item instanceof PlanUpdateRequestEntity entity) return entity.getId();
        if (item instanceof LockEntity entity) return entity.getId();
        if (item instanceof ActionConfirmationEntity entity) return entity.getId();
        if (item instanceof ReferenceEntity entity) return entity.getId();
        if (item instanceof TagEntity entity) return entity.getId();
        if (item instanceof UserDescriptionTemplateEntity entity) return entity.getId();
        if (item instanceof UserPlanBlueprintEntity entity) return entity.getId();
        if (item instanceof DescriptionTemplateEntity entity) return entity.getId();
        if (item instanceof PlanBlueprintEntity entity) return entity.getId();
        if (item instanceof PrefillingSourceEntity entity) return entity.getId();
        if (item instanceof ReferenceTypeEntity entity) return entity.getId();
        if (item instanceof PlanWorkflowEntity entity) return entity.getId();
        if (item instanceof DescriptionWorkflowEntity entity) return entity.getId();
        if (item instanceof PlanStatusEntity entity) return entity.getId();
        if (item instanceof DescriptionStatusEntity entity) return entity.getId();
        if (item instanceof PlanBlueprintTypeEntity entity) return entity.getId();
        if (item instanceof DescriptionTemplateTypeEntity entity) return entity.getId();
        if (item instanceof StorageFileEntity entity) return entity.getId();
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> survivors(List<? extends TenantScopedBaseEntity> items, Set<UUID> kept) {
        if (items == null) return new ArrayList<>();
        List<T> result = new ArrayList<>();
        for (TenantScopedBaseEntity item : items) {
            UUID id = this.idOf(item);
            if (kept != null && id != null && kept.contains(id)) continue;
            result.add((T) item);
        }
        return result;
    }

    /**
     * Purge categories cascade downward: deleting a dependency (e.g. reference types) forces
     * deletion of everything that references it, ending at plans.
     */
    private TenantPurgeRequest normalize(TenantPurgeRequest request) {
        TenantPurgeRequest normalized = request == null ? new TenantPurgeRequest() : request;
        boolean changed = true;
        while (changed) {
            changed = false;
            if (this.isTrue(normalized.getDeleteBlueprintTypes()) && !this.isTrue(normalized.getDeletePlanBlueprints())) { normalized.setDeletePlanBlueprints(true); changed = true; }
            if (this.isTrue(normalized.getDeleteTemplateTypes()) && !this.isTrue(normalized.getDeleteDescriptionTemplates())) { normalized.setDeleteDescriptionTemplates(true); changed = true; }
            if (this.isTrue(normalized.getDeleteReferenceTypes())) {
                if (!this.isTrue(normalized.getDeleteReferences())) { normalized.setDeleteReferences(true); changed = true; }
                if (!this.isTrue(normalized.getDeleteDescriptionTemplates())) { normalized.setDeleteDescriptionTemplates(true); changed = true; }
                if (!this.isTrue(normalized.getDeletePlanBlueprints())) { normalized.setDeletePlanBlueprints(true); changed = true; }
                if (!this.isTrue(normalized.getDeletePrefillingSources())) { normalized.setDeletePrefillingSources(true); changed = true; }
            }
            if (this.isTrue(normalized.getDeletePrefillingSources()) && !this.isTrue(normalized.getDeletePlanBlueprints())) { normalized.setDeletePlanBlueprints(true); changed = true; }
            if ((this.isTrue(normalized.getDeletePlanBlueprints()) || this.isTrue(normalized.getDeleteDescriptionTemplates()) || this.isTrue(normalized.getDeleteStatusesAndWorkflows())
                    || this.isTrue(normalized.getDeleteReferences()) || this.isTrue(normalized.getDeleteTags()) || this.isTrue(normalized.getDeleteStorageFiles()))
                    && !this.isTrue(normalized.getDeletePlans())) { normalized.setDeletePlans(true); changed = true; }
        }
        return normalized;
    }

    private void purgeAnnotations(TenantPurgeReport report, List<PlanEntity> plans, List<DescriptionEntity> descriptions, boolean apply) {
        List<UUID> entityIds = new ArrayList<>();
        plans.forEach(x -> entityIds.add(x.getId()));
        descriptions.forEach(x -> entityIds.add(x.getId()));
        if (entityIds.isEmpty()) return;
        if (!this.annotationTransferClient.isConfigured()) {
            report.getWarnings().add("No annotation service url is configured (tenant-transfer.annotation-service-url); comments of the deleted plans were not purged.");
            return;
        }
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("entityIds", entityIds);
            String response = this.annotationTransferClient.post("/api/annotation/transfer/purge?dryRun=" + !apply, this.jsonHandlingService.toJson(request));
            AnnotationTransferReportModel annotationReport = this.jsonHandlingService.fromJsonSafe(AnnotationTransferReportModel.class, response);
            if (annotationReport != null) {
                if (annotationReport.getAnnotations() != null) report.addType("Annotation", annotationReport.getAnnotations().getTotal(), 0);
                if (annotationReport.getAnnotationStatuses() != null) report.addType("AnnotationStatus", annotationReport.getAnnotationStatuses().getTotal(), 0);
            }
        } catch (Exception e) {
            logger.error("failed to purge annotations", e);
            report.getWarnings().add("Purging annotations failed (" + e.getMessage() + "); comments of the deleted plans may remain in the annotation service.");
        }
    }

    private void publishRemovalEvents(List<PlanEntity> plans, List<DescriptionEntity> descriptions) {
        try {
            List<UUID> planIds = plans.stream().map(PlanEntity::getId).toList();
            if (!planIds.isEmpty()) this.planRemovalIntegrationEventHandler.handlePlan(planIds);
        } catch (Exception e) {
            logger.error("failed to send plan removal events after purge", e);
        }
        for (PlanEntity plan : plans) {
            try {
                this.annotationEntityRemovalIntegrationEventHandler.handlePlan(plan.getId());
            } catch (Exception e) {
                logger.error("failed to send annotation entity removal event for purged plan " + plan.getId(), e);
            }
        }
        for (DescriptionEntity description : descriptions) {
            try {
                this.annotationEntityRemovalIntegrationEventHandler.handleDescription(description.getId());
            } catch (Exception e) {
                logger.error("failed to send annotation entity removal event for purged description " + description.getId(), e);
            }
        }
    }

    private void removeFromElastic(List<PlanEntity> plans, List<DescriptionEntity> descriptions, TenantPurgeReport report) {
        try {
            if (!this.elasticService.enabled()) return;
            for (DescriptionEntity description : descriptions) this.elasticService.deleteDescription(description);
            for (PlanEntity plan : plans) this.elasticService.deletePlan(plan);
        } catch (Exception e) {
            logger.error("failed to remove purged entities from elastic", e);
            report.getWarnings().add("Removing purged plans from the search index failed; run the Maintenance Elasticsearch rebuild.");
        }
    }

    private boolean isTrue(Boolean value) {
        return value != null && value;
    }
}
