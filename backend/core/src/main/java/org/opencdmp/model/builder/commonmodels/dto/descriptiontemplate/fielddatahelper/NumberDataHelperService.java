package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprintdescriptiontemplatefields.fielddata.NumberDataModel;
import org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata.NumberDataCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.NumberData;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;


@Component("dto.NumberDataHelperService")
public class NumberDataHelperService extends BaseFieldDataHelperService<NumberData, NumberDataModel> {
	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public NumberDataHelperService(BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}


	@Override
	protected NumberData newModelInstanceInternal() {
		return new NumberData();
	}

	@Override
	protected NumberDataModel newCommonModelInstanceInternal() {
		return new NumberDataModel();
	}

	@Override
	protected List<NumberData> buildInternal(List<NumberDataModel> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(NumberDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	public Class<?> getDataClass() {
		return NumberDataModel.class;
	}

	@Override
	public Class<?> getModelClass() {
		return NumberData.class;
	}
}
