package org.opencdmp.service.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.validation.Validator;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.LabelAndMultiplicityDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.LabelAndMultiplicityDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.LabelAndMultiplicityDataImportExport;
import org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata.LabelAndMultiplicityDataCommonModelBuilder;
import org.opencdmp.model.builder.descriptiontemplate.fielddata.LabelAndMultiplicityDataBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.LabelAndMultiplicityData;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.LabelAndMultiplicityDataPersist;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component
public class LabelAndMultiplicityFieldDataHelperService extends BaseFieldDataHelperService<LabelAndMultiplicityData, LabelAndMultiplicityDataPersist, LabelAndMultiplicityDataEntity, LabelAndMultiplicityDataImportExport, LabelAndMultiplicityDataModel> {
	
	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public LabelAndMultiplicityFieldDataHelperService(BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}

	@Override
	public LabelAndMultiplicityDataEntity newDataInstanceInternal() {
		return new LabelAndMultiplicityDataEntity();
	}

	@Override
	public LabelAndMultiplicityData newModelInstanceInternal() {
		return new LabelAndMultiplicityData();
	}

	@Override
	public LabelAndMultiplicityDataPersist newPersistModelInstanceInternal() {
		return new LabelAndMultiplicityDataPersist();
	}

	@Override
	protected LabelAndMultiplicityDataImportExport newImportExportInstanceInternal() {
		return new LabelAndMultiplicityDataImportExport();
	}

	@Override
	protected LabelAndMultiplicityDataModel newCommonModelInstanceInternal() {
		return new LabelAndMultiplicityDataModel();
	}

	@Override
	public Class<?> getDataClass() {
		return LabelAndMultiplicityDataEntity.class;
	}
	@Override
	public Class<?> getModelClass() {
		return LabelAndMultiplicityData.class;
	}
	@Override
	public Class<?> getPersistModelClass() {
		return LabelAndMultiplicityDataPersist.class;
	}

	@Override
	public Class<?> getImportExportClass() {
		return LabelAndMultiplicityDataImportExport.class;
	}

	@Override
	public Class<?> getCommonModelClass() {
		return LabelAndMultiplicityDataModel.class;
	}
	
	@Override
	public Validator getPersistModelValidator() {
		return this.validatorFactory.validator(LabelAndMultiplicityDataPersist.LabelAndMultiplicityDataPersistValidator.class);
	}

	@Override
	public List<LabelAndMultiplicityData> buildInternal(FieldSet fieldSet, List<LabelAndMultiplicityDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags){
		return this.builderFactory.builder(LabelAndMultiplicityDataBuilder.class).authorize(authorizationFlags).build(fieldSet, data);
	}
	
	@Override
	protected List<LabelAndMultiplicityDataModel> buildCommonModelInternal(List<LabelAndMultiplicityDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(LabelAndMultiplicityDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}
	
	@Override
	protected LabelAndMultiplicityDataEntity applyPersistInternal(LabelAndMultiplicityDataPersist persist, LabelAndMultiplicityDataEntity data) {
		data.setMultipleSelect(persist.getMultipleSelect());
		return data;
	}

	@Override
	protected LabelAndMultiplicityDataPersist importExportMapDataToPersistInternal(LabelAndMultiplicityDataImportExport data, LabelAndMultiplicityDataPersist persist){
		persist.setMultipleSelect(data.getMultipleSelect());
		return persist;
	}

	@Override
	protected LabelAndMultiplicityDataImportExport dataToImportExportXmlInternal(LabelAndMultiplicityDataEntity data, LabelAndMultiplicityDataImportExport xml) {
		xml.setMultipleSelect(data.getMultipleSelect());
		return xml;
	}

	@Override
	protected LabelAndMultiplicityDataPersist commonModelMapDataToPersistInternal(LabelAndMultiplicityDataModel data, LabelAndMultiplicityDataPersist persist){
		persist.setMultipleSelect(data.getMultipleSelect());
		return persist;
	}

	@Override
	protected boolean isMultiValueInternal(LabelAndMultiplicityDataEntity data) {
		return data.getMultipleSelect();
	}

	
}
