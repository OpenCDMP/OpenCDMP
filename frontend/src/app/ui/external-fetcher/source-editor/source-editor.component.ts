import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, computed, effect, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NgTemplateOutlet } from '@angular/common';
import { ReactiveFormsModule, UntypedFormArray, UntypedFormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ExternalFetcherApiHTTPMethodType } from '@app/core/common/enum/external-fetcher-api-http-method-type';
import { ExternalFetcherApiResponseFormatType } from '@app/core/common/enum/external-fetcher-api-response-format-type';
import { ExternalFetcherSourceType } from '@app/core/common/enum/external-fetcher-source-type';
import { ExternalFetcherApiHeaderType } from '@app/core/common/enum/ExternalFetcherApiHeader.enum';
import { ReferenceSourceTestResult } from '@app/core/model/reference/reference';
import { ReferenceType, ReferenceTypeField } from '@app/core/model/reference-type/reference-type';
import { ReferenceTestLookup } from '@app/core/query/reference-test.lookup';
import { ReferenceService } from '@app/core/services/reference/reference.service';
import { EnumUtils } from '@app/core/services/utilities/enum-utils.service';
import { FileUploadComponent } from '@app/library/file-uploader/file-uploader.component';
import { HeaderConfigEditorModel, QueryCaseConfigEditorModel, QueryConfigEditorModel, ResultFieldsMappingConfigurationEditorModel, XmlNamespaceConfigEditorModel } from '@app/ui/external-fetcher/external-fetcher-source-editor.model';
import { Observable } from 'rxjs';
import { ValidationErrorModel } from '@common/forms/validation/error-model/validation-error-model';
import { HttpErrorHandlingService } from '@common/modules/errors/error-handling/http-error-handling.service';
import { Guid } from '@common/types/guid';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { Rt2RuleDialogComponent, Rt2RuleDialogData } from './rule-dialog/rule.dialog';
import { rt2ReadPath, rt2SimulateQueries } from './search-rules';

export type Rt2SourceStep = 'connection' | 'request' | 'rules' | 'test' | 'display' | 'entries';

const STEP_LABELS: Record<Rt2SourceStep, string> = {
	connection: 'REFERENCE-TYPE-EDITOR.STEPS.CONNECTION',
	request: 'REFERENCE-TYPE-EDITOR.STEPS.REQUEST',
	rules: 'REFERENCE-TYPE-EDITOR.STEPS.RULES',
	test: 'REFERENCE-TYPE-EDITOR.STEPS.TEST',
	display: 'REFERENCE-TYPE-EDITOR.STEPS.DISPLAY',
	entries: 'REFERENCE-TYPE-EDITOR.STEPS.ENTRIES',
};

interface JsonLine { depth: number; key: string; path: string; preview: string; leaf: boolean; }

/** One external fetcher source (a reference type's, or a prefilling source's search or record request), split into the steps it is set up in. */
@Component({
	selector: 'app-rt2-source-editor',
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
	imports: [CommonUiModule, ReactiveFormsModule, FileUploadComponent, NgTemplateOutlet],
	templateUrl: './source-editor.component.html',
	styleUrls: ['./source-editor.component.scss']
})
export class Rt2SourceEditorComponent {
	private dialog = inject(MatDialog);
	private referenceService = inject(ReferenceService);
	private httpErrorHandlingService = inject(HttpErrorHandlingService);
	private cdr = inject(ChangeDetectorRef);
	private destroyRef = inject(DestroyRef);
	readonly enumUtils = inject(EnumUtils);

	readonly formGroup = input.required<UntypedFormGroup>();
	readonly sourceIndex = input.required<number>();
	readonly fields = input<ReferenceTypeField[]>([]);
	readonly referenceTypes = input<ReferenceType[]>([]);
	readonly sourceKeysMap = input<Map<Guid, string[]>>(new Map());
	readonly canEdit = input(true);
	readonly validationErrorModel = input.required<ValidationErrorModel>();
	readonly remove = output<void>();
	readonly addItem = output<void>();
	readonly removeItem = output<number>();
	/** The steps this source is set up in; by default those of a reference type's API or fixed-list source. */
	readonly steps = input<Rt2SourceStep[]>(null);
	/** Headers and authentication are set elsewhere (the prefilling editor's Connection tab). */
	readonly connectionManaged = input(false);
	readonly showHead = input(true);
	/** Offers the choice of filtering in the API or here; meaningless for a request that fetches one record. */
	readonly filterChoice = input(true);
	/** The host names its own mapping rows instead of following a fixed list of codes. */
	readonly freeMapping = input(false);
	readonly systemCodes = input<string[]>(['reference_id', 'label', 'description']);
	readonly validationRootPath = input<string>(null);
	readonly testFn = input<(lookup: ReferenceTestLookup) => Observable<ReferenceSourceTestResult>>(null);
	/** Test with one of these values instead of free text, e.g. a record picked in another step's test. */
	readonly testOptions = input<{ value: string, label: string }[]>(null);
	readonly testOptionsLabel = input('REFERENCE-TYPE-EDITOR.TEST.WITH');
	readonly testOptionsEmpty = input('');
	readonly mapTitle = input('REFERENCE-TYPE-EDITOR.TEST.MAP');
	readonly mapHint = input('REFERENCE-TYPE-EDITOR.TEST.MAP-HINT');
	readonly tested = output<ReferenceSourceTestResult>();
	readonly openConnection = output<void>();
	readonly mappingRenamed = output<void>();

