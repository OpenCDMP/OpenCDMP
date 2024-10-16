package org.opencdmp.service.storage;


import org.opencdmp.commons.enums.StorageType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "storage.service")
public class StorageFileProperties {
	private List<StorageConfig> storages;
	private StaticFilesConfig staticFiles;
	private MaterialConfig materialFiles;
	private String defaultLanguage;

	private int tempStoreLifetimeSeconds;

	public List<StorageConfig> getStorages() {
		return storages;
	}

	public void setStorages(List<StorageConfig> storages) {
		this.storages = storages;
	}

	public int getTempStoreLifetimeSeconds() {
		return tempStoreLifetimeSeconds;
	}

	public void setTempStoreLifetimeSeconds(int tempStoreLifetimeSeconds) {
		this.tempStoreLifetimeSeconds = tempStoreLifetimeSeconds;
	}

	public StaticFilesConfig getStaticFiles() {
		return staticFiles;
	}

	public void setStaticFiles(StaticFilesConfig staticFiles) {
		this.staticFiles = staticFiles;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public MaterialConfig getMaterialFiles() {
		return materialFiles;
	}

	public void setMaterialFiles(MaterialConfig materialFiles) {
		this.materialFiles = materialFiles;
	}

	public static class StorageConfig{
		private StorageType type;
		private String basePath;

		public StorageType getType() {
			return type;
		}

		public void setType(StorageType type) {
			this.type = type;
		}

		public String getBasePath() {
			return basePath;
		}

		public void setBasePath(String basePath) {
			this.basePath = basePath;
		}
	}
	
	public static class MaterialConfig {
		private String localizedNameLanguageKey;
		private String userGuide;
		private String userGuideNamePattern;
		private String about;
		private String aboutNamePattern;
		private String termsOfService;
		private String termsOfServiceNamePattern;
		private String glossary;
		private String glossaryNamePattern;
		private String language;
		private String languageNamePattern;
		private String faq;
		private String faqNamePattern;
		private String cookiePolicy;
		private String cookiePolicyNamePattern;

		public String getLocalizedNameLanguageKey() {
			return localizedNameLanguageKey;
		}

		public void setLocalizedNameLanguageKey(String localizedNameLanguageKey) {
			this.localizedNameLanguageKey = localizedNameLanguageKey;
		}

		public String getUserGuide() {
			return userGuide;
		}

		public void setUserGuide(String userGuide) {
			this.userGuide = userGuide;
		}

		public String getAbout() {
			return about;
		}

		public void setAbout(String about) {
			this.about = about;
		}

		public String getTermsOfService() {
			return termsOfService;
		}

		public void setTermsOfService(String termsOfService) {
			this.termsOfService = termsOfService;
		}

		public String getGlossary() {
			return glossary;
		}

		public void setGlossary(String glossary) {
			this.glossary = glossary;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public String getFaq() {
			return faq;
		}

		public void setFaq(String faq) {
			this.faq = faq;
		}

		public String getUserGuideNamePattern() {
			return userGuideNamePattern;
		}

		public void setUserGuideNamePattern(String userGuideNamePattern) {
			this.userGuideNamePattern = userGuideNamePattern;
		}

		public String getAboutNamePattern() {
			return aboutNamePattern;
		}

		public void setAboutNamePattern(String aboutNamePattern) {
			this.aboutNamePattern = aboutNamePattern;
		}

		public String getTermsOfServiceNamePattern() {
			return termsOfServiceNamePattern;
		}

		public void setTermsOfServiceNamePattern(String termsOfServiceNamePattern) {
			this.termsOfServiceNamePattern = termsOfServiceNamePattern;
		}

		public String getGlossaryNamePattern() {
			return glossaryNamePattern;
		}

		public void setGlossaryNamePattern(String glossaryNamePattern) {
			this.glossaryNamePattern = glossaryNamePattern;
		}

		public String getLanguageNamePattern() {
			return languageNamePattern;
		}

		public void setLanguageNamePattern(String languageNamePattern) {
			this.languageNamePattern = languageNamePattern;
		}

		public String getFaqNamePattern() {
			return faqNamePattern;
		}

		public void setFaqNamePattern(String faqNamePattern) {
			this.faqNamePattern = faqNamePattern;
		}

		public String getCookiePolicy() {
			return cookiePolicy;
		}

		public void setCookiePolicy(String cookiePolicy) {
			this.cookiePolicy = cookiePolicy;
		}

		public String getCookiePolicyNamePattern() {
			return cookiePolicyNamePattern;
		}

		public void setCookiePolicyNamePattern(String cookiePolicyNamePattern) {
			this.cookiePolicyNamePattern = cookiePolicyNamePattern;
		}
	}

	public static class StaticFilesConfig{
		private String semantics;

		public String getSemantics() {
			return semantics;
		}

		public void setSemantics(String semantics) {
			this.semantics = semantics;
		}

	}
}

