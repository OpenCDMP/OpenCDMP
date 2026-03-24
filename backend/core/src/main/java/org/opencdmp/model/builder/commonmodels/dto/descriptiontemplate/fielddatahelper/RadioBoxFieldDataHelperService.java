package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.validation.ValidatorFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.RadioBoxDataModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata.RadioBoxDataCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.RadioBoxData;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component("dto.RadioBoxFieldDataHelperService")
public class RadioBoxFieldDataHelperService extends BaseFieldDataHelperService<RadioBoxData, RadioBoxDataModel> {

	private final ConventionService conventionService;
	private final BuilderFactory builderFactory;

	private final ValidatorFactory validatorFactory;
	public RadioBoxFieldDataHelperService(ConventionService conventionService, BuilderFactory builderFactory, ValidatorFactory validatorFactory) {
		this.conventionService = conventionService;
		this.builderFactory = builderFactory;
		this.validatorFactory = validatorFactory;
	}


	@Override
	protected RadioBoxData newModelInstanceInternal() {
		return new RadioBoxData();
	}

	@Override
	protected RadioBoxDataModel newCommonModelInstanceInternal() {
		return new RadioBoxDataModel();
	}

	@Override
	protected List<RadioBoxData> buildInternal(List<RadioBoxDataModel> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(RadioBoxDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	public Class<?> getDataClass() {
		return RadioBoxDataModel.class;
	}

	@Override
	public Class<?> getModelClass() {
		return RadioBoxData.class;
	}
}