	readonly stepList = computed<Rt2SourceStep[]>(() => this.steps() ?? (this.isApi() ? ['request', 'rules', 'test', 'display'] : ['entries', 'display']));

	readonly httpMethods = this.enumUtils.getEnumValues<ExternalFetcherApiHTTPMethodType>(ExternalFetcherApiHTTPMethodType);
	readonly headerTypes = this.enumUtils.getEnumValues<ExternalFetcherApiHeaderType>(ExternalFetcherApiHeaderType);
	readonly HTTP = ExternalFetcherApiHTTPMethodType;
	readonly FORMAT = ExternalFetcherApiResponseFormatType;

	readonly subTab = signal<Rt2SourceStep>('request');
	readonly testLike = signal('');
	readonly testing = signal(false);
	readonly testResult = signal<ReferenceSourceTestResult>(null);
	readonly itemIndex = signal(0);
	readonly tryLike = signal('');
	activeMappingIndex = 0;

	readonly isApi = computed(() => this.formGroup().get('type').value !== ExternalFetcherSourceType.STATIC);

	constructor() {
		// a different source opens on its first step, with nothing left over from the last one's test
		effect(() => {
			this.sourceIndex();
			this.formGroup();
			this.subTab.set(this.stepList()[0]);
			this.testResult.set(null);
			this.itemIndex.set(0);
		});
	}

	// ---------------------------------------------------------------- shape

	ctl(path: string) { return this.formGroup().get(path); }
	arr(path: string): UntypedFormArray { return this.formGroup().get(path) as UntypedFormArray; }

	/** Labels for the codes every source fills: the built-in three, then the type's own fields. */
	stepLabel(step: Rt2SourceStep): string { return STEP_LABELS[step]; }

	isSystem(code: string): boolean { return this.systemCodes().includes(code); }

	isRequiredCode(code: string): boolean { return this.isSystem(code) && (code === 'reference_id' || code === 'label'); }

	codeLabel(code: string): string {
		if (!this.isSystem(code) && this.freeMapping()) return code || '…';
		switch (code) {
			case 'reference_id': return 'REFERENCE-TYPE-EDITOR.CODES.REFERENCE-ID';
			case 'label': return 'REFERENCE-TYPE-EDITOR.CODES.LABEL';
			case 'description': return 'REFERENCE-TYPE-EDITOR.CODES.DESCRIPTION';
			default: return this.fields().find(f => f.code === code)?.label || code;
		}
	}

	get codes(): string[] { return ['reference_id', 'label', 'description', ...this.fields().map(f => f.code).filter(x => !!x)]; }

	markDirty(path: string): void { this.ctl(path).markAsDirty(); }

	tok(name: string): string { return `{${name || '…'}}`; }

	// ---------------------------------------------------------------- request

	insertToken(path: string, token: string, input: HTMLInputElement | HTMLTextAreaElement): void {
		const control = this.ctl(path);
		const value: string = control.value ?? '';
		const start = input?.selectionStart ?? value.length;
		const end = input?.selectionEnd ?? value.length;
		control.setValue(value.slice(0, start) + token + value.slice(end));
		control.markAsDirty();
		setTimeout(() => { input?.focus(); input?.setSelectionRange(start + token.length, start + token.length); });
	}

	get urlTokens(): string[] { return [...this.arr('queries').getRawValue().map(q => `{${q.name}}`).filter(x => x !== '{}'), '{page}', '{pageSize}']; }

	headerTaken(type: ExternalFetcherApiHeaderType, rowIndex: number): boolean {
		return this.arr('headers').controls.some((x, i) => i !== rowIndex && x.get('key').value === type);
	}

	addHeader(): void {
		const headers = this.arr('headers');
		headers.push(new HeaderConfigEditorModel(this.validationErrorModel()).buildForm({ rootPath: `${this.rootPath}headers[${headers.length}].` }));
		headers.markAsDirty();
	}

