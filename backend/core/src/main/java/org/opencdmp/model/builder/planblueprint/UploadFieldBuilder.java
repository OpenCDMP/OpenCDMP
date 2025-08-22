package org.opencdmp.model.builder.planblueprint;

import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.planblueprint.UploadFieldEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.planblueprint.UploadField;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadFieldBuilder extends FieldBuilder<UploadField, UploadFieldEntity> {
	private final BuilderFactory builderFactory;

	@Autowired
	public UploadFieldBuilder(ConventionService conventionService, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(UploadFieldBuilder.class)));
		this.builderFactory = builderFactory;
	}

	protected UploadField getInstance() {
		return new UploadField();
	}
	@Override
	protected UploadField buildChild(FieldSet fields, UploadFieldEntity data, UploadField model) {
		FieldSet typesFields = fields.extractPrefixed(this.asPrefix(UploadField._types));

		if (fields.hasField(this.asIndexer(UploadField._maxFileSizeInMB))) model.setMaxFileSizeInMB(data.getMaxFileSizeInMB());
		if (!typesFields.isEmpty() && data.getTypes() != null) model.setTypes(this.builderFactory.builder(UploadOptionBuilder.class).build(typesFields, data.getTypes()));

		return model;
	}

	@Component
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class UploadOptionBuilder extends BaseBuilder<UploadField.UploadOption, UploadFieldEntity.UploadOptionEntity> {
	
	    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
	
	    @Autowired
	    public UploadOptionBuilder(
	            ConventionService conventionService) {
	        super(conventionService, new LoggerService(LoggerFactory.getLogger(UploadOptionBuilder.class)));
	    }
	
	    public UploadOptionBuilder authorize(EnumSet<AuthorizationFlags> values) {
	        this.authorize = values;
	        return this;
	    }
	
	    @Override
	    public List<UploadField.UploadOption> build(FieldSet fields, List<UploadFieldEntity.UploadOptionEntity> data) throws MyApplicationException {
	        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
	        this.logger.trace(new DataLogEntry("requested fields", fields));
	        if (fields == null || data == null || fields.isEmpty())
	            return new ArrayList<>();
	
	        List<UploadField.UploadOption> models = new ArrayList<>();
	        for (UploadFieldEntity.UploadOptionEntity d : data) {
				UploadField.UploadOption m = new UploadField.UploadOption();
	            if (fields.hasField(this.asIndexer(UploadField.UploadOption._label))) m.setLabel(d.getLabel());
	            if (fields.hasField(this.asIndexer(UploadField.UploadOption._value))) m.setValue(d.getValue());
	            models.add(m);
	        }
	        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
	        return models;
	    }
	}
}
