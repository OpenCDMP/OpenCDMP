import {Component, effect, input, Input, OnInit, ChangeDetectionStrategy} from "@angular/core";
import {BaseComponent} from "@common/base/base.component";
import {nameof} from "ts-simple-nameof";
import {DescriptionTemplate} from "@app/core/model/description-template/description-template";
import {DescriptionTemplateService} from "@app/core/services/description-template/description-template.service";
import {HttpErrorHandlingService} from "@common/modules/errors/error-handling/http-error-handling.service";
import {map, startWith, takeUntil, tap} from "rxjs/operators";
import {QueryResult} from "@common/model/query-result";
import {DescriptionTemplateLookup} from "@app/core/query/description-template.lookup";
import {DescriptionTemplateType} from "@app/core/model/description-template-type/description-template-type";
import {DescriptionTemplateTypeService} from "@app/core/services/description-template-type/description-template-type.service";
import {LanguageInfoService} from "@app/core/services/culture/language-info-service";
import {LanguageInfo} from "@app/core/model/language-info";
import { FormControl, UntypedFormBuilder, FormsModule, ReactiveFormsModule } from "@angular/forms";
import {Observable} from "rxjs";
import {Guid} from "@common/types/guid";
import {DescriptionTemplatePreviewDialogComponent} from "@app/ui/admin/description-template/description-template-preview/description-template-preview-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import { SortDirection } from "@common/modules/admin-listing/listing.types";
import { MatChipSet, MatChip, MatChipRemove } from "@angular/material/chips";
import { MatTooltip } from "@angular/material/tooltip";
import { MatIcon } from "@angular/material/icon";
import { MatIconButton, MatButton } from "@angular/material/button";
import { MatCard, MatCardContent } from "@angular/material/card";
import { ExpandableSearchFieldComponent } from "../../../../../common/modules/text-filter/expandable-search-field/expandable-search-field.component";
import { NgTemplateOutlet, AsyncPipe, DatePipe } from "@angular/common";
import { MatMenuTrigger, MatMenu } from "@angular/material/menu";
import { MatBadge } from "@angular/material/badge";
import { MatFormField, MatLabel } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { MatAutocompleteTrigger, MatAutocomplete } from "@angular/material/autocomplete";
import { MatOption } from "@angular/material/select";
import { MatPaginator } from "@angular/material/paginator";
import { TranslatePipe } from "@ngx-translate/core";

@Component({
    selector: 'app-description-template-table-select',
    templateUrl: './description-template-table-select.component.html',
    styleUrls: ['./description-template-table-select.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [MatChipSet, MatChip, MatTooltip, MatChipRemove, MatIcon, MatIconButton, MatCard, MatCardContent, ExpandableSearchFieldComponent, FormsModule, ReactiveFormsModule, NgTemplateOutlet, MatMenuTrigger, MatBadge, MatMenu, MatButton, MatFormField, MatLabel, MatInput, MatAutocompleteTrigger, MatAutocomplete, MatOption, MatPaginator, AsyncPipe, DatePipe, TranslatePipe]
})
export class DescriptionTemplateTableSelectComponent extends BaseComponent implements OnInit {
    templatesInUse = input<Guid[]>();

	@Input() descTemplatesControl: FormControl<Guid[]> = new FormControl([]);
	@Input() previewOnly: boolean = false;
    @Input() onlyShowSelections: boolean = false;

    descriptionTemplateTypes = input<QueryResult<DescriptionTemplateType>>(null);
    initialData = input<QueryResult<DescriptionTemplate>>(null);

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

	constructor(
        private descriptionTemplateService: DescriptionTemplateService,
		private descriptionTemplateTypeService: DescriptionTemplateTypeService,
        protected httpErrorHandlingService: HttpErrorHandlingService,
		private languageInfoService: LanguageInfoService, private fb: UntypedFormBuilder, protected dialog: MatDialog,
    ) {
		super();
        effect(() => {
            const initialTypes = this.descriptionTemplateTypes();
            if(initialTypes){
                this.initializeDescriptionTemplateTypes(initialTypes.items);
            } else {
                this.descriptionTemplateTypeService.query(this.descriptionTemplateTypeService.buildLookup()).pipe(
                takeUntil(this._destroyed))
                .subscribe(queryResults => {
                    this.initializeDescriptionTemplateTypes(queryResults?.items ?? [])
                });
            }
        })
        effect(() => {
            const initialTableData = this.initialData();
            if(initialTableData){
                this.totalDescriptionTemplates = initialTableData.count;
				this.currentPageDescriptionTemplates = initialTableData.items;
            } else {
                this.query();
            }
        })

	}

    initializeDescriptionTemplateTypes(types: DescriptionTemplateType[]){
        this.allTypes = types
        this.filteredTypeOptions = this.typeFormControl.valueChanges.pipe(
            startWith(''),
            map(value => {
                const name = typeof value === 'string' ? value : value?.name;
                return name ? this._filterTypes(name as string) : this.allTypes.slice();
            }),
        );
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

	query(selectedDescriptionIds = null) {
		this.descriptionTemplateService.query(this.initializeDescriptionTemplateLookup(selectedDescriptionIds)).pipe(
			takeUntil(this._destroyed),
			tap((descriptionTemplates: QueryResult<DescriptionTemplate>) => {
			})).subscribe(queryResults => {
				if(!selectedDescriptionIds) {
					this.totalDescriptionTemplates = queryResults.count;
					this.currentPageDescriptionTemplates = queryResults.items
				}else{
					for(const descriptionTemplate of queryResults.items){
						this.selectedTemplatesInfo.set(descriptionTemplate.groupId, descriptionTemplate);
					}
				}
		});

	}

	protected initializeDescriptionTemplateLookup(selectedDescriptionIds = null): DescriptionTemplateLookup {
        return this.descriptionTemplateService.buildLookup({
            page: {offset: (this.currentPage - 1) * this.size, size: selectedDescriptionIds?selectedDescriptionIds.length:this.size},
            groupIds: selectedDescriptionIds,
            like: this.term,
            languages: this.selectedLanguageCodes,
            typeIds: this.selectedTypesIds,
            order: {items: [this.toLookupSortField(this.sort)]}
        });
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
		const selected = this.descTemplatesControl.value;
		const index = selected.indexOf(value);
		selected.splice(index, 1);
		this.descTemplatesControl.setValue(selected);
        this.descTemplatesControl.markAsDirty();
		this.selectedTemplatesInfo.delete(value);
	}

	select(descriptionTemplate) {
		const selected = this.descTemplatesControl.value;
		selected.push(descriptionTemplate.groupId)
		this.descTemplatesControl.setValue(selected);
		this.descTemplatesControl.markAsDirty();
		this.selectedTemplatesInfo.set(descriptionTemplate.groupId, descriptionTemplate);
	}

	onPreviewDescriptionTemplate(description) {
        if(!description){return;}
		const dialogRef = this.dialog.open(DescriptionTemplatePreviewDialogComponent, {
			minWidth: 'min(800px, 95vw)',
			minHeight: '200px',
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