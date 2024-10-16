package org.opencdmp.service.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.validation.Validator;
import gr.cite.tools.validation.ValidatorFactory;
import org.jetbrains.annotations.NotNull;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.SelectDataModel;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SelectDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.SelectDataImportExport;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.descriptiontemplate.fielddata.SelectDataCommonModelBuilder;
import org.opencdmp.model.builder.descriptiontemplate.fielddata.SelectDataBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.SelectData;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.SelectDataPersist;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Component
public class SelectFieldDataHelperService extends BaseFieldDataHelperService<SelectData, SelectDataPersist, SelectDataEntity, SelectDataImportExport, SelectDataModel> {

	private final ConventionService conventionService;
	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public SelectFieldDataHelperService(ConventionService conventionService, BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.conventionService = conventionService;
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}


	@Override
	public SelectDataEntity newDataInstanceInternal() {
		return new SelectDataEntity();
	}

	@Override
	public SelectData newModelInstanceInternal() {
		return new SelectData();
	}

	@Override
	public SelectDataPersist newPersistModelInstanceInternal() {
		return new SelectDataPersist();
	}

	@Override
	protected SelectDataImportExport newImportExportInstanceInternal() {
		return new SelectDataImportExport();
	}

	@Override
	protected SelectDataModel newCommonModelInstanceInternal() {
		return new SelectDataModel();
	}

	@Override
	public Class<?> getDataClass() {
		return SelectDataEntity.class;
	}
	@Override
	public Class<?> getModelClass() {
		return SelectData.class;
	}
	@Override
	public Class<?> getPersistModelClass() {
		return SelectDataPersist.class;
	}

	@Override
	public Class<?> getCommonModelClass() {
		return SelectDataModel.class;
	}

	@Override
	public Class<?> getImportExportClass() {
		return SelectDataImportExport.class;
	}

	@Override
	public Validator getPersistModelValidator() {
		return this.validatorFactory.validator(SelectDataPersist.SelectDataPersistValidator.class);
	}

	@Override
	public List<SelectData> buildInternal(FieldSet fieldSet, List<SelectDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags){
		return this.builderFactory.builder(SelectDataBuilder.class).authorize(authorizationFlags).build(fieldSet, data);
	}

	@Override
	protected List<SelectDataModel> buildCommonModelInternal(List<SelectDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(SelectDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	protected SelectDataEntity applyPersistInternal(SelectDataPersist persist, SelectDataEntity data) {
		data.setMultipleSelect(persist.getMultipleSelect());
		if (!this.conventionService.isListNullOrEmpty(persist.getOptions())){
			data.setOptions(new ArrayList<>());
			for (SelectDataPersist.OptionPersist optionPersist: persist.getOptions()) {
				data.getOptions().add(this.buildOption(optionPersist));
			}
		}
		return data;
	}


	private @NotNull SelectDataEntity.OptionEntity buildOption(SelectDataPersist.OptionPersist persist){
		SelectDataEntity.OptionEntity data = new SelectDataEntity.OptionEntity();
		if (persist == null) return data;

		data.setLabel(persist.getLabel());
		data.setValue(persist.getValue());

		return data;
	}

	@Override
	protected SelectDataPersist importExportMapDataToPersistInternal(SelectDataImportExport data, SelectDataPersist persist) {
		persist.setMultipleSelect(data.getMultipleSelect());
		if (!this.conventionService.isListNullOrEmpty(data.getOptions())){
			persist.setOptions(new ArrayList<>());
			for (SelectDataImportExport.OptionImportExport option: data.getOptions()) {
				persist.getOptions().add(this.buildOption(option));
			}
		}
		return persist;
	}

	private @NotNull SelectDataPersist.OptionPersist buildOption(SelectDataImportExport.OptionImportExport data){
		SelectDataPersist.OptionPersist persist = new SelectDataPersist.OptionPersist();
		if (data == null) return persist;

		persist.setLabel(data.getLabel());
		persist.setValue(data.getValue());

		return persist;
	}

	@Override
	protected SelectDataPersist commonModelMapDataToPersistInternal(SelectDataModel data, SelectDataPersist persist) {
		persist.setMultipleSelect(data.getMultipleSelect());
		if (!this.conventionService.isListNullOrEmpty(data.getOptions())){
			persist.setOptions(new ArrayList<>());
			for (SelectDataModel.OptionModel option: data.getOptions()) {
				persist.getOptions().add(this.buildOption(option));
			}
		}
		return persist;
	}

	private @NotNull SelectDataPersist.OptionPersist buildOption(SelectDataModel.OptionModel data){
		SelectDataPersist.OptionPersist persist = new SelectDataPersist.OptionPersist();
		if (data == null) return persist;

		persist.setLabel(data.getLabel());
		persist.setValue(data.getValue());

		return persist;
	}


	@Override
	protected SelectDataImportExport dataToImportExportXmlInternal(SelectDataEntity data, SelectDataImportExport xml) {
		xml.setMultipleSelect(data.getMultipleSelect());
		if (!this.conventionService.isListNullOrEmpty(data.getOptions())){
			xml.setOptions(new ArrayList<>());
			for (SelectDataEntity.OptionEntity option: data.getOptions()) {
				xml.getOptions().add(this.buildOption(option));
			}
		}
		return xml;
	}

	@Override
	protected boolean isMultiValueInternal(SelectDataEntity data) {
		return data.getMultipleSelect();
	}

	private @NotNull SelectDataImportExport.OptionImportExport buildOption(SelectDataEntity.OptionEntity data){
		SelectDataImportExport.OptionImportExport xml = new SelectDataImportExport.OptionImportExport();
		if (data == null) return xml;

		xml.setLabel(data.getLabel());
		xml.setValue(data.getValue());

		return xml;
	}

}
