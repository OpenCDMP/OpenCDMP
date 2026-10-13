package org.opencdmp.model.builder.descriptiontemplate.fielddata;

import org.opencdmp.authorization.AuthorizationFlags;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.RadioBoxDataEntity;
import org.opencdmp.commons.types.descriptiontemplate.fielddata.SemanticValueEntity;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.model.builder.BaseBuilder;
import org.opencdmp.model.descriptiontemplate.fielddata.RadioBoxData;
import org.opencdmp.model.descriptiontemplate.fielddata.SemanticValue;
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
	
	        FieldSet semanticsFields = fields.extractPrefixed(this.asPrefix(RadioBoxData.RadioBoxOption._semantics));

	        List<RadioBoxData.RadioBoxOption> models = new ArrayList<>();
	        for (RadioBoxDataEntity.RadioBoxDataOptionEntity d : data) {
	            RadioBoxData.RadioBoxOption m = new RadioBoxData.RadioBoxOption();
	            if (fields.hasField(this.asIndexer(RadioBoxData.RadioBoxOption._label))) m.setLabel(d.getLabel());
	            if (fields.hasField(this.asIndexer(RadioBoxData.RadioBoxOption._value))) m.setValue(d.getValue());
	            if (fields.hasField(this.asIndexer(RadioBoxData.RadioBoxOption._exportsNothing))) m.setExportsNothing(d.getExportsNothing());
	            if (!semanticsFields.isEmpty() && d.getSemantics() != null) m.setSemantics(this.buildSemantics(semanticsFields, d.getSemantics()));
	            models.add(m);
	        }
	        this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
	        return models;
	    }

	    private List<SemanticValue> buildSemantics(FieldSet fields, List<SemanticValueEntity> data) {
	        List<SemanticValue> models = new ArrayList<>();
	        for (SemanticValueEntity d : data) {
	            SemanticValue m = new SemanticValue();
	            if (fields.hasField(this.asIndexer(SemanticValue._key))) m.setKey(d.getKey());
	            if (fields.hasField(this.asIndexer(SemanticValue._value))) m.setValue(d.getValue());
	            models.add(m);
	        }
	        return models;
	    }
	}
}
