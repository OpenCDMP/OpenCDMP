export enum TemplateValidationIssueLevel {
	Error = 0,
	Warning = 1
}

export interface TemplateValidationIssue {
	level: TemplateValidationIssueLevel;
	code: string;
	message: string;
}

export interface TemplateValidationResult {
	valid: boolean;
	issues: TemplateValidationIssue[];
}
