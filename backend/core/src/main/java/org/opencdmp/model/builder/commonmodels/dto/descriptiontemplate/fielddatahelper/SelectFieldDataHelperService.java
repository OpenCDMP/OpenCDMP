package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.SelectDataModel;
import org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata.SelectDataCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.SelectData;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component("dto.SelectFieldDataHelperService")
public class SelectFieldDataHelperService extends BaseFieldDataHelperService<SelectData, SelectDataModel> {

	private final BuilderFactory builderFactory;

	public SelectFieldDataHelperService(BuilderFactory builderFactory) {
		this.builderFactory = builderFactory;
	}


	@Override
	protected SelectData newModelInstanceInternal() {
		return new SelectData();
	}

	@Override
	protected SelectDataModel newCommonModelInstanceInternal() {
		return new SelectDataModel();
	}

	@Override
	protected List<SelectData> buildInternal(List<SelectDataModel> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(SelectDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	public Class<?> getDataClass() {
		return SelectDataModel.class;
	}

	@Override
	public Class<?> getModelClass() {
		return SelectData.class;
	}
}
