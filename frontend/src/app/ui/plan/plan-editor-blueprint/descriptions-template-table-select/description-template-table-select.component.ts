import {Component, EventEmitter, input, Input, Output} from "@angular/core";
import {BaseComponent} from "@common/base/base.component";
import {nameof} from "ts-simple-nameof";
import {DescriptionTemplate} from "@app/core/model/description-template/description-template";
import {PlanBlueprintService} from "@app/core/services/plan/plan-blueprint.service";
import {DescriptionTemplateService} from "@app/core/services/description-template/description-template.service";
import {HttpErrorHandlingService} from "@common/modules/errors/error-handling/http-error-handling.service";
import {map, startWith, takeUntil, tap} from "rxjs/operators";
import {QueryResult} from "@common/model/query-result";
import {DescriptionTemplateLookup} from "@app/core/query/description-template.lookup";
import {IsActive} from "@app/core/common/enum/is-active.enum";
import {DescriptionTemplateStatus} from "@app/core/common/enum/description-template-status";
import {DescriptionTemplateVersionStatus} from "@app/core/common/enum/description-template-version-status";
import {DescriptionTemplateType} from "@app/core/model/description-template-type/description-template-type";
import {DescriptionTemplateTypeService} from "@app/core/services/description-template-type/description-template-type.service";
import {DescriptionTemplateTypeLookup} from "@app/core/query/description-template-type.lookup";
import {DescriptionTemplateTypeStatus} from "@app/core/common/enum/description-template-type-status";
import {LanguageInfoService} from "@app/core/services/culture/language-info-service";
import {LanguageInfo} from "@app/core/model/language-info";
import {FormControl, UntypedFormBuilder} from "@angular/forms";
import {Observable} from "rxjs";
import {Guid} from "@common/types/guid";
import {DescriptionTemplatePreviewDialogComponent} from "@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import { DescriptionTemplateEditorResolver } from "@app/ui/admin/description-template/editor/description-template-editor.resolver";
import { SortDirection } from "@common/modules/hybrid-listing/hybrid-listing.component";

@Component({
	selector: 'app-description-template-table-select',
	templateUrl: './description-template-table-select.component.html',
	styleUrls: ['./description-template-table-select.component.scss'],
	standalone: false

})
export class DescriptionTemplateTableSelectComponent extends BaseComponent {
    templatesInUse = input<Guid[]>();

	@Input() descTemplatesControl: FormControl<Guid[]> = new FormControl([]);
	@Input() previewOnly: boolean = false;

    @Output() descriptionTemplateLoaded = new EventEmitter<DescriptionTemplate>();
	templates;
	currentPage = 1;
	size = 5;
	totalDescriptionTemplates = 0;
	currentPageDescriptionTemplates: DescriptionTemplate[] = [];
	//Filters
	term = "";
	termControl = this.fb.control('');

    sort = {
        prop: nameof<DescriptionTemplate>(x => x.updatedAt),
        direction: SortDirection.Descending
    }

    //TODO: needs refactoring - use similar logic to hybrid listing
    readonly ColumnName = {
        'name': nameof<DescriptionTemplate>(x => x.label),
        'updatedAt': nameof<DescriptionTemplate>(x => x.updatedAt)
    }
    readonly SortDirection = SortDirection

	allTypes: DescriptionTemplateType[] = null;
	selectedTypesIds = null;
	typeFormControl = this.fb.control('');

	availableLanguages: LanguageInfo[] = this.languageInfoService.getLanguageInfoValues();
	languageFormControl = this.fb.control('');
	selectedLanguageCodes = null;

	filteredLanguageOptions: Observable<LanguageInfo[]>;
	filteredTypeOptions: Observable<DescriptionTemplateType[]>;

