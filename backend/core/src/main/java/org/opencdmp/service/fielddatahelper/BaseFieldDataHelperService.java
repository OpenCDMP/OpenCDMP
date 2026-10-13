package org.opencdmp.service.fielddatahelper;

import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.BaseFieldDataModel;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SemanticValueModel;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.BaseFieldDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SemanticValueEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.BaseFieldDataImportExport;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.SemanticValueImportExport;
import org.opencdmp.model.descriptiontemplate.fielddata.BaseFieldData;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.BaseFieldDataPersist;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.SemanticValuePersist;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class BaseFieldDataHelperService<M extends BaseFieldData, PM extends BaseFieldDataPersist, D extends BaseFieldDataEntity, IE extends BaseFieldDataImportExport, CM extends BaseFieldDataModel> implements FieldDataHelperService {
	protected FieldType fieldType;
	
	protected abstract D newDataInstanceInternal();
	protected abstract M newModelInstanceInternal();
	protected abstract PM newPersistModelInstanceInternal();
	protected abstract IE newImportExportInstanceInternal();
	protected abstract CM newCommonModelInstanceInternal();
	
	protected abstract List<M> buildInternal(FieldSet fieldSet, List<D> data, EnumSet<AuthorizationFlags> authorizationFlags);
	protected abstract D applyPersistInternal(PM persist, D data);
	protected abstract PM importExportMapDataToPersistInternal(IE data, PM persist);
	protected abstract PM commonModelMapDataToPersistInternal(CM data, PM persist);
	protected abstract IE dataToImportExportXmlInternal(D data, IE xml);
	protected abstract boolean isMultiValueInternal(D data);
	protected abstract List<CM> buildCommonModelInternal(List<D> data, EnumSet<AuthorizationFlags> authorizationFlags);

	@Override
	public FieldType getFieldType() {
		return this.fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	@Override
	public BaseFieldDataEntity newDataInstance() {
		return this.newDataInstanceInternal();
	}

	@Override
	public BaseFieldData newModelInstance() {
		return this.newModelInstanceInternal();
	}

	@Override
	public BaseFieldDataModel newCommonModelInstance() {
		return this.newCommonModelInstanceInternal();
	}

	@Override
	public BaseFieldDataPersist newPersistModelInstance() {
		return this.newPersistModelInstanceInternal();
	}

	@Override
	public BaseFieldDataImportExport newImportExportInstance() {
		return this.newImportExportInstanceInternal();
	}
	
	@Override
	public BaseFieldDataPersist importExportMapDataToPersist(BaseFieldDataImportExport data) {
		PM model = this.newPersistModelInstanceInternal();
		model.setFieldType(this.getFieldType());
		model.setLabel(data.getLabel());
		return this.importExportMapDataToPersistInternal((IE)data, model);
	}



	@Override
	public BaseFieldDataPersist commonModelMapDataToPersist(BaseFieldDataModel data) {
		PM model = this.newPersistModelInstanceInternal();
		model.setFieldType(this.getFieldType());
		model.setLabel(data.getLabel());
		return this.commonModelMapDataToPersistInternal((CM)data, model);
	}

	@Override
	public BaseFieldDataImportExport dataToImportExportXml(BaseFieldDataEntity data){
		IE xml = this.newImportExportInstanceInternal();
		xml.setLabel(data.getLabel());
		xml.setFieldType(data.getFieldType());
		return this.dataToImportExportXmlInternal((D)data, xml);
	}

	@Override
	public boolean isMultiValue(BaseFieldDataEntity data){
		return this.isMultiValueInternal((D)data);
	}
	@Override
	public BaseFieldDataEntity applyPersist(BaseFieldDataPersist persist){
		BaseFieldDataEntity instance = this.newDataInstance();
		return this.applyPersist(persist, instance);
	}
	@Override
	public BaseFieldDataEntity applyPersist(BaseFieldDataPersist persist, BaseFieldDataEntity data){
		data.setLabel(persist.getLabel());
		data.setFieldType(persist.getFieldType());
		return this.applyPersistInternal((PM)persist, (D)data);
	}
	
	@Override
	public BaseFieldData buildOne(FieldSet fieldSet, BaseFieldDataEntity data, EnumSet<AuthorizationFlags> authorizationFlags){
		List<BaseFieldData> models = this.build(fieldSet, List.of(data), authorizationFlags);
		if (models == null || models.isEmpty()) return null;
		return models.get(0);
	}

	@Override
	public BaseFieldDataModel buildCommonModelOne(BaseFieldDataEntity data, EnumSet<AuthorizationFlags> authorizationFlags){
		List<BaseFieldDataModel> models = this.buildCommonModelInternal(List.of((D) data), authorizationFlags).stream().map(x-> (CM)x).collect(Collectors.toList());
		if (models == null || models.isEmpty()) return null;
		return models.getFirst();
	}

	@Override
	public List<BaseFieldData> build(FieldSet fieldSet, List<BaseFieldDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.buildInternal(fieldSet, data.stream().map(x-> (D) x).collect(Collectors.toList()), authorizationFlags).stream().map(x-> (M)x).collect(Collectors.toList());
	}
	protected List<SemanticValueEntity> buildSemanticValueEntities(List<SemanticValuePersist> persists) {
		if (persists == null || persists.isEmpty()) return null;
		return persists.stream().filter(Objects::nonNull).map(persist -> {
			SemanticValueEntity data = new SemanticValueEntity();
			data.setKey(persist.getKey());
			data.setValue(persist.getValue());
			return data;
		}).collect(Collectors.toList());
	}

	protected List<SemanticValuePersist> importExportSemanticValuesToPersist(List<SemanticValueImportExport> data) {
		if (data == null || data.isEmpty()) return null;
		return data.stream().filter(Objects::nonNull).map(xml -> {
			SemanticValuePersist persist = new SemanticValuePersist();
			persist.setKey(xml.getKey());
			persist.setValue(xml.getValue());
			return persist;
		}).collect(Collectors.toList());
	}

	protected List<SemanticValuePersist> commonModelSemanticValuesToPersist(List<SemanticValueModel> data) {
		if (data == null || data.isEmpty()) return null;
		return data.stream().filter(Objects::nonNull).map(model -> {
			SemanticValuePersist persist = new SemanticValuePersist();
			persist.setKey(model.getKey());
			persist.setValue(model.getValue());
			return persist;
		}).collect(Collectors.toList());
	}

	protected List<SemanticValueImportExport> semanticValuesToImportExport(List<SemanticValueEntity> data) {
		if (data == null || data.isEmpty()) return null;
		return data.stream().filter(Objects::nonNull).map(entity -> {
			SemanticValueImportExport xml = new SemanticValueImportExport();
			xml.setKey(entity.getKey());
			xml.setValue(entity.getValue());
			return xml;
		}).collect(Collectors.toList());
	}
}
