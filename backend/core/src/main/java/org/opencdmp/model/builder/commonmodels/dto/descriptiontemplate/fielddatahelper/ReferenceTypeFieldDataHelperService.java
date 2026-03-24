package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.ReferenceTypeDataModel;
import org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata.ReferenceTypeDataCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.ReferenceTypeData;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component("dto.ReferenceTypeFieldDataHelperService")
public class ReferenceTypeFieldDataHelperService extends BaseFieldDataHelperService<ReferenceTypeData, ReferenceTypeDataModel> {

	private final BuilderFactory builderFactory;

	public ReferenceTypeFieldDataHelperService(BuilderFactory builderFactory) {
		this.builderFactory = builderFactory;
	}


	@Override
	protected ReferenceTypeData newModelInstanceInternal() {
		return new ReferenceTypeData();
	}

	@Override
	protected ReferenceTypeDataModel newCommonModelInstanceInternal() {
		return new ReferenceTypeDataModel();
	}

	@Override
	protected List<ReferenceTypeData> buildInternal(List<ReferenceTypeDataModel> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(ReferenceTypeDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	public Class<?> getDataClass() {
		return ReferenceTypeDataModel.class;
	}

	@Override
	public Class<?> getModelClass() {
		return ReferenceTypeData.class;
	}
}
