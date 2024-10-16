package org.opencdmp.service.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.validation.Validator;
import gr.cite.tools.validation.ValidatorFactory;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.RadioBoxDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.RadioBoxDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.RadioBoxDataImportExport;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata.RadioBoxDataCommonModelBuilder;
import org.opencdmp.model.builder.descriptiontemplate.fielddata.RadioBoxDataBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.RadioBoxData;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.RadioBoxDataPersist;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Component
public class RadioBoxFieldDataHelperService extends BaseFieldDataHelperService<RadioBoxData, RadioBoxDataPersist, RadioBoxDataEntity, RadioBoxDataImportExport, RadioBoxDataModel> {

	private final ConventionService conventionService;
	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public RadioBoxFieldDataHelperService(ConventionService conventionService, BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.conventionService = conventionService;
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}


	@Override
	public RadioBoxDataEntity newDataInstanceInternal() {
		return new RadioBoxDataEntity();
	}

	@Override
	public RadioBoxData newModelInstanceInternal() {
		return new RadioBoxData();
	}

	@Override
	public RadioBoxDataPersist newPersistModelInstanceInternal() {
		return new RadioBoxDataPersist();
	}

	@Override
	protected RadioBoxDataImportExport newImportExportInstanceInternal() {
		return new RadioBoxDataImportExport();
	}

	@Override
	protected RadioBoxDataModel newCommonModelInstanceInternal() {
		return new RadioBoxDataModel();
	}

	@Override
	public Class<?> getDataClass() {
		return RadioBoxDataEntity.class;
	}
	@Override
	public Class<?> getModelClass() {
		return RadioBoxData.class;
	}
	@Override
	public Class<?> getPersistModelClass() {
		return RadioBoxDataPersist.class;
	}

	@Override
	public Class<?> getImportExportClass() {
		return RadioBoxDataImportExport.class;
	}

	@Override
	public Class<?> getCommonModelClass() {
		return RadioBoxDataModel.class;
	}

	@Override
	public Validator getPersistModelValidator() {
		return this.validatorFactory.validator(RadioBoxDataPersist.RadioBoxDataPersistValidator.class);
	}

	@Override
	public List<RadioBoxData> buildInternal(FieldSet fieldSet, List<RadioBoxDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags){
		return this.builderFactory.builder(RadioBoxDataBuilder.class).authorize(authorizationFlags).build(fieldSet, data);
	}

	@Override
	protected List<RadioBoxDataModel> buildCommonModelInternal(List<RadioBoxDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(RadioBoxDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	protected RadioBoxDataEntity applyPersistInternal(RadioBoxDataPersist persist, RadioBoxDataEntity data) {
		if (!this.conventionService.isListNullOrEmpty(persist.getOptions())){
			data.setOptions(new ArrayList<>());
			for (RadioBoxDataPersist.RadioBoxOptionPersist radioBoxOptionPersist: persist.getOptions()) {
				data.getOptions().add(this.buildOption(radioBoxOptionPersist));
			}
		}
		return data;
	}

	private @NotNull RadioBoxDataEntity.RadioBoxDataOptionEntity buildOption(RadioBoxDataPersist.RadioBoxOptionPersist persist){
		RadioBoxDataEntity.RadioBoxDataOptionEntity data = new RadioBoxDataEntity.RadioBoxDataOptionEntity();
		if (persist == null) return data;

		data.setLabel(persist.getLabel());
		data.setValue(persist.getValue());

		return data;
	}

	@Override
	protected RadioBoxDataPersist importExportMapDataToPersistInternal(RadioBoxDataImportExport data, RadioBoxDataPersist persist){
		if (!this.conventionService.isListNullOrEmpty(data.getOptions())){
			persist.setOptions(new ArrayList<>());
			for (RadioBoxDataImportExport.RadioBoxOption radioBoxOption: data.getOptions()) {
				persist.getOptions().add(this.buildOption(radioBoxOption));
			}
		}
		return persist;
	}

	private @NotNull RadioBoxDataPersist.RadioBoxOptionPersist buildOption(RadioBoxDataImportExport.RadioBoxOption data){
		RadioBoxDataPersist.RadioBoxOptionPersist persist = new RadioBoxDataPersist.RadioBoxOptionPersist();
		if (data == null) return persist;

		persist.setLabel(data.getLabel());
		persist.setValue(data.getValue());

		return persist;
	}



	@Override
	protected RadioBoxDataPersist commonModelMapDataToPersistInternal(RadioBoxDataModel data, RadioBoxDataPersist persist){
		if (!this.conventionService.isListNullOrEmpty(data.getOptions())){
			persist.setOptions(new ArrayList<>());
			for (RadioBoxDataModel.RadioBoxOptionModel radioBoxOption: data.getOptions()) {
				persist.getOptions().add(this.buildOption(radioBoxOption));
			}
		}
		return persist;
	}

	private @NotNull RadioBoxDataPersist.RadioBoxOptionPersist buildOption(RadioBoxDataModel.RadioBoxOptionModel data){
		RadioBoxDataPersist.RadioBoxOptionPersist persist = new RadioBoxDataPersist.RadioBoxOptionPersist();
		if (data == null) return persist;

		persist.setLabel(data.getLabel());
		persist.setValue(data.getValue());

		return persist;
	}

	@Override
	protected RadioBoxDataImportExport dataToImportExportXmlInternal(RadioBoxDataEntity data, RadioBoxDataImportExport xml) {
		if (!this.conventionService.isListNullOrEmpty(data.getOptions())){
			xml.setOptions(new ArrayList<>());
			for (RadioBoxDataEntity.RadioBoxDataOptionEntity radioBoxOption: data.getOptions()) {
				xml.getOptions().add(this.buildOption(radioBoxOption));
			}
		}
		return xml;
	}

	@Override
	protected boolean isMultiValueInternal(RadioBoxDataEntity data) {
		return false;
	}

	private @NotNull RadioBoxDataImportExport.RadioBoxOption buildOption(RadioBoxDataEntity.RadioBoxDataOptionEntity data){
		RadioBoxDataImportExport.RadioBoxOption xml = new RadioBoxDataImportExport.RadioBoxOption();
		if (data == null) return xml;

		xml.setLabel(data.getLabel());
		xml.setValue(data.getValue());

		return xml;
	}

}
