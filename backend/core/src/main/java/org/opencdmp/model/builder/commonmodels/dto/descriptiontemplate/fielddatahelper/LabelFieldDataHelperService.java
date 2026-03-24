package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.LabelDataModel;
import org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata.LabelDataCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.LabelData;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;


@Component("dto.LabelFieldDataHelperService")
public class LabelFieldDataHelperService extends BaseFieldDataHelperService<LabelData, LabelDataModel> {
	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public LabelFieldDataHelperService(BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}


	@Override
	protected LabelData newModelInstanceInternal() {
		return new LabelData();
	}

	@Override
	protected LabelDataModel newCommonModelInstanceInternal() {
		return new LabelDataModel();
	}

	@Override
	protected List<LabelData> buildInternal(List<LabelDataModel> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(LabelDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	public Class<?> getDataClass() {
		return LabelDataModel.class;
	}

	@Override
	public Class<?> getModelClass() {
		return LabelData.class;
	}
}