	removeAt(path: string, index: number): void {
		const rows = this.arr(path);
		rows.removeAt(index);
		rows.markAsDirty();
	}

	get rootPath(): string { return this.validationRootPath() ?? `definition.sources[${this.sourceIndex()}].`; }

	// ---------------------------------------------------------------- search rules

	get filtersHere(): boolean { return this.ctl('filterType').value === 'local'; }
	setFiltersHere(here: boolean): void { this.ctl('filterType').setValue(here ? 'local' : null); this.markDirty('filterType'); }

	get dependencyTypes(): ReferenceType[] {
		const ids: Guid[] = this.ctl('referenceTypeDependencyIds').value ?? [];
		return this.referenceTypes().filter(x => ids.some(id => id?.toString() === x.id?.toString()));
	}

	addQuery(name = 'like'): void {
		const queries = this.arr('queries');
		const query = new QueryConfigEditorModel(this.validationErrorModel()).buildForm({ rootPath: `${this.rootPath}queries[${queries.length}].` });
		query.get('name').setValue(queries.getRawValue().some(q => q.name === name) ? '' : name);
		queries.push(query);
		queries.markAsDirty();
	}

	openRule(queryIndex: number, caseIndex?: number): void {
		const cases = this.arr('queries').at(queryIndex).get('cases') as UntypedFormArray;
		const data: Rt2RuleDialogData = {
			placeholder: this.arr('queries').at(queryIndex).get('name').value,
			value: caseIndex != null ? cases.at(caseIndex).getRawValue() : null,
			dependencyTypes: this.dependencyTypes,
			sourceKeysMap: this.sourceKeysMap(),
			canDelete: caseIndex != null
		};
		this.dialog.open(Rt2RuleDialogComponent, { width: 'min(820px, 94vw)', data }).afterClosed()
			.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(result => {
				if (result === 'delete') { cases.removeAt(caseIndex); cases.markAsDirty(); }
				else if (result) {
					if (caseIndex == null) {
						cases.push(new QueryCaseConfigEditorModel(this.validationErrorModel()).buildForm({ rootPath: `${this.rootPath}queries[${queryIndex}].cases[${cases.length}].` }));
						caseIndex = cases.length - 1;
					}
					cases.at(caseIndex).patchValue(result);
					cases.markAsDirty();
				}
				this.cdr.markForCheck();
			});
	}

	ruleIntro(value: { likePattern?: string, separator?: string, referenceTypeId?: Guid, referenceTypeSourceKey?: string }): string {
		if (value.referenceTypeId) return 'REFERENCE-TYPE-EDITOR.RULES.WHEN-PICKED';
		return value.likePattern ? 'REFERENCE-TYPE-EDITOR.RULES.WHEN-MATCHES' : 'REFERENCE-TYPE-EDITOR.RULES.WHEN-ANY';
	}

	dependencyName(id: Guid): string { return this.referenceTypes().find(x => x.id?.toString() === id?.toString())?.name ?? ''; }

	readonly tryResult = computed(() => {
		const like = this.tryLike();
		if (!like) return null;
		return rt2SimulateQueries(this.formGroup().getRawValue(), like);
	});

	// ---------------------------------------------------------------- test and map

	// The backend cannot call an API without them and answers with an internal error, not a validation message.
	get canRunTest(): boolean { return this.ctl('httpMethod').value != null && !!this.ctl('url').value?.trim(); }

