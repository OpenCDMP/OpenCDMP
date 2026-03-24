package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper;

import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.enums.FieldType;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.BaseFieldDataModel;
import org.opencdmp.model.descriptiontemplate.fielddata.BaseFieldData;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseFieldDataHelperService<M extends BaseFieldData, CM extends BaseFieldDataModel> implements FieldDataHelperService {
	protected FieldType fieldType;

	protected abstract M newModelInstanceInternal();
	protected abstract CM newCommonModelInstanceInternal();
	
	protected abstract List<M> buildInternal(List<CM> data, EnumSet<AuthorizationFlags> authorizationFlags);

	@Override
	public FieldType getFieldType() {
		return this.fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}
	
	@Override
	public BaseFieldData buildOne(BaseFieldDataModel data, EnumSet<AuthorizationFlags> authorizationFlags){
		List<BaseFieldData> models = this.build(List.of(data), authorizationFlags);
		if (models == null || models.isEmpty()) return null;
		return models.get(0);
	}

	@Override
	public List<BaseFieldData> build(List<BaseFieldDataModel> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.buildInternal(data.stream().map(x-> (CM) x).collect(Collectors.toList()), authorizationFlags).stream().map(x-> (M)x).collect(Collectors.toList());
	}
}
