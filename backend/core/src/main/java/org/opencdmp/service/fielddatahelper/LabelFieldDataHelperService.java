package org.opencdmp.service.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.validation.Validator;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.LabelDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.LabelDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.LabelDataImportExport;
import org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata.LabelDataCommonModelBuilder;
import org.opencdmp.model.builder.descriptiontemplate.fielddata.LabelDataBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.LabelData;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.LabelDataPersist;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component
public class LabelFieldDataHelperService extends BaseFieldDataHelperService<LabelData, LabelDataPersist, LabelDataEntity, LabelDataImportExport, LabelDataModel> {
	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public LabelFieldDataHelperService(BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}


	@Override
	public LabelDataEntity newDataInstanceInternal() {
		return new LabelDataEntity();
	}

	@Override
	public LabelData newModelInstanceInternal() {
		return new LabelData();
	}

	@Override
	public LabelDataPersist newPersistModelInstanceInternal() {
		return new LabelDataPersist();
	}

	@Override
	protected LabelDataImportExport newImportExportInstanceInternal() {
		return new LabelDataImportExport();
	}

	@Override
	protected LabelDataModel newCommonModelInstanceInternal() {
		return new LabelDataModel();
	}

	@Override
	public Class<?> getDataClass() {
		return LabelDataEntity.class;
	}
	@Override
	public Class<?> getModelClass() {
		return LabelData.class;
	}
	@Override
	public Class<?> getPersistModelClass() {
		return LabelDataPersist.class;
	}

	@Override
	public Class<?> getImportExportClass() {
		return LabelDataImportExport.class;
	}

	@Override
	public Class<?> getCommonModelClass() {
		return LabelDataModel.class;
	}
	
	@Override
	public Validator getPersistModelValidator() {
		return this.validatorFactory.validator(LabelDataPersist.LabelPersistValidator.class);
	}

	@Override
	public List<LabelData> buildInternal(FieldSet fieldSet, List<LabelDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags){
		return this.builderFactory.builder(LabelDataBuilder.class).authorize(authorizationFlags).build(fieldSet, data);
	}

	@Override
	protected List<LabelDataModel> buildCommonModelInternal(List<LabelDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(LabelDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}
	
	@Override
	protected LabelDataEntity applyPersistInternal(LabelDataPersist persist, LabelDataEntity data) {
		return data;
	}

	@Override
	protected LabelDataPersist importExportMapDataToPersistInternal(LabelDataImportExport data, LabelDataPersist persist) {
		return persist;
	}

	@Override
	protected LabelDataPersist commonModelMapDataToPersistInternal(LabelDataModel data, LabelDataPersist persist) {
		return persist;
	}

	@Override
	protected LabelDataImportExport dataToImportExportXmlInternal(LabelDataEntity data, LabelDataImportExport xml) {
		return xml;
	}

	@Override
	protected boolean isMultiValueInternal(LabelDataEntity data) {
		return false;
	}

}
