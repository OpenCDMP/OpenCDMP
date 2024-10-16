package org.opencdmp.service.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.validation.Validator;
import gr.cite.tools.validation.ValidatorFactory;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.UploadDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.UploadDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.UploadDataImportExport;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata.UploadDataCommonModelBuilder;
import org.opencdmp.model.builder.descriptiontemplate.fielddata.UploadDataBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.UploadData;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.UploadDataPersist;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Component
public class UploadFieldDataHelperService extends BaseFieldDataHelperService<UploadData, UploadDataPersist, UploadDataEntity, UploadDataImportExport, UploadDataModel> {

	private final ConventionService conventionService;
	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public UploadFieldDataHelperService(ConventionService conventionService, BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.conventionService = conventionService;
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}


	@Override
	public UploadDataEntity newDataInstanceInternal() {
		return new UploadDataEntity();
	}

	@Override
	public UploadData newModelInstanceInternal() {
		return new UploadData();
	}

	@Override
	public UploadDataPersist newPersistModelInstanceInternal() {
		return new UploadDataPersist();
	}

	@Override
	protected UploadDataImportExport newImportExportInstanceInternal() {
		return new UploadDataImportExport();
	}

	@Override
	protected UploadDataModel newCommonModelInstanceInternal() {
		return new UploadDataModel();
	}

	@Override
	public Class<?> getDataClass() {
		return UploadDataEntity.class;
	}
	@Override
	public Class<?> getModelClass() {
		return UploadData.class;
	}
	@Override
	public Class<?> getPersistModelClass() {
		return UploadDataPersist.class;
	}

	@Override
	public Class<?> getImportExportClass() {
		return UploadDataImportExport.class;
	}

	@Override
	public Class<?> getCommonModelClass() {
		return UploadDataModel.class;
	}


	@Override
	public Validator getPersistModelValidator() {
		return this.validatorFactory.validator(UploadDataPersist.UploadDataPersistValidator.class);
	}

	@Override
	public List<UploadData> buildInternal(FieldSet fieldSet, List<UploadDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags){
		return this.builderFactory.builder(UploadDataBuilder.class).authorize(authorizationFlags).build(fieldSet, data);
	}

	@Override
	protected List<UploadDataModel> buildCommonModelInternal(List<UploadDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(UploadDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	protected UploadDataEntity applyPersistInternal(UploadDataPersist persist, UploadDataEntity data) {
		if (!this.conventionService.isListNullOrEmpty(persist.getTypes())){
			data.setTypes(new ArrayList<>());
			for (UploadDataPersist.UploadOptionPersist uploadOptionPersist: persist.getTypes()) {
				data.getTypes().add(this.buildOption(uploadOptionPersist));
			}
		}
		data.setMaxFileSizeInMB(persist.getMaxFileSizeInMB());
		return data;
	}

	private @NotNull UploadDataEntity.UploadDataOptionEntity buildOption(UploadDataPersist.UploadOptionPersist persist){
		UploadDataEntity.UploadDataOptionEntity data = new UploadDataEntity.UploadDataOptionEntity();
		if (persist == null) return data;

		data.setLabel(persist.getLabel());
		data.setValue(persist.getValue());

		return data;
	}

	@Override
	protected UploadDataPersist importExportMapDataToPersistInternal(UploadDataImportExport data, UploadDataPersist persist) {
		if (!this.conventionService.isListNullOrEmpty(data.getTypes())){
			persist.setTypes(new ArrayList<>());
			for (UploadDataImportExport.UploadDataOption option: data.getTypes()) {
				persist.getTypes().add(this.buildOption(option));
			}
		}
		persist.setMaxFileSizeInMB(data.getMaxFileSizeInMB());
		return persist;
	}

	private @NotNull UploadDataPersist.UploadOptionPersist buildOption(UploadDataImportExport.UploadDataOption data){
		UploadDataPersist.UploadOptionPersist persist = new UploadDataPersist.UploadOptionPersist();
		if (data == null) return persist;

		persist.setLabel(data.getLabel());
		persist.setValue(data.getValue());

		return persist;
	}

	protected UploadDataPersist commonModelMapDataToPersistInternal(UploadDataModel data, UploadDataPersist persist) {
		if (!this.conventionService.isListNullOrEmpty(data.getTypes())){
			persist.setTypes(new ArrayList<>());
			for (UploadDataModel.UploadOptionModel option: data.getTypes()) {
				persist.getTypes().add(this.buildOption(option));
			}
		}
//		persist.setMaxFileSizeInMB(data.getMaxFileSizeInMB()); //TODO: Add to common model ?
		return persist;
	}

	private @NotNull UploadDataPersist.UploadOptionPersist buildOption(UploadDataModel.UploadOptionModel data){
		UploadDataPersist.UploadOptionPersist persist = new UploadDataPersist.UploadOptionPersist();
		if (data == null) return persist;

		persist.setLabel(data.getLabel());
		persist.setValue(data.getValue());

		return persist;
	}
	
	@Override
	protected UploadDataImportExport dataToImportExportXmlInternal(UploadDataEntity data, UploadDataImportExport xml) {
		if (!this.conventionService.isListNullOrEmpty(data.getTypes())){
			xml.setTypes(new ArrayList<>());
			for (UploadDataEntity.UploadDataOptionEntity option: data.getTypes()) {
				xml.getTypes().add(this.buildOption(option));
			}
		}
		xml.setMaxFileSizeInMB(data.getMaxFileSizeInMB());
		return xml;
	}

	@Override
	protected boolean isMultiValueInternal(UploadDataEntity data) {
		return false;
	}

	private @NotNull UploadDataImportExport.UploadDataOption buildOption(UploadDataEntity.UploadDataOptionEntity data){
		UploadDataImportExport.UploadDataOption xml = new UploadDataImportExport.UploadDataOption();
		if (data == null) return xml;

		xml.setLabel(data.getLabel());
		xml.setValue(data.getValue());

		return xml;
	}
}
