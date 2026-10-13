import { PluginEntityType } from '@app/core/common/enum/plugin-entity-type';
import { PluginType } from '@app/core/common/enum/plugin-type';

/** How much a plugin depends on a semantic key it reads. Neither level makes the key required by OpenCDMP. */
export enum SemanticKeyLevel {
	/** The plugin cannot do its job, or writes only a placeholder, without it. */
	Needs = 0,
	/** The plugin gives a richer result with it; fine to leave out. */
	CanUse = 1
}

export enum SemanticValueFormat {
	Text = 0,
	Date = 1,
	DateTime = 2,
	Uri = 3,
	Email = 4,
	Integer = 5,
	Number = 6,
	Boolean = 7,
	Choice = 8,
	LanguageTwoLetter = 9,
	LanguageThreeLetter = 10,
	CountryCode = 11,
	CurrencyCode = 12,
	MimeType = 13,
	Reference = 14
}

/** A key prefix: a standard, a repository or an organisation's own. */
export interface SemanticVocabulary {
	prefix: string;
	name: string;
	/** A published metadata standard, shown as a standard mapping to complete. */
	standard: boolean;
}

/** One value a plugin reads; an input carrying any of its keys covers it. */
export interface SemanticKeyUsage {
	keys: string[];
	/** Each key is a prefix: any key starting with it and a dot also covers the value. */
	prefix: boolean;
	level: SemanticKeyLevel;
	entityTypes: PluginEntityType[];
	label: string;
	purpose: string;
	target?: string;
	format?: SemanticValueFormat;
	values?: string[];
	multiple?: boolean;
	/** What the plugin writes when the value is not answered; a Needs value's fallback is only a placeholder. */
	fallback?: string;
	/** The object the value belongs to: counted once the object is used, or always when it is always present. */
	object?: string;
	fromReferenceFields?: boolean;
	imported?: boolean;
}

/** An object in a plugin's output that several values make up, e.g. a distribution or its host. */
export interface SemanticObject {
	key: string;
	label: string;
	parent?: string;
	multiple: boolean;
	fromReference: boolean;
	/** Every output has it, so its values always count. */
	alwaysPresent: boolean;
	entityTypes: PluginEntityType[];
	target?: string;
}

export interface SemanticsManifest {
	vocabularies: SemanticVocabulary[];
	keys: SemanticKeyUsage[];
	objects?: SemanticObject[];
}

/** The semantic keys one of the tenant's plugins reads. */
export interface PluginSemantics {
	pluginType: PluginType;
	pluginId: string;
	semantics: SemanticsManifest;
}
