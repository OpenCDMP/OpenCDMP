package org.opencdmp.model.persist.descriptiontemplatedefinition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.opencdmp.commons.enums.SystemFieldType;
import org.opencdmp.commons.validation.BaseValidator;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import org.opencdmp.model.persist.descriptiontemplatedefinition.fielddata.SystemFieldDataPersist;
import org.opencdmp.model.persist.iconconfiguration.IconConfigurationPersist;
import org.opencdmp.model.persist.pluginconfiguration.PluginConfigurationPersist;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class DefinitionPersist {

    private List<SectionPersist> sections = null;
    public static final String _sections = "sections";

    private List<PluginConfigurationPersist> pluginConfigurations;
    public static final String _pluginConfigurations = "pluginConfigurations";

    private IconConfigurationPersist iconConfiguration;
    public static final String _iconConfiguration = "iconConfiguration";


    public List<SectionPersist> getSections() {
        return sections;
    }

    public void setSections(List<SectionPersist> sections) {
        this.sections = sections;
    }

    public List<PluginConfigurationPersist> getPluginConfigurations() {
        return pluginConfigurations;
    }

    public void setPluginConfigurations(List<PluginConfigurationPersist> pluginConfigurations) {
        this.pluginConfigurations = pluginConfigurations;
    }

    public IconConfigurationPersist getIconConfiguration() {
        return iconConfiguration;
    }

    public void setIconConfiguration(IconConfigurationPersist iconConfiguration) {
        this.iconConfiguration = iconConfiguration;
    }

    @JsonIgnore
    public List<FieldPersist> getAllField() {
        List<FieldPersist> fieldPersists = new ArrayList<>();
        if (this.getSections() != null) {
            for (SectionPersist rootSectionPersist : this.getSections()) {
                fieldPersists.addAll(rootSectionPersist.getAllField());
            }
        }
        return fieldPersists;
    }

    @JsonIgnore
    public List<FieldPersist> getFieldById(String id) {
        return this.getAllField().stream().filter(x -> id.equals(x.getId())).toList();
    }

    @JsonIgnore
    public List<FieldSetPersist> getAllFieldSets() {
        List<FieldSetPersist> result = new ArrayList<>();
        if (this.getSections() != null) {
            for (SectionPersist section : this.getSections()) {
                result.addAll(section.getAllFieldSets());
            }
        }
        return result;
    }

    @JsonIgnore
    public List<FieldSetPersist> getFieldSetById(String id) {
        return this.getAllFieldSets().stream().filter(x-> id != null && id.equals(x.getId())).toList();
    }

    @Component(DefinitionPersistValidator.ValidatorName)
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public static class DefinitionPersistValidator extends BaseValidator<DefinitionPersist> {

        public static final String ValidatorName = "DescriptionTemplate.DefinitionPersistValidator";

        private final MessageSource messageSource;

        private final ValidatorFactory validatorFactory;

        public DefinitionPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        @Override
        protected Class<DefinitionPersist> modelClass() {
            return DefinitionPersist.class;
        }

        @Override
        protected List<Specification> specifications(DefinitionPersist item) {
            return Arrays.asList(
                    this.spec()
                            .must(() -> !this.isListNullOrEmpty(item.getSections()))
                            .failOn(DefinitionPersist._sections).failWith(messageSource.getMessage("Validation_Required", new Object[]{DefinitionPersist._sections}, LocaleContextHolder.getLocale())),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getSections()))
                            .on(DefinitionPersist._sections)
                            .over(item.getSections())
                            .using((itm) -> this.validatorFactory.validator(SectionPersist.SectionPersistValidator.class)),
                    this.navSpec()
                            .iff(() -> !this.isListNullOrEmpty(item.getPluginConfigurations()))
                            .on(DefinitionPersist._pluginConfigurations)
                            .over(item.getPluginConfigurations())
                            .using((itm) -> this.validatorFactory.validator(PluginConfigurationPersist.PluginConfigurationPersistValidator.class)),
                    this.refSpec()
                            .iff(() -> !this.isNull(item.getIconConfiguration()))
                            .on(DefinitionPersist._iconConfiguration)
                            .over(item.getIconConfiguration())
                            .using(() -> this.validatorFactory.validator(IconConfigurationPersist.IconConfigurationPersistValidator.class)),
                    this.spec()
                            .must(() -> this.hasNoDuplicateSystemFieldTypes(item))
                            .failOn(DefinitionPersist._sections).failWith(messageSource.getMessage("Validation_DuplicateSystemFieldTypes", new Object[]{DefinitionPersist._sections}, LocaleContextHolder.getLocale()))
                    );
        }

        private boolean hasNoDuplicateSystemFieldTypes(DefinitionPersist item) {
            if (item == null || item.getAllField() == null) return true;

            EnumSet<SystemFieldType> seen = EnumSet.noneOf(SystemFieldType.class);

            for (FieldPersist field : item.getAllField()) {
                if (field == null || !(field.getData() instanceof SystemFieldDataPersist systemField)) {
                    continue;
                }

                SystemFieldType type = systemField.getSystemFieldType();

                if (type == null || !seen.add(type)) {
                    return false;
                }
            }

            return true;
        }
    }

}
