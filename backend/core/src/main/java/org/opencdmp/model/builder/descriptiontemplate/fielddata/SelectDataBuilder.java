package org.opencdmp.model.builder.descriptiontemplate.fielddata;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SelectDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.SelectData;
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
public class SelectDataBuilder extends BaseFieldDataBuilder<SelectData, SelectDataEntity> {
	private final BuilderFactory builderFactory;
	@Autowired
	public SelectDataBuilder(ConventionService conventionService, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(SelectDataBuilder.class)));
		this.builderFactory = builderFactory;
	}

	@Override
	protected SelectData getInstance() {
		return new SelectData();
	}

	@Override
	protected void buildChild(FieldSet fields, SelectDataEntity d, SelectData m) {
		FieldSet optionsFields = fields.extractPrefixed(this.asPrefix(SelectData._options));
		if (fields.hasField(this.asIndexer(SelectData._multipleSelect))) m.setMultipleSelect(d.getMultipleSelect());
		if (!optionsFields.isEmpty() && d.getOptions() != null) m.setOptions(this.builderFactory.builder(SelectOptionBuilder.class).authorize(this.authorize).build(optionsFields, d.getOptions()));
		
	}

	@Component
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class SelectOptionBuilder extends BaseBuilder<SelectData.Option, SelectDataEntity.OptionEntity> {
	
	    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
	
	    @Autowired
	    public SelectOptionBuilder(
	            ConventionService conventionService) {
	        super(conventionService, new LoggerService(LoggerFactory.getLogger(SelectOptionBuilder.class)));
	    }
	
	    public SelectOptionBuilder authorize(EnumSet<AuthorizationFlags> values) {
	        this.authorize = values;
	        return this;
	    }
	
	    @Override
	    public List<SelectData.Option> build(FieldSet fields, List<SelectDataEntity.OptionEntity> data) throws MyApplicationException {
	        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
	        this.logger.trace(new DataLogEntry("requested fields", fields));
	        if (fields == null || data == null || fields.isEmpty())
	            return new ArrayList<>();
	
	        List<SelectData.Option> models = new ArrayList<>();
	        for (SelectDataEntity.OptionEntity d : data) {
	            SelectData.Option m = new SelectData.Option();
	            if (fields.hasField(this.asIndexer(SelectData.Option._label))) m.setLabel(d.getLabel());
	            if (fields.hasField(this.asIndexer(SelectData.Option._value))) m.setValue(d.getValue());
	            models.add(m);
	        }
	        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
	        return models;
	    }
	}
}
