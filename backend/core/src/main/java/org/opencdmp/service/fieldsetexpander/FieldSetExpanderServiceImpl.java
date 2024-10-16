package org.opencdmp.service.fieldsetexpander;

import gr.cite.tools.fieldset.FieldSet;
import org.opencdmp.convention.ConventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FieldSetExpanderServiceImpl implements FieldSetExpanderService {


    private final ConventionService conventionService;
    private final FieldSetExpanderServiceConfiguration fieldSetExpanderServiceConfiguration;
    @Autowired
    public FieldSetExpanderServiceImpl(
		    ConventionService conventionService, FieldSetExpanderServiceConfiguration fieldSetExpanderServiceConfiguration) {
	    this.conventionService = conventionService;
	    this.fieldSetExpanderServiceConfiguration = fieldSetExpanderServiceConfiguration;
    }

    @Override
    public FieldSet expand(FieldSet fieldSet) {
        if (fieldSet == null) return null;
        if (this.fieldSetExpanderServiceConfiguration == null || this.fieldSetExpanderServiceConfiguration.getExpanderServiceProperties() == null || this.conventionService.isListNullOrEmpty(this.fieldSetExpanderServiceConfiguration.getExpanderServiceProperties().getMappings())) return fieldSet;
        List<String> fields = new ArrayList<>(fieldSet.getFields());

        for (String field : fields) {
            FieldSetExpanderServiceProperties.Mapping mapping = this.fieldSetExpanderServiceConfiguration.getExpanderServiceProperties().getMappings().stream().filter(x -> x.getKey().equalsIgnoreCase(field)).findFirst().orElse(null);
            if (mapping != null && !this.conventionService.isListNullOrEmpty(mapping.getFields())) fieldSet.ensure(mapping.getFields().toArray(new String[0]));
        }
        return fieldSet;
    }
}
