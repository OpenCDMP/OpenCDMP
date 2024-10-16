package org.opencdmp.model.publicapi.overviewmodels;

import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.model.publicapi.datasetprofile.DatasetProfilePublicModel;
import org.opencdmp.model.publicapi.datasetwizard.*;
import org.opencdmp.model.publicapi.listingmodels.DataManagementPlanPublicListingModel;
import org.opencdmp.data.DescriptionEntity;

import java.util.*;

public class DatasetPublicModel {
    private UUID id;
    private String label;
    private String reference;
    private String uri;
    private String description;
    private DescriptionStatus status;
    private Date createdAt;
    private DataManagementPlanPublicListingModel dmp;
    private PagedDatasetProfile datasetProfileDefinition;
    private List<RegistryPublicModel> registries;
    private List<ServicePublicModel> services;
    private List<DataRepositoryPublicModel> dataRepositories;
//    private List<Tag> tags; //TODO
    private List<ExternalDatasetPublicListingModel> externalDatasets;
    private DatasetProfilePublicModel profile;
    private Date modifiedAt;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public DescriptionStatus getStatus() {
        return status;
    }
    public void setStatus(DescriptionStatus status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public DataManagementPlanPublicListingModel getDmp() {
        return dmp;
    }
    public void setDmp(DataManagementPlanPublicListingModel dmp) {
        this.dmp = dmp;
    }

    public PagedDatasetProfile getDatasetProfileDefinition() {
        return datasetProfileDefinition;
    }
    public void setDatasetProfileDefinition(PagedDatasetProfile datasetProfileDefinition) {
        this.datasetProfileDefinition = datasetProfileDefinition;
    }

    public List<RegistryPublicModel> getRegistries() {
        return registries;
    }
    public void setRegistries(List<RegistryPublicModel> registries) {
        this.registries = registries;
    }

    public List<ServicePublicModel> getServices() {
        return services;
    }
    public void setServices(List<ServicePublicModel> services) {
        this.services = services;
    }

    public List<DataRepositoryPublicModel> getDataRepositories() {
        return dataRepositories;
    }
    public void setDataRepositories(List<DataRepositoryPublicModel> dataRepositories) {
        this.dataRepositories = dataRepositories;
    }

    public DatasetProfilePublicModel getProfile() {
        return profile;
    }
    public void setProfile(DatasetProfilePublicModel profile) {
        this.profile = profile;
    }

    public List<ExternalDatasetPublicListingModel> getExternalDatasets() {
        return externalDatasets;
    }
    public void setExternalDatasets(List<ExternalDatasetPublicListingModel> externalDatasets) {
        this.externalDatasets = externalDatasets;
    }

//    public List<Tag> getTags() {
//        return tags;
//    }
//    public void setTags(List<Tag> tags) {
//        this.tags = tags;
//    }

    public Date getModifiedAt() {
        return modifiedAt;
    }
    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public DatasetPublicModel fromDataModel(DescriptionEntity entity) {
        //TODO:
//        this.id = entity.getId();
//        this.label = entity.getLabel();
//        this.reference = entity.getReference();
//        this.description = entity.getDescription();
//        this.status = entity.getStatus();
//        this.profile = new DatasetProfilePublicModel();
//        this.profile = this.profile.fromDataModel(entity.getProfile());
//        this.uri = entity.getUri();
//        this.registries = entity.getRegistries() != null ? entity.getRegistries().stream().map(item -> new RegistryPublicModel().fromDataModel(item)).collect(Collectors.toList()) : new ArrayList<>();
//        this.dataRepositories = entity.getDatasetDataRepositories() != null ? entity.getDatasetDataRepositories().stream().map(item -> {
//            DataRepositoryPublicModel dataRepository = new DataRepositoryPublicModel().fromDataModel(item.getDataRepository());
//            if (item.getData() != null) {
//                Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) JSONValue.parse(item.getData());
//                Map<String, String> values = data.get("data");
//                dataRepository.setInfo(values.get("info"));
//            }
//            return dataRepository;
//        }).collect(Collectors.toList()) : new ArrayList<>();
//        this.services = entity.getServices() != null ? entity.getServices().stream().map(item -> new ServicePublicModel().fromDataModel(item.getService())).collect(Collectors.toList()) : new ArrayList<>();
//        this.createdAt = entity.getCreated();
//        this.dmp = new DataManagementPlanPublicListingModel().fromDataModelNoDatasets(entity.getDmp());
//        this.externalDatasets = entity.getDatasetExternalDatasets() != null ? entity.getDatasetExternalDatasets().stream().map(item -> {
//            ExternalDatasetPublicListingModel externalDatasetListingModel = new ExternalDatasetPublicListingModel().fromDataModel(item.getExternalDataset());
//            if (item.getData() != null) {
//                Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) JSONValue.parse(item.getData());
//                Map<String, String> values = data.get("data");
//                externalDatasetListingModel.setInfo(values.get("info"));
//                externalDatasetListingModel.setType(Integer.parseInt(values.get("type")));
//            }
//            return externalDatasetListingModel;
//        }).collect(Collectors.toList()) : new ArrayList<>();
//        this.modifiedAt = entity.getModified();
        return this;
    }

    public DatasetPublicModel fromDataModelNoDmp(DescriptionEntity entity) {
        //TODO:
//        this.id = entity.getId();
//        this.label = entity.getLabel();
//        this.reference = entity.getReference();
//        this.description = entity.getDescription();
//        this.status = entity.getStatus();
//        this.profile = new DatasetProfilePublicModel();
//        this.profile = this.profile.fromDataModel(entity.getProfile());
//        this.uri = entity.getUri();
//        this.registries = entity.getRegistries() != null ? entity.getRegistries().stream().map(item -> new RegistryPublicModel().fromDataModel(item)).collect(Collectors.toList()) : new ArrayList<>();
//        this.dataRepositories = entity.getDatasetDataRepositories() != null ? entity.getDatasetDataRepositories().stream().map(item -> {
//            DataRepositoryPublicModel dataRepository = new DataRepositoryPublicModel().fromDataModel(item.getDataRepository());
//            if (item.getData() != null) {
//                Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) JSONValue.parse(item.getData());
//                Map<String, String> values = data.get("data");
//                dataRepository.setInfo(values.get("info"));
//            }
//            return dataRepository;
//        }).collect(Collectors.toList()) : new ArrayList<>();
//        this.services = entity.getServices() != null ? entity.getServices().stream().map(item -> new ServicePublicModel().fromDataModel(item.getService())).collect(Collectors.toList()) : new ArrayList<>();
//        this.createdAt = entity.getCreated();
//        this.externalDatasets = entity.getDatasetExternalDatasets() != null ? entity.getDatasetExternalDatasets().stream().map(item -> {
//            ExternalDatasetPublicListingModel externalDatasetListingModel = new ExternalDatasetPublicListingModel().fromDataModel(item.getExternalDataset());
//            if (item.getData() != null) {
//                Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) JSONValue.parse(item.getData());
//                Map<String, String> values = data.get("data");
//                externalDatasetListingModel.setInfo(values.get("info"));
//                externalDatasetListingModel.setType(Integer.parseInt(values.get("type")));
//            }
//            return externalDatasetListingModel;
//        }).collect(Collectors.toList()) : new ArrayList<>();
//        this.modifiedAt = entity.getModified();
        return this;
    }

    public DescriptionEntity toDataModel() throws Exception {
        //TODO:
        DescriptionEntity entity = new DescriptionEntity();
//        entity.setId(this.id);
//        entity.setLabel(this.label);
//        entity.setStatus(this.status);
//        entity.setReference(this.reference);
//        entity.setUri(this.uri);
//        entity.setFinalizedAt(new Date());
//        DMP dmp = new DMP();
//        dmp.setId(UUID.fromString(this.dmp.getId()));
//        entity.setDmp(dmp);
//        entity.setDescription(this.description);
//        entity.setCreated(this.createdAt != null ? this.createdAt : new Date());
//        entity.setModified(new Date());
//        DescriptionTemplateEntity profile = new DescriptionTemplateEntity();
//        profile.setId(this.profile.getId());
//        entity.setProfile(profile);
//        if (this.registries != null && !this.registries.isEmpty()) {
//            entity.setRegistries(new HashSet<>());
//            for (RegistryPublicModel registry : this.registries) {
//                entity.getRegistries().add(registry.toDataModel());
//            }
//        }
//
//        if (this.dataRepositories != null && !this.dataRepositories.isEmpty()) {
//            entity.setDatasetDataRepositories(new HashSet<>());
//            for (DataRepositoryPublicModel dataRepositoryModel : this.dataRepositories) {
//                DataRepository dataRepository = dataRepositoryModel.toDataModel();
//                DatasetDataRepository datasetDataRepository = new DatasetDataRepository();
//                datasetDataRepository.setDataRepository(dataRepository);
//                Map<String, Map<String, String>> data = new HashMap<>();
//                Map<String, String> values = new HashMap<>();
//                values.put("info", dataRepositoryModel.getInfo());
//                data.put("data", values);
//                datasetDataRepository.setData(JSONValue.toJSONString(data));
//                entity.getDatasetDataRepositories().add(datasetDataRepository);
//            }
//        }
//
//        if (this.services != null && !this.services.isEmpty()) {
//            entity.setServices(new HashSet<>());
//            for (ServicePublicModel serviceModel : this.services) {
//                Service service = serviceModel.toDataModel();
//                DatasetService datasetService = new DatasetService();
//                datasetService.setService(service);
//                entity.getServices().add(datasetService);
//            }
//        }
//
//        if (this.externalDatasets != null && !this.externalDatasets.isEmpty()) {
//            entity.setDatasetExternalDatasets(new HashSet<>());
//            for (ExternalDatasetPublicListingModel externalDataset : this.externalDatasets) {
//                ExternalDataset externalDatasetEntity = externalDataset.toDataModel();
//                DatasetExternalDataset datasetExternalDataset = new DatasetExternalDataset();
//                datasetExternalDataset.setExternalDataset(externalDatasetEntity);
//                Map<String,Map<String,String>> data = new HashMap<>();
//                Map<String,String> values = new HashMap<>();
//                values.put("info",externalDataset.getInfo());
//                values.put("type",externalDataset.getType().toString());
//                data.put("data",values);
//                datasetExternalDataset.setData(JSONValue.toJSONString(data));
//                entity.getDatasetExternalDatasets().add(datasetExternalDataset);
//            }
//        }
        return entity;
    }
    
    public String getHint() {
        return "datasetOverviewModel";
    }
}
