package org.opencdmp.service.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.validation.Validator;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.NumberDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.NumberDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.NumberDataImportExport;

import org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata.NumberDataCommonModelBuilder;
import org.opencdmp.model.builder.descriptiontemplate.fielddata.NumberDataBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.NumberData;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.NumberDataPersist;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component
public class NumberFieldDataHelperService extends BaseFieldDataHelperService<NumberData, NumberDataPersist, NumberDataEntity, NumberDataImportExport, NumberDataModel> {
	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public NumberFieldDataHelperService(BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}


	@Override
	public NumberDataEntity newDataInstanceInternal() {
		return new NumberDataEntity();
	}

	@Override
	public NumberData newModelInstanceInternal() {
		return new NumberData();
	}

	@Override
	public NumberDataPersist newPersistModelInstanceInternal() {
		return new NumberDataPersist();
	}

	@Override
	protected NumberDataImportExport newImportExportInstanceInternal() {
		return new NumberDataImportExport();
	}

	@Override
	protected NumberDataModel newCommonModelInstanceInternal() {
		return new NumberDataModel();
	}

	@Override
	public Class<?> getDataClass() {
		return NumberDataEntity.class;
	}
	@Override
	public Class<?> getModelClass() {
		return NumberData.class;
	}
	@Override
	public Class<?> getPersistModelClass() {
		return NumberDataPersist.class;
	}

	@Override
	public Class<?> getImportExportClass() {
		return NumberDataImportExport.class;
	}

	@Override
	public Class<?> getCommonModelClass() {
		return NumberDataModel.class;
	}
	
	@Override
	public Validator getPersistModelValidator() {
		return this.validatorFactory.validator(NumberDataPersist.NumberDataPersistValidator.class);
	}

	@Override
	public List<NumberData> buildInternal(FieldSet fieldSet, List<NumberDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags){
		return this.builderFactory.builder(NumberDataBuilder.class).authorize(authorizationFlags).build(fieldSet, data);
	}

	@Override
	protected List<NumberDataModel> buildCommonModelInternal(List<NumberDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(NumberDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}
	
	@Override
	protected NumberDataEntity applyPersistInternal(NumberDataPersist persist, NumberDataEntity data) {
		return data;
	}

	@Override
	protected NumberDataPersist importExportMapDataToPersistInternal(NumberDataImportExport data, NumberDataPersist persist) {
		return persist;
	}

	@Override
	protected NumberDataPersist commonModelMapDataToPersistInternal(NumberDataModel data, NumberDataPersist persist) {
		return persist;
	}

	@Override
	protected NumberDataImportExport dataToImportExportXmlInternal(NumberDataEntity data, NumberDataImportExport xml) {
		return xml;
	}

	@Override
	protected boolean isMultiValueInternal(NumberDataEntity data) {
		return false;
	}

}
