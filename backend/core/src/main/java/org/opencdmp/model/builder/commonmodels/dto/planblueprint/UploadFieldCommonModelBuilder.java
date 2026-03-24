package org.opencdmp.model.builder.commonmodels.dto.planblueprint;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprint.UploadFieldModel;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.opencdmp.model.planblueprint.UploadField;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component("dto.UploadFieldCommonModelBuilder")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadFieldCommonModelBuilder extends FieldCommonModelBuilder<UploadField, UploadFieldModel> {

	private final BuilderFactory builderFactory;
	@Autowired
	public UploadFieldCommonModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
		super(conventionService);
        this.builderFactory = builderFactory;
    }

	protected UploadField getInstance() {
		return new UploadField();
	}

	protected UploadField buildChild(UploadFieldModel data, UploadField model) {
		model.setMaxFileSizeInMB(data.getMaxFileSizeInMB());
		model.setTypes(this.builderFactory.builder(UploadOptionCommonModelBuilder.class).build(data.getTypes()));

		return model;
	}

	@Component("dto.UploadOptionCommonModelBuilder")
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class UploadOptionCommonModelBuilder extends BaseCommonModelBuilder<UploadField.UploadOption, UploadFieldModel.UploadOptionModel> {
		private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
		@Autowired
		public UploadOptionCommonModelBuilder(
				ConventionService conventionService
		) {
			super(conventionService, new LoggerService(LoggerFactory.getLogger(UploadOptionCommonModelBuilder.class)));
		}

		public UploadOptionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
			this.authorize = values;
			return this;
		}


		@Override
		protected List<CommonModelBuilderItemResponse<UploadField.UploadOption, UploadFieldModel.UploadOptionModel>> buildInternal(List<UploadFieldModel.UploadOptionModel> data) throws MyApplicationException {
			this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
			if (data == null || data.isEmpty()) return new ArrayList<>();

			List<CommonModelBuilderItemResponse<UploadField.UploadOption,UploadFieldModel.UploadOptionModel>> models = new ArrayList<>();
			for (UploadFieldModel.UploadOptionModel d : data) {
				UploadField.UploadOption m = new UploadField.UploadOption();
				m.setLabel(d.getLabel());
				m.setValue(d.getValue());
				models.add(new CommonModelBuilderItemResponse<>(m, d));
			}

			this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

			return models;
		}
	}
}
