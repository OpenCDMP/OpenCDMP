package org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddatahelper;

import gr.cite.tools.data.builder.BuilderFactory;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.descriptiotemplate.fielddata.UploadDataModel;
import org.opencdmp.model.builder.commonmodels.dto.descriptiontemplate.fielddata.UploadDataCommonModelBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.UploadData;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component("dto.UploadFieldDataHelperService")
public class UploadFieldDataHelperService extends BaseFieldDataHelperService<UploadData, UploadDataModel> {

	private final BuilderFactory builderFactory;

	public UploadFieldDataHelperService(BuilderFactory builderFactory) {
		this.builderFactory = builderFactory;
	}


	@Override
	protected UploadData newModelInstanceInternal() {
		return new UploadData();
	}

	@Override
	protected UploadDataModel newCommonModelInstanceInternal() {
		return new UploadDataModel();
	}

	@Override
	protected List<UploadData> buildInternal(List<UploadDataModel> data, EnumSet<AuthorizationFlags> authorizationFlags) {
		return this.builderFactory.builder(UploadDataCommonModelBuilder.class).authorize(authorizationFlags).build(data);
	}

	@Override
	public Class<?> getDataClass() {
		return UploadDataModel.class;
	}

	@Override
	public Class<?> getModelClass() {
		return UploadData.class;
	}
}
