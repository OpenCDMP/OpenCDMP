package org.opencdmp.model.persist.externalfetcher;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.opencdmp.commons.enums.ExternalFetcherSourceType;
import org.opencdmp.commons.validation.BaseValidator;
import org.opencdmp.convention.ConventionService;
import org.opencdmp.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.validation.ValidatorFactory;
import gr.cite.tools.validation.specification.Specification;
import org.opencdmp.service.externalfetcher.config.entities.SourceBaseConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExternalFetcherApiSourceConfigurationPersist.class, name = "0"),
        @JsonSubTypes.Type(value = ExternalFetcherStaticOptionSourceConfigurationPersist.class, name = "1")
})
public abstract class ExternalFetcherBaseSourceConfigurationPersist implements SourceBaseConfiguration {

    private String key = null;

    public static final String _key = "key";

    private String label = null;

    public static final String _label = "label";

    private Integer ordinal = null;

    public static final String _ordinal = "ordinal";

    private ExternalFetcherSourceType type;

    public static final String _type = "type";

    private List<UUID> referenceTypeDependencyIds;

    public static final String _referenceTypeDependencyIds = "referenceTypeDependencyIds";

    public final static String _storageFileId = "storageFileId";
    private UUID storageFileId;

    public final static String _navigationUrl = "navigationUrl";
    private String navigationUrl;

    public final static String _tooltipMessage = "tooltipMessage";
    private String tooltipMessage;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public ExternalFetcherSourceType getType() {
        return type;
    }

    public void setType(ExternalFetcherSourceType type) {
        this.type = type;
    }

    public List<UUID> getReferenceTypeDependencyIds() {
        return referenceTypeDependencyIds;
    }

    public void setReferenceTypeDependencyIds(List<UUID> referenceTypeDependencyIds) {
        this.referenceTypeDependencyIds = referenceTypeDependencyIds;
    }

    public UUID getStorageFileId() {
        return storageFileId;
    }

    public void setStorageFileId(UUID storageFileId) {
        this.storageFileId = storageFileId;
    }

    public String getNavigationUrl() {
        return navigationUrl;
    }

    public void setNavigationUrl(String navigationUrl) {
        this.navigationUrl = navigationUrl;
    }

    public String getTooltipMessage() {
        return tooltipMessage;
    }

    public void setTooltipMessage(String tooltipMessage) {
        this.tooltipMessage = tooltipMessage;
    }

    public static abstract class ExternalFetcherBaseSourceConfigurationPersistValidator<T extends ExternalFetcherBaseSourceConfigurationPersist> extends BaseValidator<T> {

        protected final MessageSource messageSource;

        protected final ValidatorFactory validatorFactory;

        private static final Pattern URL_WITH_MAPPINGS_PATTERN = Pattern.compile(
                "^(https?:\\/\\/)[\\w.-]+(?:\\.[\\w.-]+)+(?:\\/(?:[\\w\\-._~%!$&'()*+,;=:@]|\\{[a-zA-Z0-9_-]+\\})*)*(?:\\?(?:[\\w\\-._~%!$&'()*+,;=:@/?]|\\{[a-zA-Z0-9_-]+\\})*)?(?:#(?:[\\w\\-._~%!$&'()*+,;=:@/?]|\\{[a-zA-Z0-9_-]+\\})*)?$"
        );

        protected ExternalFetcherBaseSourceConfigurationPersistValidator(ConventionService conventionService, ErrorThesaurusProperties errors, MessageSource messageSource, ValidatorFactory validatorFactory) {
            super(conventionService, errors);
            this.messageSource = messageSource;
            this.validatorFactory = validatorFactory;
        }

        protected List<Specification> getBaseSpecifications(T item) {
            List<Specification> specifications = new ArrayList<>();
            specifications.addAll(Arrays.asList(
                    this.spec()
                            .must(() -> !this.isEmpty(item.getKey()))
                            .failOn(ExternalFetcherBaseSourceConfigurationPersist._key).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherBaseSourceConfigurationPersist._key}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isEmpty(item.getLabel()))
                            .failOn(ExternalFetcherBaseSourceConfigurationPersist._label).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherBaseSourceConfigurationPersist._label}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getOrdinal()))
                            .failOn(ExternalFetcherBaseSourceConfigurationPersist._ordinal).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherBaseSourceConfigurationPersist._ordinal}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .must(() -> !this.isNull(item.getType()))
                            .failOn(ExternalFetcherBaseSourceConfigurationPersist._type).failWith(messageSource.getMessage("Validation_Required", new Object[]{ExternalFetcherBaseSourceConfigurationPersist._type}, LocaleContextHolder.getLocale())),
                    this.spec()
                            .iff(()-> !this.isEmpty(item.getNavigationUrl()))
                            .must(() -> this.isValidURLWithFieldMapping(item.getNavigationUrl()))
                            .failOn(ExternalFetcherBaseSourceConfigurationPersist._navigationUrl).failWith(this.messageSource.getMessage("Validation_UrlRequired", new Object[]{ExternalFetcherBaseSourceConfigurationPersist._navigationUrl}, LocaleContextHolder.getLocale()))

                    ));
            return specifications;
        }

        boolean isValidURLWithFieldMapping(String url){
            if (url == null || url.isBlank()) {
                return false;
            }
            return URL_WITH_MAPPINGS_PATTERN.matcher(url).matches();
        }

    }

}