	selectedTemplatesInfo: Map<Guid,DescriptionTemplate> = new Map<Guid,DescriptionTemplate>();
	private readonly lookupFields = [
        ...DescriptionTemplateEditorResolver.baseLookupFields(), 
        ...DescriptionTemplateEditorResolver.definitionLookupFields(),
		nameof<DescriptionTemplate>(x => x.language),
		[nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.id)].join('.'),
		[nameof<DescriptionTemplate>(x => x.type), nameof<DescriptionTemplateType>(x => x.name)].join('.'),
	];
	private readonly descriptionTemplateTypesLookupFields: string[] = [
		nameof<DescriptionTemplateType>(x => x.id),
		nameof<DescriptionTemplateType>(x => x.name),
		nameof<DescriptionTemplateType>(x => x.code)
	];

	constructor(private planBlueprintService: PlanBlueprintService, private descriptionTemplateService: DescriptionTemplateService,
				private descriptionTemplateTypeService: DescriptionTemplateTypeService, protected httpErrorHandlingService: HttpErrorHandlingService,
				private languageInfoService: LanguageInfoService, private fb: UntypedFormBuilder, protected dialog: MatDialog,
            ) {
		super();
		this.descriptionTemplateTypeService.query(this.initializeTypesLookup()).pipe(
			takeUntil(this._destroyed),
			tap((descriptionTemplateTypes: QueryResult<DescriptionTemplateType>) => {
			})).subscribe(queryResults => {
			this.allTypes = queryResults.items
			this.filteredTypeOptions = this.typeFormControl.valueChanges.pipe(
				startWith(''),
				map(value => {
					const name = typeof value === 'string' ? value : value?.name;
					return name ? this._filterTypes(name as string) : this.allTypes.slice();
				}),
			);

		});

		this.query();


	}

	ngOnInit() {
		this.filteredLanguageOptions = this.languageFormControl.valueChanges.pipe(
			startWith(''),
			map(value => {
				const name = typeof value === 'string' ? value : value?.name;
				return name ? this._filterLanguages(name as string) : this.availableLanguages.slice();
			}),
		);
		if(this.descTemplatesControl && this.descTemplatesControl.value.length > 0) {
			// get info for selected descriptions
			this.query(this.descTemplatesControl.value);
		}
	}


	protected initializeTypesLookup(): DescriptionTemplateTypeLookup {
		const lookup = new DescriptionTemplateTypeLookup();
		lookup.metadata = {countAll: true};
		lookup.page = {offset: 0, size: 100};
		lookup.isActive = [IsActive.Active];
		lookup.statuses = [DescriptionTemplateTypeStatus.Finalized];
		lookup.order = {items: ['-' + (nameof<DescriptionTemplateType>(x => x.name))]};

		lookup.project = {
			fields: this.descriptionTemplateTypesLookupFields
		};

		return lookup;
	}

	query(selectedDescriptionIds = null) {
		this.descriptionTemplateService.query(this.initializeDescriptionTemplateLookup(selectedDescriptionIds)).pipe(
			takeUntil(this._destroyed),
			tap((descriptionTemplates: QueryResult<DescriptionTemplate>) => {
			})).subscribe(queryResults => {
				if(!selectedDescriptionIds) {
					this.totalDescriptionTemplates = queryResults.count;
					this.currentPageDescriptionTemplates = queryResults.items
				}else{
					for(let descriptionTemplate of queryResults.items){
						this.selectedTemplatesInfo.set(descriptionTemplate.groupId, descriptionTemplate);
                        this.descriptionTemplateLoaded.emit(descriptionTemplate)
					}
				}
		});

	}

	protected initializeDescriptionTemplateLookup(selectedDescriptionIds = null): DescriptionTemplateLookup {
		const lookup = new DescriptionTemplateLookup();
		lookup.metadata = {countAll: true};
		lookup.page = {offset: (this.currentPage - 1) * this.size, size: selectedDescriptionIds?selectedDescriptionIds.length:this.size};
		lookup.isActive = [IsActive.Active];
		lookup.statuses = [DescriptionTemplateStatus.Finalized]
		lookup.versionStatuses = [DescriptionTemplateVersionStatus.Current]
		lookup.order = {items: [this.toLookupSortField(this.sort)]};
		lookup.project = {
			fields: this.lookupFields
		};
		if (this.term) {
			lookup.like = this.term;
		}
		if (this.selectedTypesIds) {
			lookup.typeIds = this.selectedTypesIds;
		}
		if (this.selectedLanguageCodes) {
			lookup.languages = this.selectedLanguageCodes;
		}
		if(selectedDescriptionIds){
			lookup.groupIds = selectedDescriptionIds;
		}
		return lookup;
	}

	onSearchTermChange(term) {
		this.term = this.termControl.value;
		this.query();
	}

    
    public toLookupSortField(value: {prop: string, direction: SortDirection}): string {
		return  (value.direction === SortDirection.Ascending ? '' : '-') + value.prop;
	}

	sortChanged(fieldName: string) {
		const dir = (this.isSorted(fieldName) && this.sort.direction === SortDirection.Descending) ? SortDirection.Ascending : SortDirection.Descending;
        this.sort = {
            direction: dir,
            prop: fieldName
        }
		this.query();
	}

	isSorted(fieldName: string) {
		return this.sort.prop === fieldName;

	}

	onTypeChanged(type) {
		this.selectedTypesIds = [this.typeFormControl.value.id];
		this.query();
	}

	pageChanged(page) {
		console.log(page)
		this.currentPage = page;
		this.query();
	}

	onLanguageChange(language) {
		this.selectedLanguageCodes = [this.languageFormControl.value.code];
		this.query();
	}

	getLanguageInfoName(code: string) {
		return this.displayFn(this.availableLanguages?.find(x => x.code === code));
	}

	displayFn(option): string {
		return option && option.name ? option.name : '';
	}

	private _filterLanguages(name: string): LanguageInfo[] {
		const filterValue = name.toLowerCase();

		return this.availableLanguages.filter(option => option.name.toLowerCase().includes(filterValue));
	}

	private _filterTypes(name: string): DescriptionTemplateType[] {
		const filterValue = name.toLowerCase();

		return this.allTypes.filter(option => option.name.toLowerCase().includes(filterValue));
	}

	clearLanguage() {
		this.selectedLanguageCodes = null;
		this.languageFormControl.setValue('');
		this.query();
	}

	clearType() {
		this.selectedTypesIds = null;
		this.typeFormControl.setValue('');
		this.query();
	}

	remove(value) {
		let selected = this.descTemplatesControl.value;
		let index = selected.indexOf(value);
		selected.splice(index, 1);
		this.descTemplatesControl.setValue(selected);
        this.descTemplatesControl.markAsDirty();
		this.selectedTemplatesInfo.delete(value);
	}

	select(descriptionTemplate) {
		let selected = this.descTemplatesControl.value;
		selected.push(descriptionTemplate.groupId)
		this.descTemplatesControl.setValue(selected);
		this.descTemplatesControl.markAsDirty();
		this.selectedTemplatesInfo.set(descriptionTemplate.groupId, descriptionTemplate);
        this.descriptionTemplateLoaded.emit(descriptionTemplate)
	}

	onPreviewDescriptionTemplate(description) {
		const dialogRef = this.dialog.open(DescriptionTemplatePreviewDialogComponent, {
			minWidth: 'min(800px, 95vw)',
			minHeight: '200px',
			restoreFocus: false,
			data: {
				descriptionTemplateId: description.id,
				showSelect: !this.previewOnly && !this.descTemplatesControl.value.includes(description.groupId)
			},
			panelClass: 'custom-modalbox'
		});
    dialogRef.afterClosed().pipe(takeUntil(this._destroyed)).subscribe(descTemplate => {
			if (descTemplate) {
				this.select(descTemplate);
			}
		});
	}

    private _canRemoveDescriptionTemplateInfo(itemId): {canRemove: boolean, message: string} {
		if (itemId && this.templatesInUse()?.length) {
            const templateInUse = this.templatesInUse().includes(itemId);
            return {
                canRemove: !templateInUse,
				message: templateInUse ? 'PLAN-EDITOR.UNSUCCESSFUL-REMOVE-TEMPLATE' : null
            };
		}
		return;
	}

    protected canRemoveItem(itemId): boolean {
        return this._canRemoveDescriptionTemplateInfo(itemId)?.canRemove ?? true;
    }

    protected chipRemoveTooltip(itemId): string {
        return this._canRemoveDescriptionTemplateInfo(itemId)?.message;
    }

}