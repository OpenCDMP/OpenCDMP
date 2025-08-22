package org.opencdmp.model.builder.commonmodels.planblueprint;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commonmodels.models.planblueprint.UploadFieldModel;
import org.opencdmp.commons.types.planblueprint.UploadFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.commonmodels.BaseCommonModelBuilder;
import org.opencdmp.model.builder.commonmodels.CommonModelBuilderItemResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadFieldCommonModelBuilder extends FieldCommonModelBuilder<UploadFieldModel, UploadFieldEntity> {

	private final BuilderFactory builderFactory;
	@Autowired
	public UploadFieldCommonModelBuilder(
            ConventionService conventionService, BuilderFactory builderFactory) {
		super(conventionService);
        this.builderFactory = builderFactory;
    }

	protected UploadFieldModel getInstance() {
		return new UploadFieldModel();
	}

	protected UploadFieldModel buildChild(UploadFieldEntity data, UploadFieldModel model) {
		model.setMaxFileSizeInMB(data.getMaxFileSizeInMB());
		model.setTypes(this.builderFactory.builder(UploadFieldCommonModelBuilder.UploadOptionCommonModelBuilder.class).build(data.getTypes()));

		return model;
	}

	@Component
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class UploadOptionCommonModelBuilder extends BaseCommonModelBuilder<UploadFieldModel.UploadOptionModel, UploadFieldEntity.UploadOptionEntity> {
		private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
		@Autowired
		public UploadOptionCommonModelBuilder(
				ConventionService conventionService
		) {
			super(conventionService, new LoggerService(LoggerFactory.getLogger(UploadFieldCommonModelBuilder.UploadOptionCommonModelBuilder.class)));
		}

		public UploadFieldCommonModelBuilder.UploadOptionCommonModelBuilder authorize(EnumSet<AuthorizationFlags> values) {
			this.authorize = values;
			return this;
		}


		@Override
		protected List<CommonModelBuilderItemResponse<UploadFieldModel.UploadOptionModel, UploadFieldEntity.UploadOptionEntity>> buildInternal(List<UploadFieldEntity.UploadOptionEntity> data) throws MyApplicationException {
			this.logger.debug("building for {}", Optional.ofNullable(data).map(List::size).orElse(0));
			if (data == null || data.isEmpty()) return new ArrayList<>();

			List<CommonModelBuilderItemResponse<UploadFieldModel.UploadOptionModel, UploadFieldEntity.UploadOptionEntity>> models = new ArrayList<>();
			for (UploadFieldEntity.UploadOptionEntity d : data) {
				UploadFieldModel.UploadOptionModel m = new UploadFieldModel.UploadOptionModel();
				m.setLabel(d.getLabel());
				m.setValue(d.getValue());
				models.add(new CommonModelBuilderItemResponse<>(m, d));
			}

			this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));

			return models;
		}
	}
}
