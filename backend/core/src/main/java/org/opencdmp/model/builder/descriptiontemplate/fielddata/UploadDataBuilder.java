package org.opencdmp.model.builder.descriptiontemplate.fielddata;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.UploadDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.UploadData;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UploadDataBuilder extends BaseFieldDataBuilder<UploadData, UploadDataEntity> {
	private final BuilderFactory builderFactory;

	@Autowired
	public UploadDataBuilder(ConventionService conventionService, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(UploadDataBuilder.class)));
		this.builderFactory = builderFactory;
	}

	protected UploadData getInstance() {
		return new UploadData();
	}
	@Override
	protected void buildChild(FieldSet fields, UploadDataEntity d, UploadData m) {
		FieldSet typesFields = fields.extractPrefixed(this.asPrefix(UploadData._types));

		if (fields.hasField(this.asIndexer(UploadData._maxFileSizeInMB))) m.setMaxFileSizeInMB(d.getMaxFileSizeInMB());
		if (!typesFields.isEmpty() && d.getTypes() != null) m.setTypes(this.builderFactory.builder(UploadOptionBuilder.class).build(typesFields, d.getTypes()));
	}

	@Component
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class UploadOptionBuilder extends BaseBuilder<UploadData.UploadOption, UploadDataEntity.UploadDataOptionEntity> {
	
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
	    public List<UploadData.UploadOption> build(FieldSet fields, List<UploadDataEntity.UploadDataOptionEntity> data) throws MyApplicationException {
	        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
	        this.logger.trace(new DataLogEntry("requested fields", fields));
	        if (fields == null || data == null || fields.isEmpty())
	            return new ArrayList<>();
	
	        List<UploadData.UploadOption> models = new ArrayList<>();
	        for (UploadDataEntity.UploadDataOptionEntity d : data) {
	            UploadData.UploadOption m = new UploadData.UploadOption();
	            if (fields.hasField(this.asIndexer(UploadData.UploadOption._label))) m.setLabel(d.getLabel());
	            if (fields.hasField(this.asIndexer(UploadData.UploadOption._value))) m.setValue(d.getValue());
	            models.add(m);
	        }
	        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
	        return models;
	    }
	}
}
