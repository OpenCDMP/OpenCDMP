package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper;

import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.BaseFieldDataModel;
import org.opencdmp.model.descriptiontemplate.fielddata.BaseFieldData;

import java.util.EnumSet;
import java.util.List;

public interface FieldDataHelperService {
	FieldType getFieldType();
	Class<?> getDataClass();
	Class<?> getModelClass();
	List<BaseFieldData> build(List<BaseFieldDataModel> data, EnumSet<AuthorizationFlags> authorizationFlags);
	BaseFieldData buildOne(BaseFieldDataModel data, EnumSet<AuthorizationFlags> authorizationFlags);
}
