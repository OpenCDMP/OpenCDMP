package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.SystemFieldDataModel;
import org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata.SystemFieldDataCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.SystemFieldData;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;


@Component("dto.SystemFieldDataHelperService")
public class SystemFieldDataHelperService extends BaseFieldDataHelperService<SystemFieldData, SystemFieldDataModel> {
	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public SystemFieldDataHelperService(BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}


	@Override
	protected SystemFieldData newModelInstanceInternal() {
		return new SystemFieldData();
	}

	@Override
	protected SystemFieldDataModel newCommonModelInstanceInternal() {
		return new SystemFieldDataModel();
	}

	@Override
	protected List<SystemFieldData> buildInternal(List<SystemFieldDataModel> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(SystemFieldDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	public Class<?> getDataClass() {
		return SystemFieldDataModel.class;
	}

	@Override
	public Class<?> getModelClass() {
		return SystemFieldData.class;
	}
}