	runTest(): void {
		if (!this.canRunTest) return;
		const lookup = new ReferenceTestLookup();
		lookup.like = this.testLike() || null;
		lookup.key = this.ctl('key').value;
		lookup.sources = [JSON.parse(JSON.stringify(this.formGroup().getRawValue()))];
		lookup.dependencyReferences = [];
		this.testing.set(true);
		(this.testFn() ?? (l => this.referenceService.testSource(l)))(lookup).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
			next: result => { this.testResult.set(result); this.itemIndex.set(0); this.testing.set(false); this.tested.emit(result); },
			error: error => { this.testing.set(false); this.httpErrorHandlingService.handleBackedRequestError(error); }
		});
	}

	setFormat(format: ExternalFetcherApiResponseFormatType): void { this.ctl('responseFormat').setValue(format); this.markDirty('responseFormat'); }

	readonly bodyLooksLike = computed(() => {
		const body = this.testResult()?.body?.trimStart() ?? '';
		return body.startsWith('<') ? ExternalFetcherApiResponseFormatType.XML : (body.startsWith('{') || body.startsWith('[') ? ExternalFetcherApiResponseFormatType.JSON : null);
	});

	addNamespace(): void {
		const namespaces = this.arr('namespaces');
		namespaces.push(new XmlNamespaceConfigEditorModel(this.validationErrorModel()).buildForm({ rootPath: `${this.rootPath}namespaces[${namespaces.length}].` }));
		namespaces.markAsDirty();
	}

	get mappingRows(): UntypedFormGroup[] { return this.arr('results.fieldsMapping').controls as UntypedFormGroup[]; }

	/**
	 * The value a mapping row gives for the result on show. It is worked out here from the reply, as the backend's
	 * ExternalFetcherServiceImpl.jsonToExternalDataResult does, so editing a path updates it at once; paths this
	 * can't follow (filters, wildcards, XML) fall back to the value from the last run.
	 */
	sample(code: string): { value: string, live: boolean } {
		const path: string = this.mappingRows.find(row => row.get('code').value === code)?.get('responsePath').value;
		const item = this.testResult()?.items?.[this.itemIndex()];
		if (item != null && this.ctl('responseFormat').value !== ExternalFetcherApiResponseFormatType.XML) {
			if (!path) return { value: null, live: true };
			const live = rt2ReadPath(item, path);
			if (live.ok) return { value: live.value, live: true };
		}
		return { value: this.testResult()?.results?.[this.itemIndex()]?.[code] ?? null, live: false };
	}

	addMapping(): void {
		const rows = this.arr('results.fieldsMapping');
		rows.push(new ResultFieldsMappingConfigurationEditorModel(this.validationErrorModel()).buildForm({ rootPath: `${this.rootPath}results.fieldsMapping[${rows.length}].` }));
		rows.markAsDirty();
		this.activeMappingIndex = rows.length - 1;
	}

	removeMapping(index: number): void {
		this.removeAt('results.fieldsMapping', index);
		this.activeMappingIndex = 0;
		this.mappingRenamed.emit();
	}

	isFirstOnly(row: UntypedFormGroup): boolean { return (row.get('responsePath').value ?? '').endsWith('.first()'); }

	setFirstOnly(row: UntypedFormGroup, first: boolean): void {
		const path: string = (row.get('responsePath').value ?? '').replace(/\.first\(\)$/, '');
		row.get('responsePath').setValue(first && path ? `${path}.first()` : path);
		row.get('responsePath').markAsDirty();
	}

	/** The extracted result shown as lines; each value line carries the path that reads it from one result. */
	readonly jsonLines = computed<JsonLine[]>(() => {
		const item = this.testResult()?.items?.[this.itemIndex()];
		if (item == null) return [];
		const lines: JsonLine[] = [];
		const walk = (value: unknown, key: string, path: string, depth: number) => {
			if (Array.isArray(value)) {
				lines.push({ depth, key, path, preview: `[${value.length}]`, leaf: value.every(v => v === null || typeof v !== 'object') });
				value.forEach((v, i) => walk(v, `${i}`, i === 0 ? `${path}.first()` : `${path}[${i}]`, depth + 1));
			} else if (value !== null && typeof value === 'object') {
				if (key) lines.push({ depth, key, path, preview: '{…}', leaf: false });
				Object.entries(value).forEach(([k, v]) => walk(v, k, path ? `${path}${/^[A-Za-z_][\w-]*$/.test(k) ? '.' + k : `['${k}']`}` : (/^[A-Za-z_][\w-]*$/.test(k) ? k : `['${k}']`), key ? depth + 1 : depth));
			} else {
				lines.push({ depth, key, path, preview: JSON.stringify(value), leaf: true });
			}
		};
		walk(item, '', '', 0);
		return lines;
	});

	pickPath(line: JsonLine): void {
		const row = this.mappingRows[this.activeMappingIndex];
		if (!row || !this.canEdit()) return;
		row.get('responsePath').setValue(line.path);
		row.get('responsePath').markAsDirty();
	}

	get previewEntry(): Record<string, string> {
		if (!this.isSystem('label') || (!this.testResult()?.items?.length && !this.testResult()?.results?.length)) return null;
		return { label: this.sample('label').value, description: this.sample('description').value, reference_id: this.sample('reference_id').value };
	}

	// ---------------------------------------------------------------- display

	resolvedNavigationUrl(): string {
		const template: string = this.ctl('navigationUrl').value;
		const entry = this.previewEntry;
		if (!template || !entry) return null;
		return template.replace(/\{([\w-]+)\}/g, (_m, code) => entry[code] != null ? encodeURIComponent(entry[code]) : `{${code}}`);
	}

	// ---------------------------------------------------------------- fixed list

	get items(): UntypedFormGroup[] { return this.arr('items').controls as UntypedFormGroup[]; }

	option(item: UntypedFormGroup, code: string) {
		return (item.get('options') as UntypedFormArray).controls.find(o => o.get('code').value === code)?.get('value');
	}
}
