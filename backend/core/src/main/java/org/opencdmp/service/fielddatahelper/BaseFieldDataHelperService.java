package org.opencdmp.service.fielddatahelper;

import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.BaseFieldDataModel;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.BaseFieldDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.BaseFieldDataImportExport;
import org.opencdmp.model.descriptiontemplate.fielddata.BaseFieldData;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.BaseFieldDataPersist;

import java.util.EnumSet;
import java.util.List;
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
}
