package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.LabelAndMultiplicityDataModel;
import org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata.LabelAndMultiplicityDataCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.LabelAndMultiplicityData;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component("dto.LabelAndMultiplicityFieldDataHelperService")
public class LabelAndMultiplicityFieldDataHelperService extends BaseFieldDataHelperService<LabelAndMultiplicityData, LabelAndMultiplicityDataModel> {
	
	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public LabelAndMultiplicityFieldDataHelperService(BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}

	@Override
	public LabelAndMultiplicityData newModelInstanceInternal() {
		return new LabelAndMultiplicityData();
	}

	@Override
	protected LabelAndMultiplicityDataModel newCommonModelInstanceInternal() {
		return new LabelAndMultiplicityDataModel();
	}

	@Override
	public Class<?> getDataClass() {
		return LabelAndMultiplicityDataModel.class;
	}
	@Override
	public Class<?> getModelClass() {
		return LabelAndMultiplicityData.class;
	}

	@Override
	public List<LabelAndMultiplicityData> buildInternal(List<LabelAndMultiplicityDataModel> data, EnumSet<AuthorizationFlags> authorizationFlags){
		return this.builderFactory.builder(LabelAndMultiplicityDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}




}
