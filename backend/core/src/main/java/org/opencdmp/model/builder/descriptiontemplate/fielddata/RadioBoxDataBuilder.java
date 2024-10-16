package org.opencdmp.model.builder.descriptiontemplate.fielddata;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.RadioBoxDataEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.RadioBoxData;
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
public class RadioBoxDataBuilder extends BaseFieldDataBuilder<RadioBoxData, RadioBoxDataEntity> {
	private final BuilderFactory builderFactory;

	@Autowired
	public RadioBoxDataBuilder(ConventionService conventionService, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(RadioBoxDataBuilder.class)));
		this.builderFactory = builderFactory;
	}

	protected RadioBoxData getInstance() {
		return new RadioBoxData();
	}
	@Override
	protected void buildChild(FieldSet fields, RadioBoxDataEntity d, RadioBoxData m) {
		FieldSet optionsFields = fields.extractPrefixed(this.asPrefix(RadioBoxData._options));
		if (!optionsFields.isEmpty() && d.getOptions() != null) m.setOptions(this.builderFactory.builder(RadioBoxOptionBuilder.class).authorize(this.authorize).build(optionsFields, d.getOptions()));
	}

	@Component
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class RadioBoxOptionBuilder extends BaseBuilder<RadioBoxData.RadioBoxOption, RadioBoxDataEntity.RadioBoxDataOptionEntity> {
	
	    private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
	
	    @Autowired
	    public RadioBoxOptionBuilder(
	            ConventionService conventionService) {
	        super(conventionService, new LoggerService(LoggerFactory.getLogger(RadioBoxOptionBuilder.class)));
	    }
	
	    public RadioBoxOptionBuilder authorize(EnumSet<AuthorizationFlags> values) {
	        this.authorize = values;
	        return this;
	    }
	
	    @Override
	    public List<RadioBoxData.RadioBoxOption> build(FieldSet fields, List<RadioBoxDataEntity.RadioBoxDataOptionEntity> data) throws MyApplicationException {
	        this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
	        this.logger.trace(new DataLogEntry("requested fields", fields));
	        if (fields == null || data == null || fields.isEmpty())
	            return new ArrayList<>();
	
	        List<RadioBoxData.RadioBoxOption> models = new ArrayList<>();
	        for (RadioBoxDataEntity.RadioBoxDataOptionEntity d : data) {
	            RadioBoxData.RadioBoxOption m = new RadioBoxData.RadioBoxOption();
	            if (fields.hasField(this.asIndexer(RadioBoxData.RadioBoxOption._label))) m.setLabel(d.getLabel());
	            if (fields.hasField(this.asIndexer(RadioBoxData.RadioBoxOption._value))) m.setValue(d.getValue());
	            models.add(m);
	        }
	        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
	        return models;
	    }
	}
}
