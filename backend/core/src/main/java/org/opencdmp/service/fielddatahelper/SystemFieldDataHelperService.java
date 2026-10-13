package org.opencdmp.service.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.validation.Validator;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SystemFieldDataModel;
import org.opencdmp.commons.enums.SystemFieldType;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SystemFieldDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.SystemFieldDataImportExport;
import org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata.SystemFieldDataCommonModelBuilder;
import org.opencdmp.model.builder.descriptiontemplate.fielddata.SystemFieldDataBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.SystemFieldData;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.SystemFieldDataPersist;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component
public class SystemFieldDataHelperService extends BaseFieldDataHelperService<SystemFieldData, SystemFieldDataPersist, SystemFieldDataEntity, SystemFieldDataImportExport, SystemFieldDataModel> {

	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public SystemFieldDataHelperService(BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}


	@Override
	public SystemFieldDataEntity newDataInstanceInternal() {
		return new SystemFieldDataEntity();
	}

	@Override
	public SystemFieldData newModelInstanceInternal() {
		return new SystemFieldData();
	}

	@Override
	public SystemFieldDataPersist newPersistModelInstanceInternal() {
		return new SystemFieldDataPersist();
	}

	@Override
	protected SystemFieldDataImportExport newImportExportInstanceInternal() {
		return new SystemFieldDataImportExport();
	}

	@Override
	protected SystemFieldDataModel newCommonModelInstanceInternal() {
		return new SystemFieldDataModel();
	}

	@Override
	public Class<?> getDataClass() {
		return SystemFieldDataEntity.class;
	}
	@Override
	public Class<?> getModelClass() {
		return SystemFieldData.class;
	}
	@Override
	public Class<?> getPersistModelClass() {
		return SystemFieldDataPersist.class;
	}

	@Override
	public Class<?> getImportExportClass() {
		return SystemFieldDataImportExport.class;
	}

	@Override
	public Class<?> getCommonModelClass() {
		return SystemFieldDataModel.class;
	}


	@Override
	public Validator getPersistModelValidator() {
		return this.validatorFactory.validator(SystemFieldDataPersist.SystemFieldPersistValidator.class);
	}

	@Override
	public List<SystemFieldData> buildInternal(FieldSet fieldSet, List<SystemFieldDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags){
		return this.builderFactory.builder(SystemFieldDataBuilder.class).authorize(authorizationFlags).build(fieldSet, data);
	}

	@Override
	protected List<SystemFieldDataModel> buildCommonModelInternal(List<SystemFieldDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(SystemFieldDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	protected SystemFieldDataEntity applyPersistInternal(SystemFieldDataPersist persist, SystemFieldDataEntity data) {
		data.setSystemFieldType(persist.getSystemFieldType());
		return data;
	}

	@Override
	protected SystemFieldDataPersist importExportMapDataToPersistInternal(SystemFieldDataImportExport data, SystemFieldDataPersist persist) {
		persist.setSystemFieldType(data.getSystemFieldType());
		return persist;
	}

	@Override
	protected SystemFieldDataPersist commonModelMapDataToPersistInternal(SystemFieldDataModel data, SystemFieldDataPersist persist) {
		switch (data.getSystemFieldType()){
			case PlanTitle -> persist.setSystemFieldType(SystemFieldType.PlanTitle);
			case PlanDescription -> persist.setSystemFieldType(SystemFieldType.PlanDescription);
			case PlanLanguage -> persist.setSystemFieldType(SystemFieldType.PlanLanguage);
			case PlanContact -> persist.setSystemFieldType(SystemFieldType.PlanContact);
			case PlanAccessRights -> persist.setSystemFieldType(SystemFieldType.PlanAccessRights);
			case PlanUser -> persist.setSystemFieldType(SystemFieldType.PlanUser);
			case DescriptionTitle -> persist.setSystemFieldType(SystemFieldType.DescriptionTitle);
			case DescriptionDescription -> persist.setSystemFieldType(SystemFieldType.DescriptionDescription);
			case DescriptionTag -> persist.setSystemFieldType(SystemFieldType.DescriptionTag);
			case null -> persist.setSystemFieldType(null);
			default -> throw new MyApplicationException("unrecognized type " + data.getSystemFieldType());
		}
		return persist;
	}

	@Override
	protected SystemFieldDataImportExport dataToImportExportXmlInternal(SystemFieldDataEntity data, SystemFieldDataImportExport xml) {
		xml.setSystemFieldType(data.getSystemFieldType());
		return xml;
	}

	@Override
	protected boolean isMultiValueInternal(SystemFieldDataEntity data) {
		return false;
	}

}
