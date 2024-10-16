package org.opencdmp.service.fielddatahelper;

import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.validation.Validator;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.BaseFieldDataModel;
import org.opencdmp.commons.enums.FieldType;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.BaseFieldDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.importexport.fielddata.BaseFieldDataImportExport;
import org.opencdmp.model.descriptiontemplate.fielddata.BaseFieldData;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.BaseFieldDataPersist;

import java.util.EnumSet;
import java.util.List;

public interface FieldDataHelperService {
	FieldType getFieldType();
	BaseFieldDataEntity newDataInstance();
	BaseFieldData newModelInstance();
	BaseFieldDataModel newCommonModelInstance();
	BaseFieldDataPersist newPersistModelInstance();
	BaseFieldDataImportExport newImportExportInstance();
	Class<?> getDataClass();
	Class<?> getModelClass();
	Class<?> getPersistModelClass();
	Class<?> getCommonModelClass();
	Class<?> getImportExportClass();
	Validator getPersistModelValidator();
	List<BaseFieldData> build(gr.cite.tools.fieldset.FieldSet fieldSet, List<BaseFieldDataEntity> data, EnumSet<AuthorizationFlags> authorizationFlags);
	BaseFieldData buildOne(FieldSet fieldSet, BaseFieldDataEntity data, EnumSet<AuthorizationFlags> authorizationFlags);
	BaseFieldDataModel buildCommonModelOne(BaseFieldDataEntity data, EnumSet<AuthorizationFlags> authorizationFlags);
	BaseFieldDataEntity applyPersist(BaseFieldDataPersist persist);
	BaseFieldDataEntity applyPersist(BaseFieldDataPersist persist, BaseFieldDataEntity data);
	BaseFieldDataPersist importExportMapDataToPersist(BaseFieldDataImportExport xml);
	BaseFieldDataPersist commonModelMapDataToPersist(BaseFieldDataModel commonModel);
	BaseFieldDataImportExport dataToImportExportXml(BaseFieldDataEntity data);
	boolean isMultiValue(BaseFieldDataEntity data);
}
